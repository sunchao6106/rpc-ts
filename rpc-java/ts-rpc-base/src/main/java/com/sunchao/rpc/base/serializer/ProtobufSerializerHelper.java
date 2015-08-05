package com.sunchao.rpc.base.serializer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.Message;
import com.sunchao.rpc.base.RPCUtils;
import com.sunchao.rpc.base.ReflectUtils;
import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.utils.HolderUtil;

/**
 * Google Protobuf Serializer Helper.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ProtobufSerializerHelper {
	
	//private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufSerializerHelper.class);
	
	private static final Objenesis OBJENESIS = new ObjenesisStd(true);
	
	private static final ConcurrentMap<String, com.google.protobuf.Message> MESSAGE_CACHE = 
			                                 new ConcurrentHashMap<String, com.google.protobuf.Message>();
	
	//private static final ConcurrentMap<String, HolderUtil<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<String, HolderUtil<Class<?>>>();
	
	public static com.google.protobuf.Message getMessage(String className) {
		com.google.protobuf.Message message = MESSAGE_CACHE.get(className);
		if (message == null) {
			Class<?> clazz = ReflectUtils.forName(className);
			message = (Message) OBJENESIS.newInstance(clazz);
			MESSAGE_CACHE.putIfAbsent(className, message);
		}
		return message;
	   /* Builder builder = map.get(clazz);
		if (builder == null) {
			try {
				Method method = clazz.getMethod(METHOD);
				builder = (Builder) method.invoke(clazz, new Object[0]);
				map.put(clazz, builder);
			} catch (NoSuchMethodException e) {
                    LOGGER.error("the method: " + METHOD + "( " + ") of class: " + clazz.getName() + " not exits", e);
                    throw new RPCApplicationException(RPCApplicationException.UNKNOWN_METHOD, 
                    		"the method: " + METHOD + "( " + ") of class: " + clazz.getName() + " not exits", e);
			} catch (SecurityException e) {
				    LOGGER.error("unknown exception", e);
				    throw new RPCApplicationException(RPCApplicationException.INTERNAL_ERROR, 
				    		"unknown internal error!", e);
			} catch (Exception e) {
				    LOGGER.error("unknown exception", e);
				    throw new RPCApplicationException(RPCApplicationException.INTERNAL_ERROR, 
				    		"unknown internal error!", e);
			}
		}
		return builder;*/
	}
	
	/*public static Class<?> getClass(String className) throws ClassNotFoundException {
		if (className == null || className.length() == 0) {
			throw new RPCSerializationException(RPCSerializationException.INVALID_DATA, 
					    "Error when deserialize the class name(" +  className + ")");
		}
		
		HolderUtil<Class<?>> holder = CLASS_CACHE.get(className);
		if (holder == null) {
			holder = new HolderUtil<Class<?>>();
			HolderUtil<Class<?>> oldHolder = CLASS_CACHE.putIfAbsent(className, holder);
			if (oldHolder != null) {
				holder = oldHolder;
			}
		}
		
		Class<?> clazz = holder.get();
		if (clazz == null) {
			synchronized (holder) {
				clazz = holder.get();
				if (clazz == null) {
					 clazz = ReflectUtils.forName(className);
			         if (clazz == null ){
			            throw new ClassNotFoundException("Not Found the class name: " + className);	                                                       //这里有这种结构有点大材小用了，这步操作一般为比较耗时的操作，所以用多级锁，
			         }
					 holder.set(clazz);                                       //分离单一并发热点。
				}
			}
		}
	    return clazz;	  
	}*/
	

	
	private ProtobufSerializerHelper() {}
}
