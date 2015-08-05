package com.sunchao.rpc.base.transport.peer.support.impl;

import java.net.InetSocketAddress;

import com.sunchao.rpc.base.async.ClientManager;
import com.sunchao.rpc.base.async.support.DefaultFuture;
import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.ChannelHandlerDelegate;
import com.sunchao.rpc.base.transport.additional.HeartbeatHandler;
import com.sunchao.rpc.base.transport.peer.PeerChannel;
import com.sunchao.rpc.base.transport.peer.PeerHandler;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

public class DefaultPeerHandler implements ChannelHandlerDelegate {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultPeerHandler.class);
	
	public static String READ_TIMESTAMP = HeartbeatHandler.READ_TIMESTAMP;
	
	public static String WRITE_TIMESTAMP = HeartbeatHandler.WRITE_TIMESTAMP;
	
	private final PeerHandler handler;
	
	public DefaultPeerHandler(PeerHandler handler) {
		if (handler == null) 
			throw new IllegalArgumentException("the underlying delegate handler cannot be null.");
		this.handler = handler;
		
	}
	
	public void onConnected(Channel channel) throws RPCException {
		channel.setAttribute(READ_TIMESTAMP, System.currentTimeMillis());
		channel.setAttribute(WRITE_TIMESTAMP, System.currentTimeMillis());
        DefaultPeerChannel peerChannel = DefaultPeerChannel.getOrAddChannel(channel);
        try {
        	handler.onConnected(peerChannel);
        } finally {
        	DefaultPeerChannel.removeChannelIfDisconnected(channel);
        }
	}

	public void onDisconnected(Channel channel) throws RPCException {
		channel.setAttribute(READ_TIMESTAMP, System.currentTimeMillis());
		channel.setAttribute(WRITE_TIMESTAMP, System.currentTimeMillis());
		DefaultPeerChannel peerChannel = DefaultPeerChannel.getOrAddChannel(channel);
		try {
			handler.onDisconnected(peerChannel);
		} finally {
			DefaultPeerChannel.removeChannelIfDisconnected(channel);
		}

	}

	public void onSent(Channel channel, Object message) throws RPCException {
		Throwable exception = null;
		try {
			channel.setAttribute(WRITE_TIMESTAMP, System.currentTimeMillis());
			DefaultPeerChannel peerChannel = DefaultPeerChannel.getOrAddChannel(channel);
			try {
				handler.onSent(peerChannel, message);
			} finally {
				DefaultPeerChannel.removeChannelIfDisconnected(channel);
			} 
		} catch (Throwable t) {
			exception = t;
		}
		if (message instanceof Request) {
			Request request = (Request) message;
			ClientManager.startPendingSend(channel, request);
		}
		if (exception != null) {
			if (exception instanceof RuntimeException) {
				throw (RuntimeException) exception;
			} else if (exception instanceof RPCException) {
				throw (RPCException) exception;
			} else {
				throw new RPCException("error when send packet request when the local address: " + channel.getLocalAddress()
						+ " , the remote address: " + channel.getRemoteAddress(), exception);
			}
		}
	}

	public void onReceived(Channel channel, Object message) throws RPCException {
		channel.setAttribute(READ_TIMESTAMP, System.currentTimeMillis());
		DefaultPeerChannel peerChannel = DefaultPeerChannel.getOrAddChannel(channel);
		try {
			if (message instanceof Request) {
				Request request = (Request) message;
				if (! request.isOneway()) { //two way
					Response response = handleRequest(peerChannel, request);
					channel.send(response);
				} else { //one way.
					handler.onReceived(peerChannel, request.getData());
				}
			} else if (message instanceof Response){
				handleResponse(channel, (Response) message);
			} else {
				handler.onReceived(peerChannel, message);
			}
		} finally {
			DefaultPeerChannel.removeChannelIfDisconnected(channel);
		}
	}

	/**
	 * the response handle. just for client.
	 * 
	 * @param channel
	 * @param message
	 */
	static void handleResponse(Channel channel, Response message) {
		if (message != null && !message.isHeartBeatFlag()) 
			ClientManager.received(channel, message);
	}

	public void onError(Channel channel, Throwable cause) throws RPCException {
		if (cause instanceof RPCApplicationException) {
			RPCApplicationException e = (RPCApplicationException) cause;
	    	Object msg = e.getRequest();
	    	if (msg instanceof Request) {
	    		Request req = (Request) msg;
	    		if (!req.isOneway() && ! req.isHeartBeatFlag()) {
	    			Response resp = new Response(req.getId());
	    			resp.setStatus_code(Response.SERVER_ERROR);
	    			resp.setErrorMsg(e.getMessage());
	    			channel.send(resp);
	    			return;
	    		}
	    	}
		}
		DefaultPeerChannel peerChannel = DefaultPeerChannel.getOrAddChannel(channel);
		try {
			handler.onError(peerChannel, cause);
		} finally {
			DefaultPeerChannel.removeChannelIfDisconnected(channel);
		}

	}

	public ChannelHandler getHandler() {
		if (handler instanceof ChannelHandlerDelegate) {
			return ((ChannelHandlerDelegate) handler).getHandler();
		} else {
			return handler;
		}
	}
	
	// two way.
	Response handleRequest(PeerChannel channel, Request request) throws RPCException {
		Response response = new Response(request.getId());
		if (request.isErrorRequest()) { //request is illegal 
			Object data = request.getData();
			String msg;
			if (data == null) msg = null;
			else if (data instanceof Throwable) msg = ((Throwable) data).getMessage();
			else msg = data.toString();
			response.setErrorMsg("fail to decode request due to: " + msg);
			response.setStatus_code(Response.BAD_REQUEST);
			return response;
		} 
		//find the handler by message class.
		Object message = request.getData();
		try {
			//handle request message.
			Object result = handler.reply(channel, message);
			response.setStatus_code(Response.OK);
			response.setResult(result);
		} catch (Throwable t) {
			response.setStatus_code(Response.SERVICE_ERROR);
			response.setErrorMsg(t.getMessage());
		}
		return response;
	}
	
	/**
	 * check the channel is from the client-side.
	 * 
	 * @param channel
	 * @return
	 */
	private static boolean isClientSide(Channel channel) {
		InetSocketAddress address = channel.getRemoteAddress();
		ClientConfig config = channel.getConfig();
		return config.getPort() == address.getPort() && 
				(config.getHost().equals(address.getAddress().getHostName())
						|| config.getHost().equals(address.getAddress().getHostAddress()));
				
	}

}
