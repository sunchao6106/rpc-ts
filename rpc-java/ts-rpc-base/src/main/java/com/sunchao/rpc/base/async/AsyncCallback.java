package com.sunchao.rpc.base.async;

import com.sunchao.rpc.base.metadata.Response;

/**
 * Interface for receiving asynchronous callback.
 * For each request with an asynchronous callback with two way,
 * either {@link #handleResult(Object)} or {@link #handleError(Throwable)}
 * will be invoked.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 * @param <T> The result type.
 */
public interface AsyncCallback {
	
	/**
	 * Invoked in the normal RPC method call result.
	 * @param result
	 *              the result returned in the callback. 
	 */
	void handleResult(Object result);
	
	
	/**
	 * Invoked in the error RPC method call result.
	 * @param error
	 *              the error returned in the callback.
	 */
	void handleError(Throwable error);

}
