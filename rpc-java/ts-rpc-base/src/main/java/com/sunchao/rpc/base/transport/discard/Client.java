package com.sunchao.rpc.base.transport.discard;

import java.io.IOException;
import java.nio.channels.Channel;

/**
 * The client transport need to finish the abstract method to
 * implement the <code>reconnect</code> function.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Deprecated
public abstract class Client {
	
	/**
	 * Reconnects on the failure.
	 * 
	 * @throws IOException
	 */
	@Deprecated
	public abstract void reconnect() throws IOException;
	
	/**
	 * Open the SocketChannel.
	 * @see java.nio.channels.SocketChannel#open();
	 * @throws IOException
	 */
	protected abstract void doOpen() throws IOException;
	
	/**
	 * connection initialization.
	 * @see java.nio.channels.SocketChannel#connect(java.net.SocketAddress);
	 * 
	 * @throws IOException
	 */
	public abstract boolean startConnect() throws IOException;
	
	
	/**
	 * Non-blocking connection completion.
	 * @see java.nio.channels.SocketChannel#finishConnect();
	 * 
	 * @throws IOException
	 */
	public abstract boolean finishConnect() throws IOException;
	
	/**
	 * disconnect the underlying channel.
	 * 
	 * @throws IOException
	 */
	@Deprecated
	protected abstract void disConnect() throws IOException;
	
	/**
	 * Get the underlying channel.
	 * 
	 * @return
	 */
	public abstract Channel getChannel();
	
	//protected abstract void doClose() throws IOException;

}
