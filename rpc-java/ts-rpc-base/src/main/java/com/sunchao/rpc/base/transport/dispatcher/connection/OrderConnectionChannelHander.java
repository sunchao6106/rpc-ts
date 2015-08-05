package com.sunchao.rpc.base.transport.dispatcher.connection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.dispatcher.ChannelEventTask;
import com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler;
import com.sunchao.rpc.base.transport.dispatcher.ChannelEventTask.ChannelState;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.threadpool.support.AbortPolicyWithReport;
import com.sunchao.rpc.common.utils.NamedThreadFactory;

public class OrderConnectionChannelHander extends WrapperChannelHandler {
	
	protected final ThreadPoolExecutor connectionExecutor;
	
	private final int queueWariningLimit;

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onConnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onConnected(Channel channel) throws RPCException {
		try {
			checkQueueLength();
			connectionExecutor.execute(new ChannelEventTask(channel, handler, ChannelState.CONNECTED));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "error when handle connected event", channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onDisconnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onDisconnected(Channel channel) throws RPCException {
		try {
			checkQueueLength();
			connectionExecutor.execute(new ChannelEventTask(channel, handler, ChannelState.DISCONNECTED));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "error when handle disconnected event", channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onSent(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onSent(Channel channel, Object message) throws RPCException {
		// TODO Auto-generated method stub
		super.onSent(channel, message);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onReceived(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onReceived(Channel channel, Object message) throws RPCException {
		ExecutorService es = executor;
		if (es == null || es.isShutdown()) 
			es = WORKER_SHARE_THREAD;
		try {
			es.execute(new ChannelEventTask(channel,handler, ChannelState.REVEIVED, message));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "error when handle received event", channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onError(com.sunchao.rpc.base.transport.Channel, java.lang.Throwable)
	 */
	@Override
	public void onError(Channel channel, Throwable cause) throws RPCException {
		ExecutorService es = executor;
		if (es == null || es.isShutdown()) 
			es = WORKER_SHARE_THREAD;
		try {
			es.execute(new ChannelEventTask(channel,handler, ChannelState.CAUGHT, cause));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "error when handle error event", channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#getHandler()
	 */
	@Override
	public ChannelHandler getHandler() {
		// TODO Auto-generated method stub
		return super.getHandler();
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#getConfig()
	 */
	@Override
	public ClientConfig getConfig() {
		// TODO Auto-generated method stub
		return super.getConfig();
	}

	public OrderConnectionChannelHander(ChannelHandler handler,
			ClientConfig config) {
		super(handler, config);
		connectionExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, 
				new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
				new NamedThreadFactory("RPC-Thread-Pool", true),
				new AbortPolicyWithReport("RPC-Thread-Pool"));
		queueWariningLimit = 200;
	}
	
	private void checkQueueLength() {
		if (connectionExecutor.getQueue().size() > queueWariningLimit) {
			LOGGER.warn(new IllegalThreadStateException("connection ordered channel handler' queue size: " + connectionExecutor.getQueue().size()
					+  " exceed the warning limit number: " + queueWariningLimit));
		}
	}

}
