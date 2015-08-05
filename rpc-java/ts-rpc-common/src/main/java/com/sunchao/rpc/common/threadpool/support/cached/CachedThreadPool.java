package com.sunchao.rpc.common.threadpool.support.cached;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sunchao.rpc.common.Constants;
import com.sunchao.rpc.common.threadpool.ThreadPool;
import com.sunchao.rpc.common.threadpool.support.AbortPolicyWithReport;
import com.sunchao.rpc.common.utils.NamedThreadFactory;

/**
 * <p>
 * this thread pool is flexible, elastic, and the idle thread will be collected
 * after <i>1 minute<i>. And when new request will create a new thread.
 * from : <code>Executors.newCachedThreadPool()</code>.
 * @see java.util.concurrent.Executors#newCachedThreadPool();
 * </p>
 * @author sunchao
 *
 */
public class CachedThreadPool implements ThreadPool {

	public Executor getExecutor() {
		String name =  Constants.DEFAULT_THREAD_NAME;
		int cores   =  Constants.DEFAULT_CORE_THREADS;
		int threads =  Integer.MAX_VALUE;
		int queues =   Constants.DEFAULT_QUEUES;
		int alives =  Constants.DEFAULT_ALIVE;
		return new ThreadPoolExecutor(cores, threads, alives, TimeUnit.MILLISECONDS, 
				queues == 0 ? new SynchronousQueue<Runnable>() : 
					(queues < 0 ? new LinkedBlockingQueue<Runnable>()
							: new LinkedBlockingQueue<Runnable>(queues)),
							new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
	}

	
}
