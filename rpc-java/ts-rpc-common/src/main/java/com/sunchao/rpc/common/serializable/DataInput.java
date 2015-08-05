package com.sunchao.rpc.common.serializable;

import java.io.IOException;

/**
 * Data input
 * @author sunchao
 *
 */
public interface DataInput {
	
	/**
	 * read byte.
	 * @return
	 *       byte
	 * @throws IOException
	 */
	byte readByte() throws IOException;
	
	/**
	 * read boolean.
	 * @return
	 *       boolean.
	 * @throws IOException
	 */
	boolean readBool() throws IOException;
	
	/**
	 * read short.
	 * @return
	 *       short.
	 * @throws IOException
	 */
	short readShort() throws IOException;
	
	/**
	 * read int.
	 * @return
	 *       int.
	 * @throws IOException
	 */
	int readInt() throws IOException;
	
	/**
	 * read float.
	 * @return
	 *       float.
	 * @throws IOException
	 */
	float readFloat() throws IOException;
	
	/**
	 * read long.
	 * @return
	 *       long.
	 * @throws IOException
	 */
	long readLong() throws IOException;
	
	/**
	 * read double.
	 * @return
	 *      double.
	 * @throws IOException
	 */
	double readDouble() throws IOException;
	
	/**
	 * read bytes.
	 * @return
	 *      bytes.
	 * @throws IOException
	 */
	byte[] readBytes() throws IOException;
	
	/**
	 * read string.
	 * @return
	 *      string.
	 * @throws IOException
	 */
	String readUTF() throws IOException;

}
