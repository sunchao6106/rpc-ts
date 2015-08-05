package com.sunchao.rpc.common.threadpool.support.fixed;

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
 * this executor pool start with the fixed size threads, and not
 * elastic, <code>Executors.newFixedThreadPool()</code>.
 * 
 * @author sunchao
 *@see java.util.concurrent.Executors #newFixedThreadPool(int);
 *
 */
public class FixedThreadPool implements ThreadPool {

	public Executor getExecutor() {
		String name =  Constants.DEFAULT_THREAD_NAME;
		int threads =  Constants.DEFAULT_THREADS;
		int queues =   Constants.DEFAULT_QUEUES;
		return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
				queues == 0 ? new SynchronousQueue<Runnable>() :
					(queues < 0 ? new LinkedBlockingQueue<Runnable>()
							: new LinkedBlockingQueue<Runnable>(queues)),
							new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
	}

}
