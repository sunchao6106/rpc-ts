package com.sunchao.rpc.common.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sunchao.rpc.common.annotation.Utility;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Executor Utility.
 * 
 * @author sunchao
 * @see java.util.concurrent.ExecutorService #awaitTermination(long, TimeUnit);
 * @see java.util.concurrent.ExecutorService #execute(Runnable);
 * @see java.util.concurrent.ExecutorService #invokeAll(java.util.Collection);
 * @see java.util.concurrent.ExecutorService #isShutdown();
 * @see java.util.concurrent.ExecutorService #isTerminated();
 * @see java.util.concurrent.ExecutorService #shutdown();
 * @see java.util.concurrent.ExecutorService #shutdownNow();
 * @see java.util.concurrent.ExecutorService #submit(java.util.concurrent.Callable);
 * @see java.util.concurrent.ExecutorService #submit(Runnable);
 */
@Utility("ExecutorUtil")
public class ExecutorUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtil.class);
	/**
	 * this theadPoolExecutor equals to  
	 *  @see java.util.concurrent.Executors # newSingleThreadExecutor();
	 *   Daemon thread.
	 */
	private static final ThreadPoolExecutor SHUT_DOWN_EXECUTOR =  new ThreadPoolExecutor(0, 1
			,0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1000),
			new NamedThreadFactory("Close-ExecutorService-Timer", true)); 
	
	/**
	 * The super interface is executor.
	 * and the sub interface is executor service.
	 * @see java.util.concurrent.Executor
	 * @see java.util.concurrent.ExecutorService.
	 * 
	 * @param executor
	 *        the thread executor.
	 * @return
	 *        the flag whether is shut down?
	 */
	public static boolean isShutdown(Executor executor) {
		if (executor instanceof ExecutorService) {
			if (((ExecutorService) executor).isShutdown()) {
				return true;
			}
		}
		return false;
	}
	
	 
	/**
	 * make difference between the method <code>ExecutorService.shutdown()</code>
	 * and the method <code>ExecutorService.shutdownNow()</code>
	 * also the method <code>ExecutorService.awaitTermination()</code>,
	 * <code>ExecutorService.isTerminated()</code>
	 * @param executor
	 *         the task executor.
	 * @param timeout
	 *         the timeout time.
	 */
    public static void gracefulShutdown(Executor executor, int timeout) {
		if (! (executor instanceof ExecutorService) || isShutdown(executor)) {
			return;
		}
		
		final ExecutorService es = (ExecutorService) executor;
		try {
			es.shutdown(); // reject and disable the task from being submitted, and try to run the tasks that have been submitted.
		} catch (SecurityException ex2) {
			return;
		} catch (NullPointerException ex2) {
			return;
		}
		
		try {
			if (! es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
				es.shutdownNow(); // reject and disable the task from being submitted, cancel and return  the tasks list have been submitted but not running,
			}  // and try to cancel the running task with the <code>Thread.interrupt()</code>, if the running task now has no response to the interrupt metod,
			// the running task now can't stop.
		} catch (InterruptedException ex) {
			es.shutdownNow(); // be interrupted. so call the shutdownNow() method again.
			Thread.currentThread().interrupt(); // reserve the flag of interrupt.
		}
		if (! isShutdown(es)) {
			newThreadToCloseExecutor(es);
		}
	}
	
	public static void shutdownNow(Executor executor, final int timeout) {
		if (! (executor instanceof ExecutorService) || isShutdown(executor)) {
			return;
		}
		
		final ExecutorService es = (ExecutorService) executor;
		try {
			es.shutdownNow();
		} catch (SecurityException es2) {
			return;
		} catch (NullPointerException ex2) {
			return;
		}
		
		try {
			es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		if (! isShutdown(es)) {
			newThreadToCloseExecutor(es);
		}
	}
	
	/**
	 * create a new thread to close the executor.
	 * @param es
	 *         the executor which need to be closed.
	 */
	private static void newThreadToCloseExecutor(final ExecutorService es) {
		if (! isShutdown(es)) {
			SHUT_DOWN_EXECUTOR.execute(new Runnable(){
				public void run() {
					try {
						for (int i = 0; i < 1000; i++) 
						{
							es.shutdownNow();
							if (es.awaitTermination(10, TimeUnit.MILLISECONDS)) {
								break;
							}
						}
					} catch (InterruptedException  e) {
						Thread.currentThread().interrupt();//reserve the interrupt flag!
					} catch (Throwable t) {
						LOGGER.warn(t.getMessage(), t);
					}
				}
			});
		}
	}
	

}
 