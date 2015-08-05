package com.sunchao.rpc.base.transport.peer;

import java.net.InetSocketAddress;
import java.util.Collection;

import com.sunchao.rpc.base.transport.Server;

public interface PeerServer extends Server {

	Collection<PeerChannel> getPeerChannels();
	
	PeerChannel getPeerChannel(InetSocketAddress remoteAddress);
}
