package com.sunchao.rpc.base.serializer;

import java.util.HashMap;
import java.util.Map;

import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.serializer.support.AvroSerializer;
import com.sunchao.rpc.base.serializer.support.HessianSerializer;
import com.sunchao.rpc.base.serializer.support.JSONSerializer;
import com.sunchao.rpc.base.serializer.support.JavaSerializer;
import com.sunchao.rpc.base.serializer.support.KryoSerializer;
import com.sunchao.rpc.base.serializer.support.ProtobufSerializer;
import com.sunchao.rpc.base.serializer.support.ThriftSerializer;
import com.sunchao.rpc.base.serializer.support.varint.VarintSerializer;
import com.sunchao.rpc.common.serializable.support.java.JavaSerialization;

/**
 * Serialization factory which define the type of serialization. 
 * And register the serialization module, also you can use other 
 * serialization module which you like, just to register it with
 * 
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Deprecated
public class SerializationFactory {

	/**Java Serialization */
	public static final byte SERIALIZER_JAVA              = (byte) 0x01;
	
	/**Hessian Serialization */
	public static final byte SERIALIZER_HESSION           = (byte) 0x02;
	
	/**Kryo Serialization */
	public static final byte SERIALIZER_KRYO              = (byte) 0x03;
	
	/**Thrift Serialization*/
	public static final byte SERIALIZER_THRIFT            = (byte) 0x04;
	
	/**JSON Serialization*/
	public static final byte SERIALIZER_JSON               = (byte) 0x05;
	
	/**Avro Serialization*/
	public static final byte SERIALIZER_AVRO               = (byte) 0x06;
	
	/**Protobuf Serialization*/
	public static final byte SERIALIZER_PROTOBUF           = (byte) 0x07;
	
	public static final byte SERIALIZER_VARINT             = (byte) 0x08;  
	
	private static final Map<Byte, TSerializer> SERIALIER_MAP = new HashMap<Byte, TSerializer>();
	
	@Deprecated
	/*static {
		SerializationFactory.registerSerializer(SERIALIZER_VARINT,  VarintSerializer.getInstance());
		SerializationFactory.registerSerializer(SERIALIZER_JAVA,    JavaSerializer.getInstance());
		SerializationFactory.registerSerializer(SERIALIZER_HESSION, HessianSerializer.getInstance());
		SerializationFactory.registerSerializer(SERIALIZER_KRYO,    KryoSerializer.getInstance());
		SerializationFactory.registerSerializer(SERIALIZER_THRIFT,  ThriftSerializer.getInstance());
		SerializationFactory.registerSerializer(SERIALIZER_JSON,    JSONSerializer.getInstance());
		SerializationFactory.registerSerializer(SERIALIZER_AVRO,    AvroSerializer.getInstance());
		SerializationFactory.registerSerializer(SERIALIZER_PROTOBUF, ProtobufSerializer.getInstance());
	}*/
	
	public static void registerSerializer(byte type, TSerializer serializer) {
		SERIALIER_MAP.put(type, serializer);
	}
	
	public static Serializer getSerializer() {
		return SERIALIER_MAP.get(SERIALIZER_VARINT);
	}
	
	public static TSerializer getSerializer(byte type) {
		return SERIALIER_MAP.get(type);
	}
	
/*	public static void main(String args[]) throws RPCSerializationException, Exception {
		TSerializer s = SerializationFactory.getSerializer((byte)0x08);
		s.serialize(new SerializationFactory(), new Context());
	}*/
	
	private SerializationFactory() {}
}
