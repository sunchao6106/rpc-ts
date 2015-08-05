package com.sunchao.rpc.base.transport;

import com.sunchao.rpc.base.exception.RPCException;
/**
 * Channel Handler. Do the specified thing by the 
 * specified channel event happened.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface ChannelHandler {
	
	/**
	 * When the channel is connected, the handler
	 * method will be invoked.
	 * 
	 * @param channel
	 * @throws RPCException
	 */
	void onConnected(Channel channel) throws RPCException;
	
	/**
	 * When the channel is disconnected, the handler 
	 * method will be invoked.
	 * 
	 * @param channel
	 * @throws RPCException
	 */
	void onDisconnected(Channel channel) throws RPCException;
	
	/**
	 * When the channel happen write event, and the handler
	 * method will be invoked.
	 * 
	 * @param channel
	 * @param message
	 * @throws RPCException
	 */
	void onSent(Channel channel, Object message) throws RPCException;
	
	/**
	 * When the channel happen read event, and the handler 
	 * method will be invoked.
	 * 
	 * @param channel
	 * @param message
	 * @throws RPCException
	 */
	void onReceived(Channel channel, Object message) throws RPCException;
	
	/**
	 * When the channel happen throwable, and the handler 
	 * method will be invoked.
	 * 
	 * @param channel
	 * @param cause
	 * @throws RPCException
	 */
	void onError(Channel channel, Throwable cause) throws RPCException;

}
