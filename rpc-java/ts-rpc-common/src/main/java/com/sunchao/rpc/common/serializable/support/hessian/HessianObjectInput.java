package com.sunchao.rpc.common.serializable.support.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.caucho.hessian.io.Hessian2Input;
import com.sunchao.rpc.common.serializable.ObjectInput;

/**
 * Hession Object Input.
 * 
 * @author sunchao
 *
 */
public class HessianObjectInput implements ObjectInput {

	private final Hessian2Input hn2i;

	public byte readByte() throws IOException {
		return (byte) hn2i.readInt();
	}

	public boolean readBool() throws IOException {
		return hn2i.readBoolean();
	}

	public short readShort() throws IOException {
		return (short) hn2i.readInt();
	}

	public int readInt() throws IOException {
		return hn2i.readInt();
	}

	public float readFloat() throws IOException {
		return (float) hn2i.readDouble();
	}

	public long readLong() throws IOException {
		return hn2i.readLong();
	}

	public double readDouble() throws IOException {
		return hn2i.readDouble();
	}

	public byte[] readBytes() throws IOException {
		return hn2i.readBytes();
	}

	public Object readObject() throws IOException, ClassNotFoundException {
		return hn2i.readObject();
	}

	@SuppressWarnings("unchecked")
	public <T> T readObject(Class<T> cls) throws IOException,
			ClassNotFoundException {
		return (T) hn2i.readObject(cls);
	}

	public <T> T readObject(Class<T> cls, Type type) throws IOException,
			ClassNotFoundException {
		return readObject(cls);
	}
	
	public HessianObjectInput(InputStream is) {
		hn2i = new Hessian2Input(is);
		hn2i.setSerializerFactory(HessianSerializerFactory.getInstance());
	}

	public String readUTF() throws IOException {
		return hn2i.readString();
	}
}
