package com.sunchao.rpc.common.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory class.
 * which can set the arguments; e.g. thread group, the thread name,
 * whether is daemon, 
 * @author sunchao
 *
 */
public class NamedThreadFactory implements ThreadFactory {
	
	private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);// the thread pool seq,
	
	private final AtomicInteger mThreadNum = new AtomicInteger(1); // the thread seq.
	
	private final String mPrefix; // the prefix.
	
	private final boolean mDaemon; // the flag whether is daemon.
	/**
	 * thread group .
	 */
	private final ThreadGroup mGroup; // the thread group.
	
	public NamedThreadFactory()
	{
		this("pool-" + POOL_SEQ.getAndIncrement(), false);
	}
	
	public NamedThreadFactory(String prefix)
	{
		this(prefix, false);
	}
	
	public NamedThreadFactory(String prefix, boolean daemon)
	{
		this.mPrefix = prefix + "-thread-";
		this.mDaemon = daemon;
		SecurityManager s = System.getSecurityManager();// security manager.
		mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
	}

	public Thread newThread(Runnable runnable) {
	    String name = mPrefix + mThreadNum.getAndIncrement();
	    Thread ret = new Thread(mGroup,runnable,name,0);
	    ret.setDaemon(mDaemon);
	    return ret;
	}
	
	public ThreadGroup getThreadGroup() {
		return mGroup;
	}

}
