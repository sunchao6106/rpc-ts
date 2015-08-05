package com.sunchao.rpc.base.transport.dispatcher.message;

import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.Dispatcher;
import com.sunchao.rpc.common.ClientConfig;

/**
 * return the message only channel handler.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class MessageOnlyDispatcher implements Dispatcher {

	public ChannelHandler dispatch(ChannelHandler handler, ClientConfig config) {
		return new MessageOnlyChannelHandler(handler, config);
	}

}
