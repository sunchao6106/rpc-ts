package com.sunchao.rpc.base.transport.peer.support;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.channelhandler.ChannelHandlerDispatcher;
import com.sunchao.rpc.base.transport.peer.PeerChannel;
import com.sunchao.rpc.base.transport.peer.PeerHandler;
import com.sunchao.rpc.base.transport.peer.Replier;

public class PeerHandlerDispatcher implements PeerHandler {

	/**
	 * the request handler dispatcher.
	 */
	private final ReplierDispatcher replierDispatcher;
	
	/**
	 * the event handler dispatcher. (connect, disconnect, send ,receive and so on).
	 */
	private final ChannelHandlerDispatcher handlerDispatcher;
	
	public PeerHandlerDispatcher() {
		replierDispatcher = new ReplierDispatcher();
		handlerDispatcher = new ChannelHandlerDispatcher();
	}
	
	public PeerHandlerDispatcher(Replier<?> replier) {
		replierDispatcher = new ReplierDispatcher(replier);
		handlerDispatcher = new ChannelHandlerDispatcher();
	}
	
	public PeerHandlerDispatcher(ChannelHandler... channelHandlers) {
		replierDispatcher = new ReplierDispatcher();
		handlerDispatcher = new ChannelHandlerDispatcher(channelHandlers);
	}
	
	public PeerHandlerDispatcher(Replier<?> replier, ChannelHandler... channelHandlers) {
		replierDispatcher = new ReplierDispatcher(replier);
		handlerDispatcher = new ChannelHandlerDispatcher(channelHandlers);
	}
	
	public PeerHandlerDispatcher addChannelHandler(ChannelHandler handler) {
		handlerDispatcher.addChannelHandler(handler);
		return this;
	}
	
	public PeerHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
		handlerDispatcher.removeChannelHandler(handler);
		return this;
	}
	
	public <T> PeerHandlerDispatcher addReplier(Class<T> type , Replier<T> replier) {
		replierDispatcher.addReplier(type, replier);
		return this;
	}
	
	public <T> PeerHandlerDispatcher removeReplier(Class<T> type) {
		replierDispatcher.removeReplier(type);
		return this;
	}
	
	public void onConnected(Channel channel) throws RPCException {
		handlerDispatcher.onConnected(channel);
	}

	public void onDisconnected(Channel channel) throws RPCException {
		handlerDispatcher.onDisconnected(channel);
	}

	public void onSent(Channel channel, Object message) throws RPCException {
        handlerDispatcher.onSent(channel, message);
	}

	public void onReceived(Channel channel, Object message) throws RPCException {
        handlerDispatcher.onReceived(channel, message);
	}

	public void onError(Channel channel, Throwable cause) throws RPCException {
        handlerDispatcher.onError(channel, cause);
	}

	public Object reply(PeerChannel channel, Object request)
			throws RPCException {
		return replierDispatcher.reply(channel, request);
	}

}
