package com.sunchao.rpc.base.serializer.support.varint;

import java.nio.ByteBuffer;

import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Varint Serializer
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class VarintSerializer extends TSerializer implements Serializer {

	//private static final Logger LOGGER = LoggerFactory.getLogger(VarintSerializer.class);
	
	/*public static TSerializer getInstance() {
		return VarintSerializerHolder.instance;
	}*/
	
	@Override
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) {
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Output out = null;
		try {
			out = new Output(bbos, new DefaultClassResolve());
			out.writeObject(obj);
			out.flush();
			ByteBuffer buffer = bbos.toByteBuffer();
			//return ByteBuffer.wrap(bbos.toByteArray());
			buffer.flip();
			return buffer;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config) {
		if (buf == null || buf.capacity() == 0) return null;
		ByteBufferInputStream bbis = null;
		Input input = null;
		T obj = null;
		if (buf.hasArray()) {
			bbis = new ByteBufferInputStream(buf.array(), buf.arrayOffset() + buf.position(), 
					buf.remaining());
		} else {
			ByteBuffer tmp = buf.duplicate();
			byte[] buffer = new byte[tmp.remaining()];
			tmp.get(buffer);
			bbis = new ByteBufferInputStream(buffer);
		}
		
		try {
			input = new Input(bbis, new DefaultClassResolve());
			obj = (T) input.readObject();
			return obj;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	/*PRIVATE STATIC CLASS VARINTSERIALIZERHOLDER {
		STATIC VARINTSERIALIZER INSTANCE = NEW VARINTSERIALIZER();
	}
	
	PRIVATE VARINTSERIALIZER(){}*/

	@Override
	public byte getIdentifyId() {
		return 1;
	}
}
