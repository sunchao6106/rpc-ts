package com.sunchao.rpc.base.transport.dispatcher.direct;

import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.Dispatcher;
import com.sunchao.rpc.common.ClientConfig;

/**
 * The caller thread execute.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class DirectExecutionDispatcher implements Dispatcher {

	public ChannelHandler dispatch(ChannelHandler handler, ClientConfig config) {
		return handler;
	}

}
