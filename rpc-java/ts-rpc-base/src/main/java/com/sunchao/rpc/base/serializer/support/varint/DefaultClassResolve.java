package com.sunchao.rpc.base.serializer.support.varint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sunchao.rpc.base.ReflectUtils;
import com.sunchao.rpc.base.serializer.support.varint.collection.IdentityObjectIntMap;
import com.sunchao.rpc.base.serializer.support.varint.collection.IntObjectHashMap;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Resolves classes by ID or by fully qualified class desc when
 * read/write in the wire stream.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class DefaultClassResolve implements ClassResolve {
	protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultClassResolve.class);
	protected Map<String, Integer> desc2NameId = new ConcurrentHashMap<String, Integer>(); //desc => class name id.
	protected List<String> descList = new ArrayList<String>(); 
	protected ClassLoader loader;
	//protected Map<String, Class<?>> name2Class ;
	private Class<?> memoizedClass; //cached previous read class.
	private int memoizedClassId;   // cached previous read class id.
	private IntObjectHashMap<Class<?>> nameId2Class = new IntObjectHashMap<Class<?>>();
	//private Map<Object, Integer> reference = new ConcurrentHashMap<Object, Integer>();
	//private IntObjectHashMap<Object> referenceId2Object = new IntObjectHashMap<Object>();
	private IdentityObjectIntMap<Object> referenceId2Object = new IdentityObjectIntMap<Object>();
	
	public DefaultClassResolve() {
		//primitive box class.
		register(void.class);
		register(Boolean.class);
		register(Byte.class);
		register(Short.class);
		register(Character.class);
		register(Integer.class);
		register(Float.class);
		register(Double.class);
		register(Long.class);
		//primitive array
		register(boolean[].class);
		register(byte[].class);
		register(char[].class);
		register(short[].class);
		register(int[].class);
		register(float[].class);
		register(double[].class);
		register(long[].class);
		
		//string
		register(String.class);
		register(String[].class);
		
		//collection
		register(ArrayList.class);
		register(LinkedList.class);
		register(HashSet.class);
		register(LinkedHashSet.class);
		//map
		register(HashMap.class);
		register(LinkedHashMap.class);
		//date
		register(Date.class);
		register(java.sql.Date.class);
		register(java.sql.Time.class);
		register(java.sql.Timestamp.class);
	}
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.support.varint.ClassRe#register(java.lang.Class)
	 */
	private int register(Class<?> clazz) {
		String desc = ReflectUtils.getDesc(clazz);
		int idx = descList.size();
		descList.add(desc);
	    desc2NameId.put(desc, idx);
	    nameId2Class.put(idx, clazz);
	    return idx;
	}
	
	public String getDesc(int nameId) {
		if (nameId < 0 || nameId >= descList.size()) return null;
		String desc = descList.get(nameId);
		if (desc == null) throw new IllegalStateException("Invalid class name id: " + nameId + " , cannot find the class desc.");
		return desc;
	}
	
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.support.varint.ClassRe#writeClass(com.sunchao.rpc.base.serializer.support.varint.Output, java.lang.Class)
	 */
	public void writeClass(Output output, Class<?> type) throws IOException {
		if (type == null) {
			output.writeVarIntNonZigZag(FLAG_NULL);
			return;
		}
		
		if (type == Object.class) {
			output.writeVarIntNonZigZag(FLAG_OBJECT);
			return;
		}
		
		Integer id = desc2NameId.get(ReflectUtils.getDesc(type));
		if (id == null) {
			 writeName(output, type); //first encounter.
		} else {
			output.writeVarIntNonZigZag(id.intValue() + 3); //0 and 1 ,2 there used in the specified flag, so we just from 3;
			//return true;
		}
	}
	
	/**
	 * First time encounter the class, so we need register the class and
	 * generate the class's name id, meanwhile we store the <name id, class> pair.
	 * subsequence we do not need write class desc again. 
	 * 
	 * @param output
	 * @param type
	 * @throws IOException
	 */
	protected void writeName(Output output, Class<?> type) throws IOException {
		output.writeVarIntNonZigZag(FLAG_NOT_NULL); // write the flag of first encounter. 
		Integer id = desc2NameId.get(ReflectUtils.getDesc(type));
		if (id != null) {
			output.writeVarIntNonZigZag(id.intValue() + 3);
			return;
		}
		//only write the class desc the first time encountered in object graph.
		int nameId = register(type);
		output.writeVarIntNonZigZag(nameId + 3); //first write the new nameId, and write the class desc, subsequence just only write the nameId.
		output.writeString(ReflectUtils.getDesc(type));  //write the class name
		//return tr;
		
	}
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.support.varint.ClassRe#readClass(com.sunchao.rpc.base.serializer.support.varint.Input)
	 */
	public Class<?> readClass(Input input) throws IOException {
		int classId = input.readVarIntNonZigZag();
		switch (classId) {
		case ClassResolve.FLAG_NULL :    //null
			return null;
		case ClassResolve.FLAG_OBJECT :  //object
			return Object.class;
		case ClassResolve.FLAG_NOT_NULL :  //not_null the first read, so need record the map <id, class>.
			return readName(input);
		}
		if (classId == memoizedClassId) return memoizedClass;
		Class<?> clazz = nameId2Class.get(classId - 3);
		if (clazz == null)
			throw new IOException("The invalid class name id: " + classId + ", which's class = null");
		
		memoizedClassId = classId; //record the previous read, when encounter the same name id , return the class directly.
		memoizedClass = clazz;
		return clazz;
	}
	
	
	protected Class<?> readName(Input input) throws IOException {
		int nameId = input.readVarIntNonZigZag(); //the name id, need look up the table .
		Class<?> clazz = nameId2Class.get(nameId - 3);
		if (clazz == null) {
			final String classDesc = input.readString();
			try {
				clazz = ReflectUtils.desc2class(getClassLoader(), classDesc);
			} catch (ClassNotFoundException e) {
				throw new IOException("Unable to find class, and the class descriptor: " + classDesc, e);
			}
			nameId2Class.put(nameId - 3, clazz);
		}
	    return clazz;
	}
	
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.support.varint.ClassRe#reset()
	 */
	public void reset() {
		if (desc2NameId != null) desc2NameId.clear();
		if (descList != null) descList.clear();
	}
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.support.varint.ClassRe#setClassLoader(java.lang.ClassLoader)
	 */
	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}
	
	/* (non-Javadoc)
	 * @see com.sunchao.rpc.base.serializer.support.varint.ClassRe#getClassLoader()
	 */
	public ClassLoader getClassLoader() {
		if (loader == null) {
			loader = getClass().getClassLoader();
		}
		return loader;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object readObject(Input in) throws IOException {
		Class clazz = readClass(in);
		if (clazz == null) 
			return null;
		if (clazz == Object.class)
			return new Object();
		Builder b = Builder.register(clazz);
		return b.parseFrom(in);
	}
	
	public void addReference(Object obj) {
		this.referenceId2Object.put(obj, this.referenceId2Object.size);
		//this.reference.put(obj, this.reference.size());
	}
	
	public int getReference(Object obj) {
		return this.referenceId2Object.get(obj, -1);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeObject(Output out, Object obj) throws IOException {
		if (obj == null) {
			out.writeVarIntNonZigZag(FLAG_NULL);
			return;
		}
		Class clazz = obj.getClass();
		if (clazz == Object.class) {
			out.writeVarIntNonZigZag(FLAG_OBJECT);
			return;
		}
		
		Integer id = desc2NameId.get(ReflectUtils.getDesc(clazz));
		if (id == null) {
			 writeName(out, clazz); //first encounter.
			 if (LOGGER.isInfoEnabled()) {
				 System.out.println("*******************************first write************************" );
			     System.out.println(clazz.getName());
			 }
		} else {
			out.writeVarIntNonZigZag(id.intValue() + 3); //0 and 1 , 2there used in the specified flag, so we just from 3;
			if (LOGGER.isInfoEnabled()) {
			      System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::" + descList.get(id));
			      System.out.println(id.intValue());
			}
		}
		Builder b = Builder.register(clazz);
		b.writeTo(obj, out);
	}

	public Object getReference(int index) {
		if (index < 0 || index > this.referenceId2Object.size) 
			return null;
		return referenceId2Object.findKey(index);
	}

/*	public Object getReference(int index) {
		
	}*/
}
