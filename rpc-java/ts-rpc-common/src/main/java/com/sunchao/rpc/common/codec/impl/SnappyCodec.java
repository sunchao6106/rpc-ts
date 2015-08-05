package com.sunchao.rpc.common.codec.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xerial.snappy.Snappy;

import com.sunchao.rpc.common.codec.Codec;

/**
 * Google.snappy
 * 
 * @author sunchao
 *
 */


final public class SnappyCodec implements Codec {

	public ByteBuffer compress(ByteBuffer uncompressedData) throws IOException {
		ByteBuffer out = 
				ByteBuffer.allocate(Snappy.maxCompressedLength(uncompressedData.remaining()));
		int size = Snappy.compress(uncompressedData.array(), uncompressedData.position(), uncompressedData.remaining(),
				out.array(), 0);
		out.limit(size);
		return out;
	}

	public ByteBuffer decompress(ByteBuffer compressedData) throws IOException {
		ByteBuffer out =
				ByteBuffer.allocate(Snappy.uncompressedLength(compressedData.array(), compressedData.position(), compressedData.remaining()));
		int size = Snappy.uncompress(compressedData.array(), compressedData.position(), compressedData.remaining(),
				out.array(), 0);
		out.limit(size);
		return out;
	}

	
}
