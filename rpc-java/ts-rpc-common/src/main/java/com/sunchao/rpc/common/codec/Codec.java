package com.sunchao.rpc.common.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sunchao.rpc.common.extension.Component;

/**
 * Interface for compression codecs.
 * @author sunchao
 *
 */
@Component("deflater")
public interface Codec {

	/**
	 * Compress data.
	 * 
	 * @param uncompressedData
	 *           the data which need compressed.
	 * @return
	 *           the  compressed data
	 * @throws IOException
	 */
	ByteBuffer compress(ByteBuffer uncompressedData) throws IOException;
	
	/**
	 * decompress data.
	 * @param compressedData
	 * @return
	 * @throws IOException
	 */
	ByteBuffer decompress(ByteBuffer compressedData) throws IOException;
}
