package com.sunchao.rpc.base.serializer;

import java.nio.ByteBuffer;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.extension.Component;
import com.sunchao.rpc.common.extension.HotSwap;
@Component("varint")
public interface Serializer {

	/**
	 * encode the message object with the specified serialization context.
	 * 
	 * @param obj the message which need to be encoded.
	 * @param context the serialize/deserialized context.
	 * @return the serialized message byte buffer.
	 * @throws RPCSerializationException 
	 * @throws Exception 
	 */
	@HotSwap("encode")
	public abstract <T> ByteBuffer serialize(T obj, Context context, ClientConfig config)
			throws RPCSerializationException, Exception;

	/**
	 * deserialize the data from the byte buffer with the specified
	 * serialization context, and return the message object.
	 * @param buf  the byte buffer.
	 * @param context the specified serialization context.
	 * @return the message object.
	 * @throws RPCSerializationException 
	 * @throws RPCException 
	 */
	@HotSwap("decode")
	public abstract <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config)
			throws RPCSerializationException, RPCException;

	/**
	 * 
	 * @return get the serializer byte identity.
	 */
	public abstract byte getIdentifyId();

}