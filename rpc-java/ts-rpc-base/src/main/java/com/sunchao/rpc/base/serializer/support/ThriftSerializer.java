package com.sunchao.rpc.base.serializer.support;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;

import com.sunchao.rpc.base.MessageType;
import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.exception.RPCTransportException;
import com.sunchao.rpc.base.metadata.PacketHeader;
import com.sunchao.rpc.base.metadata.RPCMetaData;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.base.serializer.ThriftSerializerHelper;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;

/**
 *   Thrift Serializer.
 *     
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ThriftSerializer extends TSerializer implements Serializer {

	private static final int MESSAGE_LENGTH_INDEX = 0;
	
	/*public static ThriftSerializer getInstance() {
		 return ThriftSerializerHolder.instance;
	}*/
	
	@Override
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) throws Exception {
		if (context.isRequest()) 
		    return serializeRequest(obj, context);
		else 
			return serializeResponse(obj, context);
	}
	
	@SuppressWarnings("rawtypes")
	private <T> ByteBuffer serializeRequest(T obj, Context context) throws RPCSerializationException {
		if (context == null || context.getCallMeta() == null) {
			throw new IllegalArgumentException(
					new StringBuilder().append(
							"Could not find the remote call context which in attachment: ")
							.append(obj.getClass().getName())
							.toString());
		}
		RPCMetaData metadata = context.getCallMeta();
		String serviceName = metadata.getServiceName();
		String methodName = metadata.getMethodName();
		if (serviceName == null || serviceName.length() == 0) {
			throw new IllegalArgumentException(
					new StringBuilder()
					.append("Could not find service name in attachment with ")
					.append(obj.getClass().getName())
					.append("'s serializer context!") 
					.toString());
		}
		
		TMessage message = new TMessage(
				methodName, 
				TMessageType.CALL,
				ThriftSerializerHelper.getNextSeqId());
		
		String methodArgClassName = ThriftSerializerHelper.getServiceMethodArgClassName(
				serviceName, methodName);
		Class<?> clazz =  ThriftSerializerHelper.getThriftArgOrReturnClass(methodArgClassName);
		TBase argObject;
		try {
			argObject = (TBase) clazz.newInstance();
		} catch (Throwable t ) {
			 argObject = ThriftSerializerHelper.getTBase(clazz);
		}
		if (argObject == null) {
			throw new RPCSerializationException(RPCSerializationException.UNKNOWN, 
					new StringBuilder().append("Could not initailize the TBase request object: ")
					.append(clazz.getName())
					.toString());
		}
		for (int i = 0;  i < context.getArguments().length; i ++) {
			Object arg = context.getArguments()[i];
			if (arg == null) {continue; }
			TFieldIdEnum field = argObject.fieldForId(i + 1);
			String setMethodName = ThriftSerializerHelper.generateGetMethodName(field.getFieldName());
			Method method;
			
			try {
				method = clazz.getMethod(setMethodName, context.getArgumentTypes()[i]);
			} catch (NoSuchMethodException e) {
				throw new RPCSerializationException(
						RPCSerializationException.INVALID_DATA, 
						e.getMessage(), e);
			}
			
			try {
				method.invoke(argObject, arg);
			} catch (Throwable e) {
				throw new RPCSerializationException(
						RPCSerializationException.INVALID_DATA,
						e.getMessage(), e);
			} 
		}
		ByteBufferOutputStream output = new ByteBufferOutputStream();
		TProtocol protocol = ThriftSerializerHelper.getTProtocol(output);
		int messageLength;
		byte[] bytes = new byte[4];
		try {
			
			//message length placeholder. 4 bytes.
			protocol.writeI32(Integer.MAX_VALUE);
	    	protocol.getTransport().flush();
			
			//message body
			protocol.writeMessageBegin(message);
			argObject.write(protocol);
            protocol.writeMessageEnd();
            protocol.getTransport().flush();
            int tag = output.size();
            //message size.
            messageLength = tag - 4;
            
            try {
            	TFramedTransport.encodeFrameSize(messageLength, bytes);
            	output.setWireTag(MESSAGE_LENGTH_INDEX);
            	protocol.writeI32(messageLength);
            } finally {
            	output.setWireTag(tag);
            }
		} catch (TException e) {
			throw new RPCSerializationException(
					RPCSerializationException.INVALID_DATA, e.getMessage(), e);
		}
		byte[] data = output.toByteArray();
		ByteBuffer buffer = ByteBuffer.allocate(messageLength + 4);
		//buffer.putInt(messageLength);
		buffer.put(data);
		return buffer;
		
	}
	
	@SuppressWarnings("rawtypes")
	private <T> ByteBuffer serializeResponse(T obj, Context context) throws Exception {
		if (context == null || context.getCallMeta() == null) {
			throw new IllegalArgumentException(
					new StringBuilder().append(
							"Could not find the remote call context which in attachment: ")
							.append(obj.getClass().getName())
							.toString());
		}
		RPCMetaData metadata = context.getCallMeta();
		int seqId = context.getSeqId();
		Object result = context.getResult();
		String serviceName = metadata.getServiceName();
		String methodName = metadata.getMethodName();
		if (serviceName == null || serviceName.length() == 0) {
			throw new IllegalArgumentException(
					new StringBuilder()
					.append("Could not find service name in attachment with ")
					.append(obj.getClass().getName())
					.append("'s serializer context!") 
					.toString());
		}
		String returnTypeName = ThriftSerializerHelper.getServiceReturnClassName(serviceName, methodName);
		if (returnTypeName == null || returnTypeName.length() > 0) {
			throw new RPCSerializationException(
					RPCSerializationException.INVALID_DATA, 
					"Not found the return type class: " + returnTypeName);
		}
		
		Class<?> clazz = ThriftSerializerHelper.getThriftArgOrReturnClass(returnTypeName);
		TBase resultObj;
		try {
			resultObj = (TBase) clazz.newInstance();
		} catch (Throwable t ) {
			 resultObj = ThriftSerializerHelper.getTBase(clazz);
		}
		if (resultObj == null) {
			throw new RPCSerializationException(RPCSerializationException.UNKNOWN, 
					new StringBuilder().append("Could not initailize the TBase request object: ")
					.append(clazz.getName())
					.toString());
		}
		RPCException exception = null;
		TMessage message;
		if (! context.isResultNormally()) {
			Class<?> clz = context.getExceptionType();
			int index = 1;
			boolean found = false;
			while (true) {
				TFieldIdEnum fieldEnum = resultObj.fieldForId(index++);
				if (fieldEnum == null) { break; }
				String fieldName = fieldEnum.getFieldName();
				String getMethodName = ThriftSerializerHelper.generateGetMethodName(fieldName);
				String setMethodName = ThriftSerializerHelper.generateSetMethodName(fieldName);
				Method getMethod;
				Method setMethod;
				try {
					getMethod = clazz.getMethod(getMethodName);
					if (getMethod.getReturnType().equals(clz)) {
						found = true;
						setMethod = clazz.getMethod(setMethodName, clz);
						setMethod.invoke(resultObj, clz.newInstance());
					}
				} catch (NoSuchMethodException e) {
					throw new RPCSerializationException(
							RPCSerializationException.INVALID_DATA,
							e.getMessage(), e);
				} catch (InvocationTargetException e) {
					throw new RPCSerializationException(
							RPCSerializationException.INVALID_DATA,
							e.getMessage(), e);
				} catch (IllegalAccessException e) {
					throw new RPCSerializationException(
							RPCSerializationException.INVALID_DATA,
							e.getMessage(), e);
				} catch (Exception e) {
					throw new RPCSerializationException(
							RPCSerializationException.INVALID_DATA,
							e.getMessage(), e);
				}
			}
			if (! found) {
				exception = new RPCException(RPCApplicationException.INTERNAL_ERROR,
						"Error when repsonse to the RPC call, the exception type: " +
				          clz.getName() + " not found ! ");
			}	
		} else {
			String fieldName = resultObj.fieldForId(0).getFieldName();
			String setMethodName = ThriftSerializerHelper.generateSetMethodName(fieldName);
			String getMethodName = ThriftSerializerHelper.generateGetMethodName(fieldName);
			Method setMethod;
			Method getMethod;
			try {
				getMethod = clazz.getMethod(getMethodName);
				setMethod = clazz.getMethod(setMethodName, getMethod.getReturnType());
				setMethod.invoke(resultObj, result); // the method result;
			} catch (NoSuchMethodException e) {
				throw new RPCSerializationException(
						RPCSerializationException.INVALID_DATA,
						e.getMessage(), e);
			} catch (InvocationTargetException e) {
				throw new RPCSerializationException(
						RPCSerializationException.INVALID_DATA,
						e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new RPCSerializationException(
						RPCSerializationException.INVALID_DATA,
						e.getMessage(), e);
			}
		}
		
		if (exception != null) {
			message = new TMessage(methodName, TMessageType.EXCEPTION, seqId);
		} else {
			message = new TMessage(methodName, TMessageType.REPLY, seqId);
		}
		
		ByteBufferOutputStream output = new ByteBufferOutputStream();
		TProtocol protocol = ThriftSerializerHelper.getTProtocol(output);
		int messageLength;
		byte[] bytes = new byte[4];
		
		try {
			
			protocol.writeI32(Integer.MAX_VALUE);
			protocol.getTransport().flush();
			
			//message body
			protocol.writeMessageBegin(message);
			resultObj.write(protocol);
            protocol.writeMessageEnd();
            protocol.getTransport().flush();
            //message size.
            int tag = output.size();
            messageLength = tag - 4;
            try {
            	TFramedTransport.encodeFrameSize(messageLength, bytes);
            	output.setWireTag(MESSAGE_LENGTH_INDEX);
            	protocol.writeI32(messageLength);
            	//output.setWireTag(MESSAGE_HEADER_LENGTH_INDEX);
            	//protocol.writeI16((short)(0xFFFF & headerLength));
            } finally {
            	output.setWireTag(tag);
            }
		} catch (TException e) {
			throw new RPCSerializationException(
					RPCSerializationException.INVALID_DATA, e.getMessage(), e);
		}
		byte[] data = output.toByteArray();
		ByteBuffer buffer = ByteBuffer.allocate(messageLength + 4);
		//buffer.putInt(messageLength);
		buffer.put(data);
		return buffer;	
	}

	@SuppressWarnings({ "rawtypes", "static-access" })
	@Override
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config) throws RPCException{
		int available = buf.getInt();
		if (available <= 0 ) {
			throw new RPCTransportException(
					RPCTransportException.UNKNOWN, "the packet length low to the lowest value!");
		} else {
			TIOStreamTransport transport = new TIOStreamTransport(new ByteBufferInputStream(
					buf.array(), buf.arrayOffset() + buf.position(), buf.limit() - buf.position()));
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			
			String serviceName;
			int seqId;
			TMessage message;
			try {
				serviceName = context.getCallMeta().getServiceName();
				message = protocol.readMessageBegin();
			} catch (TException e) {
				throw new RPCTransportException(
						RPCTransportException.UNKNOWN, 
						e.getMessage(), e);
			}
			
			if (message.type == TMessageType.CALL) {
				RPCMetaData metadata = new RPCMetaData();
				metadata.setServiceName(serviceName);
				metadata.setMethodName(message.name);
				seqId = message.seqid;
				context.setSeqId(seqId);
				
				String argsClassName = ThriftSerializerHelper.getServiceMethodArgClassName(
						serviceName, message.name);
				
				if (argsClassName == null || argsClassName.length() == 0) {
					throw new RPCSerializationException(
							RPCSerializationException.INVALID_DATA
							, "Invalid method argument name ");
				}
				Class<?> clazz = ThriftSerializerHelper.getThriftArgOrReturnClass(argsClassName);
				
				TBase args = null;
				try {
					args = (TBase) clazz.newInstance();
				} catch (Throwable t) {
					if (args == null) {
						args = ThriftSerializerHelper.getTBase(clazz);
						if (args == null) {
							throw new RPCException(t.getMessage(), t); 
						 }
					}
				}
				try {
					args.read(protocol);
					protocol.readMessageEnd();
				} catch (TException e) {
					throw new RPCSerializationException(
							RPCSerializationException.INVALID_DATA, 
							e.getMessage(), e);
				}
				
				List<Object> parameters = new ArrayList<Object>();
				List<Class<?>> parameterType = new ArrayList<Class<?>>();
				int index = 1;
				
				while (true) {
					TFieldIdEnum fieldIdEnum = args.fieldForId(index++);
					if (fieldIdEnum == null) {break; }
					String fieldName = fieldIdEnum.getFieldName();
					String getMethodName = ThriftSerializerHelper.generateGetMethodName(fieldName);
					Method getMethod;
					try {
						getMethod = clazz.getMethod(getMethodName);
					} catch (NoSuchMethodException e) {
						throw new RPCException(e.getMessage(), e);
					}
					
					parameterType.add(getMethod.getReturnType());
					try {
						parameters.add(getMethod.invoke(args));
					} catch (Throwable t) {
						throw new RPCException(t.getMessage(), t);
					}
				}
				context.setArguments(parameters.toArray());
				context.setArgumentTypes(parameterType.toArray(
						new Class<?>[parameterType.size()]));
				return null;
				
			} else if (message.type == TMessageType.EXCEPTION) {
				
				TApplicationException exception = null;
				try {
					exception.read(protocol);
					protocol.readMessageEnd();
				} catch (TException e) {
					throw new RPCSerializationException(
							RPCSerializationException.UNKNOWN, 
							e.getMessage(), e);
				}
				context.setException(new RPCException(exception.getMessage()));
				context.setSeqId(message.seqid);
				return null;
			} else if (message.type == TMessageType.REPLY) {
				String resultName = ThriftSerializerHelper.getServiceReturnClassName(serviceName, message.name);
				if (resultName == null || resultName.length() == 0) {
					throw new RPCSerializationException(
							RPCSerializationException.INVALID_DATA,
							new StringBuilder()
							.append("Could not infer service result class name from service name: ")
							.append(serviceName)
							.append(", the service name you specified may not generated by idl compiler")
							.toString());
				}
				
				Class<?> clazz = ThriftSerializerHelper.getThriftArgOrReturnClass(resultName);
				
				TBase resultBase = null;
				
				try {
					try {
						resultBase = (TBase) clazz.newInstance();
					} catch (Throwable t) {
						if (resultBase == null) {
							resultBase =ThriftSerializerHelper.getTBase(clazz);
							if (resultBase == null) {
								throw new RPCSerializationException(
										RPCSerializationException.INVALID_DATA,
										new StringBuilder()
										.append("Could not infer service result class name from service name: ")
										.append(serviceName)
										.append(", the service name you specified may not generated by idl compiler")
										.toString());
							}
						}
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					resultBase.read(protocol);
					protocol.readMessageEnd();
				} catch (TException t) {
					throw new RPCException(t.getMessage(), t);
				}
				Object result = null;
				int index = 0;
				while (true) {
					TFieldIdEnum fieldIdEnum = resultBase.fieldForId(index++);
					if (fieldIdEnum == null) { break; }
					Field field;
					try {
						 field = clazz.getDeclaredField(fieldIdEnum.getFieldName());
						 field.setAccessible(true);
					} catch (NoSuchFieldException e) {
						throw new RPCException(e.getMessage(), e);
					}
					
					if (result != null) { break; }
					
				}
				context.setResult(result);
				context.setSeqId(message.seqid);
				return null;
			} else {
				throw new RPCException();
			}
		}
	}

/*	private static class ThriftSerializerHolder {
		static ThriftSerializer instance = new ThriftSerializer();
	}
	
	private ThriftSerializer() {}*/

	@Override
	public byte getIdentifyId() {
		return 4;
	}
}
