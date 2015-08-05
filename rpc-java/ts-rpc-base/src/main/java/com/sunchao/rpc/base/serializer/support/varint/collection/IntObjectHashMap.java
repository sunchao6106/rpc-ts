package com.sunchao.rpc.base.serializer.support.varint.collection;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A hash map implementation of {@link IntObjectMap} that uses open addressing for keys.
 * To minimize the memory footprint, this class uses <i>open addressing</i> rather that <i>chaining</i>.
 * Collections are resolved using linear probing. Deletions implement compaction, so cost of
 * remove can approach O(n) for full maps, which makes a small loadFactor recommended.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 * @param <V> The value type stored in the map.
 */
public class IntObjectHashMap<V> implements IntObjectMap<V>, Iterable<IntObjectMap.Entry<V>> {
	
	/** Default initial capacity. Used if not specified in the constructor */
	private static final int DEFAULT_CAPACITY = 11;
	
	/** Default load factor. Used if not specified in the constructor */
	private static final float DEFAULT_LOAD_FACTOR = 0.5F;
	
	/**
	 * PlaceHolder for null values, so we can use the actual null to mean available.
	 * (Better that using a placeholder for available: less references for GC processing.)
	 */
	private static final Object NULL_VALUE = new Object();

	/** The maximum number of elements allowed without allocating more space */
	private int maxSize;
	
	/** the load factor for the map. Used to calculate {@link #maxSize }. */
	private final float loadFactor;
	
	
	private int[] keys;
	private V[] values;
	private Collection<V> valueCollection;
	private int size;
	
	public IntObjectHashMap() {
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	

	public IntObjectHashMap(int initialCapacity, float loadFactor) {
		if (initialCapacity < 1) 
			throw new IllegalArgumentException("initialCapacity must be >= 1");
		if (loadFactor <= 0.0F || loadFactor > 1.0F) //Cannot exceed 1 because we can never store more that capacity elements;
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1"); //using a bigger loadFactor would trigger rehashing before the 
		this.loadFactor = loadFactor;              //desired load is reached.
		int capacity = adjustCapacity(initialCapacity); //ensure the capacity is odd, 
		//Allocate the arrays.
		keys = new int[capacity];
		@SuppressWarnings("unchecked")
		V[] temp = (V[]) new Object[capacity];
		values = temp;
		
		//initialize the maximum size value.
		maxSize = calcMaxSize(capacity); 
	}
	
	private static <T> T toExternal(T value) {
		return value == NULL_VALUE ? null : value;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T toInternal(T value) {
		return (T) (value == null ? NULL_VALUE : value);
	}
	
	public V get(int key) {
		int index = indexOf(key);
		return index == -1 ? null : toExternal(values[index]);
	}

	public V put(int key, V value) {
	    int startIndex = hashIndex(key);
	    int index = startIndex;
	    
	    for (;;) {
	    	if (values[index] == null) {
	    		keys[index] = key; //found empty slot, use it.
	    		values[index] = toInternal(value);
	    		growSize();
	    		return null;
	    	}
	    	if (keys[index] == key) { //found existing entry with this key, just replace the value.
	    		V previousValue = values[index];
	    		values[index] = toInternal(value);
	    		return toExternal(previousValue);
	    	}
	    	//Conflict, keep probing...
	    	if ((index = probeNext(index)) == startIndex) {
	    		//Can only happen is the map was full at MAX_ARRAY_SIZE and could't grow.
	    		throw new IllegalStateException("Unable to insert");
	    	}
	    }
	}
	
	private int probeNext(int index) {
		return index == values.length - 1 ? 0 : index + 1;
	}

	public void putAll(IntObjectMap<V> sourceMap) {
		if (sourceMap instanceof IntObjectHashMap) {
			//Optimization - iterate through the array.
			IntObjectHashMap<V> source = (IntObjectHashMap<V>) sourceMap;
			for (int i = 0; i < source.values.length; i++) {
				V sourceValue = source.values[i];
				if (sourceValue != null) {
					put(source.keys[i], sourceValue);
				}
			}
			return;
		}
		
		//Otherwise, just add each entry.
		for (Entry<V> entry :  sourceMap.entries()) {
			put(entry.key(), entry.value());
		}	
	}

	public V remove(int key) {
		int index = indexOf(key);
		if (index == -1) 
			return null;
		V prev = values[index];
		removeAt(index);
		return toExternal(prev);
	}
	

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void clear() {
		Arrays.fill(keys, 0);
		Arrays.fill(values, null);
		size = 0;
	}

	public boolean containsKey(int key) {
		return indexOf(key) >= 0;
	}
	
   /**
    * The map supports null value; 
    * this will be matched as NULL_VALUE.equals(NULL_VALUE).
    */
	public boolean containsValue(V value) {
		V v1 = toInternal(value);
		for (V v2 : values) {
			if (v2 != null && v2.equals(v1)) {
				return true;
			}
		}
		return false;
	}

	public Iterable<com.sunchao.rpc.base.serializer.support.varint.collection.IntObjectMap.Entry<V>> entries() {
		return this;
	}

	public int[] keys() {
		int[] outKeys = new int[size()];
		int targeIndex = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				outKeys[targeIndex++] = keys[i];
			}
		}
		return outKeys;
	}

	@SuppressWarnings("unchecked")
	public V[] values(Class<V> clazz) {
		V[] outValues = (V[]) Array.newInstance(clazz, size());
		int targeIndex = 0;
		for (V value : values) {
			if (value != null) {
				outValues[targeIndex++] = value;
			}
		}
		return outValues;
	}

	/**
	 * OuterClass.this.
	 * OuterClass.InnerClass c = new OuterClass().new InnerClass();
	 */
	public Collection<V> values() {
		Collection<V> valueCollection = this.valueCollection;
		if (valueCollection == null) {
			this.valueCollection = valueCollection = new AbstractCollection<V>() {
				@Override
				public Iterator<V> iterator() {
					return new Iterator<V>() {
						final Iterator<Entry<V>> iter = IntObjectHashMap.this.iterator();

						public boolean hasNext() {
							return iter.hasNext();
						}

						public V next() {
							return iter.next().value();
						}

						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}

				@Override
				public int size() {
					return size;
				}
			};
		}
		return valueCollection;
	}

	public Iterator<com.sunchao.rpc.base.serializer.support.varint.collection.IntObjectMap.Entry<V>> iterator() {
		return new IteratorImpl();
	}
	
	/**
	 * Adjust the given capacity value to ensure that it is odd. Even capacities can break probing.
	 * 
	 * @param capacity the specified capacity.
	 * @return the new capacity.
	 */
	private static int adjustCapacity(int capacity) {
		return capacity | 1;
	}
	
	/**
	 * Calculates the maximum size allowed before rehashing.
	 * @param capacity 
	 * @return
	 */
	private int calcMaxSize(int capacity) {
		//clip the upper capacity so that there will always be at least one available slot.
		int upperBound = capacity - 1;
		return Math.min(upperBound, (int) (capacity * loadFactor));
	}
	
	/**
	 * Grows the map size after an insertion. If necessary, performs a rehash or the map.
	 */
	private void growSize() {
		size++;
		if (size > maxSize) {
			//Need to grow the arrays. We take care to detect integer overflow,
			//also limit array size to ArrayList.MAX_ARRAY_SIZE.
			rehash(adjustCapacity((int) Math.min(keys.length * 2.0, Integer.MAX_VALUE - 8)));
		} else if (size == keys.length) {
			//Open addressing requires that we have at least 1 slot available. Need to refresh
			//the arrays to clear any removed elements.
			rehash(keys.length);
		}
	}
	
	/**
	 * Removes entry at the given index position. Also performs opportunistic, incremental rehashing
	 * if necessary to not break conflict chains.
	 * @param index the index position of the element to remove.
	 */
	private void removeAt(int index) {
		--size;
		//clearing the key is not strictly necessary(for GC like in a regular collection),
		//but recommended for security. The memory location is still fresh in the cache anyway.
		keys[index] = 0;
		values[index] = null;
		//In the interval from index to the next available entry, the arrays may have entries
		//that are displaced from their base position due to prior conflicts. Iterate these entries
		//and move them back if possible, optimizing future lookups.
		int nextFree = index;
		for (int i = probeNext(index); values[i] != null; i = probeNext(i)) {
			int bucket = hashIndex(keys[i]);
			if (i < bucket && (bucket <= nextFree || nextFree <= i) ||
					bucket <= nextFree && nextFree <= i) {
				//Move the displaced entry "back" to the first available position.
				keys[nextFree] = keys[i];
				values[nextFree] = values[i];
				//Put the first entry after the displaced entry.
				keys[i] = 0;
				values[i] = null;
				nextFree = i;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void rehash(int newCapacity) {
		int[] oldKeys = keys;
		V[] oldVals = values;
		
		keys = new int[newCapacity];
		V[] temp = (V[]) new Object[newCapacity];
		values = temp;
		maxSize = calcMaxSize(newCapacity);
		
		for (int i = 0; i < oldVals.length; i++) {
			V oldVal = oldVals[i];
			if (oldVal != null) { //and contains the function of clear the remove entry. 
				int oldKey = oldKeys[i];
				int index = hashIndex(oldKey);
				
				for (;;) {
					if (values[index] == null) {
						keys[index] = oldKey;
						values[index] = toInternal(oldVal);
						break;
					}
					//Conflict, keep probing. Can wrap around, but never reaches startIndex again.
					index = probeNext(index);
				}
			}
		}
	}
	
	/**
	 * Locates the index for the given key. This method probes using double hashing.
	 * 
	 * @param key the key for an entry in the map.
	 * @return  the index where the key was found, or {@code -1} if no entry is found that key.
	 */
	private int indexOf(int key) {
		int startIndex = hashIndex(key);
		int index = startIndex;
		
		for (;;) {
			if (values[index] == null) 
				return -1; //It's available, so no chance that this value exists anywhere in the map.
			if (key == keys[index])
				return index;
			if ((index = probeNext(index)) == startIndex) //Conflict, keep probing....
				return -1;
		}
	}
	
	/**
	 * 
	 * @param key
	 * @return the hashed index for the given key.
	 */
	private int hashIndex(int key) {
		//Allowing for negative keys by adding the length after the first mod operation.
		return (key % keys.length + keys.length)  % keys.length;
	}
	
	@Override
	public int hashCode() {
		int hash = size;
		for (int key : keys) {
			hash ^= key;
		}
		return hash;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof IntObjectMap)) return false;
		IntObjectMap other = (IntObjectMap) obj;
		if (size != other.size())  return false;
		
		for (int i = 0; i < values.length; i++) {
			V value = values[i];
			if (value != null) {
				int key = keys[i];
				Object otherValue = other.get(key);
				if (value == NULL_VALUE) {
					if (otherValue != null) {
						return false;
					}
				} else if (!value.equals(otherValue)) {
					return false;
				}
			}
		}
		return true;  
	}
	
	@Override
	public String toString() {
		if (size == 0) 
			return "{}";
		StringBuilder sb = new StringBuilder(4 * size);
		for (int i = 0; i < values.length; i++) {
			V value = values[i];
			if (value != null) {
				sb.append(sb.length() == 0 ? "{" : ", ");
				sb.append(keyToString(keys[i])).append('=').append(value == this ? "(this Map)" : value);
			}
		}
		return sb.append('}').toString();
	}
	
	private String keyToString(int key) {
		return Integer.toString(key);
	}
	
	private final class IteratorImpl implements Iterator<Entry<V>> , Entry<V> {
		private int prevIndex = -1;
		private int nextIndex = -1;
		private int entryIndex = -1;
		
		private void scanNext() {
			for (;;) {
				if (++nextIndex == values.length || values[nextIndex] != null) 
					break;
			}
		}

		public int key() {
			return keys[nextIndex];
		}

		public V value() {
			return toExternal(values[entryIndex]);
		}

		public void setValue(V value) {
			values[entryIndex] = toInternal(value);
		}

		public boolean hasNext() {
			if (nextIndex == -1) {
				scanNext();
			}
			return nextIndex < keys.length;
		}

		public com.sunchao.rpc.base.serializer.support.varint.collection.IntObjectMap.Entry<V> next() {
			if (!hasNext()) 
				throw new NoSuchElementException();
			prevIndex = nextIndex;
			scanNext();
			
			entryIndex = prevIndex;
			return this;
		}

		public void remove() {
			if (prevIndex < 0) 
				throw new IllegalStateException("next must be called before each remove.");
			removeAt(prevIndex);
			prevIndex = -1;
		}	
	}

}
