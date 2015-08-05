package com.sunchao.rpc.base.serializer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.common.ClientConfig;

/**
 * The serialization Interface.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public abstract class TSerializer implements Serializer {

	/**
	 * The default charset.
	 */
	public static final Charset SYS_CHARSET = Charset.forName("UTF-8");
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.Serializer#serialize(T, com.sunchao.rpc.base.serializer.Context)
	 */
	public abstract <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) throws RPCSerializationException, Exception;
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.Serializer#deserialize(java.nio.ByteBuffer, com.sunchao.rpc.base.serializer.Context)
	 */
	public abstract <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config) throws RPCSerializationException, RPCException;
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.Serializer#getIdentifyId()
	 */
	public abstract byte getIdentifyId();
	
	
	public static String byte2String(byte[] data) {
		return new String(data, SYS_CHARSET);
	}
	
	public static byte[] stringtoBytes(String str) {
		return str.getBytes(SYS_CHARSET);
	}
}
