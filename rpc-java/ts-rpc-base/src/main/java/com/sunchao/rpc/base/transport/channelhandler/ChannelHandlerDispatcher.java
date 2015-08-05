package com.sunchao.rpc.base.transport.channelhandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
/**
 * Channel Listener Dispatcher.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ChannelHandlerDispatcher implements ChannelHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelHandlerDispatcher.class);
	
	private final Collection<ChannelHandler> channelHandlers = new CopyOnWriteArraySet<ChannelHandler>();
	
	public ChannelHandlerDispatcher() {
		
	}
	
	public ChannelHandlerDispatcher(ChannelHandler... handlers) {
		this(handlers == null ? null : Arrays.asList(handlers));
	}
	
	public ChannelHandlerDispatcher(Collection<ChannelHandler> handlers) {
		if (handlers != null && handlers.size() > 0)
			this.channelHandlers.addAll(handlers);
	}
	
	public Collection<ChannelHandler> getChannelHandlers() {
		return channelHandlers;
	}
	
	public ChannelHandlerDispatcher addChannelHandler(ChannelHandler handler) {
		this.channelHandlers.add(handler);
		return this;
	}
	
	public ChannelHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
		this.channelHandlers.remove(handler);
		return this;
	}
	
	/**
	 * delegate.
	 */
	public void onConnected(Channel channel) throws RPCException {
		for (ChannelHandler listener : channelHandlers) {
			try {
				listener.onConnected(channel);
			} catch (Throwable t) {
				LOGGER.error(t.getMessage(), t);
			}
		}
	}

	public void onDisconnected(Channel channel) throws RPCException {
		for (ChannelHandler listener : channelHandlers) {
			try {
				listener.onDisconnected(channel);
			} catch (Throwable t) {
				LOGGER.error(t.getMessage(), t);
			}
		}
	}

	public void onSent(Channel channel, Object message) throws RPCException {
		for (ChannelHandler listener : channelHandlers) {
			try {
				listener.onSent(channel, message);
			} catch (Throwable t) {
				LOGGER.error(t.getMessage(), t);
			}
		}
	}

	public void onReceived(Channel channel, Object message) throws RPCException {
		for (ChannelHandler listener : channelHandlers) {
			try {
				listener.onReceived(channel, message);
			} catch (Throwable t) {
				LOGGER.error(t.getMessage(), t);
			}
		}
	}

	public void onError(Channel channel, Throwable cause) throws RPCException {
		for (ChannelHandler listener : channelHandlers) {
			try {
				listener.onError(channel, cause);
			} catch (Throwable t) {
				LOGGER.error(t.getMessage(), t);
			}
		}
	}

}
