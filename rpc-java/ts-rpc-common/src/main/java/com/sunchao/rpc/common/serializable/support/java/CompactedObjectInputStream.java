package com.sunchao.rpc.common.serializable.support.java;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/**
 * Compacted Object Input Stream. 
 * <p>
 * Which opposite the {@link CompactedObjectInputStream}.
 * </p>
 * @author sunchao
 *
 */
public class CompactedObjectInputStream extends ObjectInputStream {
	/**the class loader used to load the serialized class */
	private ClassLoader loader;

	public CompactedObjectInputStream(InputStream in) throws IOException {
		this(in, Thread.currentThread().getContextClassLoader() == null ?
				CompactedObjectInputStream.class.getClassLoader() :
					Thread.currentThread().getContextClassLoader());
	}
	
	public CompactedObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
		super(in);
		this.loader = loader;
	}
	
	@Override
	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException
	{
         int type = read();
         if (type < 0)
        	 throw new EOFException();
         switch (type) 
         {
              case 0 :
            	  return super.readClassDescriptor();
              case 1 :
            	  Class<?> clazz = loadClass(readUTF());
            	  return ObjectStreamClass.lookup(clazz); // @see java.io.ObjectStreamClass #lookup(class);
              default :
            	  throw new StreamCorruptedException("Unexcepted class descriptor type: " + type);
         }
	}
	
	private Class<?> loadClass(String className) throws ClassNotFoundException 
	{
		return this.loader.loadClass(className);
	}

}
