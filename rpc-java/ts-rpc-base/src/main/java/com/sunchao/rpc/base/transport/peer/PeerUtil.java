package com.sunchao.rpc.base.transport.peer;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.channelhandler.ChannelHandlerAdapter;
import com.sunchao.rpc.base.transport.peer.support.PeerHandlerDispatcher;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.extension.HotSwapLoader;

/**
 * Peer facade.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class PeerUtil {

	public static PeerServer bind(ClientConfig config, Replier<?> replier) throws RPCException{
		return bind(config, new ChannelHandlerAdapter(), replier);
	}
	
	public static PeerServer bind(ClientConfig config, ChannelHandler handler, Replier<?> replier) throws RPCException {
		return bind(config, new PeerHandlerDispatcher(replier, handler));
	}
	
	public static PeerServer bind(ClientConfig config, PeerHandler handler) throws RPCException {
		if (config == null) 
			throw new IllegalArgumentException("the configuration cannot be null.");
		if (handler == null)
			throw new IllegalArgumentException("the peer handler cannot be null.");
		config = config.setParameterIfAbsent("codec", "peerHeader");
		return getPeer(config).bind(config, handler);
	}
	
	public static PeerClient connect(ClientConfig config) throws RPCException {
		return connect(config, new ChannelHandlerAdapter(), null);
	}
	
	public static PeerClient connect(ClientConfig config, ChannelHandler handler, 
			Replier<?> replier) throws RPCException {
		return connect(config, new PeerHandlerDispatcher(replier, handler));
	}
	
	public static PeerClient connect(ClientConfig config, PeerHandler handler) throws RPCException {
		if (config == null)
			throw new IllegalArgumentException("the configuration cannot be null.");
		if (handler == null)
			throw new IllegalArgumentException("the peer handler cannot be null.");
		config = config.setParameterIfAbsent("codec", "peerHeader");
		return getPeer(config).connect(config, handler);
	}
	
	public static Peer getPeer(ClientConfig config) {
		String type = config.getParameterOrDefault("transport", "peer");
		return getPeer(type);
	}
	
	public static Peer getPeer(String type) {
		return HotSwapLoader.getExtensionLoader(Peer.class).getExtension(type);
	}
}
