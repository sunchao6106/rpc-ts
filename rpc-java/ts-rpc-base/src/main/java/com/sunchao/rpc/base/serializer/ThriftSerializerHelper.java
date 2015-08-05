package com.sunchao.rpc.base.serializer;

import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TIOStreamTransport;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.sunchao.rpc.base.ReflectUtils;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;

/**
 * Thrift Serializer Helper.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ThriftSerializerHelper {
	
	/**
	 * OBj Initialization instead of the reflect when the no access. or <code>reflectASM</code>
	 */
	private static final Objenesis OBJ = new ObjenesisStd(true);

	/**
	 * Thrift TMessage SEQ_ID.
	 */
	private static final AtomicInteger SEQ_ID = new AtomicInteger(0);
	
	/**
	 * Thrift argument suffix.
	 */
	private static final String THRIFT_ARGS_SUFFIX = "_args";
	
	/**
	 * Thrift result suffix.
	 */
	private static final String THRIFT_RESULT_SUFFIX = "_result";
	
	@SuppressWarnings("rawtypes")
	public static TBase getTBase(Class<?> clazz) {
		return (TBase) OBJ.newInstance(clazz);
	}
	
	/**
	 * Get the method argument class.
	 * 
	 * @param serviceName
	 * @param methodName
	 * @return
	 */
	public static String getServiceMethodArgClassName(String serviceName, String methodName) {
		return  serviceName + methodName + THRIFT_ARGS_SUFFIX;
	}
	
	/**
	 * Get the method return type class.
	 * 
	 * @param serviceName
	 * @param methodName
	 * @return
	 */
	public static String getServiceReturnClassName(String serviceName, String methodName) {
		return serviceName + methodName + THRIFT_RESULT_SUFFIX;
	}
	
	public static int getNextSeqId() {
		return SEQ_ID.incrementAndGet();
	}
	
	private static final ConcurrentMap<String, Class<?>> CACHED_CLASS = 
			new ConcurrentHashMap<String, Class<?>>();
	
	/**
	 * Here only return the <code>TCompactProtocol</code>.
	 * 
	 * @return {@link org.apache.thrift.protocol.TCompactProtocol} 
	 */
	public static TProtocol getTProtocol(OutputStream output) {
		return new TBinaryProtocol(new TIOStreamTransport(output));
	}
	
	public static Class<?> getThriftArgOrReturnClass(String className) {
		Class<?> clazz = CACHED_CLASS.get(className);
		if (clazz == null) {
			clazz = ReflectUtils.forName(className);
			if (clazz == null) {
				throw new RPCSerializationException(RPCSerializationException.INVALID_DATA,
						   "Not found in thrift serializer of the class:" + 
				             className);
			}
			CACHED_CLASS.putIfAbsent(className, clazz);
		}
		return clazz;
	}
	
	/**
	 * Set field method.
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String generateSetMethodName(String fieldName) {
		return new StringBuilder( )
		         .append("set")
		         .append(Character.toUpperCase(fieldName.charAt(0)))
		         .append(fieldName.substring(1))
		         .toString();
	}
	
	/**
	 * get field Method.
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String generateGetMethodName(String fieldName) {
		return new StringBuilder( )
		         .append("get")
		         .append(Character.toUpperCase(fieldName.charAt(0)))
		         .append(fieldName.substring(1))
		         .toString();
	}
	
	
		 
  /*  public static Class<?> getTType(byte type) throws TProtocolException {
			    switch ((byte)(type & 0x0f)) {
			      case TType.STOP:
			        return TType.STOP;
			      case Types.BOOLEAN_FALSE:
			      case Types.BOOLEAN_TRUE:
			        return TType.BOOL;
			      case Types.BYTE:
			        return TType.BYTE;
			      case Types.I16:
			        return TType.I16;
			      case Types.I32:
			        return TType.I32;
			      case Types.I64:
			        return TType.I64;
			      case Types.DOUBLE:
			        return TType.DOUBLE;
			      case Types.BINARY:
			        return TType.STRING;
			      case Types.LIST:
			        return TType.LIST;
			      case Types.SET:
			        return TType.SET;
			      case Types.MAP:
			        return TType.MAP;
			      case Types.STRUCT:
			        return TType.STRUCT;
			      default:
			        throw new RPCSerializationException("don't know what type: " + (byte)(type & 0x0f));
			    }
			  }
	}*/
	
	private ThriftSerializerHelper() {}
}
