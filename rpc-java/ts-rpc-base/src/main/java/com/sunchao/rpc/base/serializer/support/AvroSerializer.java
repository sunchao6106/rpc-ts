package com.sunchao.rpc.base.serializer.support;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.serializer.AvroSerializerHelper;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;

/**
 * Avro Serializer.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class AvroSerializer extends TSerializer implements Serializer {

	/*public static AvroSerializer getInstance() {
		return AvroSerializerHolder.instance;
	}*/
	
	@Override
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) 
			throws RPCSerializationException {
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Schema schema = context.getSchema();
		if (schema == null) {
			if (context.isRequest()) {
			    schema = AvroSerializerHelper.getProtocol(context.getCallMeta().getServiceName())
					          .getMessages().get(context.getCallMeta().getMethodName()).getRequest();
			} else if (context.isResultNormally()) {
				schema = AvroSerializerHelper.getProtocol(context.getCallMeta().getServiceName())
						      .getMessages().get(context.getCallMeta().getMethodName()).getResponse();
			} else {
				schema = AvroSerializerHelper.getProtocol(context.getCallMeta().getServiceName())
						      .getMessages().get(context.getCallMeta().getMethodName()).getErrors().getElementType();
			}
		}
		GenericDatumWriter<T> writer = new GenericDatumWriter<T>(schema);
		Encoder encoder = AvroSerializerHelper.getEncoder(bbos);
		try {
			writer.write(obj, encoder);
			encoder.flush();
		} catch (IOException e) {
		    throw new RPCSerializationException((byte) 0x00, "Error when serialize the object(" +
		                      obj.getClass().getSimpleName() + ")", e);
		}
		return ByteBuffer.wrap(bbos.toByteArray());
	}

	@Override
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config)
			throws RPCSerializationException {
		byte[] buffer;
		if (buf.hasArray()) {
			buffer = buf.array();
		} else {
			ByteBuffer tmp = buf.duplicate();
			buffer = new byte[tmp.remaining()];
			tmp.get(buffer);
		}
		Schema schema = context.getSchema();
		if (schema == null) {
			if (context.isRequest()) {
				schema = AvroSerializerHelper.getProtocol(context.getCallMeta().getServiceName())
						.getMessages().get(context.getCallMeta().getMethodName()).getRequest();
			} else if (context.isResultNormally()) {
				schema = AvroSerializerHelper.getProtocol(context.getCallMeta().getServiceName())
						.getMessages().get(context.getCallMeta().getMethodName()).getResponse();
			} else {
				schema = AvroSerializerHelper.getProtocol(context.getCallMeta().getServiceName())
						.getMessages().get(context.getCallMeta().getMethodName()).getErrors().getElementType();
			}
		}
		Decoder decoder = AvroSerializerHelper.getDecoder(buffer);
		GenericDatumReader<T> reader = new GenericDatumReader<T>(schema);
		try {
			T obj = reader.read(null, decoder);
			return obj;
		} catch (IOException e) {
			throw new RPCSerializationException((byte) 0x00, "Error when deserializer the object(" +
		                       schema.getFullName() + ")", e);
		}
	}

	/*private static class AvroSerializerHolder {
		private static AvroSerializer instance = new AvroSerializer();
	}
	
	private AvroSerializer() {}*/

	@Override
	public byte getIdentifyId() {
		return 6;
	};
}
