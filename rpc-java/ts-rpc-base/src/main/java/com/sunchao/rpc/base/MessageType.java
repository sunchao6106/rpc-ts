package com.sunchao.rpc.base;

import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.exception.RPCTransportException;

/**
 * Message type of the ts-rpc protocol.
 * 这里为什么把什么的异常全用Byte表示出来， 主要是为了，可以在消息头中
 * 显示具体的异常类型，这样用JSON之类的解析时，较方便的判定类型信息。
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class MessageType {
	
	public static final byte REQUEST                                        = (byte) 0x00;
	public static final byte REPLY                                          = (byte) 0x01;
	public static final byte ONEWAY                                         = (byte) 0x02;
	public static final byte APPLICATIONEXCEPTION_UNKNOWN                   = (byte) 0x03;
	public static final byte APPLICATIONEXCEPTION_UNKNOWN_METHOD            = (byte) 0x04;
	public static final byte APPLICATIONEXCEPTION_INVALID_MESSAGE_TYPE      = (byte) 0x05;
	public static final byte APPLICATIONEXCEPTION_MISSING_RESULT            = (byte) 0x06;
	public static final byte APPLICATIONEXCEPTION_INTERNAL_ERROR            = (byte) 0x07;
	public static final byte SERIALIZATION_UNKNOWN                          = (byte) 0x08;
	public static final byte SERIALIZATION_NEGATIVE                         = (byte) 0x09;
	public static final byte SERIALIZATION_INVALID_DATA                     = (byte) 0x0A;
	public static final byte SERIALIZATION_SIZE_LIMIT                       = (byte) 0x0B;
	public static final byte TRANSPORT_UNKNOWN                              = (byte) 0x0C;
	public static final byte TRANSPORT_NOT_OPEN                             = (byte) 0x0D;
	public static final byte TRANSPORT_ALREADY                              = (byte) 0x0E;
	public static final byte TRANSPORT_TIMEOUT                              = (byte) 0x0F;
	
    public static Class<?> getExceptionTypeByID(byte messageType) {
    	switch((byte)(messageType & 0x0F)) {
    	  case  APPLICATIONEXCEPTION_UNKNOWN:
    	  case  APPLICATIONEXCEPTION_UNKNOWN_METHOD:
    	  case  APPLICATIONEXCEPTION_INVALID_MESSAGE_TYPE:
    		  return RPCApplicationException.class;
    	  case  SERIALIZATION_UNKNOWN:
    	  case  SERIALIZATION_NEGATIVE:
    	  case  SERIALIZATION_INVALID_DATA:
    	  case  SERIALIZATION_SIZE_LIMIT:
    		  return RPCSerializationException.class;
    	  case  TRANSPORT_UNKNOWN:
    	  case  TRANSPORT_NOT_OPEN:
    	  case  TRANSPORT_ALREADY:
    	  case  TRANSPORT_TIMEOUT:
    		  return RPCTransportException.class;
    	  default :
    		  return void.class;
    	
    	}
    }
}
