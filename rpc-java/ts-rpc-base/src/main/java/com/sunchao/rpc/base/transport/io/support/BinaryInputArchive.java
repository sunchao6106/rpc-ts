package com.sunchao.rpc.base.transport.io.support;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.metadata.RPCMetaData;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.transport.io.InputArchive;
import com.sunchao.rpc.common.ClientConfig;


/**
 * The implementation direct pull data from the socke stream.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class BinaryInputArchive implements InputArchive {

	private DataInput in;
	
	public static BinaryInputArchive getArchive(InputStream is) {
		return new BinaryInputArchive(new DataInputStream(is));
	}
	
	public BinaryInputArchive(DataInput in) {
		this.in = in;
	}
	
	public byte readByte(String tag) throws IOException {
		return this.in.readByte();
	}

	public boolean readBool(String tag) throws IOException {
		return this.in.readBoolean();
	}

	public int readInt(String tag) throws IOException {
		return this.in.readInt();
	}

	public long readLong(String tag) throws IOException {
		return this.in.readLong();
	}

	public float readFloat(String tag) throws IOException {
		return this.in.readFloat();
	}

	public double readDouble(String tag) throws IOException {
		return this.in.readDouble();
	}

	public String readString(String tag) throws IOException {
		int len = this.in.readInt();
		if (len == -1) return null;
		byte[] b = new byte[len];
		this.in.readFully(b);
		return new String(b, "UTF8");
	}

	public byte[] readBuffer(String tag) throws IOException {
		int len = readInt(tag);
		if (len == -1) return null;
		if (len < 0 || len > 1024) 
			throw new IOException("unreasonable length = " + len);
		byte[] buf = new byte[len];
		this.in.readFully(buf);
		return buf;
	}

	public short readShort(String tag) throws IOException {
		return this.in.readShort();
	}

	public ByteBuffer readByteBuffer(String tag) throws IOException {
		byte[] buf = readBuffer(tag);
		return ByteBuffer.wrap(buf);
	}

	@Deprecated
	public void readRPCMetadata(RPCMetaData metadata, String tag)
			throws IOException {
		
	}

	public void readRequest(Serializer serializer, Request request,
			Context context, String tag) throws IOException, Exception {
		if (serializer == null)
			throw new IllegalStateException("serializer cannot be null.");
		int len = this.in.readInt();
		if (len == -1) return ;
		byte[] buf = new byte[len];
		this.in.readFully(buf);
		request = serializer.deserialize(ByteBuffer.wrap(buf), context, new ClientConfig(null, 0,null , null));
	}

	public void readResponse(Serializer serializer, Response response,
			Context context, String tag) throws IOException, Exception {
		if (serializer == null)
			throw new IllegalStateException("serializer cannot be null");
		int len = this.in.readInt();
		if(len == -1) return;
		byte[] buf = new byte[len];
		this.in.readFully(buf);
		response = serializer.deserialize(ByteBuffer.wrap(buf), context, new ClientConfig(null, 0,null , null));
	}

}
