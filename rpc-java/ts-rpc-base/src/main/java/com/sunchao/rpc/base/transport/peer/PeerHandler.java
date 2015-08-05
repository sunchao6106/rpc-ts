package com.sunchao.rpc.base.transport.peer;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.ChannelHandler;

public interface PeerHandler extends ChannelHandler {
	
	/**
	 * reply.
	 * 
	 * @param channel
	 * @param request
	 * @return
	 * @throws RPCException
	 */
	Object reply(PeerChannel channel, Object request) throws RPCException;

}
