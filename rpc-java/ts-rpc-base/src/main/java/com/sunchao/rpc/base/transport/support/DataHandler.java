package com.sunchao.rpc.base.transport.support;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;

public class DataHandler extends AbstractChannelHandlerDelegate {

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.support.AbstractChannelHandlerDelegate#onReceived(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onReceived(Channel channel, Object message) throws RPCException {
		if (message instanceof Request) 
			decode(((Request) message).getData());
		else if (message instanceof Response) 
			decode(((Response) message).getResult());
		handler.onReceived(channel, message);
	}
	
	private void decode(Object message) {
		
	}

	public DataHandler(ChannelHandler handler) {
		super(handler);
	}

}
