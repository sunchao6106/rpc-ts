package com.sunchao.rpc.base.serializer.support;

import java.nio.ByteBuffer;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerialContext;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.metadata.RPCMetaData;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.common.ClientConfig;

/**
 * JSON Serializer.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class JSONSerializer extends TSerializer implements Serializer {

	/*public static JSONSerializer getInstance() {
		return JSONSerializerHolder.instance;
	}*/
	/**
	 * {@link SerializerFeature #WriteClassName}
	 * 全序列化， 用原来的类型可以直接反序列化回来，不过得打开 SerializerFeature.WriteClassName特性。
	 * 客户端序列化的是方法参数值， 服务器端序列化的是返回值，或异常。
	 */
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) {
		String objString = JSON.toJSONString(obj, SerializerFeature.WriteClassName);
		byte[] buf = objString.getBytes(SYS_CHARSET);
		return ByteBuffer.wrap(buf);
	}

	/**
	 * 与上面相反，接收上面序列化的结果。
	 * @throws RPCSerializationException 
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config) 
			throws RPCSerializationException {
		byte[] buffer = null;
		if (buf.hasArray()) {
			buffer = buf.array();
		} else {
			ByteBuffer duplicate = buf.duplicate();
			buffer = new byte[duplicate.remaining()];
			duplicate.get(buffer);
		}
		if (context.isRequest()) {
			RPCMetaData metaData = context.getCallMeta();
			String[] parameterTypeName = metaData.getParameterTypeName();
			int size = parameterTypeName.length;
			Class[] argumentClass = new Class[size];
			ClassLoader loader = getClassLoader();
			for (int i = 0; i < size; i++) {
				try {
					argumentClass[i] = Class.forName(parameterTypeName[i], true, loader);
				} catch (ClassNotFoundException e) {
					throw new RPCSerializationException((byte)0x02, "Error when deserializer RPC request, the class(" + 
				                   parameterTypeName[i] + ")", e);
				}
			}
			return (T) JSON.parseArray(new String(buffer,SYS_CHARSET), argumentClass);
		} else {
			if (context.isResultNormally()) {
				return (T) JSON.parseObject(new String(buffer,SYS_CHARSET), context.getReturnClass());
			} else {
				return (T) JSON.parseObject(new String(buffer, SYS_CHARSET), context.getExceptionType());
			}
		}
	}
	
	public static ClassLoader getClassLoader() {
		ClassLoader classLoader;
		classLoader =  JSONSerializer.class.getClassLoader();
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		return classLoader;
	}
	
	/*private static class JSONSerializerHolder {
		static JSONSerializer instance = new JSONSerializer();
	}
	
	private JSONSerializer() {}*/
	@Override
	public byte getIdentifyId() {
		return 5;
	}

}
