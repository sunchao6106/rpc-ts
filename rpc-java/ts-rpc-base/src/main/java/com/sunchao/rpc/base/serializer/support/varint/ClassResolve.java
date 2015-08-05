package com.sunchao.rpc.base.serializer.support.varint;

import java.io.IOException;

import com.sunchao.rpc.base.serializer.support.varint.collection.IntObjectHashMap;

/**
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public interface ClassResolve {

	//private static final Logger LOGGER = LoggerFactory.getLogger(ClassResolve.class);
	/**
	 * flag when write/read class, 0 denote <code>null</code>, 1 denote Not <code>null</code>
	 * , 2 denote the {@link java.lang.Object}. 
	 */
	public static final byte FLAG_NULL = 0;
	public static final byte FLAG_NOT_NULL = 1; //the first encounter flag.
	public static final byte FLAG_OBJECT = 2;

	/**  object reference write. When repeat write the same object, can optimize write the id of the object. */
	public static final byte FLAG_REFERENCE = -1;
	
//	@Deprecated
//	public abstract int register(Class<?> clazz);
	
	
	//protected IntObjectHashMap<Class<?>> nameId2Class = new IntObjectHashMap<Class<?>>();

	/**
	 * The method the caller need to call, not the {@link #writeName(Output, Class)};
	 * 
	 * @param output
	 * @param type
	 * @throws IOException
	 */
	@Deprecated
	public abstract void writeClass(Output output, Class<?> type)
			throws IOException;

	/**
	 * The Caller need call the method directly, and for efficiently need record the 
	 * previous read's name id and class instance, so subsequence read the same name id,
	 * so can return the recorded cached class instance directly, not to look up the table.
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	@Deprecated
	public abstract Class<?> readClass(Input input) throws IOException;
	
	
	public abstract void writeObject(Output out, Object obj) throws IOException;
	
	public abstract Object readObject(Input in) throws IOException;

	public abstract void reset();

	public abstract void setClassLoader(ClassLoader loader);

	public abstract ClassLoader getClassLoader();
	
	void addReference(Object obj);
	
	int getReference(Object obj);
	
	Object getReference(int index);

}