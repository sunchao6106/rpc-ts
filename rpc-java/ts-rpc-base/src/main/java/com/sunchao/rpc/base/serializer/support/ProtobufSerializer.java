package com.sunchao.rpc.base.serializer.support;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.ProtobufSerializerHelper;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Google Protobuf Serializer.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ProtobufSerializer extends TSerializer implements Serializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufSerializer.class);
	
/*	public static ProtobufSerializer getInstance() {
		return ProtobufSerializerHolder.instance;
	}*/
	
	/**
	 * Firstly write the number of arguments, and secondly
	 * for loop write the every argument's class type, and 
	 * the content of argument.
	 */
	@Override
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) 
			throws RPCSerializationException {
		Object[] args;
		int capacity = 4;
		if (obj.getClass().isArray()) {
			 args = (Object[]) obj;
		} else {
			args = new Object[] {obj};
		}
		int argumentLength = args.length;
		Object[] types = new Object[argumentLength];
		Object[] arguments  = new Object[argumentLength];
		for (int i = 0; i < argumentLength;  i++) {
			Message meg = (Message) args[i];
			types[i] = meg.getClass().getName().getBytes(SYS_CHARSET);
			arguments[i] = meg.toByteArray();
			capacity +=  4 + ((byte[])types[i]).length + ((byte[])arguments[i]).length + 4;
		}
		ByteBuffer buffer = ByteBuffer.allocate(capacity);
		buffer.putInt(argumentLength);
		for (int i = 0; i < argumentLength; i++) {
			buffer.putInt(((byte[])types[i]).length);
			buffer.put((byte[])types[i]);
			
			buffer.putInt(((byte[])arguments[i]).length);
			buffer.put((byte[])arguments[i]);
		}
		buffer.flip();
		return buffer;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config)
			throws RPCSerializationException {
		int argumentLength = buf.getInt();
		if (argumentLength == 0) return null;
		List<Object> arguments = new ArrayList<Object>();
		//Builder builder = null;
		int namelen, contentlen;
		byte[] data;
		for (int i = 0; i < argumentLength; i++) {
			namelen = buf.getInt();
			data = new byte[namelen];
			buf.get(data);
			String className = new String(data, SYS_CHARSET);
			try {
				Message message = ProtobufSerializerHelper.getMessage(className);
				contentlen = buf.getInt();
				data = new byte[contentlen];
				buf.get(data);
				arguments.add(((Message)message).newBuilderForType().mergeFrom(data));
			} catch (InvalidProtocolBufferException e) {
				LOGGER.error("Invaild deserialize data!", e);
				throw new RPCSerializationException(RPCSerializationException.INVALID_DATA, 
						"Error when deserialize the class: " + className + "!", e);
			}
		}
		if (context.isRequest()) {
			if (arguments.size() == 0) {
				return (T) new Object[0];
			}
			return (T) arguments.toArray();
		} else {
			return (T) arguments.get(0);
		}
	}
	
/*	private static class ProtobufSerializerHolder {
		static ProtobufSerializer instance = new ProtobufSerializer();
	}
	
	private ProtobufSerializer() {}
*/
	@Override
	public byte getIdentifyId() {
		return 7;
	}

}
