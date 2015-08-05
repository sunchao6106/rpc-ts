package com.sunchao.rpc.common.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sunchao.rpc.common.annotation.Utility;

@Utility("classutil")
public class ClassUtil {
	
	public static Class<?> forNameWithThreadContextClassLoader(String name) 
			throws ClassNotFoundException {
		return forName(name, Thread.currentThread().getContextClassLoader());
	}
	
	public static Class<?> forNameWithCallerClassLoader(String name, Class<?> caller) 
			throws ClassNotFoundException {
		return forName(name, caller.getClassLoader());
	}
	
	public static ClassLoader getCallerClassLoader(Class<?> caller) {
		return caller.getClassLoader();
	}
	/**
	 * get class loader.
	 * class.getClassLoader();
	 * jdk document explain : if the class is primitive or void ,
	 * will return null, 
	 * @param cls 
	 *          the class argument.
	 * @return
	 *          the class loader.
	 */
	public static ClassLoader getClassLoader(Class<?> cls)
	{
		ClassLoader cl = null;
		try {
		    cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable t) {
			// Cannot access thread context ClassLoader - falling back to get System ClassLoader....
		}
		if (cl == null)
		{   // No thread context class loader -> use class loader of this class.
			cl = cls.getClassLoader();
		}
		
		return cl;
	}
	
	/**
	 * Return the default ClassLoader to use: typically the thread context 
	 * ClassLoader, if available; the ClassLoader that loaded the ClassUtil
	 * class will be used as fallback.
	 * <p>
	 * Call this method if you intent to use the thread context ClassLoader in
	 * a scenario where you absolutely need a not-null ClassLoader reference:
	 * example, for class path resource loading (but not necessarily for <code>
	 * Class.forName</code>,which accepts a <code>null</code> ClassLoader reference
	 * as well). that will use bootstrap class loader to load the class.
	 * </P>
	 * 
	 *
	 * @return
	 *         the default ClassLoader
	 */
	public static ClassLoader getClassLoader() {
		return getClassLoader(ClassUtil.class);
	}
	
	/**
     * Same as <code>Class.forName()</code>, except that it works for primitive
     * types.
     */
	public static Class<?> forName(String name) throws ClassNotFoundException
	{
		return forName(name, getClassLoader());
	}
	 /**
     * Replacement for <code>Class.forName()</code> that also returns Class
     * instances for primitives (like "int") and array class names (like
     * "String[]").
     * 
     * @param name 
     *          the name of the Class
     * @param classLoader 
     *          the class loader to use (may be <code>null</code>,
     *          which indicates the default class loader)
     * @return Class 
     *           instance for the supplied name
     * @throws ClassNotFoundException 
     *           if the class was not found
     * @throws LinkageError 
     *           if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
	public static Class<?> forName(String name, ClassLoader cl) throws ClassNotFoundException
	{
		Class<?> clazz = resolvePrimitiveClassName(name);
		if (clazz != null) {
			return clazz;
		}
		//"java.lang.String[]" styple arrays.
		if (name.endsWith(ARRAY_SUFFIX)) {
			String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
			Class<?> eleClass  =  forName(elementClassName,cl);
			return Array.newInstance(eleClass, 0).getClass();// the reflect utility of array.
		}
		//"[Ljava.lang.String;" style arrays;
		int internaleArrayMarker = name.indexOf(INTERNAL_ARRAY_PREFIX);
		if (internaleArrayMarker != -1 && name.endsWith(";")) {
			String elementClassName = null;
			if (internaleArrayMarker == 0) {
				elementClassName = name.
						substring(INTERNAL_ARRAY_PREFIX.length(), name.length()-1);
				
			} else if (name.startsWith("[")) {
				elementClassName = name.substring(1);
			}
			Class<?> elementClass = forName(elementClassName,cl);// recursive call .
			return Array.newInstance(elementClass, 0).getClass();
		}
		
		ClassLoader classLoaderToUse  = cl;
		if (classLoaderToUse == null)
			classLoaderToUse = getClassLoader();
		return classLoaderToUse.loadClass(name);
	}
	
	/**
	 * Resolve the give class name as primitive class, if appropriate,
	 * according to the JVM's a naming rules for primitive classes.
	 * <p>
	 * Also supports JVM's internal class names for primitive arrays.
	 * Dose <i>not</i> support the '[]' suffix notation for primitive arrays;
	 * only supported by {@link #forName}.
	 * </p>
	 * 
	 * @param name 
	 *           the name of the potentially primitive class.
	 * @return
	 *           the primitive class, or <code>null</code> if the name does denote
	 *           a primitive class or primitive array class.
	 */
	public static Class<?> resolvePrimitiveClassName(String name)
	{
		// Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
		Class<?> result = null;
		if (name != null && name.length() <= 8)
			result = PRIMITIVE_TYPE_NAME_MAP.get(name);
		return result;
	}
	
	
	/**
	 * Suffix for array class names:"[]"
	 */
	public static final String ARRAY_SUFFIX = "[]";
	/**
	 * Prefix for internal array class names: "[L"
	 */
	private static final String INTERNAL_ARRAY_PREFIX = "[L";
	/**
	 * Map with primitive type name as key and corresponding primitive type as value,
	 * e.g. "int" => "int.class".
	 */
	private static final Map<String, Class<?>> PRIMITIVE_TYPE_NAME_MAP =
			new HashMap<String, Class<?>>(16);
	/**
	 * Map with primitive wrapper type as key and corresponding primitive type as value, 
	 *e.g. Integer.class  = >  int.class
	 */
	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = 
			new HashMap<Class<?>, Class<?>>(8);
		
	static {
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, char.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, short.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, int.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, float.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, double.class);
		PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, long.class);
		
		Set<Class<?>> primitiveTypeNames = new HashSet<Class<?>>(16);
		primitiveTypeNames.addAll(PRIMITIVE_WRAPPER_TYPE_MAP.values());
		primitiveTypeNames.addAll(Arrays.
				asList(new Class<?>[]{ boolean[].class, byte[].class, char[].class, short[].class,
						int[].class, float[].class, double[].class, long[].class}));//16 contains the 8 digit type and the 8 array type.
		// 8 digit primitive type  : class.getName () =>  int.class . int ..........
		// 8 digit primitive array type, class.getName() => boolean[].class = > [Z,[I,[J
		// String[].class .getName() [Ljava.lang.String; object Lxxxx.xxx.;
		
		for (Iterator<Class<?>> it = primitiveTypeNames.iterator(); it.hasNext();)
		{
			Class<?> primitiveClass = it.next();
			PRIMITIVE_TYPE_NAME_MAP.put(primitiveClass.getName(), primitiveClass);
		}
			
	}
	
	public static String toShortString(Object obj)
	{
		if (obj == null) return "null";
		return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
	}


}
