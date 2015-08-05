package com.sunchao.rpc.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sunchao.rpc.common.annotation.Utility;

/**
 * The LRU Cache used by the linkedHashMap  .
 * and overwrite the  method of  "removeEldestEntry(java.util
 * .Map.Entry<K,V> eldest)"
 * 
 * <p>
 * Because in the multiple thread case,so the class use a lock
 * to assure the thread-safe.
 * </p>
 * @author sunchao
 *
 */
@Utility("LRUCache")
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
	
	public void setMaxCapacity() {
		this.maxCapacity = maxCapacity;
	}
	
	public int getMaxCapacity() {
		return this.maxCapacity;
	}
	
	
	@Override
	public V put(K key, V value) {
		synchronized (DEFAULT_OBJECT_LOCK) {
			return super.put(key, value);
		}
	}

	@Override
	public V remove(Object key) {
		synchronized (DEFAULT_OBJECT_LOCK) {
		    return super.remove(key);
		}
	}

	@Override
	public int size() {
		synchronized (DEFAULT_OBJECT_LOCK) {
			return super.size();
		}
	}
	
	public Collection<Map.Entry<K,V>> getAll()
	{
		synchronized (DEFAULT_OBJECT_LOCK) {
			return new ArrayList<Map.Entry<K,V>> (super.entrySet());
		}
	}

	public LRUCache() {
		this(DEFAULT_MAX_CAPACITY);
	}

	public LRUCache(int maxCapacity) {
		/**
		 * the linked hash map constructor
		 * @param 16 
		 *        the initial capacity
		 * @param 0.75f
		 *        the load_factor.
		 * @param true
		 *        the insert sequence.        
		 */
		super(16, DEFAULT_LOAD_FACTOR, true);
		this.maxCapacity = maxCapacity;
	}
	
	@Override
	public void clear() {
		synchronized (DEFAULT_OBJECT_LOCK) {
			super.clear();
		}
	}

	@Override
	public boolean containsValue(Object arg0) {
		synchronized (DEFAULT_OBJECT_LOCK) {
		     return super.containsValue(arg0);
		}
	}

	@Override
	public V get(Object arg0) {
		synchronized (DEFAULT_OBJECT_LOCK) {
			return super.get(arg0);
		}
	}

	/**
	 * <i>
	 * we need overwrite the method to implement
	 * the lru.
	 * please @see LinkedHashMap.
	 * </i>
	 */
	@Override
	protected boolean removeEldestEntry(Entry<K, V> arg0) {
			   return size() > maxCapacity;
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4868251383145590944L;
	
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	
	private static final int DEFAULT_MAX_CAPACITY = 1000;
	
	private volatile int maxCapacity ;
	
	private final Object DEFAULT_OBJECT_LOCK = new Object();
	

}
