package com.sunchao.rpc.base.transport;

public interface ChannelHandlerDelegate extends ChannelHandler {
	
	public ChannelHandler getHandler();

}
