package com.sunchao.rpc.common.serializable.support.nativejava;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;

import com.sunchao.rpc.common.serializable.ObjectInput;
import com.sunchao.rpc.common.utils.Assert;

/**
 * the native java input stream.
 * {@link java.io.ObjectInputStream}
 * @author sunchao
 * 
 * @see java.io.ObjectInputStream #readObject();
 *
 */
public class NativeJavaObjectInput implements ObjectInput {

	private final ObjectInputStream inputStream;
	
	public NativeJavaObjectInput(InputStream is) throws IOException {
		this(new ObjectInputStream(is));
	}
	
	public NativeJavaObjectInput(ObjectInputStream ois) throws IOException {
		Assert.notNull(ois, "input == null");
		inputStream = ois;
	}
	
	protected ObjectInputStream getObjectInputStream() {
		return this.inputStream;
	}
	
	public byte readByte() throws IOException {
		return inputStream.readByte();
	}

	public boolean readBool() throws IOException {
		return inputStream.readBoolean();
	}

	public short readShort() throws IOException {
		return inputStream.readShort();
	}

	public int readInt() throws IOException {
		return inputStream.readInt();
	}

	public float readFloat() throws IOException {
		return inputStream.readFloat();
	}

	public long readLong() throws IOException {
		return inputStream.readLong();
	}

	public double readDouble() throws IOException {
		return inputStream.readDouble();
	}

	/**
	 * read firstly the length of the byte array,
	 * if the length less than 0(-1), return 
	 * <code>null</code>; and the length = 0,
	 * return empty byte array.
	 */
	public byte[] readBytes() throws IOException {
		int len = inputStream.readInt(); 
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return new byte[0];
		} else {
			byte[] result = new byte[len];
			inputStream.readFully(result);
			return result;
		}
	}

	public Object readObject() throws IOException, ClassNotFoundException {
		return inputStream.readObject();
	}

	@SuppressWarnings("unchecked")
	public <T> T readObject(Class<T> cls) throws IOException,
			ClassNotFoundException {
		return (T) readObject();
	}

	@SuppressWarnings("unchecked")
	public <T> T readObject(Class<T> cls, Type type) throws IOException,
			ClassNotFoundException {
		return (T) readObject();
	}

	public String readUTF() throws IOException {
		return inputStream.readUTF();
	}

}
