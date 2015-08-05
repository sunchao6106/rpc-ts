package com.sunchao.rpc.base;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.Client;
import com.sunchao.rpc.base.transport.Server;
import com.sunchao.rpc.base.transport.Transport;
import com.sunchao.rpc.base.transport.channelhandler.ChannelHandlerAdapter;
import com.sunchao.rpc.base.transport.channelhandler.ChannelHandlerDispatcher;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.extension.HotSwapLoader;


/**
 * Transport layer facade.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class TransportUtil {
	
	public static Server bind(ClientConfig address, ChannelHandler... handlers) throws RPCException {
		if (address == null)
			throw new IllegalArgumentException("the server bind address cannot be null");
		if (handlers == null || handlers.length == 0)
			throw new IllegalArgumentException("the channel handler cannot be null.");
		ChannelHandler handler;
		if (handlers.length == 1) {
			handler = handlers[0];
		} else {
			handler = new ChannelHandlerDispatcher(handlers);
		}
		return getTransport().bind(address, handler);
	}
	
	public static Client connect(ClientConfig address, ChannelHandler...channelHandlers) throws RPCException {
		if (address == null)
			throw new IllegalArgumentException("the client connect remote address cannot be null.");
		ChannelHandler handler;
		if (channelHandlers == null || channelHandlers.length == 0) {
			handler = new ChannelHandlerAdapter();
		} else if (channelHandlers.length == 1) {
			handler = channelHandlers[0];
		} else {
			handler = new ChannelHandlerDispatcher(channelHandlers);
		}
		return getTransport().connect(address, handler);	
	}
	
	public static Transport getTransport() {
		return HotSwapLoader.getExtensionLoader(Transport.class).getAdaptiveExtension();
	}
	
	private TransportUtil() {
		
	}

}
