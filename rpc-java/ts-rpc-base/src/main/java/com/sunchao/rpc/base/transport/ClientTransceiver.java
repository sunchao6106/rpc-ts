package com.sunchao.rpc.base.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.exception.RPCTransportException;
import com.sunchao.rpc.base.transport.discard.Client;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Creates a connection to server. The actual connect dosen't get
 * established until needed.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ClientTransceiver extends Client implements Transceiver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTransceiver.class);
	
	private final InetSocketAddress remoteAddress;
	//private int connectTimeout;
	private SocketChannel channel;
	
	/**
	 * Read lock must be acquired whenever using non-final state.
	 * Write lock must be acquired whenever modifying state.
	 */
	private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
	
	public ClientTransceiver(final String ip, final int port) 
			throws IOException {
		this(ip, port, 0);
	}
	
	/**
	 * Open the <code>SocketChannel</code>, and configure the the underlying
	 * socket's attribute.
	 * 
	 * @param ip  the remote server ip address.
	 * @param port the remote server port.
	 * @param timeout the connect time out.
	 * @throws IOException
	 */
	public ClientTransceiver(final String ip, final int port, final int timeout)
			throws IOException {
		if (ip == null || ip.length() == 0)
			throw new IllegalArgumentException("Invalid ip argument, ip cannot be null or \\\"\\\".");
		if (port < 0)
			throw new IllegalArgumentException("Invalid port argument, port value must be >= 0.");
		this.remoteAddress = new InetSocketAddress(ip, port);
		doOpen();
	}
	
	/**
	 * Open the <code>SocketChannel</code>, and configure the underlying 
	 * socket attribute.
	 * 
	 * @param remoteAddress the remote server address.
	 * @throws IOException
	 */
    public ClientTransceiver(final InetSocketAddress remoteAddress) throws IOException {
	    this.remoteAddress = remoteAddress;
	    doOpen();
	}

	/**
	 * Configure the underlying socket attribute.
	 * @throws IOException 
	 */
	protected void doOpen() throws IOException {
		channel = SocketChannel.open();
		//make it a non-blocking channel.
		channel.configureBlocking(false);
		//set options
		Socket socket = channel.socket();
		socket.setKeepAlive(true);
		socket.setSoLinger(false, 0);
		socket.setTcpNoDelay(true);
		//setTimeOut(timeout);
	}

	/**
	 * Get the remote server address.
	 */
	public String getRemoteAddress(){
		if (this.channel == null)
			return null;
		return this.channel.socket().getRemoteSocketAddress().toString();
	}

	public int writeBuffer(ByteBuffer buffer) throws RPCTransportException {
		if (this.channel == null) 
			throw new IllegalStateException("The underlying socket channel cannot be null.");
		if ((this.channel.validOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN, 
					"Cannot write to read-only socket channel.");
		}
		try {
			return this.channel.write(buffer);
		} catch (IOException e) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN, e);
		}
	}

	public int readBuffer(ByteBuffer buffer) throws RPCTransportException {
		if (this.channel == null) 
			throw new IllegalStateException("The underlying socket channel cannot be null.");
		if ((this.channel.validOps() & SelectionKey.OP_READ) != SelectionKey.OP_READ) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN,
					"Cannot read from write-only socket channel.");
		}
		try {
			return this.channel.read(buffer);
		} catch (IOException e) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN, e);
		}
	}

	/**
	 * close the socket channel.
	 */
	public void close() {
		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Doing the socketChannel close.");
			} 
			this.channel.close();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Closed the " + getClass().getSimpleName() + " " + getLocalAddress() +
						" -> " + getRemoteAddress() + " connect.");
			}
		} catch (IOException e) {
			LOGGER.warn("Error when close the socket channel, and ignore exception.", e);
		}
	}

	public SelectionKey registerSelector(Selector selector, int interests)
			throws IOException {
		if (selector == null)
			throw new IllegalStateException("The Selector not open, please initial it firstly.");
		return this.channel.register(selector, interests);
	}

	public void write(byte[] buf, int offset, int len)
			throws RPCTransportException {
		writeBuffer(ByteBuffer.wrap(buf, offset, len));
	}

	/**
	 * Judge the remote server whether or not connected.
	 */
	public boolean isOpen() {
		if (this.channel == null)
			return false;
		return this.channel.isConnected() && this.channel.isOpen();
	}

	public int read(byte[] buf, int offset, int len)
			throws RPCTransportException {
		return readBuffer(ByteBuffer.wrap(buf, offset, len));
	}
	
	@Override
	public String toString() {
		return  new StringBuilder(getClass().getSimpleName()).append(" [")
				.append(getLocalAddress()).append(" -->").append(" " + getRemoteAddress())
				.append("]").toString();
	}

	/**
	 * Get the local address.
	 */
	public String getLocalAddress() {
	    try {
	    	if (this.channel != null) {
			   return this.channel.getLocalAddress().toString();
	    	}
		} catch (IOException e) {
			LOGGER.warn("error when get the local address.", e);
	    }
		return null;
	}
		

	/**
	 * Get the underlying socket channel.
	 */
	@Override
	public Channel getChannel() {
		if (this.channel != null)
			return this.channel;
		return null;
	}

	/**
	 * The Non-blocking socket set timeOut has no meaning.
	 */
	public void setTimeOut(int timeout) {}

	@Override
	public boolean startConnect() throws IOException {
		if (this.channel == null) 
			throw new IllegalStateException("The underlying socket channel cannot be null.");
		return this.channel.connect(remoteAddress);
	}

	@Override
	public boolean finishConnect() throws IOException {
		if (this.channel == null)
			throw new IllegalStateException("The underlying socket channel cannot be null.");
		return this.channel.finishConnect();
	}

	@Override
	public void reconnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
