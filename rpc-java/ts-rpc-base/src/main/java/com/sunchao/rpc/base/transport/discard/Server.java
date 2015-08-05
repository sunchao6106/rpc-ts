package com.sunchao.rpc.base.transport.discard;

import com.sunchao.rpc.base.exception.RPCTransportException;

/**
 * Server Interface.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Deprecated
public interface Server {
	
	/**
	 * server start method.
	 * @param port
	 *           the listen port.
	 * @throws Exception
	 */
	void start(int port) throws RPCTransportException;
	
	
	/**
	 * server prepare, handle, clean up.
	 * 
	 * @throws Exception
	 */
	void serve() throws RPCTransportException;
	
	/**
	 * server stop method.
	 * 
	 * @throws Exception
	 */
	void stop() throws RPCTransportException;
	
	
}
