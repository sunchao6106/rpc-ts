package com.sunchao.rpc.base.transport;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface Server extends Endpoint, Resetable {
	/**
	 * the server whether bind on the address.
	 * 
	 * @return
	 */
	boolean isBinded();
	
	/**
	 * get the client connection channels.
	 * 
	 * @return
	 */
	Collection<Channel> getChannels(); 
	
	/**
	 * get the specified remote channel.
	 * 
	 * @param remoteAddress
	 * @return
	 */
	Channel getChannel(InetSocketAddress remoteAddress);

}
