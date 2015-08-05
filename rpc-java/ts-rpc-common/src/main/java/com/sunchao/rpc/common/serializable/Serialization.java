package com.sunchao.rpc.common.serializable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sunchao.rpc.common.extension.HotSwap;
import com.sunchao.rpc.common.extension.Component;

/**
 * Serialization (SPI, Singleton, ThreadSafe)
 * @author sunchao
 *
 */
@Component(" ")
public interface Serialization {

	/**
	 * get content type id.
	 * @return
	 *       content type id.
	 */
	byte getContentTypeId();
	
	/**
	 * get content type .
	 * @return
	 *       content type.
	 */
	String getContentType();
	
	/**
	 * create serializer.
	 * @param url
	 *       @see com.sunchao.rpc.common.URL;
	 * @param os
	 *       @see java.io.OnputStream;
	 * @return
	 *       @see com.sunchao.rpc.common.serializable.ObjectOutput.
	 * @throws IOException
	 */
	@HotSwap
	ObjectOutput serialize(OutputStream os) throws IOException;
	
	/**
	 * create deserializer.
	 * @param url
	 *      @see com.sunchao.rpc.common.URL.
	 * @param is
	 *      @see java.io.IutputStream.
	 * @return
	 *      @see com.sunchao.rpc.common.serializable.ObjectInput.
	 * @throws IOException
	 */
	@HotSwap
	ObjectInput deserialize(InputStream is) throws IOException;
}
