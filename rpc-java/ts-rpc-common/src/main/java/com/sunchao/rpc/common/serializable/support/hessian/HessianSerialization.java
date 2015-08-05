package com.sunchao.rpc.common.serializable.support.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sunchao.rpc.common.serializable.ObjectInput;
import com.sunchao.rpc.common.serializable.ObjectOutput;
import com.sunchao.rpc.common.serializable.Serialization;

public class HessianSerialization implements Serialization {
	
	public static final byte ID = 2;
	

	public byte getContentTypeId() {
		return ID;
	}

	public String getContentType() {
		return "x-application/hessian";
	}

	public ObjectOutput serialize(OutputStream os) throws IOException {
		return new HessianObjectOutput(os);
	}

	public ObjectInput deserialize(InputStream is) throws IOException {
		return new HessianObjectInput(is);
	}

}
