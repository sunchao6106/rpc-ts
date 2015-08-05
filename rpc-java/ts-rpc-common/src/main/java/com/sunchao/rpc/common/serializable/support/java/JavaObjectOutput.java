package com.sunchao.rpc.common.serializable.support.java;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sunchao.rpc.common.serializable.support.nativejava.NativeJavaObjectOutput;

/**
 * Java Object output.
 * 
 * @author sunchao
 *
 */
public class JavaObjectOutput extends NativeJavaObjectOutput {

	public JavaObjectOutput(OutputStream os) throws IOException {
		super(new ObjectOutputStream(os));
	}
	
	public JavaObjectOutput(OutputStream os, boolean compact) throws IOException {
		super(compact ? new CompactedObjectOutputStream(os) :
			new ObjectOutputStream(os));
	}
	
	/**
	 * firstly write the string length, and later write the string.
	 */
	public void writeUTF(String s) throws IOException 
	{
		if (s == null)
		{
			getObjectOutputStream().writeInt(-1);
		}
		else 
		{
			getObjectOutputStream().writeInt(s.length());
			getObjectOutputStream().writeUTF(s);
		}
	}

	/**
	 * if the object is null ,write (byte) 0.
	 */
	public void writeObject(Object obj) throws IOException 
	{
		if (obj == null)
		{
			getObjectOutputStream().writeByte(0);
		}
		else 
		{
			getObjectOutputStream().writeByte(1);
			getObjectOutputStream().writeObject(obj);
		}
	}
	
	public void flushBuffer() throws IOException 
	{
		getObjectOutputStream().flush();
	}
}
