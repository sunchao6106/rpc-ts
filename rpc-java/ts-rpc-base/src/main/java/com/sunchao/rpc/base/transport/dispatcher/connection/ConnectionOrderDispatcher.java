package com.sunchao.rpc.base.transport.dispatcher.connection;

import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.Dispatcher;
import com.sunchao.rpc.common.ClientConfig;

public class ConnectionOrderDispatcher implements Dispatcher {

	public ChannelHandler dispatch(ChannelHandler handler, ClientConfig config) {
		return new OrderConnectionChannelHander(handler, config);
	}

}
