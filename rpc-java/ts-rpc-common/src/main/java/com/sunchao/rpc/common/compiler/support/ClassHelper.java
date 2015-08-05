package com.sunchao.rpc.common.compiler.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassHelper utility.
 */
public class ClassHelper {
	/** the suffix of class file  */
	public static final String CLASS_EXTENSION = ".class";
	/** the suffix of java file */
	public static final String JAVA_EXTENSION = ".java";
	
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static Object newInstance(String name) {
		try {
			return  forName(name).newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	/**
	 *for the jdk <code>Class.forName(String className)</code>
	 *can't pass the primitive type. e.g. [int, boolean , void.]..
	 *,so if the class is array type, it will be loaded, but no initialize
	 *the component type class . 
	 * @param className
	 *           the string class name.
	 * @return
	 *           the class instance.
	 */
	public static Class<?> forName(String className) {
		try {
			return _forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param packages
	 *            the package name string array.
	 * @param className
	 *            the class name.
	 * @return
	 *            the class instance.
	 */
	public static Class<?> forName(String[] packages, String className) {
		try {
			return _forName(className);
		} catch (ClassNotFoundException e) {
			if (packages != null && packages.length > 0) {
				for (String pkg : packages) {
					try {
						return _forName(pkg + "." + className);
					} catch (ClassNotFoundException e1) {
						
					}
				}
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	/**
	 * return the class object.
	 * the class core.
	 * @param className
	 *            the class name.
	 * @return
	 *            the class object
	 * @throws ClassNotFoundException
	 */
	public static Class<?> _forName(String className) throws ClassNotFoundException {
		if ("boolean".equals(className))
			return boolean.class;
		else if ("byte".equals(className))
			return byte.class;
		else if ("char".equals(className))
			return char.class;
		else if ("short".equals(className))
			return short.class;
		else if ("int".equals(className))
			return int.class;
		else if ("float".equals(className))
			return float.class;
		else if ("double".equals(className))
			return double.class;
		else if ("long".equals(className))
			return long.class;
		else if ("void".equals(className))
			return void.class;
		else if ("boolean[]".equals(className))
			return boolean[].class;
		else if ("byte[]".equals(className))
			return byte[].class;
		else if ("char[]".equals(className))
			return byte[].class;
		else if ("short[]".equals(className))
			return short[].class;
		else if ("int[]".equals(className))
			return int[].class;
		else if ("float[]".equals(className))
			return float[].class;
		else if ("double[]".equals(className))
			return double[].class;
		else if ("long[]".equals(className))
			return long[].class;
		
		try {
			return arrayForName(className);// get the component type of array .
		} catch (ClassNotFoundException e) {
			if (className.indexOf('.') == -1) { // has no packages name.
				try {
					return arrayForName("java.lang." + className);//e.g. name = "String";
				} catch (ClassNotFoundException e2) {
				}
			}
			throw e;
		}
		
	}

	/**
	 * call the Class.forName() to load the class.
	 * @param className
	 *          the class string name.
	 * @return
	 *         the class instance.
	 * @throws ClassNotFoundException
	 */
	private static Class<?> arrayForName(String className) throws ClassNotFoundException {
		return Class.forName(className.endsWith("[]")
				? "[L" + className.substring(0, className.length() - 2) + ";"
			    : className, true, Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * get  the wrapper class of primitive
	 *           types.
	 * @param clazz
	 *          the primitive class
	 * @return
	 *          the wrapper class.
	 */
	public static Class<?> getWrapperClass(Class<?> clazz) {
			if (clazz == boolean.class) {
				return Boolean.class;
			} else if (clazz == byte.class) {
				return Byte.class;
			} else if (clazz == char.class) {
				return Character.class;
			} else if (clazz == short.class) {
				return Short.class;
			} else if (clazz == int.class) {
				return Integer.class;
			} else if (clazz == float.class) {
				return Float.class;
			} else if (clazz == double.class) {
				return Double.class;
			} else if (clazz == long.class) {
				return Long.class;
			} else {
			    return clazz;
		    }
	}
	
	public static Boolean wraper(boolean z) {
		return Boolean.valueOf(z);
	}
	
	public static Character wraper(char c) {
		return Character.valueOf(c);
	}
	
	public static Short wraper(short s) {
		return Short.valueOf(s);
	}
	
	public static Byte wraper(byte b) {
		return Byte.valueOf(b);
	}
	
	public static Integer wraper(int i) {
		return Integer.valueOf(i);
	}
	
	public static Float wraper(float f) {
		return Float.valueOf(f);
	}
	
	public static Double wraper(double d) {
		return Double.valueOf(d);
	}
	
	public static Long wraper(long j) {
		return Long.valueOf(j);
	}
	
	public static Object wraper(Object o) {
		return o;
	}
	
	/**
	 * if argument is null,
	 * return the corresponding default primitive value.
	 * 
	 * @param Z
	 *       the wrapper.
	 * @return
	 *       the corresponding primitive value.
	 */
	public static boolean unWraper(Boolean Z) {
		return Z == null ? false : Z.booleanValue();
	}
	
	public static byte unWraper(Byte B) {
		return B == null ? 0 : B.byteValue();
	}
	
	public static char unWraper(Character C) {
		return C == null ? '\0' : C.charValue();
	}
	
	public static short unWraper(Short S) {
		return S == null ? 0 : S.shortValue();
	}
	
	public static int unWraper(Integer I) {
		return I == null ? 0 : I.intValue();
	}
	
	public static float unWraper(Float F) {
		return F == null ? 0 : F.floatValue();
	}
	
	public static double unWraper(Double D) {
		return D == null ? 0 : D.doubleValue();
	}
	
	public static long unWraper(Long J) {
		return J == null ? 0 : J.longValue();
	}
	
	public static Object unWraper(Object O) {
		return O;
	}
	
	public static boolean isNotEmpty(Object obj) {
		return getSize(obj) > 0;
	}
	
	public static int getSize(Object obj) {
		if (obj == null) {
			return 0;
		} else if (obj instanceof Collection<?>) {
			return ((Collection<?>) obj).size();
		} else if (obj instanceof Map<?, ?>) {
			return ((Map<?, ?>) obj).size();
		} else if (obj.getClass().isArray()) {
			return Array.getLength(obj);
		} else {
			return -1;
		}
	}
	
	public static URI toURI(String name) {
		try {
			return new URI(name);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Class<?> getGenericClass(Class<?> cls) {
		return getGenericClass(cls, 0);
	}
	
	/**get superclass or interfaces generic type.
	 * 
	 * @param cls
	 *          the current class.
	 * @param i
	 *          the index.
	 * @return
	 *          the type of generic.
	 */
	public static Class<?> getGenericClass(Class<?> cls, int i) {
		
	 try{	
		ParameterizedType parameteredType = (ParameterizedType) cls.getGenericInterfaces()[0];
		Type genericClass = parameteredType.getActualTypeArguments()[i];
		if (genericClass instanceof ParameterizedType) { // nested generic  e.g.      Interface<Map<K,V>>;
			return (Class<?>)((ParameterizedType) genericClass).getRawType();
		} else if (genericClass instanceof GenericArrayType) {  // generic array
			return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
		} else if (genericClass != null) {
			return (Class<?>) genericClass;      // return ;
		}
	 } catch (Throwable t) {
	 }
	 if (cls.getSuperclass() != null) { // fetch the generic from the super class.
		 return getGenericClass(cls.getSuperclass(), 0);
	 } else {
		 throw new IllegalArgumentException(cls.getName() + " generic undefined!");
	 }
			
	}
	
	/**
	 * javassist version request.
	 * @param javaVersion
	 * @return
	 */
	public static boolean isBeforeJava5(String javaVersion) {
		return (javaVersion == null || javaVersion.length() == 0 || "1.0".equals(javaVersion) 
				|| "1.1".equals(javaVersion) || "1.2".equals(javaVersion) 
				|| "1.3".equals(javaVersion) || "1.4".equals(javaVersion) );
	}
	
	public static boolean isBeforeJava6(String javaVersion) {
		return isBeforeJava5(javaVersion) || "1.5".equals(javaVersion);
	}
	
	/**
	 * the  string writer and  print writer
	 * combine used
	 * @param e
	 * @return
	 */
	public static String toString(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.print(e.getClass().getName() + ": ");
		if (e.getMessage() != null) {
			pw.print(e.getMessage() + "\n");
		}
		pw.println();
		try {
			e.printStackTrace(pw);
			return pw.toString();
		} finally {
			pw.close();
		}
	}
	
	/**  the length binary code array  limit   */
	private static final int JIT_LIMIT = 5 * 1024;
	
	
	public static void checkByteCode(String name, byte[] bytecode) {
		if (bytecode.length > JIT_LIMIT) {
			System.err.println("the template bytecode too long, may be affect the JIT compiler. template class : " + name);
		}
	}
	
	/**
	 * get the method about size.
	 * e.g. size(). length(), getSize(),
	 * getLength(). 
	 * @param cls
	 *          the searched class.
	 * @return
	 *          the method string name().
	 */
	public static String getSizeMethod(Class<?> cls) {
		try {
			return  cls.getMethod("size", new Class<?>[0]).getName() + "()";
		} catch (NoSuchMethodException e) {
			try {
				return cls.getMethod("length", new Class<?>[0]).getName() + "()";
			} catch (NoSuchMethodException e1) {
				  try {
					return cls.getMethod("getSize", new Class<?>[0]).getName() + "()";
				} catch (NoSuchMethodException e2) {
					    try {
							return cls.getMethod("getLength", new Class<?>[0]).getName() + "()";
						} catch (NoSuchMethodException e3) {
							return null;
						} 
				} 
			} 
		} 
	}
	
	/**
	 * get method name + (method parameters desc) string.
	 * 
	 * @param method
	 *         the method instance.
	 * @param parameterClasses
	 *         the specified method parameter
	 *         classes.
	 * @param rightCode
	 *         the method arguments desc in the ().
	 * @return
	 */
	public static String getMethodName(Method method, Class<?>[] parameterClasses, String rightCode) {
		if (method.getParameterTypes().length > parameterClasses.length) {
			Class<?>[] types = method.getParameterTypes();
			StringBuilder sb = new StringBuilder(rightCode);
			for (int i = parameterClasses.length; i < types.length; i++) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				Class<?> type = types[i];
				String def;
				if (type == boolean.class) {
					def = "false";
				} else if (type == char.class) {
					def = "\'\\0\'"; // '\0' =>  "\'\\0\'"
				} else if (type == short.class 
						|| type == byte.class
						|| type == int.class
						|| type == float.class
						|| type == double.class
						|| type == long.class) {
					def = "0";
				} else {
					def = "null";
				}
				sb.append(def);
			}
		}
		return method.getName() + "(" + rightCode + ")";
	}
	
	/**
	 * search the method in the current class (or super class, interfaces)
	 * .  according to the method name, and the method argument types
	 * @param currentClass
	 *              the current class.
	 * @param name
	 *              the method name.
	 * @param parameterTypes
	 *               the parameter classes (Class<?>[])
	 * @return
	 *               the method instance 
	 * @throws NoSuchMethodException
	 *               if the method is not existed ,throw e.
	 */
	public static Method searchMethod(Class<?> currentClass, String name, Class<?>[] parameterTypes) throws NoSuchMethodException {
		if (currentClass == null) {
			throw new NoSuchMethodException("class == null");
		}
		try {
			return currentClass.getMethod(name, parameterTypes);// can,t get the <init> <clinit> method.
		} catch (NoSuchMethodException e) {
			for (Method method : currentClass.getMethods()) {//getMethods() = > get the class or superclass, interface declare the methods
				if (method.getName().equals(name)    // with the modifier "public"
						&& parameterTypes.length == method.getParameterAnnotations().length
						&& Modifier.isPublic(method.getModifiers())) {
					if (parameterTypes.length > 0) {
						Class<?>[] types = method.getParameterTypes();
						boolean match = true;
						for (int i = 0; i < parameterTypes.length; i++) {
							if (! types[i].isAssignableFrom(parameterTypes[i])) {
								match = false;
								break;
							}
						}
						if (! match) {
							continue;
						}
					}
					return method;
				}
			}
			throw e;
		}
	}
	
	/**
	 * get the default value of primitive, object types;
	 * boolean /= > false;
	 * char  = > '\0'
	 * long, double,int,...= > 0;
	 * object = > null;
	 * @param type
	 *        the argument type
	 * @return
	 *        the default value string.
	 */
	public static String getInitCode(Class<?> type) {
		if (byte.class.equals(type)
				|| short.class.equals(type)
				|| int.class.equals(type)
				|| long.class.equals(type)
				|| float.class.equals(type)
				|| double.class.equals(type)) {
			return "0";
		} else if (char.class.equals(type)) {
			return "\'\\0\'";
		} else if (boolean.class.equals(type)) {
			return "false";
		} else {
			return "null";
		}
		
	}
	
	/**
	 * 
	 * @param entries
	 * @return
	 */
	public static <K,V> Map<K,V> toMap(Map.Entry<K, V>[] entries) {
		Map<K,V> map = new HashMap<K,V>();
		if (entries != null && entries.length > 0) {
			for (Map.Entry<K, V> entry : entries) {
				map.put(entry.getKey(), entry.getValue());
			}
				
		}
		return map;
	}
	
	private  ClassHelper() {
		
	}
}
 