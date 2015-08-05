package com.sunchao.rpc.common.serializable;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Object input
 * @author sunchao
 *
 */
public interface ObjectInput extends DataInput {

	/**
	 * read object.
	 * @return
	 *       object instance.
	 * @throws IOException
	 *      
	 * @throws ClassNotFoundException
	 */
	Object readObject() throws IOException, ClassNotFoundException;
	
	/**
	 * read object.
	 * @param cls
	 *       the class type.
	 * @return
	 *       the instance.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	<T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException;
	
	/**
	 * read object.
	 * @param cls
	 *       the class type.
	 * @param type
	 *       this is super class of all types.
	 *        includes the primitive type, class type, type variable, array class, generic type.
	 *      @see java.lang.reflect.Type.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	<T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException;
}
