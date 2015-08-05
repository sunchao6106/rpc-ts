package com.sunchao.rpc.common.codec.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.sunchao.rpc.common.codec.Codec;

/**
 * BZip2 compression and decompression.
 * @author sunchao
 *
 */
public class BZip2Codec implements Codec {
	
	private ByteArrayOutputStream outputBuffer;
	public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

	
	public ByteBuffer compress(ByteBuffer uncompressedData) throws IOException {
		ByteArrayOutputStream baos = getOutputBuffer(uncompressedData.remaining());
		BZip2CompressorOutputStream outputStream = new BZip2CompressorOutputStream(baos);
		
		try {
			outputStream.write(uncompressedData.array());
		} finally {
			outputStream.close();
		}
		
		ByteBuffer result = ByteBuffer.wrap(baos.toByteArray());
		return result;
	}

	public ByteBuffer decompress(ByteBuffer compressedData) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(compressedData.array());
		BZip2CompressorInputStream inputStream  = new BZip2CompressorInputStream(bais);
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			int readCount = -1;
			while ((readCount = inputStream.read(buf, compressedData.position(), buf.length)) > 0 )
			{
				baos.write(buf, 0, readCount);
			}
			ByteBuffer result = ByteBuffer.wrap(baos.toByteArray());
			return result;
		} finally {
			inputStream.close();
		}
	}

	private ByteArrayOutputStream getOutputBuffer(int suggestedLength) {
		if (null == outputBuffer)
		{
			outputBuffer = new ByteArrayOutputStream(suggestedLength);
		}
		outputBuffer.reset();
		return outputBuffer;
	}
}
