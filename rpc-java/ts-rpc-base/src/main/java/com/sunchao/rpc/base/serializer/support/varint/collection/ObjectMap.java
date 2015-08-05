package com.sunchao.rpc.base.serializer.support.varint.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * <b>
 * An unordered map. This implementation is a cuckoo hash map using 3 hashes, random walking, and a small stash for problematic
 * keys. Null keys are not allowed. Null values are allowed. No allocation is done except when growing the table size</b>
 * 
 * <br>This map performs very fast <code>get</code><code>constainsKey</code><code>remove(typically O(1), worst case O(log(n))</code>. Put ay be a bit slower,
 * depending on hash collisions. Load factors greater than 0.91 greatly increase the chances the map will have to rehash to the next higher size</br>
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class ObjectMap<K, V> {
	
	private static final int PRIME1 = 0xbe1f14b1;
	private static final int PRIME2 = 0xb4b82e39;
	private static final int PRIME3 = 0xced1c241;
	
	static Random random = new Random();
	
	public int size;
	
	K[] keyTable;
	V[] valueTable;
	int capacity, stashSize;
	
	private float loadFactor;
	private int hashShift, mask, threshold;
	private int stashCapacity;
	private int pushIterations;
	
	public ObjectMap() {
		this(32, 0.8F);
	}
	
	public ObjectMap(int initialCapacity) {
		this(initialCapacity, 0.8F);
	}
	
	@SuppressWarnings("unchecked")
	public ObjectMap(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0) 
			throw new IllegalArgumentException("the initialCapacity must be >= 0: " + initialCapacity);
		if (initialCapacity > 1 << 30) 
			throw new IllegalArgumentException("the initialCapacity is too large: " + initialCapacity);
		capacity = nextPowerOfTwo(initialCapacity);
		
		if (loadFactor <= 0) 
			throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
		this.loadFactor = loadFactor;
		
		threshold = (int) (capacity * loadFactor);
		mask = capacity - 1;
		hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
		stashCapacity =  Math.max(3, (int)Math.ceil(Math.log(capacity)) * 2);
		pushIterations = Math.max(Math.min(capacity, 8), (int)Math.sqrt(capacity) / 8);
		
		keyTable = (K[])new Object[capacity + stashCapacity];
		valueTable = (V[])new Object[keyTable.length];
	}
	
	public ObjectMap(ObjectMap<? extends K, ? extends V> map) {
		this(map.capacity, map.loadFactor);
		stashSize = map.stashSize;
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
	}
	
	public V put(K key, V value) {
		if (key == null)
			throw new IllegalArgumentException("key cannot be null.");
		return putInternale(key, value);
	}
	
	private V putInternale(K key, V value) {
		K[] keyTable = this.keyTable;
		
		int hashCode = key.hashCode();
		int index1 = hashCode & mask;
		K key1 = keyTable[index1];
		if (key.equals(key1)) {
			V oldValue = valueTable[index1];
			valueTable[index1] = value;
			return oldValue;
		}
		
		int index2 = hash2(hashCode);
		K key2 = keyTable[index2];
		if (key.equals(key2)) {
			V oldValue = valueTable[index2];
			valueTable[index2] = value;
			return oldValue;
		}
		
		int index3 = hash3(hashCode);
		K key3 = keyTable[index3];
		if (key.equals(key3)) {
			V oldValue = valueTable[index3];
			valueTable[index3] = value;
			return oldValue;
		}
		
		//update key in the stash.
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (key.equals(keyTable[i])) {
				V oldValue = valueTable[i];
				valueTable[i] = value;
				return oldValue;
			}
		}
		
		//check for empty buckets.
		if (key1 == null) {
			keyTable[index1] = key;
			valueTable[index1] = value;
			if (size++ > threshold)
				resize(capacity << 1);
			return null;
		}
		
		if (key2 == null) {
			keyTable[index2] = key;
			valueTable[index2] = value;
			if (size++ >= threshold)
				resize(capacity << 1);
			return null;
		}
		
		if (key3 == null) {
			keyTable[index3] = key;
			valueTable[index3] = value;
			if (size++ >= threshold)
				resize(capacity << 1);
			return null;
		}
		
		push(key, value, index1, key1, index2, key2, index3, key3);
		return null;
		
	}
	
	public void putAll(ObjectMap<K, V> map) {
		ensureCapacity(map.size);
		for (Entry<K, V> entry : map.entries())
			put(entry.key, entry.value);
	}
	
	/**
	 * Skips checks for existing keys.
	 * @param key
	 * @param value
	 */
	private void putResize(K key, V value) {
		//check for empty buckets.
		int hashCode = key.hashCode();
		int index1 = hashCode & mask;
		K key1 = keyTable[index1];
		if (key1 == null) {
			keyTable[index1] = key;
			valueTable[index1] = value;
			if (size++ >= threshold) 
				resize(capacity << 1);
			return;
		}
		
		int index2 = hash2(hashCode);
		K key2 = keyTable[index2];
		if (key2 == null) {
			keyTable[index2] = key;
			valueTable[index2] = value;
			if (size >= threshold)
				resize(capacity << 1);
			return;
		}
		
		int index3 = hash3(hashCode);
		K key3 = keyTable[index3];
		if (key3 == null) {
			keyTable[index3] = key;
			valueTable[index3] = value;
			if (size++ > threshold) 
				resize(capacity << 1);
			return;
		}
		
		push(key, value, index1, key1, index2, key2, index3, key3);
	}
	
	private void push(K insertKey, V insertValue, int index1, K key1,
			int index2, K key2, int index3, K key3) {
		K[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		int mask = this.mask;
		
		//push keys until an empty bucket is found.
		K evictedKey;
		V evictedValue;
		int i = 0, pushIterations = this.pushIterations;
		do {
			//replace the key and value for one of the hashes.
			switch(random.nextInt(3)) {
			case 0:
				evictedKey = key1;
				evictedValue = valueTable[index1];
				keyTable[index1] = insertKey;
				valueTable[index1] = insertValue;
				break;
			case 1:
				evictedKey = key2;
				evictedValue = valueTable[index2];
				keyTable[index2] = insertKey;
				valueTable[index2] = insertValue;
				break;
			default:
				evictedKey = key3;
				evictedValue = valueTable[index3];
				keyTable[index3] = insertKey;
				valueTable[index3] = insertValue;
				break;
			}
			
			//if the evicted key hashes to an empty bucket, put it there and stop.
			int hashCode = evictedKey.hashCode();
			index1 = hashCode & mask;
			key1 = keyTable[index1];
			if (key1 == null) {
				keyTable[index1] = evictedKey;
				valueTable[index1] = evictedValue;
				if (size++ >= threshold)
					resize(capacity << 1);
				return;
			}
			
			index2 = hash2(hashCode);
			key2 = keyTable[index2];
			if (key2 == null) {
				keyTable[index2] = evictedKey;
				valueTable[index2] = evictedValue;
				if (size++ >= threshold) 
					resize(capacity << 1);
				return;
			}
			
			index3 = hash3(hashCode);
			key3 = keyTable[index3];
			if (key3 == null) {
				keyTable[index3] = evictedKey;
				valueTable[index3] = evictedValue;
				if (size++ >= threshold) 
					resize(capacity << 1);
				return;
			}
			
			if (++i == pushIterations)
				break;
			
			insertKey = evictedKey;
			insertValue = evictedValue;
		} while (true);
		
		putStash(evictedKey, evictedValue);
	}
	
	private void putStash(K key, V value) {
		if (stashSize == stashCapacity) {
			//Too many pushes occurs and the stash if full, increase the table size.
			resize(capacity << 1);
			putInternale(key, value);
			return;
		}
		//store key in the stash.
		int index = capacity + stashSize;
		keyTable[index] = key;
		valueTable[index] = value;
		stashSize++;
		size++;
	}
	
	public V get(K key) {
		int hashCode = key.hashCode();
		int index = hashCode & mask;
		if (!key.equals(keyTable[index])) {
			index = hash2(hashCode);
			if (!key.equals(keyTable[index])) {
				index = hash3(hashCode);
				if (!key.equals(keyTable[index]))
					return getStash(key);
			}
		}
		return valueTable[index];
	}
	
	private V getStash(K key) {
		K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) 
			if (key.equals(keyTable[i]))
				return valueTable[i];
		return null;
	}
	
	/**
	 * Return the value for the specified key, or the default value if the key is not in the map.
	 * @param key the specified key.
	 * @return the value mapping to the key or defaultValue when the specified key not in the map.
	 */
	
	public V get(K key, V defaultValue) {
		int hashCode = key.hashCode();
		int index = hashCode & mask;
		if (!key.equals(keyTable[index])) {
			index = hash2(hashCode);
			if (!key.equals(keyTable[index])) {
				index = hash3(hashCode);
				if (!key.equals(keyTable[index])) 
					return getStash(key, defaultValue);
			}
		}
		return valueTable[index];
	}
	
	private V getStash(K key, V defaultValue) {
		K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) 
			if (key.equals(keyTable[i]))
				return valueTable[i];
		return defaultValue;
	}
	
	public V remove(K key) {
		int hashCode = key.hashCode();
		int index = hashCode & mask;
		if (key.equals(keyTable[index])) {
			keyTable[index] = null;
			V oldValue = valueTable[index];
			valueTable[index] = null;
			size--;
			return oldValue;
		}
		
		index = hash2(hashCode);
		if (key.equals(keyTable[index])) {
			keyTable[index] = null;
			V oldValue = valueTable[index];
			valueTable[index] = null;
			size--;
			return oldValue;
		}
		
		index = hash3(hashCode);
		if (key.equals(keyTable[index])) {
			keyTable[index] = null;
			V oldValue = valueTable[index];
			valueTable[index] = null;
			size--;
			return oldValue;
		}
		return removeStash(key);
	}
	
	V removeStash(K key) {
		K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (key.equals(keyTable[i])) {
				V oldValue = valueTable[i];
				removeStashIndex(i);
				size--;
				return oldValue;
			}
		}
		return null;
	}
	
	void removeStashIndex(int index) {
		//if the removed location was not last, move the last tuple to the removed location.
		stashSize--;
		int lastIndex = capacity + stashSize;
		if (index < lastIndex) {
			keyTable[index] = keyTable[lastIndex];
			valueTable[index] = valueTable[lastIndex];
			valueTable[lastIndex] = null;
		} else {
			valueTable[index] = null;
		}
	}
	
	/**
	 * Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is 
	 * already less, nothing is done. If the map contains more items that the specified capacity, the next
	 * highest power of two capacity is used instead.
	 * 
	 * @param maximumCapacity
	 */
	public void shrink(int maximumCapacity) {
		if (maximumCapacity < 0)
			throw new IllegalArgumentException("maximun Capacity must be >= 0: " + maximumCapacity);
		if (size > maximumCapacity) 
			maximumCapacity = size;
		if (capacity <= maximumCapacity)
			return;
		maximumCapacity = nextPowerOfTwo(maximumCapacity);
		resize(maximumCapacity);
	}
	
	public void clear(int maximumCapacity) {
		if (capacity <= maximumCapacity) {
			clear();
			return;
		}
		size = 0;
		resize(maximumCapacity);
	}
	
	public void clear() {
		K[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		for (int i = capacity + stashSize; i-- > 0;) {
			keyTable[i] = null;
			valueTable[i] = null;
		}
		size = 0;
		stashSize = 0;
	}
	
	/**
	 * Returns true if the specified value is in the map. Note this traverses the map entries and 
	 * compares every value, which may be an expensive operation.
	 * @param value the specified value which want to be look up.
	 * @param identity  If true, uses ==  to compare the specified value in the map. If false,
	 * uses {@link #equals(Object)}
	 * @return
	 */
	public boolean containsValue(Object value, boolean identity) {
		V[] valueTable = this.valueTable;
		if (value == null) {
			K[] keyTable = this.keyTable;
			for (int i = capacity + stashSize; i-- > 0;) {
				if (keyTable[i] != null && valueTable[i] == null)
					return true;
			}
		} else if (identity) {
			for (int i = capacity + stashSize; i-- > 0;) {
				if (valueTable[i] == value)
					return true;
			}
		} else {
			for (int i = capacity + stashSize; i-- > 0;) {
				if (value.equals(valueTable[i]))
					return true;
			}
		}
		return false;
	}
	
	public boolean containsKey(K key) {
		int hashCode = key.hashCode();
		int index = hashCode & mask;
		if (!key.equals(keyTable[index])) {
			index = hash2(hashCode) ;
			if (!key.equals(keyTable[index])) {
				index = hash3(hashCode);
				if (!key.equals(keyTable[index]))
					return containsKeyStash(key);
			}
		}
		return true;
	}
	
	private boolean containsKeyStash(K key) {
		K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) 
			if (key.equals(keyTable[i]))
				return true;
		return false;
	}
	
	/**
	 * Returns the key for the specified value, or null if it is not in the map. Note this traverses the map entries and compares
	 * every value, which may be an expensive operation.
	 * @param value the specified value.
	 * @param identity  If true, uses == to compare the speified value with values in the map. If false, uses {@link #equals(Object)}.
	 * @return
	 */
	public K findKey(Object value, boolean identity) {
		V[] valueTable = this.valueTable;
		if (value == null) {
			K[] keyTable = this.keyTable;
			for (int i = capacity + stashSize; i-- > 0;) 
				if (keyTable[i] != null && valueTable[i] == null) 
					return keyTable[i];
		} else if (identity) {
			for (int i = capacity + stashSize; i-- > 0;) 
				if (valueTable[i] == value)
					return keyTable[i];
		} else {
			for (int i = capacity + stashSize; i-- > 0;)
				if (value.equals(valueTable[i]))
					return keyTable[i];
		}
		return null;
	}
	
	/**
	 * Increases the size of the backing array to accommodate the specified number of additional items. Useful
	 * before adding many items to avoid multiple backing array resizes.
	 * @param additionalCapacity
	 */
	public void ensureCapacity(int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold) 
			resize(nextPowerOfTwo((int) (sizeNeeded / loadFactor)));
	}
	
	@SuppressWarnings("unchecked")
	private void resize(int newSize) {
		int oldEndIndex = capacity + stashSize;
		capacity = newSize;
		threshold = (int) (newSize * loadFactor);
		mask = newSize - 1;
		hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
		stashCapacity = Math.max(3, (int) Math.ceil(Math.log(newSize)) * 2);
		pushIterations = Math.max(Math.min(newSize, 8), (int) Math.sqrt(newSize) / 8);
		
		K[] oldKeyTable = keyTable;
		V[] oldValueTable = valueTable;
		
		keyTable = (K[]) new Object[newSize + stashCapacity];
		valueTable = (V[]) new Object[newSize + stashCapacity];
		
		int oldSize =size;
		size = 0;
		stashSize = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldEndIndex; i++) {
				K key = oldKeyTable[i];
				if (key != null) 
					putResize(key, oldValueTable[i]);
			}
		}
	}
	
	public static int nextPowerOfTwo(int value) {
		if (value == 0) return 1;
		value --;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		return value + 1;
	}
	
	private int hash2(int h) {
		h *= PRIME2;
		return (h ^ h >>> hashShift) & mask;
	}
	
	private int hash3(int h) {
		h *= PRIME3;
		return (h ^ h >>> hashShift) & mask;
	}
	
	public String toString() {
		if (size == 0) return "{}";
		StringBuilder sb = new StringBuilder(32);
		sb.append('{');
		K[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		int i = keyTable.length;
		while (i-- > 0 ) {
			K key = keyTable[i];
			if (key == null) 
				continue;
			sb.append(key);
			sb.append('=');
			sb.append(valueTable[i]);
		}
		sb.append('}');
		return sb.toString();
	}
	
	public Entries<K,V> entries() {
		return new Entries(this);
	}
	
	public Values<V> values() {
		return new Values(this);
	}
	
	public Keys<K> keys() {
		return new Keys(this);
	}
	
	public static class Entry<K, V> {
		public K key;
		public V value;
		
		public String toString() {
			return key + " = " + value;  
		}
	}
	
	private static class MapIterator<K, V> {
		public boolean hasNext;
		
		final ObjectMap<K, V> map;
		int nextIndex, currentIndex;
		
		public MapIterator(ObjectMap<K, V> map) {
			this.map = map;
			reset();
		}
		
		public void reset() {
			currentIndex = -1;
			nextIndex = -1;
			advance();
		}
		
		void advance() {
			hasNext = false;
			K[] keyTable = map.keyTable;
			for (int n = map.capacity + map.stashSize; ++nextIndex < n;) {
				if (keyTable[nextIndex] != null) {
					hasNext = true;
					break;
				}
			}
		}
		
		public void remove() {
			if (currentIndex < 0)
				throw new IllegalStateException("next must be called before remove.");
			if (currentIndex >= map.capacity) {
				map.removeStashIndex(currentIndex);
				nextIndex = currentIndex - 1;
				advance();
			} else {
				map.keyTable[currentIndex] = null;
				map.valueTable[currentIndex] = null;
			}
			currentIndex = -1;
			map.size--;
		}
	}
	
	public static class Entries<K, V> extends MapIterator<K, V> implements Iterable<Entry<K, V>>, Iterator<Entry<K, V>> {

		
		Entry<K, V> entry = new Entry();
		
		public Entries(ObjectMap<K, V> map) {
			super(map);
		}

		public boolean hasNext() {
			return hasNext;
		}

		public Entry<K, V> next() {
			if (!hasNext) 
				throw new NoSuchElementException();
			K[] keyTable = map.keyTable;
			entry.key = keyTable[nextIndex];
			entry.value = map.valueTable[nextIndex];
			currentIndex = nextIndex;
			advance();
			return entry;
		}

		public Iterator<Entry<K, V>> iterator() {
			return this;
		}	
	}
	
	public static class Values<V> extends MapIterator<Object, V> implements Iterable<V>, Iterator<V> {

		@SuppressWarnings("unchecked")
		public Values(ObjectMap<?, V> map) {
			super((ObjectMap<Object, V>)map);
		}

		public boolean hasNext() {
			return hasNext;
		}

		public V next() {
			if (!hasNext)
				throw new NoSuchElementException();
			V value = map.valueTable[nextIndex];
			currentIndex = nextIndex;
			advance();
			return value;
		}

		public Iterator<V> iterator() {
			return this;
		}
		
		/**
		 * Returns a new array containing the remaining values.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ArrayList<V> toArray() {
			ArrayList array = new ArrayList(map.size);
			while (hasNext) {
				array.add(next());
			}
			return array;
		}
		
		/**
		 * Adds the remaining values to the specified array.
		 * @param array
		 */
		public void toArray(ArrayList<V> array) {
			while (hasNext)
				array.add(next());
		}
	}
	
	public static class Keys<K> extends MapIterator<K, Object> implements Iterable<K>, Iterator<K> {

		@SuppressWarnings("unchecked")
		public Keys(ObjectMap<K, ?> map) {
			super((ObjectMap<K, Object>)map);
		}

		public boolean hasNext() {
			return hasNext;
		}

		public K next() {
			if (!hasNext)
				throw new NoSuchElementException();
			K key = map.keyTable[nextIndex];
			currentIndex = nextIndex;
			advance();
			return key;
		}

		public Iterator<K> iterator() {
			return this;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ArrayList<K> toArray() {
			ArrayList array = new ArrayList(map.size);
			while (hasNext)
				array.add(next());
			return array;
		}
	}
	
	/*public static void main(String args[]) {
		int value = 15;
		value = nextPowerOfTwo(value);
		System.out.println(value);
	}*/

}
