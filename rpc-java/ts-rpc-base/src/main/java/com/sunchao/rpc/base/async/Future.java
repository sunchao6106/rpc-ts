package com.sunchao.rpc.base.async;

import org.apache.http.annotation.ThreadSafe;

import com.sunchao.rpc.base.exception.RPCException;

/**
 * Client-Side response future.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@ThreadSafe
public interface Future {

	/**
	 * Get Result.
	 * 
	 * @return
	 * @throws RPCException
	 */
	Object get() throws RPCException;
	
	/**
	 * Get result with the specified timeout.
	 * 
	 * @param timeout
	 * @return
	 * @throws RPCException
	 */
	Object get(int timeout) throws RPCException;
	
	/**
	 * set the asynchronous call back.
	 * 
	 * @param callback
	 */
	void setCallback(AsyncCallback callback);
	
	/**
	 * judge whether or not done.
	 * 
	 * @return
	 */
	boolean isDone();
	
	/**
	 * the client cancel the remote invocation.
	 * 
	 * @return
	 */
	void cancel();
}
