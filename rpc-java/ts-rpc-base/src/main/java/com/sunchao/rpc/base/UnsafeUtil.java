package com.sunchao.rpc.base;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.PrivilegedActionException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;
import sun.misc.Cleaner;
/**
 * Unsafe Utility. {@link sun.misc.Unsafe}
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@SuppressWarnings("restriction")
public final class UnsafeUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnsafeUtil.class);
	
	private static final sun.misc.Unsafe UNSAFE;
	public static final long     byteArrayBaseOffset;
	public static final long    floatArrayBaseOffset;
	public static final long   doubleArrayBaseOffset;
	public static final long      intArrayBaseOffset;
	public static final long     longArrayBaseOffset;
	public static final long    shortArrayBaseOffset;
	public static final long     charArrayBaseOffset;
	
	//Constructor to be used for creation of ByteBuffers that use preallocated memory regions.
	static Constructor<? extends ByteBuffer> directByteBufferConstructor;
	
	static {
		sun.misc.Unsafe tmpUnsafe = null;
		long    tmpByteArrayBaseOffset  =  0;
		long   tmpFloatArrayBaseOffset  =  0;
		long  tmpDoubleArrayBaseOffset  =  0;
		long     tmpIntArrayBaseOffset  =  0;
		long    tmpLongArrayBaseOffset  =  0;
		long   tmpShortArrayBaseOffset  =  0;
		long    tmpCharArrayBaseOffset  =  0;
		
		try {
			tmpUnsafe = getUnsafe();
			tmpByteArrayBaseOffset   = tmpUnsafe.arrayBaseOffset(byte[].class);
			tmpCharArrayBaseOffset   = tmpUnsafe.arrayBaseOffset(char[].class);
			tmpShortArrayBaseOffset  = tmpUnsafe.arrayBaseOffset(short[].class);
			tmpIntArrayBaseOffset    = tmpUnsafe.arrayBaseOffset(int[].class);
			tmpFloatArrayBaseOffset  = tmpUnsafe.arrayBaseOffset(float[].class);
			tmpLongArrayBaseOffset   = tmpUnsafe.arrayBaseOffset(long[].class);
			tmpDoubleArrayBaseOffset = tmpUnsafe.arrayBaseOffset(double[].class);
		} catch (Exception e) {
			LOGGER.warn("sun.misc.Unsafe is not accessible or not available");
		}
		
		byteArrayBaseOffset   = tmpByteArrayBaseOffset;
		charArrayBaseOffset   = tmpCharArrayBaseOffset;
		shortArrayBaseOffset  = tmpShortArrayBaseOffset;
		intArrayBaseOffset    = tmpIntArrayBaseOffset;
		floatArrayBaseOffset  = tmpFloatArrayBaseOffset;
		longArrayBaseOffset   = tmpLongArrayBaseOffset;
		doubleArrayBaseOffset = tmpDoubleArrayBaseOffset;
		UNSAFE = tmpUnsafe;
				
	}
	
	static {
		ByteBuffer buf = ByteBuffer.allocateDirect(1);
		try {
			directByteBufferConstructor = buf.getClass().getDeclaredConstructor(long.class, int.class, Object.class);
			directByteBufferConstructor.setAccessible(true);
		} catch (Exception e) {
			directByteBufferConstructor = null;
		}
	}
	
	/**
	 * Return the sun.misc.Unsafe object. If null is returned,
	 * no further Unsafe-related methods are allowed to be invoked from {@code UnsafeUtil}
	 * 
	 * @return
	 */
    public static final Unsafe unsafe() {
    	return UNSAFE;
    }
    
    /**
     * Sort the set of lists by their offsets from the object start address.
     * 
     * @param allFields
     * @return
     */
    public static Field[] sortFieldsByOffset(List<Field> allFields) {
    	Field[] allFieldsArray = allFields.toArray(new Field[0]);
    	
    	Comparator<Field> fieldOffsetComparator = new Comparator<Field>() {

			public int compare(Field f1, Field f2) {
				long offset1 = unsafe().objectFieldOffset(f1);
				long offset2 = unsafe().objectFieldOffset(f2);
				if (offset1 < offset2) return -1;
				if (offset1 == offset2) return 0;
				return 1;
			}	
    	};
    	
    	Arrays.sort(allFieldsArray, fieldOffsetComparator);
    	return allFieldsArray;
    }
	
    /**
     * Create a ByteBuffer that uses a provided (off-heap) memory region instead of 
     * allocating a new one.
     * 
     * @param address address of the memory region to be used for a {@link ByteBuffer}
     * @param size size of the memory region.
     * @return a new {@link ByteBuffer} that uses a provided memory region instead of allocating a new one
     */
	public static final ByteBuffer getDirectBufferAt(Long address, int size) {
		if (directByteBufferConstructor == null) 
			return null;
		try {
			return directByteBufferConstructor.newInstance(address, size, null);
		} catch (Exception e) {
			throw new RuntimeException("Cannot allocate ByteBuffer at a given address: " + address, e);
		}
	}
	
	/**
	 * Release a direct buffer.
	 * NOTE: If Cleaner is not accessible due to SecurityManager restrictions, reflection could
	 * be used to obtain the "clean" method and then invoke it.
	 * 
	 * @param nioBuffer
	 */
	public static void releaseBuffer(ByteBuffer nioBuffer) {
		if (nioBuffer != null && nioBuffer.isDirect()) {
			Object cleaner = ((sun.nio.ch.DirectBuffer) nioBuffer).cleaner();
			if (cleaner != null) 
				((sun.misc.Cleaner)cleaner).clean();
			nioBuffer = null;
		}
	}
	
	/**
	 * Return a sun.misc.Unsafe.
	 * 
	 * @return sun.misc.Unsafe.
	 */
	private static sun.misc.Unsafe getUnsafe() {
		try {
			return sun.misc.Unsafe.getUnsafe();
		} catch (SecurityException e) {}
		
		try {
			return java.security.AccessController.doPrivileged
					(new java.security.PrivilegedExceptionAction<sun.misc.Unsafe>(){
						public sun.misc.Unsafe run() throws Exception {
							Class<sun.misc.Unsafe> clazz = sun.misc.Unsafe.class;
							for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
								field.setAccessible(true);
								Object instance = field.get(null);
								if (clazz.isInstance(instance)) {
									return clazz.cast(instance);
								}
						    }
						throw new NoSuchFieldException("Non the Unsafe Field!");
					}});
		} catch (PrivilegedActionException e) {
			throw new RuntimeException("Could not initialize the Unsafe", 
					e.getCause());
		}
	}
	
	private UnsafeUtil() {}
	
}
