package com.sunchao.rpc.common.serializable.support.nativejava;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sunchao.rpc.common.serializable.ObjectInput;
import com.sunchao.rpc.common.serializable.ObjectOutput;
import com.sunchao.rpc.common.serializable.Serialization;

/**
 *  java Native serialization method.
 *  {@link java.io.ObjectInputStream},
 *  {@link java.io.ObjectOutputStream};
 * 
 * @author sunchao
 *
 */
public class NativeJavaSerialization implements Serialization{
	
	/**serialization name*/
	public static final String NAME = "nativejava";

	public byte getContentTypeId() {
		return 7;
	}

	/** serialization type*/
	public String getContentType() {
		return "x-application/nativejava";
	}

	public ObjectOutput serialize(OutputStream os) throws IOException {
		return new NativeJavaObjectOutput(os);
	}

	public ObjectInput deserialize(InputStream is) throws IOException {
		return new NativeJavaObjectInput(is);
	}

}
