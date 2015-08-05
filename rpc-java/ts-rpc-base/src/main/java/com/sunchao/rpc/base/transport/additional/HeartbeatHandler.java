package com.sunchao.rpc.base.transport.additional;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.support.AbstractChannelHandlerDelegate;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

public class HeartbeatHandler extends AbstractChannelHandlerDelegate {
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.support.AbstractChannelHandlerDelegate#onConnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onConnected(Channel channel) throws RPCException {
		setReadTimestamp(channel);
		setWriteTimestamp(channel);
		super.onConnected(channel);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.support.AbstractChannelHandlerDelegate#onDisconnected(com.sunchao.rpc.base.transport.Channel)
	 */
	@Override
	public void onDisconnected(Channel channel) throws RPCException {
		clearReadTimestamp(channel);
		clearWriteTimestamp(channel);
		super.onDisconnected(channel);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.support.AbstractChannelHandlerDelegate#onSent(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onSent(Channel channel, Object message) throws RPCException {
		setWriteTimestamp(channel);
		super.onSent(channel, message);
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.support.AbstractChannelHandlerDelegate#onReceived(com.sunchao.rpc.base.transport.Channel, java.lang.Object)
	 */
	@Override
	public void onReceived(Channel channel, Object message) throws RPCException {
		setReadTimestamp(channel);
		if (isHeartbeatRequest(message)) { //heart beat request. the server-side.
			Request req = (Request) message;
			Response response = new Response(req.getId());
			response.setHeartBeatFlag(true);
			response.setStatus_code(Response.OK);
			channel.send(message);
			if (LOGGER.isInfoEnabled()) {
				int heartbeat = Integer.parseInt(channel.getConfig().getParameterOrDefault(Config.HEARTBEAT_KEY, 
						Config.DEFAULT_HEARTBEAT));
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("receive heartbeat from remote channel " + channel.getRemoteAddress()
							+ ", cause: The channel has no data-transmission exceeds a heartbeat period"
							+ (heartbeat > 0 ? ": " + heartbeat + "ms" : ""));
				}
			}
			return;
		}
		if (isHeartbeatResponse(message)) { //heart beat response . the client side.
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(
						new StringBuilder(32)
						.append("Receive heartbeat response in thread ")
						.append(Thread.currentThread().getName())
						.toString());
			}
			return;
		}
		super.onReceived(channel, message); //common non-heart beat request/response .
	}


	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.support.AbstractChannelHandlerDelegate#getHandler()
	 */
	@Override
	public ChannelHandler getHandler() {
		return super.getHandler();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatHandler.class);
	
	public static String READ_TIMESTAMP = "READ_TIMESTAMP";
	
	public static String WRITE_TIMESTAMP = "WRITE_TIMESTAMP";
	
	/**
	 * set the read time stamp.
	 * 
	 * @param channel
	 */
	private void setReadTimestamp(Channel channel) {
		channel.setAttribute(READ_TIMESTAMP, System.currentTimeMillis());
	}
	
	/**
	 * set the write time stamp.
	 * 
	 * @param channel
	 */
	private void setWriteTimestamp(Channel channel) {
		channel.setAttribute(WRITE_TIMESTAMP, System.currentTimeMillis());
	}
	
	/**
	 * clear the read time stamp.
	 * 
	 * @param channel
	 */
	private void clearReadTimestamp(Channel channel) {
		channel.removeAttribute(READ_TIMESTAMP);
	}
	
	/**
	 * clear the write time stamp.
	 * 
	 * @param channel
	 */
	private void clearWriteTimestamp(Channel channel) {
		channel.removeAttribute(WRITE_TIMESTAMP);
	}
	
	/**
	 * check the request whether or not heart beat;
	 * 
	 * @param message
	 * @return
	 */
	private boolean isHeartbeatRequest(Object message) {
		return message instanceof Request && ((Request) message).isHeartBeatFlag();
	}
	
	/**
	 * check the response whether or not heart beat.
	 * 
	 * @param message
	 * @return
	 */
	private boolean isHeartbeatResponse(Object message) {
		return message instanceof Response && ((Response)message).isHeartBeatFlag();
	}

	public HeartbeatHandler(ChannelHandler handler) {
		super(handler);
	}

}
