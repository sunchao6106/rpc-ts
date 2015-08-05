package com.sunchao.rpc.base.transport.dispatcher.message;

import java.util.concurrent.ExecutorService;

import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.dispatcher.ChannelEventTask;
import com.sunchao.rpc.base.transport.dispatcher.ChannelEventTask.ChannelState;
import com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler;
import com.sunchao.rpc.common.ClientConfig;

/**
 * Just only receive handle use the thread pool.
 * And all other operation execute by the caller thread.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class MessageOnlyChannelHandler extends WrapperChannelHandler {

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onReceived(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onReceived(Channel channel, Object message) throws RPCException {
		ExecutorService es = executor;
		if (es == null || es.isShutdown()) 
			es = WORKER_SHARE_THREAD;
		try {
			es.execute(new ChannelEventTask(channel, handler, ChannelState.REVEIVED, message));
		} catch (Throwable t) {
			throw new RPCApplicationException(message, channel, "error when handle receive event.", t);
		}
	}

	public MessageOnlyChannelHandler(ChannelHandler handler, ClientConfig config) {
		super(handler, config);
	}

}
