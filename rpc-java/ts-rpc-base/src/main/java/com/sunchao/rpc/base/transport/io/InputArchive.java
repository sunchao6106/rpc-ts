package com.sunchao.rpc.base.transport.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sunchao.rpc.base.metadata.RPCMetaData;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;

/**
 * Interface that implement the uniform call for deserialize.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface InputArchive {
	
	 byte readByte(String tag) throws IOException;
	
	 boolean readBool(String tag) throws IOException;
	 
	 int readInt(String tag) throws IOException;
	 
	 long readLong(String tag) throws IOException;
	 
	 float readFloat(String tag) throws IOException;
	 
	 double readDouble(String tag) throws IOException;
	 
	 String readString(String tag) throws IOException;
	 
	 byte[] readBuffer(String tag) throws IOException;
	 
	 short readShort(String tag) throws IOException;
	 
	 ByteBuffer readByteBuffer(String tag) throws IOException;
	 
	 //String readString(String tag) throws IOException;
	
	 @Deprecated
	 void readRPCMetadata(RPCMetaData metadata, String tag) throws IOException;
	 
	 void readRequest(Serializer serializer, Request request, Context context, String tag) throws IOException, Exception;
	 
	 void readResponse(Serializer serializer, Response response, Context context, String tag) throws IOException, Exception;

}
