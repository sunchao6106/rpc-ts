package com.sunchao.rpc.base.transport;

import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.extension.Component;
import com.sunchao.rpc.common.extension.HotSwap;

/**
 * The transport event dispatcher.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Component("threadpool")
public interface Dispatcher {

	@HotSwap("dispatcher")
	ChannelHandler dispatch(ChannelHandler handler, ClientConfig config);
}
