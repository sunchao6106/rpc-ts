package com.sunchao.rpc.base.serializer.support;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;

/**
 * Hessian serializer.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class HessianSerializer extends TSerializer implements Serializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(HessianSerializer.class);
	
	/*public static TSerializer getInstance() {
		return HessianSerializerHolder.instance;
	}*/
	
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) {
		ByteBufferOutputStream baos = new ByteBufferOutputStream();
		Hessian2Output heOut = null;
		try {
			heOut = new Hessian2Output(baos);
			heOut.writeObject(obj);
			return ByteBuffer.wrap(baos.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		} finally {
			if (heOut != null) {
				try {
					heOut.close();
				} catch (IOException e) {
					LOGGER.error("IO can't close normally when serializer the object : " + 
							obj.getClass().getName());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config) {
		if (buf == null || buf.capacity() == 0) return null;
		ByteBufferInputStream bbis = null;
		Hessian2Input heInput = null;
		T obj = null;
		if (buf.hasArray()) {
			bbis = new ByteBufferInputStream(buf.array(), buf.arrayOffset() + buf.position(),
					buf.remaining());
		} else {
			ByteBuffer temp = buf.duplicate();
			byte[] buffer = new byte[temp.remaining()];
			temp.get(buffer);
			bbis = new ByteBufferInputStream(buffer);
		}
		try {
            heInput = new Hessian2Input(bbis);
			obj = (T) heInput.readObject();
			return obj;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		} finally {
			if (heInput != null) {
				try {
					heInput.close();
				} catch (IOException e) {
					LOGGER.error("error happen when close the deseriablize stream");
				}
			}
		}
	}
	
/*	private static class HessianSerializerHolder {
	    static HessianSerializer instance = new HessianSerializer();	
	}
	
	private HessianSerializer() {}*/

	@Override
	public byte getIdentifyId() {
		return 2;
	};

}
