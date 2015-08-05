package com.sunchao.rpc.base.serializer.support.varint.generic;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to map type variables name to concrete classes that are used during instantiation.
 * And the generic helper has its structure for <i>parent-son</i>
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class GenericStructure {

	@SuppressWarnings("rawtypes")
	public Map<String, Class> getTypeVar2Class() {
		return typeVar2Class;
	}

	public GenericStructure getParent() {
		return parent;
	}

	public void setParent(GenericStructure parent) {
		if (this.parent != null) throw new IllegalStateException("Parent just set value once.");
		this.parent = parent;
	}
	
	@SuppressWarnings("rawtypes")
	public Class getConcreteClass(String typeVariable) {
		Class clazz = this.typeVar2Class.get(typeVariable);
		if (clazz == null && parent != null) return parent.getConcreteClass(typeVariable);
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public GenericStructure() {
		this.typeVar2Class = new HashMap<String, Class>();
		this.parent = null;
	}
	
	@SuppressWarnings("rawtypes")
	public GenericStructure(GenericStructure parent) {
		this.typeVar2Class = new HashMap<String, Class>();
		this.parent = parent;
	}
	
	@SuppressWarnings("rawtypes")
	public GenericStructure(Map<String, Class> mappings) {
		this.typeVar2Class = new HashMap<String, Class>(mappings);
		this.parent = null;
	}
	
	@SuppressWarnings("rawtypes")
	public void add(String typeVariable, Class clazz) {
		this.typeVar2Class.put(typeVariable, clazz);
	}
	
	public String toString() {
		return this.typeVar2Class.toString();
	}
	
	public void resetParent() {
		this.parent = null;
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Class> typeVar2Class;
	
	private GenericStructure parent;
	
	
}
