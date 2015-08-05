package com.sunchao.rpc.common.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.sunchao.rpc.common.annotation.Utility;

/**
 * The LRUCache use the hashmap and two double linked list
 * to implements, and the class is thread-safe with the read
 * write lock.
 * @author sunchao
 *
 * @param <K>
 * @param <V>
 */
@Utility("LRUUtil")
public final class LRUUtil<K,V> implements Serializable {

	
	private static final long serialVersionUID = 5940322079811788815L;
	
	/**  the default size of the cache  */
	private static final int DEFAULT_MAX_CAPACITY = 1000;
		
	/** the read write lock */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	/**
	 * if without the map table, 
	 * get cache element of the two double linked list will take O(n) ,
	 * and now just O(1)  
	 */
	private final Map<K,Entry<K,V>> map;
	private volatile int maxCapacity;
	/** the head and tail of the two double linked list  */
	private Entry<K,V> first, last;
	
	
	public LRUUtil() {
		this(DEFAULT_MAX_CAPACITY);
	}
	
	public LRUUtil(int maxCapacity) {	
		if (maxCapacity <= 0)
			throw new IllegalArgumentException("the max capacity can't be < 0");
		this.maxCapacity = maxCapacity;
		this.first = this.last = null;
		map = new HashMap<K,Entry<K,V>>(maxCapacity);
		
	}
	
	/**
	 * while add a new cache element,
	 * or access the element in the double linked list,
	 * the cache will move the element to head,so after 
	 * a long time, the tail element just as the least 
	 * access element, and when the cache size more that the
	 * capacity, the cache will remove the tail element, and
	 * add new element. 
	 * @param node
	 *         the new element or the access element now.
	 */
	private void moveToHead(Entry<K,V> node)
	{
		/**
		 * the two double linked list operation.
		 */
		try {
	         writeLock.lock();
	         if (first == node)
	         {
	        	 return;
	         }
	         if (node.pre != null)
	         {
	        	 node.pre.next = node.next;
	         }
	         if (node.next != null)
	         {
	        	 node.next.pre = node.pre;
	         }
	         if (last == node)
	         {
	        	 last = node.pre;
	         }
	         if (first == null || last == null)
	         {
	        	 first = last = node;
	        	 return;
	         }
	         node.next =first;
	         first.pre = node;
	         first = node;
	         node.pre = null;
		} finally {
			writeLock.unlock();
		}
	}
	
	public void setMaxCapacity() {
		this.maxCapacity = maxCapacity;
	}
	
	public int getMaxCapacity() {
		return this.maxCapacity;
	}
	
	
	/**
	 * add a new element to the cache.
	 * and detailed explanation, please
	 * see {@link moveToHead()}.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public V put(K key, V value) {
	  try {
		  writeLock.lock();
		  Entry<K,V> node = map.get(key);
		  if (node == null) {
			  if (map.size() >= maxCapacity) {
				  map.remove(last.key);
				  removeLast();
			  }
			  node = new Entry();
			  node.key = key;  
		  }
		  node.value = value;
		  moveToHead(node);
		  map.put(key, node);
		  return node.value;
	  } finally {
		  writeLock.unlock();
	  }
	}

	/**
	 * <p>
	 * remove a cache element.
	 * </p>
	 * and just a two double linked list remove operation.
	 * but and remove the hash map element added.
	 * @param key
	 *         the key attached the element.
	 * @return
	 *         the removed element's value.
	 */
	public V remove(K key) {
		try {
			writeLock.lock();
			Entry<K,V> node = map.get(key);
			if (node.pre != null) 
			{
				node.pre.next = node.next;
			}
			if (node.next != null)
			{
				node.next.pre = node.pre;
			}
			if (last == node)
			{
				last = node.pre;
			}
			if (first == node)
			{
				first = node.next;
			}
			map.remove(key);
			return node.value;
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * when the capacity insufficient,
	 * and the operation will happen to take room for 
	 * a new element.
	 */
	private void removeLast() {
	    try{
	    	writeLock.lock();
	    	if (last != null) {
	    		if (last.pre != null)
	    		{
	    			last.pre.next = null;
	    		} 
	    		else {
	    			first = null;
	    		}
	    		last = last.pre;
	    	}
	    } finally {
	    	writeLock.unlock();
	    }
	}

	
	public int size() {
		try {
			readLock.lock();
			return this.map.size();
		} finally {
			readLock.unlock();
		}
	}
	
	
	public void clear() {
		try {
			writeLock.lock();
			first = last = null;
			map.clear();
		} finally {
			writeLock.unlock();
		}
	}

	
	public boolean containsValue(K key) {
		try {
			readLock.lock();
			Entry<K,V> entry = map.get(key);
			if (entry != null && entry.value != null)
				return true;
			return false;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * get element value by the key,
	 * and if the cache will not storage
	 * the element, it will return <i>null</i>
	 * 
	 * @param key
	 *         the element key.
	 * @return
	 *         the element value.
	 */
	public V get(K key) {
		try {
			readLock.lock();
			Entry<K,V> node = map.get(key);
			if (node != null && node.value != null)
			{
				moveToHead(node);
				return node.value;					
			}
			else {
				return null;
			}
		} finally {
			readLock.unlock();
		}
		
	}
	
	
    @SuppressWarnings("hiding")
	private class Entry<K,V> implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6135976323971970858L;
		
		Entry<K,V> pre;
		Entry<K,V> next;
		K key;
		V value;
		
		public Entry() {
		
		}
	}
	
    public String toString() {	
    	StringBuilder sb =  new StringBuilder();
    	Entry<K,V> node = first;
    	while (node != null) {
    		sb.append(String.format("%s : %s ; ",node.key,node.value));
    		node = node.next;
    	}
    	return sb.toString();
    }
}
