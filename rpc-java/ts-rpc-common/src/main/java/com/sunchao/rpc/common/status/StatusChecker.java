package com.sunchao.rpc.common.status;

import com.sunchao.rpc.common.extension.Component;

/**
 * StatusChecker.
 * @author sunchao
 *
 */
@Component
public interface StatusChecker {
	
	/**
	 * check status.
	 * @return
	 *       status.
	 */
	Status check();

}
