package com.sunchao.rpc.base.io;
import static org.junit.Assert.*;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;

import junit.framework.TestCase;

public class TestWriteTag extends TestCase {
	
	@SuppressWarnings("resource")
	public void testMain() throws IOException {
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		DataOutput out = new DataOutputStream(bbos);
		out.writeByte((byte) 0x08); //0
		out.writeInt(123);//
		//out.writeUTF("helloworld");
		out.writeInt(Integer.MAX_VALUE);
		out.writeLong(899887L);
		bbos.setWireTag(5);
		out.writeInt(26);
		bbos.resetWriteTag();
		byte[] buf = bbos.toByteArray();
		DataInput input = new DataInputStream(new ByteBufferInputStream(buf));
		assertEquals(input.readByte(), (byte) 0x08);
	//	assertEquals(input.readUTF(), "hellowor");
		assertEquals(input.readInt(), 123);
		assertEquals(input.readInt(), 26);
	}

}
