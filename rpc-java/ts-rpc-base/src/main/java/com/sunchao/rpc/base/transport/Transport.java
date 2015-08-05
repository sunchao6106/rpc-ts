package com.sunchao.rpc.base.transport;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.extension.Component;
import com.sunchao.rpc.common.extension.HotSwap;

/**
 * The network communication. can I understand the interface different from the
 * {@link Peer} just it's {@code ChannelHandler} has no reply function,
 * just only one way. or the more high layer's Abstraction 
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Component("jdk")
public interface Transport {
	
	@HotSwap("server")
	Server bind(ClientConfig config, ChannelHandler handler) throws RPCException;
	
	@HotSwap("client")
	Client connect(ClientConfig config, ChannelHandler handler) throws RPCException;

}
