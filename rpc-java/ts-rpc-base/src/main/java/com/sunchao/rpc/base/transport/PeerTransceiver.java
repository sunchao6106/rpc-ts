package com.sunchao.rpc.base.transport;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.exception.RPCTransportException;
import com.sunchao.rpc.base.metadata.PacketHeader;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.utils.NetUtil;

/**
 * 
 * The Server transport base class.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class PeerTransceiver implements Transceiver  {
	
	static final Logger LOGGER = LoggerFactory.getLogger(PeerTransceiver.class);
	
	protected  SocketChannel channel;
	protected  SocketAddress address;
	//private ByteBuffer header = ByteBuffer.allocate(PacketHeader.PACK_HEADER_SIZE);
	
    /**
     * The Constructor used by the server.
     * @see java.nio.channels.ServerSocketChannel#accept().
     * @see com.sunchao.rpc.base.transport.AbstractServer#accept().
     * 
     * @param channel
     * @throws IOException
     */
    public PeerTransceiver(final SocketChannel channel) throws IOException {
    	//address = channel.getRemoteAddress()
    	this(channel, 0, null);
    	//this.address = channel.getRemoteAddress();
    }
	
    /**
     * The Constructor used by the server.
     * @see java.nio.channels.ServerSocketChannel#accept().
     * @see com.sunchao.rpc.base.transport.AbstractServer#accept().
     * 
     * @param channel
     * @throws IOException
     */
    public PeerTransceiver(final SocketChannel channel, final int timeout, 
    		final SocketAddress remoteAddress) throws IOException {
    	if (!channel.isConnected()) {
    		throw new RPCTransportException(
    				RPCTransportException.NOT_OPEN,
    				"Socket must already be connected!");
    	}
    	this.channel = channel;
    	this.address = channel.getRemoteAddress();
    	// Non-blocking.
    	this.channel.configureBlocking(false);
    	//options.
    	Socket socket = this.channel.socket();
    	socket.setSoLinger(false, 0);
    	socket.setTcpNoDelay(true);
    	socket.setKeepAlive(true);
    	setTimeOut(timeout);
    	if (LOGGER.isInfoEnabled()) {
    		LOGGER.info("Start " + getClass().getSimpleName() + " " + NetUtil.getLocalAddress()
    				+ " connect to " + address.toString());
    	}
    }

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#getRemoteAddress()
	 */
	
	public String getRemoteAddress() {
		try {
		   if (this.channel != null) {
			    return this.channel.getRemoteAddress().toString();
		   }
	    } catch (Throwable t) {
	    	LOGGER.warn("unexpected error when get the remote address: " + t.getMessage(), t);
	    }
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#writeBuffer(java.nio.ByteBuffer)
	 */
	
	public int writeBuffer(ByteBuffer buffer)
			throws RPCTransportException {
		if (this.channel == null || !isOpen()) { 
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"The underlying channel is not open or close alreay. please invoke the open operation!");
		}
		
		if ((this.channel.validOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"Cannot write from read-only socket channel!");
		}
	    try {
			return this.channel.write(buffer);
		} catch (IOException e) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN, e);
		}
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#readBuffer(java.nio.ByteBuffer)
	 */
	
	public int readBuffer(ByteBuffer buffer) 
			throws RPCTransportException {
		if (this.channel == null || !isOpen()) { 
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"The underlying channel is not open or close alreay. please invoke the open operation!");
		}
		
		if ((this.channel.validOps() & SelectionKey.OP_READ) != SelectionKey.OP_READ) {
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"Cannot read from write-only socket channel!");
		}
		try {
			return this.channel.read(buffer);
		} catch (IOException e) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN, e);
		}
	}
 
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#setTimeOut(int)
	 */
	public void setTimeOut(int timeout) {
		try {
			this.channel.socket().setSoTimeout(timeout);
		} catch (SocketException e) {
			LOGGER.warn("unexpected error happen.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#close()
	 */
	public void close() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Close " + getClass().getSimpleName() + " " + NetUtil.getLocalAddress() + " connection --> " +
		  address.toString());
		}
		try {
			this.channel.close();
		} catch (IOException e) {
			LOGGER.warn("could not close the socket!", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#registerSelector(java.nio.channels.Selector, int)
	 */
	public SelectionKey registerSelector(Selector selector, int interests) 
			throws IOException {
		return this.channel.register(selector, interests);
	}
	

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#write(byte[], int, int)
	 */
	
	public void write(byte[] buf, int offset, int len)
			throws RPCTransportException {
		if (this.channel == null || !isOpen()) { 
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"The underlying channel is not open or close alreay. please invoke the open operation!");
		}
		if ((this.channel.validOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"Cannot write from read-only socket channel!");
		}
	    try {
			this.channel.write(ByteBuffer.wrap(buf, offset, len));
		} catch (IOException e) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN, e);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#isOpen()
	 */
	public boolean isOpen() {
		//the API {@link SocketChannel #isConnected()} does not return false after the <code>close()<code>
		return this.channel.isOpen() && this.channel.isConnected();
	}

	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.transport.Transceiver1#read(byte[], int, int)
	 */
	
	public int read(byte[] buf, int offset, int len)
			throws RPCTransportException {
		if (this.channel == null || !isOpen()) {
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"The underlying channel is not open or close already. please invoke the open operation!");
		}
		if ((this.channel.validOps() & SelectionKey.OP_READ) != SelectionKey.OP_READ) {
			throw new RPCTransportException(RPCTransportException.NOT_OPEN, 
					"Cannot read from write-only socket channel!");
		}
		try {
			return this.channel.read(ByteBuffer.wrap(buf, offset, len));
		} catch (IOException e) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN, e);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getLocalAddress() + "-->" + address.toString() + "]";
	}

	public String getLocalAddress() {
		try {
			if (this.channel != null) {
			   return this.channel.getLocalAddress().toString();
			}
		} catch (Throwable t) {
			LOGGER.warn("error when get the local address: " + t.getMessage(), t);
		}
		return null;
	}
}
