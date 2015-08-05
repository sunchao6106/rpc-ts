package com.sunchao.rpc.base.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.sunchao.rpc.base.exception.RPCTransportException;

public interface Transceiver {

	public abstract String getRemoteAddress();
	
	public abstract String getLocalAddress();

	public abstract int writeBuffer(ByteBuffer buffer)
			throws RPCTransportException;

	public abstract int readBuffer(ByteBuffer buffer)
			throws RPCTransportException;

	/**
	 * Set the socket timeout, because the Non-blocking channel.
	 * so the operation is unused!
	 * 
	 * @param timeout.
	 */
	public abstract void setTimeOut(int timeout);

	/**
	 * close the channel.
	 */
	public abstract void close();

	/**
	 * register the socket channel with the selector, indicating
	 * will be noticed when it's ready for I/O;
	 * @param selector
	 *               the selector which register for.
	 * @param interests 
	 *               the selection key for this socket.
	 * @return
	 * @throws IOException
	 */
	public abstract SelectionKey registerSelector(Selector selector,
			int interests) throws IOException;

	public abstract void write(byte[] buf, int offset, int len)
			throws RPCTransportException;

	/**
	 * Judges the underlying channel whether or not open already.
	 * 
	 * @return
	 */
	public abstract boolean isOpen();

	public abstract int read(byte[] buf, int offset, int len)
			throws RPCTransportException;

}