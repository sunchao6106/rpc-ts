package com.sunchao.rpc.base.transport.dispatcher;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.ChannelHandlerDelegate;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.utils.NamedThreadFactory;

/**
 * Thread pool . The handle operation all use caller thread
 * to execute, so there just provide the common thread pool,
 * and sub class need to override the handle method to implement
 * the mutil-thread operation.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class WrapperChannelHandler implements ChannelHandlerDelegate {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(WrapperChannelHandler.class);
	
	protected static final ExecutorService WORKER_SHARE_THREAD = Executors.newCachedThreadPool(
			new NamedThreadFactory("RPC-Worker-SHAREThreadPool", true));
	
	protected final ExecutorService executor;
	
	protected final ChannelHandler handler;
	
	protected final ClientConfig config;
	
	public WrapperChannelHandler(ChannelHandler handler, ClientConfig config) {
		this.handler = handler;
		this.config = config;
		executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1,
				Runtime.getRuntime().availableProcessors() + 1, 0, TimeUnit.MILLISECONDS, 
				new SynchronousQueue<Runnable>());
	}
	
	public void close() {
		try {
			executor.shutdown();
		} catch (Throwable t) {
			LOGGER.warn(t.getMessage(), t);
		}
	}

	public void onConnected(Channel channel) throws RPCException {
		handler.onConnected(channel);
	}

	public void onDisconnected(Channel channel) throws RPCException {
		handler.onDisconnected(channel);
	}

	public void onSent(Channel channel, Object message) throws RPCException {
		handler.onSent(channel, message);
	}

	public void onReceived(Channel channel, Object message) throws RPCException {
		handler.onReceived(channel, message);
	}

	public void onError(Channel channel, Throwable cause) throws RPCException {
		handler.onError(channel, cause);
	}

	public ChannelHandler getHandler() {
		if (handler instanceof ChannelHandlerDelegate)
			return ((ChannelHandlerDelegate) handler).getHandler();
		return handler;
	}

	public ClientConfig getConfig() {
		return config;
	}
}
