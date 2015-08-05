package com.sunchao.rpc.base.transport.peer;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.extension.Component;
import com.sunchao.rpc.common.extension.HotSwap;

/**
 * Two way.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Component("peer")
public interface Peer {

	/**
	 * bind.
	 * 
	 * @param config
	 * @param handler
	 * @return
	 * @throws RPCException
	 */
	@HotSwap("peer")
	PeerServer bind(ClientConfig config, PeerHandler handler) throws RPCException;
	
	/**
	 * connect.
	 * 
	 * @param config
	 * @param handler
	 * @return
	 * @throws RPCException
	 */
	@HotSwap("peer")
	PeerClient connect(ClientConfig config, PeerHandler handler) throws RPCException;
}
