package com.sunchao.rpc.base.transport.additional;

import java.util.Collection;

import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.Client;
import com.sunchao.rpc.base.transport.additional.PingTask.ChannelProvider;
import com.sunchao.rpc.base.transport.peer.support.impl.DefaultPeerHandler;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

public final class PingTask implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PingTask.class);

	public void run() {
		try {
			long now = System.currentTimeMillis();
			for (Channel channel : channelProvider.getChannels()) {
				if (channel.isClose())
					continue;
				try {
					long lastRead = (Long) channel.getAttribute(
							DefaultPeerHandler.READ_TIMESTAMP);
					long lastWrite = (Long) channel.getAttribute(
							DefaultPeerHandler.WRITE_TIMESTAMP);
					if ((lastRead != 0 && now - lastRead > heartbeat) //try to send heartbeat.
							|| (lastWrite != 0 && now - lastWrite > heartbeat)) {
						Request request = new Request();
						request.setOneway(false);
						request.setHeartBeatFlag(true);
						channel.send(request);
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Send heartbeat to remote channel " + channel.getRemoteAddress()
									+ ", cause: The channel has no data-transmission exceeds s heartbeat period: " + heartbeat + " ms" );
						}
					}
					if (lastRead != 0 && now - lastRead > heartbeatTimeout) { //exceed the heart beat time out and close the channel.
						LOGGER.warn("Close channel " + channel 
								+ ", because heartbeat read idle time out: " + heartbeatTimeout + "ms");
						if (channel instanceof Client) { //client.
							try {
								((Client)channel).reconnect();
							} catch (Exception e) {
								//do nothing.
							}
						} else {
							channel.close();
						}
					}
				} catch (Throwable t) {
					LOGGER.warn("Exception when heart beat to remote channel " + channel.getRemoteAddress(), t);
				}
			}
		} catch (Throwable t) {
			LOGGER.warn("unhandled exception when heart beat, cause: " + t.getMessage(), t);
		}
		
	}
	
	public PingTask(ChannelProvider provider, int heartbeat, int heartbeatTimeout) {
		this.channelProvider = provider;
		this.heartbeat = heartbeat;
		this.heartbeatTimeout = heartbeatTimeout;
	}
	
	private ChannelProvider channelProvider;
	
	private int heartbeat; //the heart beat interval.
	
	private int heartbeatTimeout; // the timeout control.
	
	public static interface ChannelProvider {
		Collection<Channel> getChannels();
	}

	
}

