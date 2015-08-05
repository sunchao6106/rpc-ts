package com.sunchao.rpc.base.transport;

import java.net.InetSocketAddress;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.common.ClientConfig;

/**
 * The class denote the two-side of transport peers.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface Endpoint {
	
	/**
	 * Get the transport-side's configuration.
	 * 
	 * @return
	 */
	ClientConfig getConfig();
	
	/**
	 * Get the local socket address.
	 * 
	 * @return
	 */
	InetSocketAddress getLocalAddress();
	
	/**
	 * one-side of peer transport send
	 * the message to other-side.
	 * 
	 * @param message
	 * @throws RPCException
	 */
	void send(Object message) throws RPCException;
	
	/**
	 * close the connection to other-side.
	 * 
	 * @throws RPCException
	 */
	void close() throws RPCException;
	
	/**
	 * close gracefully with specified await time.
	 * 
	 * @param awaitTime
	 * @throws RPCException
	 */
	void closeGracefully(int awaitTime) throws RPCException;
	
	/**
	 * check closed.
	 * 
	 * @return
	 */
	boolean isClose();
	
	/**
	 * Get the channel handler.
	 * 
	 * @return
	 */
	ChannelHandler getChannelHandler();

}
