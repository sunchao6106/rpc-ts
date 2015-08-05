package com.sunchao.rpc.base.service;

import java.util.List;

import com.sunchao.rpc.common.extension.Component;

/**
 * Service Factory.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */

@Component
public interface ServiceFactory {
	
	
	/**
	 * 
	 * @param type
	 *           The service interface.
	 * @return
	 *           The service impl instances.
	 */
	<T> List<T> getService(Class<T> type);

}
