package com.sunchao.rpc.common.serializable;

import java.io.IOException;

/**
 * Object read.
 * @author sunchao
 *
 */
public interface ObjectOutput extends DataOutput {

	/**
	 * object write
	 * @param obj
	 *        the object instance.
	 *        
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	void writeObject(Object obj) throws IOException, ClassNotFoundException;
	
	
}
