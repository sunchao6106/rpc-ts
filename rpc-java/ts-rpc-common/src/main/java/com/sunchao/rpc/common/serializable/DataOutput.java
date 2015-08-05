package com.sunchao.rpc.common.serializable;

import java.io.IOException;

/**
 * Data output. 
 * @author sunchao
 *
 */
public interface DataOutput {
	
    /**
     * write boolean.
     * @param z
     *       boolean.
     * @throws IOException
     */
	void writeBool(boolean z) throws IOException;
	
	/**
	 * write byte.
	 * @param b
	 *       byte.
	 * @throws IOException
	 */
	void writeByte(byte b) throws IOException;
	
	/**
	 * write short.
	 * @param s
	 *       short.
	 * @throws IOException
	 */
	void writeShort(short s) throws IOException;
	
	/**
	 * write int.
	 * @param i
	 *        int.
	 * @throws IOException
	 */
	void writeInt(int i) throws IOException;
	
	/**
	 * write long.
	 * @param j
	 *       long.
	 * @throws IOException
	 */
	void writeLong(long j) throws IOException;
	
	/**
	 * write float
	 * @param f
	 *        float.
	 * @throws IOException
	 */
	void writeFloat(float f) throws IOException;
	
	/**
	 * write double.
	 * @param d
	 *       double.
	 * @throws IOException
	 */
	void writeDouble(double d) throws IOException;
	
	/**
	 * write string.
	 * @param s
	 *       string.
	 * @throws IOException
	 */
	void writeUTF(String s) throws IOException;
	
	/**
	 * write bytes
	 * @param v
	 *       bytes.
	 * @throws IOException
	 */
	void writeBytes(byte[] v) throws IOException;
	
	/**
	 * write bytes.
	 * @param v
	 *       bytes.
	 * @param off
	 *       the offset value.
	 * @param len
	 *       the length.
	 * @throws IOException
	 */
	void writeBytes(byte[] v, int off, int len) throws IOException;
	
	/**
	 * flush the buffer.
	 * @throws IOException
	 */
	void flushBuffer() throws IOException;
}
