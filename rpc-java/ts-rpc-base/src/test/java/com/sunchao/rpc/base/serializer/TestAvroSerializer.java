package com.sunchao.rpc.base.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Before;
import org.junit.Test;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.common.ClientConfig;

public class TestAvroSerializer {
	
	private static final String path = "com/sunchao/rpc/base/serializer/helloworld.avpr";
	private static final String seriveName = "helloworld";
	
	@Before
	public void testPrepare() throws IOException, RPCException {
        AvroSerializerHelper.registerProtocol(seriveName, path);
	}
	
	@Test
	public void testRequest() throws Exception {
		Protocol protocol = AvroSerializerHelper.getProtocol(seriveName);
		Message message = protocol.getMessages().get("hello");
		System.out.println(message.getRequest());
		GenericRecord request = new GenericData.Record(message.getRequest());
		GenericRecord argument = new GenericData.Record(protocol.getType("Greeting"));
		argument.put("message", "hello every body!");
		argument.put(1, 65);
		//request.put("message", "hello every body!");
	//	request.put("userId", 65);
		request.put(0, argument);
		TSerializer serializer = SerializationFactory.getSerializer(SerializationFactory.SERIALIZER_AVRO);
		Context context  = new Context();
		context.setSchema(message.getRequest());
		context.setRequest(true);
		ByteBuffer buffer = serializer.serialize(request, context,new ClientConfig(null, 0,null , null));
		
		Object obj = serializer.deserialize(buffer, context, new ClientConfig(null, 0,null , null));
		System.out.println(obj);
		
		
		GenericRecord request1 = new GenericData.Record(message.getRequest());
		GenericRecord argument1 = new GenericData.Record(protocol.getType("Greeting"));
		argument1.put("message", "hello  body1!");
		argument1.put(1, 20);
		//request.put("message", "hello every body!");
	//	request.put("userId", 65);
		request1.put(0, argument1);
		//TSerializer serializer1 = SerializationFactory.getSerializer(SerializationFactory.SERIALIZER_AVRO);
		Context context1  = new Context();
		context1.setSchema(message.getRequest());
		context1.setRequest(true);
		ByteBuffer buffer1 = serializer.serialize(request1, context1, new ClientConfig(null, 0,null , null));
		
		Object obj1 = serializer.deserialize(buffer1, context1, new ClientConfig(null, 0,null , null));
		System.out.println(obj1);
	}
	
	
}
