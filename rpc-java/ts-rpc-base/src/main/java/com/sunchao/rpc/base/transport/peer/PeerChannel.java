package com.sunchao.rpc.base.transport.peer;

import com.sunchao.rpc.base.async.Future;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
/**
 * PeerChannel.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface PeerChannel extends Channel {

	/**
	 * Send request.
	 * 
	 * @param request
	 * @return
	 * @throws RPCException
	 */
	Future request(Object request) throws RPCException;
	
	/**
	 * sent request with the specified timeout.
	 * 
	 * @param request
	 * @param timeout
	 * @return
	 * @throws RPCException
	 */
	Future request(Object request, int timeout) throws RPCException;
	
	/**
	 * Get message handler.
	 * 
	 * @return
	 */
	PeerHandler getPeerHandler();
}
