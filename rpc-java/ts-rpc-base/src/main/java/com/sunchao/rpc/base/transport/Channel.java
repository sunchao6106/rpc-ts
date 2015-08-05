package com.sunchao.rpc.base.transport;

import java.net.InetSocketAddress;

/**
 * The channel represent the underlying socket transport.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface Channel extends Endpoint {

	/**
	 * Get the peer-side of other side of transport
	 * address.
	 * 
	 * @return
	 */
	InetSocketAddress getRemoteAddress();
	
	/**
	 * check the channel connected.
	 * 
	 * @return
	 */
	boolean isConnected();
	
	/**
	 * check whether or not has the attribute
	 * specified by the key.
	 * 
	 * @param key
	 * @return
	 */
	boolean hasAttribute(String key);
	
	/**
	 * Get the attribute specified by the key.
	 * 
	 * @param key
	 * @return
	 */
	Object getAttribute(String key);
	
	/**
	 * Set the attribute
	 * 
	 * @param key
	 * @param value
	 */
	void setAttribute(String key, Object value);
	
	/**
	 * remove the attribute by the specified key. 
	 * 
	 * @param key
	 */
	void removeAttribute(String key);
}
