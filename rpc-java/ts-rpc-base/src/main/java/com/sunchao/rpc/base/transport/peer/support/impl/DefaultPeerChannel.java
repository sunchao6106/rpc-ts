package com.sunchao.rpc.base.transport.peer.support.impl;

import java.net.InetSocketAddress;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.MessageType;
import com.sunchao.rpc.base.async.ClientManager;
import com.sunchao.rpc.base.async.Future;
import com.sunchao.rpc.base.async.support.DefaultFuture;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.metadata.PacketHeader;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.ChannelHandler;
import com.sunchao.rpc.base.transport.peer.PeerChannel;
import com.sunchao.rpc.base.transport.peer.PeerHandler;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

public class DefaultPeerChannel implements PeerChannel {
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultPeerChannel other = (DefaultPeerChannel) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		return true;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPeerChannel.class);

	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public ClientConfig getConfig() {
		return channel.getConfig();
	}

	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	public void send(Object message) throws RPCException {
		if (closed)
			throw new RPCException("Failed to send message " + message + ", cause: The channel " + this + " is closed!" + this.getLocalAddress());
		if (message instanceof Request
				|| message instanceof Response
				|| message instanceof String) {
			channel.send(message);
		} else {
			throw new RPCException("transport format undefined.");
		}
	}

	public void close() throws RPCException {
		try {
			channel.close();
		} catch (Throwable t) {
			LOGGER.warn(t.getMessage(), t);
		}
	}

	/**
	 * close gracefully.
	 */
	public void closeGracefully(int awaitTime) throws RPCException {
		if (closed) {
			return;
		}
		closed = true;
		if (awaitTime > 0) {
			long start = System.currentTimeMillis();
			while (ClientManager.hasFuture(DefaultPeerChannel.this)
					&& System.currentTimeMillis() - start < awaitTime) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					LOGGER.warn(e.getMessage(), e);
				}
			}
		}
		close();
	}

	public boolean isClose() {
		return closed;
	}

	public Future request(Object request) throws RPCException {
		return request(request, Config.RESPONSE_TIMEOUT);
	}

	public Future request(Object request, int timeout) throws RPCException {
		if (closed)
			throw new RPCException("Failed to send message " + request + ", cause: The channel " + this + " is closed!" + this.getLocalAddress());
		//request
		Request req = new Request();
		req.setOneway(false);
		//req.setMagic(PacketHeader.MAGIC_NUMBER);
		//req.setVersion(PacketHeader.VERSION);
		//req.setMessageType(MessageType.REQUEST);
		//req.setServiceType(PacketHeader.MESSAGE);
		req.setData(request);
		Future future = new DefaultFuture(req, channel, timeout);
		try {
			channel.send(req);
		} catch (RPCException e) {
			future.cancel();
			throw e;
		}
		return future;
	}

	public PeerHandler getPeerHandler() {
		return (PeerHandler) channel.getChannelHandler();
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
	
	DefaultPeerChannel(Channel channel) {
		if (channel == null)
			throw new IllegalArgumentException("the channel argument cannot be null");
		this.channel = channel;
	}
	
    private static final String CHANNEL_KEY = DefaultPeerChannel.class.getName() + ".CHANNEL";
	
	private final Channel channel;
	
	private volatile boolean closed = false;
	
	/**
	 * When the underlying channel is not connected.
	 * remove the attribute of the channel, equals to
	 * remove the <code>DefaultPeerChannel</code>
	 * 
	 * @param channel
	 */
	static void removeChannelIfDisconnected(Channel channel) {
		if (channel != null && ! channel.isConnected()) 
			channel.removeAttribute(CHANNEL_KEY);
	}

	/**
	 * Wrap the underlying channel to the <code>DefaultPeerChannel</code>
	 * 
	 * @param channel
	 * @return
	 */
	static DefaultPeerChannel getOrAddChannel(Channel channel) {
		if (channel == null)
			return null;
		DefaultPeerChannel defaultChannel = (DefaultPeerChannel) channel.getAttribute(CHANNEL_KEY);
		if (defaultChannel == null) {
			defaultChannel = new DefaultPeerChannel(channel);
			if (channel.isConnected()) {
				channel.setAttribute(CHANNEL_KEY, defaultChannel);
			}
		}
		return defaultChannel;
	}
}
