package com.sunchao.rpc.common.serializable.support.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import com.sunchao.rpc.common.serializable.ObjectInput;
import com.sunchao.rpc.common.serializable.ObjectOutput;
import com.sunchao.rpc.common.serializable.Serialization;

public class CompactedJavaSerialization implements Serialization {

	public byte getContentTypeId() {
		return 4;
	}

	public String getContentType() {
		return "x-application/compactedjava";
	}

	public ObjectOutput serialize(OutputStream os) throws IOException {
		return new JavaObjectOutput(os, true);
	}

	public ObjectInput deserialize(InputStream is) throws IOException {
		return new JavaObjectInput(is, true);
	}

}
