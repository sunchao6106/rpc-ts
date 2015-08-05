package com.sunchao.rpc.base.transport.peer.support.impl;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.async.Future;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.Client;
import com.sunchao.rpc.base.transport.additional.PingTask;
import com.sunchao.rpc.base.transport.peer.PeerChannel;
import com.sunchao.rpc.base.transport.peer.PeerClient;
import com.sunchao.rpc.base.transport.peer.PeerHandler;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.utils.NamedThreadFactory;


public class DefaultPeerClient implements PeerClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPeerClient.class);

	public ClientConfig getConfig() {
		return channel.getConfig();
	}

	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	public void send(Object message) throws RPCException {
		channel.send(message);
	}

	public void close() throws RPCException {
		doClose();
		channel.close();
	}

	public void closeGracefully(int awaitTime) throws RPCException {
		doClose();
		channel.closeGracefully(awaitTime);
	}

	public boolean isClose() {
		return channel.isClose();
	}

	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public Future request(Object request) throws RPCException {
		return channel.request(request);
	}

	public Future request(Object request, int timeout) throws RPCException {
		return channel.request(request, timeout);
	}

	public PeerHandler getPeerHandler() {
		return channel.getPeerHandler();
	}
	
	private static final ScheduledThreadPoolExecutor SCHEDULE = 
			new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("rpc-remoteing-client-heartbeat", true));
	
	private ScheduledFuture<?> heartbeatFuture;
	
	private int heartbeat;
	
	private int heartbeatTimeout;
	
	private final Client client;
	
	private final PeerChannel channel;
	
	public DefaultPeerClient(Client client) {
		if (client == null)
			throw new IllegalArgumentException("the client argument cannot be null.");
		this.client = client;
		this.channel = new DefaultPeerChannel(client);
		this.heartbeat = Integer.parseInt(client.getConfig().getParameterOrDefault(Config.HEARTBEAT_KEY, 
				Config.DEFAULT_HEARTBEAT));
		this.heartbeatTimeout = Integer.parseInt(client.getConfig().getParameterOrDefault(Config.HEARTBEAT_TIMEOUT_KEY, 
				Config.DEFAULT_HEARTBEAT_TIMEOUT));
		if (heartbeatTimeout < heartbeat * 2)
			throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
		startHeartbeatTimer();
	}


	public ChannelHandler getChannelHandler() {
		return channel.getChannelHandler();
	}

	public boolean hasAttribute(String key) {
		return channel.hasAttribute(key);
	}

	public Object getAttribute(String key) {
		return channel.getAttribute(key);
	}

	public void setAttribute(String key, Object value) {
		channel.setAttribute(key, value);
	}

	public void removeAttribute(String key) {
		channel.removeAttribute(key);
	}
	
	/**
	 * start the heart beat thread.
	 */
	private void startHeartbeatTimer() {
		stopHeartbeatTimer();
		if (heartbeat > 0) {
			heartbeatFuture = SCHEDULE.scheduleWithFixedDelay(
					new PingTask(new PingTask.ChannelProvider() {
				        public List<Channel> getChannels() {
					        return Collections.<Channel>singletonList(DefaultPeerClient.this);
				        }
			        }, heartbeat, heartbeatTimeout), heartbeat, heartbeatTimeout, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * stop the heart beat time.
	 */
	private void stopHeartbeatTimer() {
		if (heartbeatFuture != null && !heartbeatFuture.isCancelled()) {
			try {
				heartbeatFuture.cancel(true);
				SCHEDULE.purge();
			} catch (Throwable t) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(t.getMessage(), t);
				}
			}
		}
		heartbeatFuture = null;
	}

	public void reconnect() throws RPCException {
		client.reconnect();
	}

	private void doClose() {
		stopHeartbeatTimer();
	}
}
