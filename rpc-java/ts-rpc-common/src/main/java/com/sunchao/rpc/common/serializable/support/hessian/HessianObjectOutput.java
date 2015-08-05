package com.sunchao.rpc.common.serializable.support.hessian;

import java.io.IOException;
import java.io.OutputStream;
import com.caucho.hessian.io.Hessian2Output;
import com.sunchao.rpc.common.serializable.ObjectOutput;

/**
 * Hession Object output.
 * 
 * @author sunchao
 *
 */
public class HessianObjectOutput implements ObjectOutput {
	
	private final Hessian2Output hn2o;
	
	public HessianObjectOutput(OutputStream os)
	{
		hn2o = new Hessian2Output(os);
		hn2o.setSerializerFactory(HessianSerializerFactory.getInstance());
	}

	public void writeBool(boolean z) throws IOException {
		hn2o.writeBoolean(z);
	}

	public void writeByte(byte b) throws IOException {
		hn2o.writeInt(b);
	}

	public void writeShort(short s) throws IOException {
		hn2o.writeInt(s);
	}

	public void writeInt(int i) throws IOException {
		hn2o.writeInt(i);
	}

	public void writeLong(long j) throws IOException {
		hn2o.writeLong(j);
	}

	public void writeFloat(float f) throws IOException {
		hn2o.writeDouble(f);
	}

	public void writeDouble(double d) throws IOException {
		hn2o.writeDouble(d);
	}

	public void writeUTF(String s) throws IOException {
		hn2o.writeString(s);
	}

	public void writeBytes(byte[] v) throws IOException {
		hn2o.writeBytes(v);
	}

	public void writeBytes(byte[] v, int off, int len) throws IOException {
		hn2o.writeBytes(v, off, len);
	}

	public void flushBuffer() throws IOException {
		hn2o.flushBuffer();
	}

	public void writeObject(Object obj) throws IOException,
			ClassNotFoundException {
		hn2o.writeObject(obj);
	}

}
