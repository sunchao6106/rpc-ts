package com.sunchao.rpc.common.serializable.support.nativejava;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sunchao.rpc.common.serializable.ObjectOutput;
import com.sunchao.rpc.common.utils.Assert;

/**
 * java native object output.
 * underlying stream {@link java.io.ObjectOutputStream};
 * 
 * @author sunchao
 *
 */
public class NativeJavaObjectOutput implements ObjectOutput {
	
	private final ObjectOutputStream outputStream;
	
	public NativeJavaObjectOutput(OutputStream os) throws IOException {
		this(new ObjectOutputStream(os));
	}
	
	public NativeJavaObjectOutput(ObjectOutputStream oos) throws IOException {
		Assert.notNull(oos, "output == null");
		this.outputStream = oos;
	}
	
	protected ObjectOutputStream getObjectOutputStream() {
		return this.outputStream;
	}

	public void writeBool(boolean z) throws IOException {
         this.outputStream.writeBoolean(z);
	}

	public void writeByte(byte b) throws IOException {
		 this.outputStream.writeByte(b);
	}

	public void writeShort(short s) throws IOException {
   		 this.outputStream.writeShort(s);
	}

	public void writeInt(int i) throws IOException {
         this.outputStream.writeInt(i);
	}

	public void writeLong(long j) throws IOException {
		 this.outputStream.writeLong(j);
	}

	public void writeFloat(float f) throws IOException {
		 this.outputStream.writeFloat(f);
	}

	public void writeDouble(double d) throws IOException {
          this.outputStream.writeDouble(d);
	}

	public void writeUTF(String s) throws IOException {
		  this.writeUTF(s);
	}

	/**
	 * if the byte array is <code>null</code>; the underlying
	 * stream write the length <i>-1</i> and <i>return</i>,
	 * else firstly write the byte array length, and later
	 * write the byte array.
	 * 
	 */
	public void writeBytes(byte[] v) throws IOException {
		  if (v == null) {
			  this.outputStream.writeInt(-1);
		  } else {
			  this.outputStream.writeInt(v.length);
			  this.outputStream.write(v, 0, v.length);
		  }
	}


	public void writeBytes(byte[] v, int off, int len) throws IOException {
		   if (v == null) {
			   this.outputStream.writeInt(-1);
		   } else {
			   this.outputStream.writeInt(len);
			   this.outputStream.write(v, off, len);
		   }
	}

	public void flushBuffer() throws IOException {
		this.outputStream.flush();
	}

	public void writeObject(Object obj) throws IOException,
			ClassNotFoundException {
		this.outputStream.writeObject(obj);
	}

}
