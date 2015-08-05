package com.sunchao.rpc.base.serializer.varint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.sunchao.rpc.base.serializer.support.varint.DefaultClassResolve;
import com.sunchao.rpc.base.serializer.support.varint.Input;
import com.sunchao.rpc.base.serializer.support.varint.Output;
import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;

import junit.framework.TestCase;
import static org.junit.Assert.*;

public class PrimitiveInputOutputTest extends TestCase {

	private static final String SMALL_STRING = PrimitiveInputOutputTest.class.getName();
	private static final String BIG_STRING = "123456788hgfnasjsjahdsahdkashdkhadasdsadasdsa"
			+ "dsadasdjlajldsajdlasjdasidqwjdasdnsanxsamxsalmksad4677hsahdhsagdhgadgsadsakdsad"
			+ "!@#$%^&*()_+";
	private static final byte[] SMALL_BYTES = SMALL_STRING.getBytes();
	private static final byte[] BIG_BYTES = BIG_STRING.getBytes();
	
	public void testMain() throws IOException {
		//write
		ByteBufferOutputStream os = new ByteBufferOutputStream();
		Output out = new Output(os, new DefaultClassResolve());
		writeTest(out);
		
		//read
		byte[] b = os.toByteArray();
		Input in = new Input(new ByteBufferInputStream(b), new DefaultClassResolve());
		readTest(in);
	}
	
	private void writeTest(Output out) throws IOException {
		out.writeBoolean(true);
		out.writeBoolean(false);
		out.writeShort((short)'a');
		out.writeShort((short) -1);
		out.writeShort((short)1234);
		out.writeChar('a');
		out.writeChar((char)23);
		out.writeChar((char)234);
		out.writeInt(0x22);
		out.writeInt(-0x22);
		out.writeInt(0x2222);
		out.writeInt(-0x2222);
		out.writeInt(0x22222222);
		out.writeInt(-0x22222222);
		out.writeLong(0x22);
		out.writeLong(-0x22);
		out.writeLong(0x2222);
		out.writeLong(-0x2222);
		out.writeLong(0x222222);
		out.writeLong(-0x222222);
		out.writeLong(0x22222222);
		out.writeLong(0x2222222222l);
		out.writeLong(-0x2222222222l);
		out.writeLong(0x222222222222l);
		out.writeLong(-0x222222222222l);
		out.writeLong(0x22222222222222l);
		out.writeLong(-0x22222222222222l);
		out.writeLong(0x2222222222222222l);
		out.writeLong(-0x2222222222222222l);
    	out.writeDouble(1212.454);
		out.writeDouble(12345.67d);
		out.writeFloat(1224.34f);
		out.writeFloat(-345.67f);
		out.write(BIG_BYTES);
		out.write(SMALL_BYTES);
		out.writeUTF(SMALL_STRING);
		out.writeUTF(BIG_STRING);
		out.writeString(BIG_STRING);
		out.writeString(SMALL_STRING);
		out.writeAscii(BIG_STRING);;
		out.writeAscii(SMALL_STRING);
		out.flush();
	}
	
	private void readTest(Input in) throws IOException {
		assertEquals(in.readBoolean(), true);
		assertEquals(in.readBoolean(), false);
		assertEquals(in.readShort(), 'a');
		assertEquals(in.readShort(), -1);
		assertEquals(in.readShort(), 1234);
		assertEquals(in.readChar(), 'a');
		assertEquals(in.readChar(), 23);
		assertEquals(in.readChar(), 234);
		assertEquals(in.readInt(), 0x22);
		assertEquals(in.readInt(), -0x22);
		assertEquals(in.readInt(),  0x2222);
		assertEquals(in.readInt(), -0x2222);
		assertEquals(in.readInt(), 0x22222222);
		assertEquals(in.readInt(), -0x22222222);
		assertEquals(in.readLong(), 0x22);
		assertEquals(in.readLong(), -0x22);
		assertEquals(in.readLong(), 0x2222);
		assertEquals(in.readLong(), -0x2222);
		assertEquals(in.readLong(), 0x222222);
		assertEquals(in.readLong(), -0x222222);
		assertEquals(in.readLong(), 0x22222222);
		assertEquals(in.readLong(), 0x2222222222L);
		assertEquals(in.readLong(), -0x2222222222L);
		assertEquals(in.readLong(), 0x222222222222L);
		assertEquals(in.readLong(), -0x222222222222L);
		assertEquals(in.readLong(), 0x22222222222222L);
		assertEquals(in.readLong(), -0x22222222222222L);
		assertEquals(in.readLong(), 0x2222222222222222L);
		assertEquals(in.readLong(), -0x2222222222222222L);
 		assertEquals(in.readDouble(), 1212.454);
		assertEquals(in.readDouble(), 12345.67D);
		assertEquals(in.readFloat(), 1224.34F);
		assertEquals(in.readFloat(), -345.67F);
		assertSameArray(in.readBytes(BIG_BYTES.length), BIG_BYTES);
		assertSameArray(in.readBytes(SMALL_BYTES.length), SMALL_BYTES);
		assertEquals(in.readUTF(), SMALL_STRING);
		assertEquals(in.readUTF(), BIG_STRING);
		assertEquals(in.readString(), BIG_STRING);
		assertEquals(in.readString(), SMALL_STRING);
		assertEquals(in.readAscii(), BIG_STRING);;
		assertEquals(in.readAscii(), SMALL_STRING);
		
	}
	
	private static void assertSameArray(byte[] b1, byte[] b2) {
		assertEquals(b1.length, b2.length);
		for (int i = 0; i < b1.length; i++) {
			assertEquals(b1[i], b2[i]);
		}
	}
}
