package com.sunchao.rpc.base.transport.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.codehaus.jackson.map.ser.StdSerializers.IntegerSerializer;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.metadata.Packet;
import com.sunchao.rpc.base.metadata.RPCMetaData;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.TSerializer;

/**
 * Interface that implement the uniform call for serialize.
 * 
 * {@code ZooKeeper#OutputArhcive}
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface OutputAchive {
	
	void writeByte(byte b, String tag) throws IOException;
	
	void writeBool(boolean b, String tag) throws IOException;
	
	void writeInt(int i, String tag) throws IOException;
	
	void writeLong(long l, String tag) throws IOException;
	
	void writeFloat(float f, String tag) throws IOException;
	
	void writeDouble(double d, String tag) throws IOException;
	
	void writeShort(short s, String tag) throws IOException;
	
	void writeByteBuffer(ByteBuffer buffer, String tag) throws IOException;
	
	void writeBuffer(byte[] buf, String tag) throws IOException;
	
	void writeString(String s, String tag) throws IOException;
	
	//void writePacket(Packet p, String tag) throws IOException;
	@Deprecated
	void writeRPCMeta(RPCMetaData metaData, String tag) throws IOException;
	
	void writeRequest(TSerializer serialier,Request request, Context context, String tag) throws IOException, Exception;
	
	void writeResponse(TSerializer serializer,Response response, Context context, String tag) throws IOException, RPCException, Exception;

}
