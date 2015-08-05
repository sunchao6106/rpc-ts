package com.sunchao.rpc.base.transport.dispatcher.threadpool;

import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.Dispatcher;
import com.sunchao.rpc.common.ClientConfig;

public class ThreadPoolDispatcher implements Dispatcher {

	public ChannelHandler dispatch(ChannelHandler handler, ClientConfig config) {
		return new ThreadPoolChannelHandler(handler, config);
	}

	
}
