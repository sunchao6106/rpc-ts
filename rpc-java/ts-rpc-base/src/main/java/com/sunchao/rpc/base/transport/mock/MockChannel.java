package com.sunchao.rpc.base.transport.mock;

import java.net.InetSocketAddress;
import java.util.HashMap;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.common.ClientConfig;

/**
 * Test case. No used
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */

public class MockChannel implements Channel {

	public ClientConfig getConfig() {
		return new ClientConfig(null, 0, new HashMap<String,String>(), "1111");
	}

	public InetSocketAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public void send(Object message) throws RPCException {
		// TODO Auto-generated method stub
		
	}

	public void close() throws RPCException {
		// TODO Auto-generated method stub
		
	}

	public void closeGracefully(int awaitTime) throws RPCException {
		// TODO Auto-generated method stub
		
	}

	public boolean isClose() {
		// TODO Auto-generated method stub
		return false;
	}

	public ChannelHandler getChannelHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasAttribute(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAttribute(String key, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void removeAttribute(String key) {
		// TODO Auto-generated method stub
		
	}

}
