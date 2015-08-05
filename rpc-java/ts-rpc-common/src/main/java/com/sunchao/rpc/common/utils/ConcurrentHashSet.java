package com.sunchao.rpc.common.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sunchao.rpc.common.annotation.Utility;

@Utility
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, java.io.Serializable {
	
	private static final long serialVersionUID = 8472898170828387819L;
	
	private static final Object PRESENT = new Object();
	
	private final ConcurrentHashMap<E, Object> map;
	
	public ConcurrentHashSet() {
		map = new ConcurrentHashMap<E, Object>();
	}
	
	public ConcurrentHashSet(int initialCapacity) {
		map = new ConcurrentHashMap<E, Object>(initialCapacity);
	}

	@Override
	public Iterator<E> iterator() {
	    return	map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public boolean contains(Object o) {
		return map.containsKey(o);
	}
	
	public boolean add(E e) {
		return map.put(e, PRESENT) == null;
	}
	
	public boolean remove(Object o) {
		return map.remove(o) == PRESENT;
	}
	
	public void clear() {
		map.clear();
	}

}
