package com.sunchao.rpc.base.transport.channelhandler;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
/**
 * empty operation(no-op)
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ChannelHandlerAdapter implements ChannelHandler {

	public void onConnected(Channel channel) throws RPCException {
		
	}

	public void onDisconnected(Channel channel) throws RPCException {
	
	}

	public void onSent(Channel channel, Object message) throws RPCException {
		
	}

	public void onReceived(Channel channel, Object message) throws RPCException {
		
	}

	public void onError(Channel channel, Throwable cause) throws RPCException {
		
	}
}
