package com.sunchao.rpc.base.transport.peer.support.impl;

import com.sunchao.rpc.base.TransportUtil;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.peer.Peer;
import com.sunchao.rpc.base.transport.peer.PeerClient;
import com.sunchao.rpc.base.transport.peer.PeerHandler;
import com.sunchao.rpc.base.transport.peer.PeerServer;
import com.sunchao.rpc.base.transport.support.DataHandler;
import com.sunchao.rpc.common.ClientConfig;

public class HeaderPeer implements Peer {

	public PeerServer bind(ClientConfig config, PeerHandler handler)
			throws RPCException {
		return new DefaultPeerServer(TransportUtil.bind(config, new DataHandler(new DefaultPeerHandler(handler))));
	}

	public PeerClient connect(ClientConfig config, PeerHandler handler)
			throws RPCException {
		return new DefaultPeerClient(TransportUtil.connect(config, new DataHandler(new DefaultPeerHandler(handler))));
	}

}
