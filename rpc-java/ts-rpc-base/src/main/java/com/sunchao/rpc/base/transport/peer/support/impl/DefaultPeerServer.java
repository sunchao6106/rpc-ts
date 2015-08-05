package com.sunchao.rpc.base.transport.peer.support.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.async.ClientManager;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.Server;
import com.sunchao.rpc.base.transport.additional.PingTask;
import com.sunchao.rpc.base.transport.peer.PeerChannel;
import com.sunchao.rpc.base.transport.peer.PeerServer;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.utils.NamedThreadFactory;

public class DefaultPeerServer implements PeerServer {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultPeerServer.class);
	
	public boolean isBinded() {
		return server.isBinded();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Channel> getChannels() {
		return (Collection)getPeerChannels();
	}

	public Channel getChannel(InetSocketAddress remoteAddress) {
		return getPeerChannel(remoteAddress);
	}

	public ClientConfig getConfig() {
		return server.getConfig();
	}

	public InetSocketAddress getLocalAddress() {
		return server.getLocalAddress();
	}

	public void send(Object message) throws RPCException {
		if (closed) 
			throw new RPCException("Failed to send message " + message + ", cause the channel has closed: " + this.getLocalAddress() );
		server.send(message);
	}

	public void close() throws RPCException {
		doClose();
		server.close();
	}

	public void closeGracefully(int awaitTime) throws RPCException {
		if (awaitTime > 0) {
			final long max = (long) awaitTime;
			final long start = System.currentTimeMillis();
			while (DefaultPeerServer.this.isRunning()
					&& System.currentTimeMillis() - start < max) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					LOGGER.warn(e.getMessage(), e);
				}
			}
		}
			
		doClose();
		server.closeGracefully(awaitTime);
	}

	public boolean isClose() {
		return server.isClose();
	}

	public ChannelHandler getChannelHandler() {
		return server.getChannelHandler();
	}

	public Collection<PeerChannel> getPeerChannels() {
		Collection<PeerChannel> peerChannels = new ArrayList<PeerChannel>();
		Collection<Channel> channels = server.getChannels();
		if (channels != null && channels.size() > 0) {
			for (Channel channel : channels)
				peerChannels.add(DefaultPeerChannel.getOrAddChannel(channel));
		}
		return peerChannels;
	}

	public PeerChannel getPeerChannel(InetSocketAddress remoteAddress) {
		Channel channel = server.getChannel(remoteAddress);
		return DefaultPeerChannel.getOrAddChannel(channel);
	}

	private final ScheduledThreadPoolExecutor SCHEDULE = 
			new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("rpc-remoteing-server-heartbeat", true));
	
	private ScheduledFuture<?> heartbeatFuture;
	
	private int  heartbeat;
	
	private int  heartbeatTimeout;
	
	private final Server server;
	
	private volatile boolean closed = false;
	
	public DefaultPeerServer(Server server) {
		if (server == null) 
			throw new IllegalArgumentException("the server cannot be null.");
		this.server = server;
		this.heartbeat = Integer.parseInt(server.getConfig().
				getParameterOrDefault(Config.HEARTBEAT_KEY, Config.DEFAULT_HEARTBEAT));
		this.heartbeatTimeout = Integer.parseInt(server.getConfig().getParameterOrDefault(
				Config.HEARTBEAT_TIMEOUT_KEY, " 3 * " + Config.DEFAULT_HEARTBEAT_TIMEOUT));
		if (heartbeatTimeout < heartbeat * 2) 
			throw new IllegalStateException("heart beat time out < heart beat interval * 2");
		startHeartbeatTimer();
	}

	/**
	 * start the server-side heart beat thread.
	 */
	private void startHeartbeatTimer() {
		stopHeartbeatTimer();
		if (heartbeat > 0) {
			heartbeatFuture = SCHEDULE.scheduleWithFixedDelay(
					new PingTask(new PingTask.ChannelProvider() {
						public Collection<Channel> getChannels() { //non-modified.
							return Collections.unmodifiableCollection(DefaultPeerServer.this.getChannels());
						}
					}, heartbeat, heartbeatTimeout), heartbeat, heartbeatTimeout, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * stop the server-side heart beat thread.
	 */
	private void stopHeartbeatTimer() {
		try {
			ScheduledFuture<?> timer = heartbeatFuture;
			if (timer != null && ! timer.isCancelled()) {
				timer.cancel(true);
			}
		} catch (Throwable t) {
			LOGGER.warn(t.getMessage(), t);
		} finally {
			heartbeatFuture = null;

		}
	}
	
	private void doClose() {
		if (closed)
			return;
		closed = true;
		stopHeartbeatTimer();
		try {
			SCHEDULE.shutdown();
		} catch (Throwable t) {
			LOGGER.warn(t.getMessage(), t);
		}
	}
	
	private boolean isRunning() {
		Collection<Channel> channels = getChannels();
		for (Channel channel : channels) {
			if (ClientManager.hasFuture(channel)) {
				return true;
			}
		}
		return false;
	}

	public void reset(ClientConfig config) {
		server.reset(config);
		try {
			startHeartbeatTimer();
		} catch (Throwable t) {
			LOGGER.error(t.getMessage(), t);
		}
	}
}
