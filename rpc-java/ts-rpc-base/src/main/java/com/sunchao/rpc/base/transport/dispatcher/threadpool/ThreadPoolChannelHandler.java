package com.sunchao.rpc.base.transport.dispatcher.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.dispatcher.ChannelEventTask;
import com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler;
import com.sunchao.rpc.base.transport.dispatcher.ChannelEventTask.ChannelState;
import com.sunchao.rpc.common.ClientConfig;

/**
 * except the send operation, other all operation execute by thread pool.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ThreadPoolChannelHandler extends WrapperChannelHandler {

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#close()
	 */
	@Override
	public void close() {
		super.close();
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onConnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onConnected(Channel channel) throws RPCException {
		ExecutorService es = getExecutorService();
		try {
		   es.execute(new ChannelEventTask(channel, handler, 
				   ChannelState.CONNECTED));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "connect operation error", t, channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onDisconnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onDisconnected(Channel channel) throws RPCException {
		ExecutorService es = getExecutorService();
		try {
		   es.execute(new ChannelEventTask(channel, handler, 
				   ChannelState.DISCONNECTED));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "disconnect operation error", t, channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onSent(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 
	@Override
	@Deprecated
	public void onSent(Channel channel, Object message) throws RPCException {
		ExecutorService es = getExecutorService();
		try {
		   es.execute(new ChannelEventTask(channel, handler, 
				   ChannelState.SENT));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "sent operation error", t, channel, null);
		}
	}*/

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onReceived(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onReceived(Channel channel, Object message) throws RPCException {
		ExecutorService es = getExecutorService();
		try {
		   es.execute(new ChannelEventTask(channel, handler, 
				   ChannelState.REVEIVED, message));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "receive operation error", t, channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onError(com.sunchao.rpc.base.transport.Channel, java.lang.Throwable)
	 */
	@Override
	public void onError(Channel channel, Throwable cause) throws RPCException {
		ExecutorService es = getExecutorService();
		try {
		   es.execute(new ChannelEventTask(channel, handler, 
				   ChannelState.CAUGHT, cause));
		} catch (Throwable t) {
			throw new RPCApplicationException(RPCApplicationException.UNKNOWN, "error operation error", t, channel, null);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#getHandler()
	 */
	@Override
	public ChannelHandler getHandler() {
		return super.getHandler();
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#getConfig()
	 */
	@Override
	public ClientConfig getConfig() {
		return super.getConfig();
	}

	public ThreadPoolChannelHandler(ChannelHandler handler, ClientConfig config) {
		super(handler, config);
	}

	private ExecutorService getExecutorService() {
		ExecutorService es = executor;
		if (es == null || es.isShutdown()) 
			es = WORKER_SHARE_THREAD;
		return es;
		
	}
}
