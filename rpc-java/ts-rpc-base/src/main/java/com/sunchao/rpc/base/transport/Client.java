package com.sunchao.rpc.base.transport;

import com.sunchao.rpc.base.exception.RPCException;


public interface Client extends Endpoint, Channel {

	void reconnect() throws RPCException;
}
