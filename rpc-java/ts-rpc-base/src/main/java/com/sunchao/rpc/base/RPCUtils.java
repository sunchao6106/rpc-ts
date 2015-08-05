package com.sunchao.rpc.base;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

import com.sunchao.rpc.base.metadata.PacketHeader;
import com.sunchao.rpc.base.metadata.RPCMetaData;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * RPC Utility.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class RPCUtils {
 
	private static final Logger LOGGER = LoggerFactory.getLogger(RPCUtils.class);
	
	/**
	 * The client get the packet id for packet header.
	 */
	private static final AtomicLong PACKET_ID = new AtomicLong(0);
	
	public static long getNextPacketId() {
		return PACKET_ID.incrementAndGet();
	}
	
	/**
	 * Get the return type.
	 * 
	 * @param metadata
	 * @return
	 */
	public static Class<?> getReturnType(RPCMetaData metadata) {
		try {
			if (metadata != null ) {
				Class<?>[] parameterTypes = new Class<?>[0];
				String service = metadata.getServiceName();
				String methodName = metadata.getMethodName();
				String[] parameterTypeNames = metadata.getParameterTypeName();
				if (service != null && service.length() > 0 
						&& methodName != null && methodName.length() > 0) {
					Class<?> cls = ReflectUtils.forName(service);
					if (parameterTypeNames != null && parameterTypeNames.length > 0) {
						parameterTypes = new Class<?>[parameterTypeNames.length];
						for (int i = 0; i < parameterTypeNames.length; i++) {
							parameterTypes[i] = ReflectUtils.forName(parameterTypeNames[i]);
						}
					}
					Method method = cls.getMethod(methodName, parameterTypes);
					if (method.getReturnType() == void.class) {
						return null;
					}
					return method.getReturnType();
				}
			}
		} catch (Throwable t) {
			LOGGER.error(t.getMessage(), t);
		}
		return null;
	}
	
	/**
	 * Generic method type.
	 * 
	 * @param metadata
	 * @return
	 */
	public static Type[] getReturnTypes(RPCMetaData metadata) {
		try {
			if (metadata != null) {
				Class<?>[] parameterTypes = new Class<?>[0];
				String service = metadata.getServiceName();
				String methodName = metadata.getMethodName();
				String[] parameterTypeNames = metadata.getParameterTypeName();
				if (service != null && service.length() > 0
						&& methodName != null && methodName.length() > 0) {
					Class<?> cls = ReflectUtils.forName(service);
					if (parameterTypeNames != null && parameterTypeNames.length > 0) {
						parameterTypes = new Class<?>[parameterTypeNames.length];
						for (int i = 0; i < parameterTypeNames.length; i++) {
							parameterTypes[i] = ReflectUtils.forName(parameterTypeNames[i]);
						}
					}
					Method method = cls.getMethod(methodName, parameterTypes);
					if (method.getReturnType() == void.class) {
						return null;
					}
					return new Type[] {method.getReturnType(), method.getGenericReturnType()};
				}
			}
		} catch (Throwable t) {
			LOGGER.error(t.getMessage(), t);
		}
		return null;
	}
	
	/**
	 * Get the RPC method call's parameter types.
	 * If <code>null</code>, return empty class type
	 * array.
	 * @param metadata
	 * @return
	 */
	public static Class<?>[] getParameterTypes(RPCMetaData metadata) {
		if (metadata.getParameterTypeName() != null
				&& metadata.getParameterTypeName().length > 0) {
			String[] types = metadata.getParameterTypeName();
			Class<?>[] parameterTypes = new Class<?>[types.length];
			for (int i = 0; i < types.length; i++) {
				parameterTypes[i] = ReflectUtils.forName(types[i]);
			}
			return parameterTypes;
		}
		return new Class<?>[0];
	}
	
	/**
	 * Judge the request message whether or not oneWay type.
	 * (Request)
	 * @param header The Message Packet Header.
	 * @return
	 */
	public static boolean isOneWay(PacketHeader header) {
		boolean isOneWay = false;
		if (header.getMessageType() == MessageType.ONEWAY) {
			isOneWay = true;
		}
		return isOneWay;
	}
	
	/**
	 * Judge the reply message whether or not nomarly type.
	 * @param header The Message packet Header.
	 * @return
	 */
	public static boolean isReturnResult(PacketHeader header) {
		boolean isReturnResult = true;
		if (header.getMessageType() != MessageType.REPLY) {
			isReturnResult = false;
		}
		return isReturnResult;
	}
	
	/**
	 * Get the specified rpc exception type.
	 * (response)
	 * @param header The Message Packet Header.
	 * @return
	 */
	public static Class<?> getRPCExceptionType(PacketHeader header) {
		if (isReturnResult(header)) return null;
		byte type = header.getMessageType();
		Class<?> cls = MessageType.getExceptionTypeByID(type);
		return cls;
	}
}
