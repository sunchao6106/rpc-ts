package com.sunchao.rpc.common.threadpool.support;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Abort Policy.
 * <p>
 *  which used to handle the rejected tasks. 
 *  when the executor is shutdown and the limited queue and threads number is
 *  the max. 
 * </p>
 * log warn info when abort.
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>  
 * @since JDK1.7  
 * @version 1.0.0  
 *
 */
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbortPolicyWithReport.class);
	private final String threadName;

	
	
	public AbortPolicyWithReport(String threadName) {
		this.threadName = threadName;
	}

	/**
	 * @param ThreadPoolExecutor 
	 * @see   java.util.concurrent.ThreadPoolExecutor #getActiveCount();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getPoolSize();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getCorePoolSize();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getLargestPoolSize();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getMaximumPoolSize();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getKeepAliveTime(java.util.concurrent.TimeUnit);
	 * @see   java.util.concurrent.ThreadPoolExecutor #getTaskCount();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getQueue();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getActiveCount();
	 * @see   java.util.concurrent.ThreadPoolExecutor #getCompletedTaskCount();
	 * @see   java.util.concurrent.ThreadPoolExecutor #isShutdown();
	 * @see   java.util.concurrent.ThreadPoolExecutor #isTerminated();
	 * @see   java.util.concurrent.ThreadPoolExecutor #isTerminating();
	 */
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		String msg = String.format("Thread pool is EXHANSTED!" +
	           " Thread Name: %s, Pool Size: %d (active: %d, core : %d, max: %d, largest: %d), Task: %d (completed: %d)," +
				"Executor status :(isShutDown: %s, isTerminated:%s, isTerminating:%s)",
				threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
				e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
		LOGGER.warn(msg);
		throw new RejectedExecutionException(msg);
	}

}
