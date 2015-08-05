package com.sunchao.rpc.base;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sunchao.rpc.common.compiler.support.ClassHelper;
import com.sunchao.rpc.common.utils.ClassUtil;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 *  <i>?</i> <i>*</i> <i>+</i>
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class ReflectUtils {

	/**
	 * Java VM internal's primitive type.
	 * <NOTE>Long<J>, Boolean<Z></NOTE>
	 */
	private static final char VM_INTER_VOID = 'V';
	private static final char VM_INTER_BOOLEAN = 'Z';
	private static final char VM_INTER_BYTE = 'B';
	private static final char VM_INTER_CHAR = 'C';
	private static final char VM_INTER_SHORT = 'S';
	private static final char VM_INTER_INT = 'I';
	private static final char VM_INTER_FLOAT = 'F';
	private static final char VM_INTER_DOUBLE = 'D';
	private static final char VM_INTER_LONG = 'J';
	
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	
	/**
	 * @see Character #isJavaIdentifierPart(char).
	 * And the <code>regex</code> of the form => "(?:)" is Not-Capture-Group,
	 * which don't include in the match count and result match.
	 * {@link Matcher #matches()}
	 */
	public static final String JAVA_VALID_IDENTITY_REGEX = "(?:[_$a-zA-Z][_$a-zA-Z0-9]*)";
	
	public static final String JAVA_NAME_REGX = "(?:" + JAVA_VALID_IDENTITY_REGEX + "(?:\\." +JAVA_VALID_IDENTITY_REGEX + ")*)";
	
	/** e.g <code>"Ljava/lang/String;"</code> */
	public static final String CLASS_DESC = "(?:L" + JAVA_VALID_IDENTITY_REGEX + "(?:\\/" + JAVA_VALID_IDENTITY_REGEX + ")*;)";
	
	/** e.g <code>[I, [java/lang/String; [[java/lang/String;</code> */
	public static final String ARRAY_DESC = "(?:\\[+(?:(?:[VZBCDFIJS])|" + CLASS_DESC + "))";
	
	/** primitive | array | Object */
	public static final String DESC_REGEX = "(?:(?:[VZBCDFIJS])|" + CLASS_DESC + "|" + ARRAY_DESC + ")";
	
	public static final Pattern DESC_PATTERN = Pattern.compile(DESC_REGEX);
	
	/** methodName(xxxx) xxxx  */
	public static final String METHOD_DESC_REGEX = "(?:(" + JAVA_VALID_IDENTITY_REGEX + ")?\\((" + DESC_REGEX  + "*)\\)(" + DESC_REGEX + ")?)";
	
	public static final Pattern METHOD_DESC_PATTERN = Pattern.compile(METHOD_DESC_REGEX);
	
	public static final Pattern GETTER_METHOD_DESC_PATTERN = Pattern.compile("get([A-Z][_a-zA-Z0-9]*)\\(\\)(" + DESC_REGEX +")");
	
	public static final Pattern SETTER_METHOD_DESC_PATTERN = Pattern.compile("set([A-Z][_a-zA-Z0-9]*)\\((" + DESC_REGEX + ")\\)V");
	
	public static final Pattern IS_HAS_CAN_METHOD_DESC_PATTERN = Pattern.compile("(?:is|has|can)([A-Z][_a-zA-Z0-9]*)\\(\\)Z");
	
	private static final ConcurrentMap<String, Class<?>> DESC_CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();
	
	private static final ConcurrentMap<String, Class<?>> NAME_CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();
	
	private static final ConcurrentMap<String, Method> SIGNATURE_METHODS_CACHE = new ConcurrentHashMap<String, Method>();
	
	
	public static boolean isPrimitives(Class<?> cls) {
		if (cls.isArray()) {
			return isPrimitive(cls.getComponentType());
		}
		return isPrimitive(cls);
	}
	
	public static boolean isPrimitive(Class<?> cls) {
		return cls.isPrimitive() || cls == String.class || cls == Boolean.class
				|| cls == Character.class || Number.class.isAssignableFrom(cls)
				|| Date.class.isAssignableFrom(cls);
	}
	
	public static Class<?> getWrapper(Class<?> cls) {
		if (cls == int.class) 
			cls = Integer.class;
		else if (cls == boolean.class)
			cls = Boolean.class;
		else if (cls == byte.class)
			cls = Byte.class;
		else if (cls == char.class)
			cls = Character.class;
		else if (cls == short.class)
		    cls = Short.class;
		else if (cls == float.class)
			cls = Float.class;
		else if (cls == double.class)
			cls = Double.class;
		else if (cls == long.class)
			cls = Long.class;
		return cls;
	}
	
	public static boolean isCompatible(Class<?> cls, Object o) {
		boolean primitive = cls.isPrimitive();
		if (o == null) {
			return !primitive;
		}
		
		if (primitive) {
			if (cls == int.class)
				cls = Integer.class;
			else if (cls == byte.class)
				cls = Byte.class;
			else if (cls == char.class)
				cls = Character.class;
			else if (cls == short.class)
				cls = Short.class;
			else if (cls == boolean.class)
				cls = Boolean.class;
			else if (cls == float.class)
				cls = Float.class;
			else if (cls == double.class)
				cls = Double.class;
			else if (cls == long.class)
				cls = Long.class;
		} 
		if (cls == o.getClass())
			return true;
		return cls.isInstance(o);
	}
	
	public static boolean isComptible(Class<?>[] cs, Object[] os) {
		int len = cs.length;
		if (len != os.length) return false;
		if (len == 0) return true;
		for (int i = 0; i <len; i++) {
			if ( !isCompatible(cs[i], os[i])) {
				return false;
			}
		}
	    return true;
	}
	
	public static String getCodeBase(Class<?> cls) {
		if (cls == null)
			return null;
		ProtectionDomain domain = cls.getProtectionDomain();
		if (domain == null)
			return null;
		CodeSource source = domain.getCodeSource();
		if (source == null)
			return null;
		URL location = source.getLocation();
		if (location == null)
			return null;
		return location.getFile();
	}
	
	/**
	 * 
	 * Object[][] => Class Name. [[java.lang.Object; => java.lang.Object[][]
	 * @param cls
	 * @return
	 */
	public static String getName(Class<?> cls) {
		if (cls.isArray()) {
			StringBuilder sb = new StringBuilder();
			do {
				sb.append("[]");
				cls = cls.getComponentType();
			} while (cls.isArray());
			return cls.getName() + sb.toString();
		}
		return cls.getName();
	}
	
	public static Class<?> getGenericClass(Class<?> cls) {
		return getGenericClass(cls, 0);
	}
	
	public static Class<?> getGenericClass(Class<?> cls, int i) {
		ParameterizedType parameterizedType = (ParameterizedType) cls.getGenericInterfaces()[0];
		Object genericClass = parameterizedType.getActualTypeArguments()[i];
		if (genericClass instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) genericClass).getRawType();
		} else if (genericClass instanceof GenericArrayType) {
			return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
		} else {
			return (Class<?>) genericClass;
		}
	}
	/**
	 * e.g public java.lang.String java.lang.Object.toString();
	 *  "void do(int)", "void do()", "int do(java.lang.String, boolean)"
	 * 
	 * @param m
	 * @return
	 */
	public static String getName(final Method m) {
		StringBuilder sb = new StringBuilder();
		sb.append(getName(m.getReturnType())).append(' ');
		sb.append(m.getName()).append('(');
		Class<?>[] parameterTypes = m.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(getName(parameterTypes[i]));
		}
		sb.append(')');
		return sb.toString();
	}
	
	/**
	 * method signature contains two parts
	 * 1: the method name.
	 * 2: the argument name.
	 * don't contains the return value.
	 * 
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	public static String getSignature(String methodName, Class<?>[] parameterTypes) {
		StringBuilder sb = new StringBuilder(methodName);
		sb.append("(");
		if (parameterTypes != null && parameterTypes.length > 0) {
			boolean isFirst = true;
			for (Class<?> type : parameterTypes) {
				if (isFirst) {
					isFirst = false;
				} else {
				     sb.append(',');	
				}
				sb.append(type.getName());
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static String getName(final Constructor<?> c) {
		StringBuilder sb = new StringBuilder('(');
		Class<?>[] parameterTypes = c.getParameterTypes();
		for (int i = 0;  i < parameterTypes.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(getName(parameterTypes[i]));
		}
		sb.append(')');
		return sb.toString();
	}
	
	/**
	 * get the class desc.
	 * boolean[].class => [Z
	 * Object.class => Ljava/lang/Object;
	 * Object[].class => [Ljava/lang/Object;
	 * int => I
	 * 
	 * @param cls
	 * @return
	 */
	public static String getDesc(Class<?> cls) {
		StringBuilder sb =  new StringBuilder();
		while (cls.isArray()) {
			sb.append('[');
			cls = cls.getComponentType();
		}
		if (cls.isPrimitive()) {
			String t = cls.getName();
			if ("void".equals(t)) 
				sb.append(VM_INTER_VOID);
			else if ("int".equals(t))
				sb.append(VM_INTER_INT);
			else if ("byte".equals(t))
				sb.append(VM_INTER_BYTE);
			else if ("short".equals(t))
				sb.append(VM_INTER_SHORT);
			else if ("char".equals(t))
			    sb.append(VM_INTER_CHAR);
			else if ("float".equals(t))
				sb.append(VM_INTER_FLOAT);
			else if ("double".equals(t))
				sb.append(VM_INTER_DOUBLE);
			else if ("long".equals(t))
				sb.append(VM_INTER_LONG);
			else if ("boolean".equals(t))
				sb.append(VM_INTER_BOOLEAN);
		} else {
			sb.append('L');
			sb.append(cls.getName().replace('.', '/'));
			sb.append(';');
		}
		return sb.toString();
	}
	
	/**
	 * get the class array desc.
	 * [int.class, boolean[].class, Object.class] => I[ZLjava/lang/Object;
	 * 
	 * @param cs
	 *         the class array.
	 * @return
	 *         the desc
	 */
	public static String getDesc(final Class<?>[] cs) {
		if (cs.length == 0) 
			return "";
		StringBuilder sb = new StringBuilder();
		for (Class<?> cls : cs) {
			sb.append(getDesc(cls));
		}
		return sb.toString();
	}
	
	/**
	 * int do(int arg1) => do(I)I;
	 * void do (String arg1, boolean arg2) => do(Ljava/lang/String;Z)V
	 * @param m
	 * @return
	 */
	public static String getDesc(final Method m) {
		StringBuilder sb = new StringBuilder(m.getName()).append('(');
		Class<?>[] parameterTypes = m.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			sb.append(getDesc(parameterTypes[i]));
		}
		sb.append(')').append(getDesc(m.getReturnType()));
		return sb.toString();
	}
	
	/**
	 * constructor desc.
	 * "()V" ,"(Ljava/lang/String;I)V"
	 * @param c
	 * @return
	 */
	public static String getDesc(final Constructor<?> c) {
		StringBuilder sb = new StringBuilder('(');
		Class<?>[] parameterTypes = c.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			sb.append(getDesc(parameterTypes[i]));
		}
		sb.append(')').append('V');
		return sb.toString();
	}
	
	/**
	 * (I)I, ()V
	 * @param m
	 * @return
	 */
	public static String getDescWithoutMethodName(final Method m) {
		StringBuilder sb = new StringBuilder('(');
		Class<?>[] parameterTypes = m.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			sb.append(getDesc(parameterTypes[i]));
		}
		sb.append(')').append(getDesc(m.getReturnType()));
		return sb.toString();
	}
	
	/**
	 * javassist.
	 * 
	 * @param c
	 * @return
	 * @throws Exception 
	 */
	public static String getDesc(final CtClass c) throws Exception {
		StringBuilder sb =  new StringBuilder();
		if (c.isArray()) {
			sb.append('[');
			sb.append(getDesc(c.getComponentType()));
		} else if (c.isPrimitive()) {
			String t = c.getName();
			if ("void".equals(t)) 
				sb.append(VM_INTER_VOID);
			else if ("int".equals(t))
				sb.append(VM_INTER_INT);
			else if ("byte".equals(t))
				sb.append(VM_INTER_BYTE);
			else if ("short".equals(t))
				sb.append(VM_INTER_SHORT);
			else if ("char".equals(t))
			    sb.append(VM_INTER_CHAR);
			else if ("float".equals(t))
				sb.append(VM_INTER_FLOAT);
			else if ("double".equals(t))
				sb.append(VM_INTER_DOUBLE);
			else if ("long".equals(t))
				sb.append(VM_INTER_LONG);
			else if ("boolean".equals(t))
				sb.append(VM_INTER_BOOLEAN);
		} else {
			sb.append('L');
			sb.append(c.getName().replace('.', '/'));
			sb.append(';');
		}
		return sb.toString();
	}
	
	public static String getDesc(final CtMethod m) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(m.getName()).append('(');
		CtClass[] parameterTypes = m.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			sb.append(getDesc(parameterTypes[i]));
		}
		sb.append(')').append(m.getReturnType());
		return sb.toString();
	}
	
	public static String getDesc(final CtConstructor c) throws Exception {
		StringBuilder sb = new StringBuilder('(');
		CtClass[] parameterTypes = c.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			sb.append(getDesc(parameterTypes[i]));
		}
		sb.append(')').append('V');
		return sb.toString();
	}
	
	public static String getDescWithoutMethodName(final CtMethod m) throws Exception {
		StringBuilder sb = new StringBuilder('(');
		CtClass[] parameterTypes = m.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			sb.append(getDesc(parameterTypes[i]));
		}
		sb.append(')').append(getDesc(m.getReturnType()));
		return sb.toString();
	}
	
	public static Class<?> forName(String name) {
		try {
			return name2class(name);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Not found class " + name + ", cause: " + e.getMessage(), e);
		}
	}
	
	public static Class<?> name2class(String name) throws ClassNotFoundException {
		return name2class(ClassUtil.getClassLoader(), name);
	}
	
	/**
	 * name => class.
	 * 
	 * boolean => boolean.class
	 * java.util.Map[][] => java.util.Map[][].class.
	 * @param loader
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static Class<?> name2class(ClassLoader loader, String name) throws ClassNotFoundException {
		int c = 0, index = name.indexOf('[');
		if (index > 0) {
			c = (name.length() - index) >> 1;
		    name = name.substring(0, index);
		} 
		if (c > 0) {
			StringBuilder sb = new StringBuilder();
			while (c-- > 0) {
				sb.append('[');
			}
			if ("void".equals(name)) 
				sb.append(VM_INTER_VOID);
			else if ("int".equals(name))
				sb.append(VM_INTER_INT);
			else if ("byte".equals(name))
				sb.append(VM_INTER_BYTE);
			else if ("short".equals(name))
				sb.append(VM_INTER_SHORT);
			else if ("char".equals(name))
			    sb.append(VM_INTER_CHAR);
			else if ("float".equals(name))
				sb.append(VM_INTER_FLOAT);
			else if ("double".equals(name))
				sb.append(VM_INTER_DOUBLE);
			else if ("long".equals(name))
				sb.append(VM_INTER_LONG);
			else if ("boolean".equals(name))
				sb.append(VM_INTER_BOOLEAN);
			else sb.append('L').append(name).append(';'); // "java.lang.Object" => "Ljava.lang.Object;"
			name = sb.toString();
		}
		else {
			if ("void".equals(name)) 
				return void.class;
			else if ("int".equals(name))
				return int.class;
			else if ("byte".equals(name))
				return byte.class;
			else if ("short".equals(name))
				return short.class;
			else if ("char".equals(name))
			    return char.class;
			else if ("float".equals(name))
				return float.class;
			else if ("double".equals(name))
				return double.class;
			else if ("long".equals(name))
				return long.class;
			else if ("boolean".equals(name))
				return boolean.class;
		}
		if (loader == null) 
			loader = ClassUtil.getClassLoader();
		Class<?> clazz = NAME_CLASS_CACHE.get(name);
		if (clazz == null) {
			clazz = Class.forName(name, true, loader);
			NAME_CLASS_CACHE.put(name, clazz);
		}
		return clazz;
	}
	
	
	/**
	 * desc to class
	 * [Z  => boolean[].class
	 * [[Ljava/util/Map; => java.util.Map[][].class
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	
	public static Class<?> desc2class(String desc) throws ClassNotFoundException {
		return desc2class(ClassUtil.getClassLoader(), desc);
	}
	
	/**
	 * Just loop up and search the class, if not exists return null,
	 * Non <code>Class.forName</code>
	 * @param desc
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?> desc2ClassNonBuild(String desc) throws ClassNotFoundException {
		switch (desc.charAt(0)) {
		case VM_INTER_BOOLEAN : return boolean.class;
		case VM_INTER_BYTE    : return byte.class;
		case VM_INTER_CHAR    : return char.class;
		case VM_INTER_DOUBLE  : return double.class;
		case VM_INTER_FLOAT   : return float.class;
		case VM_INTER_INT     : return int.class;
		case VM_INTER_LONG    : return long.class;
		case VM_INTER_SHORT   : return short.class;
		case VM_INTER_VOID    : return void.class;
		case 'L' :
			desc = desc.substring(1, desc.length() - 1).replace('/', '.');// "Ljava/lang/Object;" ==> "java.lang.Object";
			break;
		case '[' :
			desc = desc.replace('/', '.');//"[[Ljava/lang/Object;" => "[[Ljava.lang.Object;"
			break;
		default  :
			throw new ClassNotFoundException("Class not found: " + desc);
		}
		
		Class<?> clz = DESC_CLASS_CACHE.get(desc);
		return clz;
	}
	
	/**
	 * desc to class.
	 * 
	 * @param loader
	 * @param desc
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?> desc2class(ClassLoader loader, String desc) throws ClassNotFoundException {
		switch (desc.charAt(0)) {
		case VM_INTER_BOOLEAN : return boolean.class;
		case VM_INTER_BYTE    : return byte.class;
		case VM_INTER_CHAR    : return char.class;
		case VM_INTER_DOUBLE  : return double.class;
		case VM_INTER_FLOAT   : return float.class;
		case VM_INTER_INT     : return int.class;
		case VM_INTER_LONG    : return long.class;
		case VM_INTER_SHORT   : return short.class;
		case VM_INTER_VOID    : return void.class;
		case 'L' :
			desc = desc.substring(1, desc.length() - 1).replace('/', '.');// "Ljava/lang/Object;" ==> "java.lang.Object";
			break;
		case '[' :
			desc = desc.replace('/', '.');//"[[Ljava/lang/Object;" => "[[Ljava.lang.Object;"
			break;
		default  :
			throw new ClassNotFoundException("Class not found: " + desc);
		}
		if (loader == null) 
			loader = ClassUtil.getClassLoader();
		Class<?> clz = DESC_CLASS_CACHE.get(desc);
		if (clz == null) {
			clz = Class.forName(desc, true, loader);
			DESC_CLASS_CACHE.put(desc, clz);
		}
		return clz;
	}
	
	public static Class<?>[] desc2classArray(String desc) throws ClassNotFoundException {
		Class<?>[] cls = desc2classArray(ClassUtil.getClassLoader(), desc);
		return cls;
	}
	
	private static Class<?>[] desc2classArray(ClassLoader loader, String desc) throws ClassNotFoundException {
		if (desc.length() == 0 || desc == null) 
			return EMPTY_CLASS_ARRAY;
		List<Class<?>> cs =  new ArrayList<Class<?>>();
		Matcher m = DESC_PATTERN.matcher(desc);
		while (m.find()) {
			cs.add(desc2class(loader, m.group()));
		}
		return cs.toArray(EMPTY_CLASS_ARRAY);
	}
	
	/**
	 * use the method signature to look up the method.
	 * @param clazz    the lookup class.
	 * @param methodname
	 *                 the method name.
	 * @param parameterTypes
	 *                 the parameter type string array.
	 * @return
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public static Method findMethodByMethodSignature(Class<?> clazz, String methodname, 
			String[] parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
		String signature = methodname;
		StringBuilder sb = new StringBuilder();
		if (parameterTypes != null && parameterTypes.length > 0) {
			for (int i = 0; i < parameterTypes.length; i++) {
				sb.append(parameterTypes[i]);
			}
		}
		signature += sb.toString();
		Method method = SIGNATURE_METHODS_CACHE.get(signature);
		if (method != null) {
			return method;
		}
		if (parameterTypes == null) {
			List<Method> finded = new ArrayList<Method>();
			for (Method m : clazz.getMethods()) {
				if (m.getName().equals(methodname)) {
					finded.add(m);
				}
			}
			if (finded.isEmpty()) {
					throw new NoSuchMethodException("No such method " + methodname + " in class " + clazz);
			}
			if (finded.size() > 1) {
				String msg = String.format("Not unique method for method name (%s) in class (%s), find %d methods.",
						methodname, clazz.getName(), finded.size());
				throw new IllegalStateException(msg);
			}
			method = finded.get(0);
		} else {
			Class<?>[] types = new Class<?>[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				types[i] = ReflectUtils.name2class(parameterTypes[i]);
			}
			method = clazz.getMethod(methodname, types);
		}
			
		SIGNATURE_METHODS_CACHE.put(signature, method);
		return method;
	}
	
	public static Constructor<?> findConstructor(Class<?> clazz, Class<?> parameterType) throws NoSuchMethodException {
		Constructor<?> targetConstructor;
		try {
			targetConstructor = clazz.getConstructor(new Class<?>[]{parameterType});
		} catch (NoSuchMethodException e) {
			targetConstructor = null;
			Constructor<?>[] constructors = clazz.getConstructors();
			for (Constructor<?> c : constructors) {
				if (Modifier.isPublic(c.getModifiers())
					&& c.getParameterTypes().length == 1
					&& c.getParameterTypes()[0].isAssignableFrom(parameterType)) {
						targetConstructor = c;
						break;
					}
			}
			if (targetConstructor == null)
				throw e;
		}
		return targetConstructor;
	}
	
	public static boolean isInstance(Object obj, String interfaceClassName) {
		for (Class<?> clazz = obj.getClass(); clazz != null && ! clazz.equals(Object.class);
				clazz = clazz.getSuperclass()) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> c : interfaces) {
				if (c.getName().equals(interfaceClassName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Object getEmptyObject(Class<?> returnType) {
		return getEmptyObject(returnType, new HashMap<Class<?>, Object>(), 0);
	}
	
	public static Object getEmptyObject(Class<?> returnType, Map<Class<?>, Object> emptyInstances, int level) {
		if (level > 2) 
			return null;
		if (returnType == null) {
			return null;
		} else if (returnType == boolean.class || returnType == Boolean.class) {
			return false;
		} else if (returnType == char.class || returnType == Character.class) {
			return '\0';
		} else if (returnType == byte.class || returnType == Byte.class) {
			return (byte) 0;
		} else if (returnType == short.class || returnType == Short.class) {
			return (short) 0;
		} else if (returnType == int.class || returnType == Integer.class) {
			return 0;
		} else if (returnType == float.class || returnType == Float.class) {
			return 0F;
		} else if (returnType == double.class || returnType == Double.class) {
			return 0D;
		} else if (returnType == long.class || returnType == Long.class) {
			return 0L;
		} else if (returnType.isArray()) {
			return Array.newInstance(returnType.getComponentType(), 0);
		} else if (returnType.isAssignableFrom(ArrayList.class)) {
			return new ArrayList<Object>();
		} else if (returnType.isAssignableFrom(HashSet.class)) {
			return new HashSet<Object>();
		} else if (returnType.isAssignableFrom(HashMap.class)) {
			return new HashMap<Object, Object>();
		} else if (String.class.equals(returnType)) {
			return "";
		} else if (! returnType.isInterface()) {
			try {
				Object value = emptyInstances.get(returnType);
				if (value == null) {
					value = returnType.newInstance();
					emptyInstances.put(returnType, value);
				}
				Class<?> cls = value.getClass();
				while (cls != null && cls != Object.class) {
					Field[] fields = cls.getDeclaredFields();
					for (Field field : fields) {
						Object property = getEmptyObject(field.getType(), emptyInstances, level + 1) ;
						if (property != null) {
							try {
								if (! field.isAccessible()) {
									field.setAccessible(true);
								}
								field.set(value, property);
							} catch (Throwable e) {
								
							}
						}
					}
					cls = cls.getSuperclass();
				}
				return value;
			} catch (Throwable e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, Exception {
		Boo boo = new Boo();
		System.out.println(getGenericClass(boo.getClass()));
		Goo goo = new Goo();
		System.out.println(getGenericClass(goo.getClass()));
		Zoo zoo = new Zoo();
		System.out.println(getGenericClass(zoo.getClass()));
		
	}
	//ArrayList<Set<String>[][]> arraysetarray = new ArrayList<Set<String>[][]>();
}

/****************************************************************************************/
  interface Foo<T extends Foo<T>> {
	 T get() ;
 }
  class Boo implements Foo<Boo> {

	public Boo get() {
		return null;
	}  
  }
  
  class Goo implements Foo<Boo> {

	public Boo get() {
		// TODO Auto-generated method stub
		return null;
	}  
  }
  
  interface Qoo<Foo> {
	  Foo get();
  }
  
  class Zoo implements Qoo<Foo<Boo>> {

	public Boo get() {
		// TODO Auto-generated method stub
		return null;
	}  
  }
  
/*  abstract class GenericBase<T extends Comparable<? super T>> {
	  
	  private Class<T> cls;
	  
	  @SuppressWarnings("unchecked")
	  public GenericBase() {
		  ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
		  cls = (Class<T>) superClass.getActualTypeArguments()[0];
	  }
	  
	  public void setClazz(Class<T> cls) {
		  this.cls = cls;
	  }
	  
	  public Class<T> getClazz() {
		  return this.cls;
	  }
  }
  
  class GenericDerived extends GenericBase<String> {
	  public static void main(String args[]) {
		  GenericDerived gd = new GenericDerived();
		  System.out.println(gd.getClazz());
	  }
  }*/
  
 
