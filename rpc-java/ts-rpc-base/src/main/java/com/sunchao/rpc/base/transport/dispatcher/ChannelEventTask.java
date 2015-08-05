package com.sunchao.rpc.base.transport.dispatcher;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

public class ChannelEventTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelEventTask.class);
	
	private final ChannelHandler handler;
	private final Channel channel;
	private ChannelState state;
	private final Throwable cause;
	private final Object message;
	
	public ChannelEventTask(Channel channel, ChannelHandler handler, 
			ChannelState state) {
		this(channel, handler, state, null);
	}
	
	public ChannelEventTask(Channel channel, ChannelHandler handler,
			ChannelState state, Object message) {
		this(channel, handler, state, message, null);
	}
	
	public ChannelEventTask(Channel channel, ChannelHandler handler, 
			ChannelState state, Throwable cause) {
		this(channel, handler, state, null, cause);
	}
	
	public ChannelEventTask(Channel channel, ChannelHandler handler, 
			ChannelState state, Object message, Throwable cause) {
		this.channel = channel;
		this.handler = handler;
		this.state = state;
		this.cause = cause;
		this.message = message;
	}
	
	public void run() {
		switch(state) {
		case CONNECTED:
			try {
				handler.onConnected(channel);
			} catch (Throwable t) {
				LOGGER.warn("ChannelEventWorker handler " + state + " operation error, channel is "
						+ channel, t);
			}
			break;
		case DISCONNECTED:
			try {
				handler.onDisconnected(channel);
			} catch (Throwable t) {
				LOGGER.warn("ChannelEventWorker handler " + state + " operation error, channel is "
						+ channel, t);
			}
			break;
		case SENT:
			try {
				handler.onSent(channel, message);
			} catch (Throwable t) {
				LOGGER.warn("ChannelEventWorker handler " + state + " operation error, channel is "
						+ channel, t);
			}
			break;
		case REVEIVED:
			try {
				handler.onReceived(channel, message);
			} catch (Throwable t) {
				LOGGER.warn("ChannelEventWorker handler " + state + " operation error, channel is "
						+ channel, t);
			}
			break;
		case CAUGHT:
			try {
				handler.onError(channel, cause);
			} catch (Throwable t) {
				LOGGER.warn("ChannelEventWorker handler " + state + " operation error, channel is "
						+ channel, t);
			}
		    break;
		 default:
			 LOGGER.warn("unknow state: " + state + ", message is " + message);
		}
	}
	
	public enum ChannelState {
		/**
		 * connected state
		 */
		CONNECTED,
		
		/**
		 * disconnected state.
		 */
		DISCONNECTED,
		
		/**
		 * sent state.
		 */
		SENT,
		
		/**
		 * received state.
		 */
		REVEIVED,
		
		/**
		 * error caught state.
		 */
		CAUGHT;
	}

}

 