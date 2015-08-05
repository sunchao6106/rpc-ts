package com.sunchao.rpc.base.serializer.support.varint.collection;

import java.util.Collection;

/**
 * Interface for primitive map that uses {@code int}s as keys.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 * @param <V> the value type stored in the map
 */
public interface IntObjectMap<V> {
	
	/**
	 * An Entry in the map.
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 * @param <V>
	 */
	interface Entry<V> {
		
		/**
		 * 
		 * @return the key for this entry
		 */
		int key();
		
		/**
		 *  
		 * @return the value for this entry.
		 */
		V value();
		
		/**
		 * Set the value for this entry
		 * @param value
		 */
		void setValue(V value);
	}
	
	/**
	 * Get the value in the map with the specified key.
	 * 
	 * @param key the key whose associated value  if to be returned.
	 * @return  the value or {@code null} if the key was not found in the map.
	 */
	V get(int key);
	
	/**
	 * Puts the given entry into the map.
	 * 
	 * @param key the key of the entry.
	 * @param value the value of the entry.
	 * @return the previous value for this key or {@code null} if there was no previous mapping.
	 */
	V put(int key, V value);
	
	/**
	 * Puts all of the entries from the given map into this map.
	 * 
	 * @param sourceMap  
	 */
	void putAll(IntObjectMap<V> sourceMap);

	/**
	 * Removes the entry with the specified key.
	 * 
	 * @param key the key for the entry to be removed from the map.
	 * @return the previous value for the key, or {@code null} if there was no mapping
	 */
	V remove(int key);
	
	/**
	 * 
	 * @return  the number of entries contained in this map.
	 */
	int size();
	
	/**
	 * 
	 * @return indicate whether or not this map is empty
	 */
	boolean isEmpty();
	
	/**
	 * Clears all entries from this map.
	 */
	void clear();
	
	/**
	 * Indicates whether or not this map contains a value for the specified key.
	 * 
	 * @param key
	 * @return
	 */
	boolean containsKey(int key);
	
	/**
	 * Indicates whether or not this map contains the specified value.
	 */
	boolean containsValue(V value);
	
	/**
	 * Gets an iterable collection of the entries contained in this map.
	 */
	Iterable<Entry<V>> entries();
	
	/**
	 * Gets the keys contained in this map.
	 */
	int[] keys();
	
	/**
	 * Gets the values contained in this map.
	 * 
	 * @param clazz
	 * @return
	 */
	V[] values(Class<V> clazz);
	
	/**
	 * Gets the values contains in this map as a {@link Co	llection}.
	 * @return
	 */
	Collection<V> values();
}
