package com.sunchao.rpc.base.transport.dispatcher.execution;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler;
import com.sunchao.rpc.common.ClientConfig;

public class ExecutionChannelHandler extends WrapperChannelHandler {

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onConnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onConnected(Channel channel) throws RPCException {
		// TODO Auto-generated method stub
		super.onConnected(channel);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onDisconnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onDisconnected(Channel channel) throws RPCException {
		// TODO Auto-generated method stub
		super.onDisconnected(channel);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onSent(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onSent(Channel channel, Object message) throws RPCException {
		// TODO Auto-generated method stub
		super.onSent(channel, message);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onReceived(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onReceived(Channel channel, Object message) throws RPCException {
		// TODO Auto-generated method stub
		super.onReceived(channel, message);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#onError(com.sunchao.rpc.base.transport.Channel, java.lang.Throwable)
	 */
	@Override
	public void onError(Channel channel, Throwable cause) throws RPCException {
		// TODO Auto-generated method stub
		super.onError(channel, cause);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#getHandler()
	 */
	@Override
	public ChannelHandler getHandler() {
		// TODO Auto-generated method stub
		return super.getHandler();
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.dispatcher.WrapperChannelHandler#getConfig()
	 */
	@Override
	public ClientConfig getConfig() {
		// TODO Auto-generated method stub
		return super.getConfig();
	}

	public ExecutionChannelHandler(ChannelHandler handler, ClientConfig config) {
		super(handler, config);
	}
	
	

}
