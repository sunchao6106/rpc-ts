package com.sunchao.rpc.base.serializer.support;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.KSerializerHelper;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.common.ClientConfig;

/**
 *  kryo Serializer.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class KryoSerializer extends TSerializer implements Serializer{
    
	/*public static TSerializer getInstance() {
		return KryoSerializerHolder.instance;
	}*/
	
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) {
		Output output = new Output(256);
		KSerializerHelper.getKryo().writeClassAndObject(output, obj);
		return ByteBuffer.wrap(output.toBytes());
		
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config) {
        if (buf == null || buf.remaining() == 0) return null;
        Input input = null;
        if (buf.hasArray()) {
            input = new Input(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining()); 
        } else {
        	ByteBuffer tmp = buf.duplicate();
        	byte[] buffer = new byte[buf.remaining()];
        	tmp.get(buffer);
        	input = new Input(buffer);
        }
        return (T) KSerializerHelper.getKryo().readClassAndObject(input);
	}
	
/*	private static class KryoSerializerHolder {
		static KryoSerializer instance = new KryoSerializer();
	}
	
	private KryoSerializer() {}
*/
	@Override
	public byte getIdentifyId() {
		return 3;
	}

}
