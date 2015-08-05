package com.sunchao.rpc.common.utils;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

import com.sunchao.rpc.common.annotation.Utility;

@Utility("Crc32")
final public class Crc32Checksum {

	private static CRC32 crc32 = new CRC32();
	
	public static int size() {
		return 4;
	}
	
	public static ByteBuffer compute(ByteBuffer data) {
		crc32.reset();
		crc32.update(data.array(), data.position(), data.remaining());
		
		ByteBuffer result = ByteBuffer.allocate(size());
		result.putInt((int)crc32.getValue());
		result.flip();
		return result;
	}
	
	private Crc32Checksum (){};
}
