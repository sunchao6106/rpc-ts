package com.sunchao.rpc.base.transport.support;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.ChannelHandlerDelegate;

public abstract class AbstractChannelHandlerDelegate implements ChannelHandlerDelegate {
	
	protected ChannelHandler handler;
	
	protected AbstractChannelHandlerDelegate(ChannelHandler handler) {
		if (handler == null)
			throw new IllegalStateException("the channel handler cannot be null.");
		this.handler = handler;
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.ChannelHandler#onConnected(com.sunchao.rpc.base.transport.Channel)
	 */
	public void onConnected(Channel channel) throws RPCException {
		handler.onConnected(channel);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.ChannelHandler#onDisconnected(com.sunchao.rpc.base.transport.Channel)
	 */
	public void onDisconnected(Channel channel) throws RPCException {
		handler.onDisconnected(channel);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.ChannelHandler#onSent(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	public void onSent(Channel channel, Object message) throws RPCException {
		handler.onSent(channel, message);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.ChannelHandler#onReceived(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	public void onReceived(Channel channel, Object message) throws RPCException {
		handler.onReceived(channel, message);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.ChannelHandler#onError(com.sunchao.rpc.base.transport.Channel, java.lang.Throwable)
	 */
	public void onError(Channel channel, Throwable cause) throws RPCException {
		handler.onError(channel, cause);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.ChannelHandlerDelegate#getHandler()
	 */
	public ChannelHandler getHandler() {
		if (handler instanceof ChannelHandlerDelegate) {
			return ((ChannelHandlerDelegate)handler).getHandler();
		}
		return handler;
	}

}
