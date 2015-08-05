package com.sunchao.rpc.base.serializer.support;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
/**
 * JDK Serializer.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class JavaSerializer extends TSerializer implements Serializer {
	
	private static final Logger LOGGER =  LoggerFactory.getLogger(JavaSerializer.class);

	/**
	 * use the inner class to lazy load the single instance.
	 * use the class loader character.
	 * 
	 * @return
	 */
	/*public static TSerializer getInstance() {
	     return JavaSerializerHolder.instance;	
	}*/
	
	public <T> ByteBuffer serialize(T obj, Context context, ClientConfig config) {
		  ByteBufferOutputStream baos = new ByteBufferOutputStream();
		  ObjectOutputStream oos = null ;
		  try {   
			 oos = new ObjectOutputStream(baos);
		     oos.writeObject(obj);
		     return ByteBuffer.wrap(baos.toByteArray());
		  } catch (Exception e) {
			  throw new RuntimeException(e.getMessage(), e.getCause());
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					LOGGER.error("IO can't close normally when serializer the object : " + 
				obj.getClass().getName());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(ByteBuffer buf, Context context, ClientConfig config) {
		if (buf == null || buf.capacity() == 0)  return null;
		ByteBufferInputStream bais = null;
		ObjectInputStream ois = null;
		T obj = null;
		if (buf.hasArray()) {
			bais = new ByteBufferInputStream(buf.array(), buf.arrayOffset() + buf.position(),
					buf.remaining());
		} else {
			ByteBuffer temp = buf.duplicate();
			byte[] buffer = new byte[temp.remaining()];
			temp.get(buffer);
			bais = new ByteBufferInputStream(buffer);
		}
		
		try {
		    ois = new ObjectInputStream(bais);
		    obj = (T) ois.readObject();
		    return obj;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					LOGGER.error("error happen when close the inputStream of deserialize" , e);
				}
			}
		}
	}
	
/*	private static class JavaSerializerHolder {
		static JavaSerializer instance = new JavaSerializer();
	}

	private JavaSerializer() {}*/

	@Override
	public byte getIdentifyId() {
		return 8;
	};
}
