package com.sunchao.rpc.common.threadpool;

import java.util.concurrent.Executor;

import com.sunchao.rpc.common.Constants;
import com.sunchao.rpc.common.extension.HotSwap;
import com.sunchao.rpc.common.extension.Component;

/** 
 * Thread pool.
 *
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 * @since JDK1.7
 * @version 1.0.0
 */ 

@Component("fixed")
public interface ThreadPool {
	
	/**
	 * get thread pool.
	 * @param url
	 *         thread parameters.
	 * @return
	 */
	@HotSwap({Constants.THREADPOOL_KEY})
	Executor getExecutor();

}
