package com.sunchao.rpc.common.threadpool.support.limited;

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
 * The thread pool will increment when new request coming until the max limit,
 * and the idle thread has no keep alive times limit, so they will exists, and
 * not go back.
 * 
 * @author sunchao
 *
 */

public class LimitedThreadPool implements ThreadPool {

	public Executor getExecutor() {
		String name = Constants.DEFAULT_THREAD_NAME;
		int cores =   Constants.DEFAULT_CORE_THREADS;
		int threads = Constants.DEFAULT_THREADS;
		int queues =  Constants.DEFAULT_QUEUES;
		return new ThreadPoolExecutor(cores, threads, Long.MAX_VALUE, TimeUnit.MILLISECONDS,
				queues == 0 ? new SynchronousQueue<Runnable>() :
					(queues < 0 ? new LinkedBlockingQueue<Runnable>() 
							: new LinkedBlockingQueue<Runnable>(queues)),
							new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
	}

}
