package com.sunchao.rpc.base.transport.peer;

import com.sunchao.rpc.base.exception.RPCException;

/**
 * replier denote the role of execute the rely of request.
 * Return the execute result.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface Replier<T> {

	/**
	 * reply.
	 * 
	 * @param channel the channel 
	 * @param request the request
	 * @return the reply result
	 * @throws RPCException
	 */
	Object reply(PeerChannel channel, T request) throws RPCException;
}
