package com.sunchao.rpc.common.codec.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sunchao.rpc.common.codec.Codec;

/**
 * No-op with the compression and decompression.
 * 
 * @author sunchao
 *
 */
final public class NullCodec implements Codec {

	public ByteBuffer compress(ByteBuffer uncompressedData) throws IOException {
		
		return uncompressedData;
	}

	public ByteBuffer decompress(ByteBuffer compressedData) throws IOException {
		
		return compressedData;
	}

}
