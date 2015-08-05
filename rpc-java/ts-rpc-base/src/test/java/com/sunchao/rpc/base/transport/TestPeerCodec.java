package com.sunchao.rpc.base.transport;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.SerializationFactory;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.common.extension.HotSwapLoader;

import static org.junit.Assert.*;

public class TestPeerCodec {
	
   // public static final short RPC_VERSION =  1;
	
	//public static final byte[] RPC_MAGIC = new byte[]{0x72, 0x70, 0x60};
	
	Serializer serializer = null;
	
	@Test
	private void getRequestBytes() throws Exception {
		/*Request request = new Request();
		request.setPacketLen(12);
		request.setErrorRequest(true);
		ByteBuffer buffer = serializer.serialize(request, new Context());
		return buffer.array();*/
		serializer =  HotSwapLoader.getExtensionLoader(Serializer.class).getAdaptiveExtension();
		assertNotNull(serializer);
	}
}
