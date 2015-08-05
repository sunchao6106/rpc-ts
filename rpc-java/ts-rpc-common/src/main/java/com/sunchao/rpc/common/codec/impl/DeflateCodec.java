package com.sunchao.rpc.common.codec.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import com.sunchao.rpc.common.codec.Codec;

/**
 * The default compression and decompression.
 * with zlib to compress and decompress.
 * @author sunchao
 *
 */
public class DeflateCodec implements Codec {
	
	private ByteArrayOutputStream outputBuffer;
	private Deflater deflater;
	private Inflater inflater;

	public ByteBuffer compress(ByteBuffer uncompressedData) throws IOException {
		ByteArrayOutputStream baos = getOutputBuffer(uncompressedData.remaining());
	    DeflaterOutputStream dos = new DeflaterOutputStream(baos, getDeflater());
	    
	    try {
	    	dos.write(uncompressedData.array());
	    } finally {
	    	dos.close();
	    }
	    return ByteBuffer.wrap(baos.toByteArray());
	}

	public ByteBuffer decompress(ByteBuffer compressedData) throws IOException {
		ByteArrayOutputStream baos = getOutputBuffer(compressedData.remaining());
		InflaterOutputStream ios = new InflaterOutputStream(baos, getInflater());
		
		try {
			ios.write(compressedData.array());
		} finally {
			ios.close();
		}
		return ByteBuffer.wrap(baos.toByteArray());
	}
	
	
	/**
	 * @see java.util.zip.Inflater #Inflater(boolean).
	 * with the argument(true), which is compatible to the gzip.
	 * 
	 * @return
	 *       the decompression.
	 */
	private Inflater getInflater() {
		if (null == inflater)
		{
			inflater = new Inflater(true);
		}
		inflater.reset();
		return inflater;
	}
	
	/**
	 * @see java.util.zip.Deflater #Deflater(int, boolean)
	 * @see java.util.zip.Deflater #BEST_COMPRESSION
	 * @see java.util.zip.Deflater #BEST_SPEED
	 * @see java.util.zip.Deflater #DEFAULT_COMPRESSION
	 * 
	 * @return
	 *       the compression.
	 */
	private Deflater getDeflater() {
		if (null == deflater)
		{
			deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		}
		deflater.reset();
		return deflater;
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
