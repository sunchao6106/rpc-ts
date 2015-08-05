package com.sunchao.rpc.common.store;

import java.util.Map;

import com.sunchao.rpc.common.extension.Component;

/**
 * The data store class.
 * @author sunchao
 *
 */

@Component("simple")
public interface DataStore {
	
	/**
	 * return a snapshot value of componentName.
	 * @param componentName
	 *          the component name string.
	 * @return
	 *          the snapshot.
	 */
	Map<String, Object> get(String componentName);
	
	Object get(String componentName, String key);
	
	void put(String componentName, String key, Object value);
	
	void remove(String componentName, String key);

}
