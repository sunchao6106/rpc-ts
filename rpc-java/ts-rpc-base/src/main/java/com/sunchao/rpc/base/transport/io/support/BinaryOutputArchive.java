package com.sunchao.rpc.base.transport.io.support;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.metadata.RPCMetaData;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.base.transport.io.OutputAchive;
import com.sunchao.rpc.common.ClientConfig;

/**
 * The implementation which direct output to the socket .
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class BinaryOutputArchive implements OutputAchive{

	private ByteBuffer buf = ByteBuffer.allocate(1024);
	private DataOutput out;
	
	public static BinaryOutputArchive getArchive(OutputStream os) {
		return new BinaryOutputArchive(new DataOutputStream(os));
	}
	
	/**
	 * Creates a new instance of BinaryOutputArchive.
	 * @param out the data out put.
	 */
	public BinaryOutputArchive(DataOutput out) {
		this.out = out;
	}
			
	public void writeByte(byte b, String tag) throws IOException {
		this.out.writeByte(b);
	}

	public void writeBool(boolean b, String tag) throws IOException {
		this.out.writeBoolean(b);
	}

	public void writeInt(int i, String tag) throws IOException {
		this.out.writeInt(i);
	}

	public void writeLong(long l, String tag) throws IOException {
		this.out.writeLong(l);
	}

	public void writeFloat(float f, String tag) throws IOException {
		this.out.writeFloat(f);
	}

	public void writeDouble(double d, String tag) throws IOException {
	    this.out.writeDouble(d);
	}

	public void writeShort(short s, String tag) throws IOException {
		this.out.writeShort(s);
	}

	public void writeByteBuffer(ByteBuffer buffer, String tag)
			throws IOException {
		if (buf == null || buf.remaining() == 0) {
			this.out.writeInt(0);
			return;
		}
		this.out.writeInt(buf.remaining());
		if (buf.hasArray()) {
			this.out.write(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
		} else {
			ByteBuffer tmp = buf.duplicate();
		    byte[] b = new byte[tmp.remaining()];
		    tmp.get(b);
		    this.out.write(b);
		}		
	}

	public void writeBuffer(byte[] buf, String tag) throws IOException {
		if (buf == null || buf.length == 0) {
			this.out.writeInt(-1);
			return;
		}
		this.out.writeInt(buf.length);
		this.out.write(buf);
		
	}

	@Deprecated
	public void writeRPCMeta(RPCMetaData metaData, String tag)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void writeRequest(TSerializer serialier, Request request, Context context,
			String tag) throws Exception {
		if (request == null) 
			throw new IllegalArgumentException("request cannot be null.");
		if (serialier == null)
			throw new IllegalArgumentException("serializer cannot be null.");
		ByteBuffer buf = serialier.serialize(request, context, new ClientConfig(null, 0,null , null));
		if (buf.remaining() == 0) {
			this.out.writeInt(-1);
			return;
		}
		this.out.writeInt(buf.remaining());	
		writeByteBuffer(buf, tag);
	}

	public void writeResponse(TSerializer serializer, Response response, Context context,
			String tag) throws Exception {
		if (serializer == null)
			throw new IllegalArgumentException("serializer cannot be null.");
		if (response == null)
			throw new IllegalArgumentException("response cannot be null.");
		ByteBuffer buf = serializer.serialize(response, context, new ClientConfig(null, 0,null , null));
		if (buf.remaining() == 0) {
			this.out.writeInt(-1);
			return;
		}
		this.out.writeInt(buf.remaining());
		writeByteBuffer(buf, tag);
			
	}

	public void writeString(String s, String tag) throws IOException {
		if (s == null) {
			writeInt(-1, "len");
			return;
		}
		ByteBuffer bb = stringToByteBuffer(s);
		writeInt(bb.remaining(), "len");
		this.out.write(bb.array(), bb.arrayOffset() + bb.position(), bb.limit());
	}
	
	private final ByteBuffer stringToByteBuffer(CharSequence s) {
		buf.clear();
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			if (buf.remaining() < 3) {
				ByteBuffer newBuf = ByteBuffer.allocate(buf.capacity() << 1);
				buf.flip();
				newBuf.put(buf);
				buf = newBuf;
			}
			char c = s.charAt(i);
			if (c < 0x80) {
				buf.put((byte)c);
			} else if (c < 0x800) {
				buf.put((byte) (0xC0 | (c >> 6)));
				buf.put((byte) (0x80 | (c & 0x3F)));
			} else {
				buf.put((byte) (0xE0 | (c >> 12)));
				buf.put((byte) (0x80 | ((c >> 6) & 0x3F)));
				buf.put((byte) (0x80 | (c & 0x3F)));
			}
		}
		buf.flip();
		return buf;
	}

}
