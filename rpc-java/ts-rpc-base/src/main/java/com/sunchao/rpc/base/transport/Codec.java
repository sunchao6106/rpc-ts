package com.sunchao.rpc.base.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.common.extension.Component;
import com.sunchao.rpc.common.extension.HotSwap;

@Component("peercodec")
public interface Codec {

	@HotSwap("encode")
	void encode(Channel channel, ByteBuffer buffer, Object msg, Context context) throws Exception;
	
	@HotSwap("decodeBody")
	 Object decodePacketBody(Channel channel, ByteBuffer buffer, Context context) throws Exception;
	
	@HotSwap("decodeheader")
    void decodePacketHeader(Channel channel, ByteBuffer buffer) throws Exception;
}
