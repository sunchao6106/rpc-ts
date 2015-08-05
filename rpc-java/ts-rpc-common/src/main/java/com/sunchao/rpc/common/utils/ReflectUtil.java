package com.sunchao.rpc.common.utils;

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
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;


import com.sunchao.rpc.common.annotation.Utility;

/**
 * <p>
 * The utility class use to get reflection information 
 * for the class byte code, and dynamic proxy class 
 * generate.  
 * </p>
 * @author sunchao
 *
 */
@Utility(value = "reflect")
public final class ReflectUtil {

	/**
	 * the primitive class type in the jvm internal.
	 * byte => B, char => C, short => S,int => I,
	 * float => F, double =>D, long => J;
	 */
	public static final char INTERNAL_BYTE = 'B';
	public static final char INTERNAL_CHAR = 'C';
	public static final char INTERNAL_SHORT = 'S';
	public static final char INTERNAL_INT = 'I';
	public static final char INTERNAL_FLOAT = 'F';
	public static final char INTERNAL_DOUBLE = 'D';
	public static final char INTERNAL_LONG = 'J';
	
	/**
	 * boolean => Z, void => V;
	 */
	public static final char INTERNAL_BOOLEAN = 'Z';
	public static final char INTERNAL_VOID = 'V';
	/**
	 * 1.java naming don't begin with 0-9(number)
	 * 2.'$' use in two case. inner class => Outer$Inner.class.
	 *                        proxy class => xxx.xxx.Foo$Proxy0.class.
	 * 3. (?: ) => the non-capturing group of the regex expression  
	 * which not's count the counter of total number of group and don't
	 *  generate the match value.                     
	 */
	public static final String JAVA_COMMON_NAMING = "(?:[_$a-zA-Z][_$a-zA-Z0-9]*)";
	
	public static final String JAVA_VALID_NAME = "(?:" + JAVA_COMMON_NAMING + "(?:\\." + JAVA_COMMON_NAMING + ")*)";
	/** e.g. Ljava/lang/String;  */
	public static final String JAVA_INTERNAL_CLASS = "(?:L" + JAVA_COMMON_NAMING + "(?:\\/" + JAVA_COMMON_NAMING + ")*;)";
	/** e.g. [I, [java/lang/Foo, [[I,.......   */
	public static final String JAVA_INTERNAL_ARRAY = "(?:\\[+(?:(?:[BCSIFDJZV])|" + JAVA_INTERNAL_CLASS + "))";
	/** primitive type  and array type and object type   */
	public static final String  JAVA_DATA_TYPE = "(?:(?:[BCSIFDJZV])|" + JAVA_INTERNAL_ARRAY + "|" + JAVA_INTERNAL_CLASS + ")";
	
	public static final Pattern DATATYPE_PATTERN = Pattern.compile(JAVA_DATA_TYPE);
	
	public static final String JAVA_METHOD_DESC = "(?:(" + JAVA_COMMON_NAMING +")?\\((" + JAVA_DATA_TYPE + "*)\\)(" + JAVA_DATA_TYPE + ")?)";
	
	public static final Pattern JAVA_MENTHOD_PATTERN = Pattern.compile(JAVA_METHOD_DESC);
	
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	
	public static final Pattern GETTER_METHOD_PATTERN = Pattern.compile("get([A-Z][a-zA-Z0-9]*)\\(\\)(" + JAVA_DATA_TYPE + ")");
	
	public static final Pattern SETTER_METHOD_PATTERN = Pattern.compile("set([A-Z][a-zA-Z0-9]*)\\((" + JAVA_DATA_TYPE + ")\\)V");
	/** the boolean flag of class field  which setter/getter method = > boolean isFlag()  */
	public static final Pattern IS_HAS_METHOD_PATTERN = Pattern.compile("(?:is|has|can)([A-Z][a-zA-Z0-9]*)\\(\\)Z");
	
	
	private static final ConcurrentMap<String, Class<?>> CLASS_DESC_CACHE = new ConcurrentHashMap<String, Class<?>>();
	
	private static final ConcurrentMap<String, Class<?>> CLASS_NAME_CACHE = new ConcurrentHashMap<String, Class<?>>();
	
	private static final ConcurrentMap<String, Method> METHOD_SIGNATURE_CACHE = new ConcurrentHashMap<String, Method>();
	
	
	public static boolean isPrimitive(Class<?> clazz)
	{
		if (clazz == null)
		{
			throw new IllegalArgumentException("class must not be null!");
		}
		
		return clazz.isPrimitive() || clazz == String.class ||clazz == Boolean.class || clazz == Character.class
				|| Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz);
	}
	
	/**
	 * Assert the the element of array whether
	 * is primitive type.
	 * @param clazz
	 *           the class argument.
	 * @return
	 *         the flag
	 *        
	 */
	 public static boolean isPrimitives(Class<?> clazz)
	 {
		 if (clazz == null)
		 {
			 throw new IllegalArgumentException("class must not be null!");
		 }
		 if (clazz.isArray())
		 {
			 return isPrimitive(clazz.getComponentType());
		 }
		 return isPrimitive(clazz);
	 }
	
	 /**
	  * Get the primitive class type's wrapper type.
	  *  int.class => Integer.class
	  *  if the class is not primitive class
	  *  return itself directly.
	  * @param clazz
	  *            the primitive class type
	  * @return
	  *           the wrapper class type.
	  */
	 public static Class<?> getWrapperClass(Class<?> clazz)
	 {
		 if (clazz == int.class)
			 return Integer.class;
		 else if (clazz == byte.class)
			 return Byte.class;
		 else if (clazz == short.class)
			 return Short.class;
		 else if (clazz == char.class)
			 return Character.class;
		 else if (clazz == float.class)
			 return Float.class;
		 else if (clazz == double.class)
			 return Double.class;
		 else if (clazz == long.class)
			 return Long.class;
		 else if (clazz == boolean.class)
			 return Boolean.class;
		 else
			 return clazz;
	 }
	
	 /**
	  * Assert the object is instance of the class.
	  * 1:Because the primitive auto boxed the wrapper class,
	  *   so we need change the primitive class to boxed class,
	  *   then assert isInstance().
	  * 2:and the class is not the primitive type,
	  * so we can directly use the function isInstance(),
	  * Note: in the case : the object is null.
	  * @param clazz
	  *             the class type.
	  * @param o
	  *            the  object argument. 
	  * @return
	  *           the whether is compatible or not.
	  */
	 public static boolean isCompatible(Class<?> clazz, Object o)
	 {
		 /**
		  * if class is primitive, and o == null return false;
		  * if class is not primitive, and o == null return true.
		  */
		 boolean ip = clazz.isPrimitive();
		 if (o == null)
			return !ip;
		
		 if (ip)
		 {
		     if (clazz == int.class)
	             clazz = Integer.class;
		     else if (clazz == byte.class)
				 clazz = Byte.class;
		     else if (clazz == boolean.class)
				 clazz = Boolean.class;
		     else if (clazz == short.class)
				 clazz = Short.class;
		     else if (clazz == char.class)
				 clazz = Character.class;
		     else if (clazz == float.class)
				 clazz = Float.class;
		     else if (clazz == double.class)
				 clazz = Double.class;
		     else if (clazz == long.class)
				 clazz = Long.class;
		 }
		 
		 if (clazz == o.getClass())
			 return true;
		 /**
		  * the function:
		  *             instanceof();      => object instanceof  class.
		  *             isInstance();      => class.isInstance(object),
		  *             isAssginableFrom() => superClass.isAssginableFrom(subClass).
		  */
		 return clazz.isInstance(o);
	 }
	 
	 /**
	  * Assert the object array instance is compatible for the
	  * class array.
	  * @param cs
	  *          the class array.
	  * @param os
	  *          the object array instance.
	  * @return
	  *         whether is compatible or not.
	  */
	 public static boolean isCompatible(Class<?>[] cs, Object[] os )
	 {
		 int len = cs.length;
		 if (len != os.length) return false;
		 if (len == 0) return true; // empty class array and empty object array, so return true.
		 for (int i = 0; i < len; i++)
			 if ( !isCompatible(cs[i], os[i]))
		       		 return false;
		 return true;
	 }
	 
	 /**
	  * The method intent to get absolute path of the class.
	  * And you can ask why don't use the function => " 
	  * clazz.getResource("").getFile()" and is that one ?;
	  * ok. when the class file in the eclipse or other which
	  * was not jar to together, the two method is equals,
	  * but when the class file was jar together,e.g. the class file
	  * in the jar package, use "class.getResource("").getFile" will
	  * throw Exception that nullpointerException...you can't get 
	  * information for the  security protection.
	  * @param cls
	  *           the class .
	  * @return
	  *          the absolute path of the class.(if can)
	  */
	 public static String getCodeBase(Class<?> cls)
	 {
		 if (cls == null)
			 return null;
		 ProtectionDomain domain = cls.getProtectionDomain();
		 if (domain == null)
			 return null;
		 CodeSource codeSource = domain.getCodeSource();
		 if (codeSource == null)
			 return null;
		 URL location  = codeSource.getLocation();
		 if (location == null)
			 return null;
		 return location.getFile();
	 }
	 
	 /**
	  * Get the name of the arguments class;
	  * object type => Ljava/lang/String  => java/lang/String;
	  * array type  => [[Ljava/lang/String => java/lang/String[][]; 
	  * @param cls
	  *          the class argument
	  * @return
	  *         the name of class,which is the form of above.
	  */
	 public static String getName(Class<?> cls)
	 {
		 if (cls == null)
			 return null;
		 
		 if (cls.isArray())
		 {
			 StringBuilder sb = new StringBuilder();
			 do {
				 sb.append("[]");
				 cls = cls.getComponentType();
			 } while (cls.isArray());
			 return cls.getName() + sb.toString();
		 }
		 return cls.getName();
	 }
	 
	 public static Class<?> getGenericClass(Class<?> cls)
	 {
		 if (cls == null)
			 return null;
		  return getGenericClass(cls,0);
	 }
	 
	 /**
	  * getGenericInterfaces() , getGenericSuperClass();
	  * getInterfaces(), getSuperClass(); not equals .
	  * The first methods can show the superclass and interfaces
	  * generic type .
	  * 
	  * @param cls
	  *          the class arguments
	  * @param i
	  *          the index of the interface.
	  * @return
	  *          the type of generic. 
	  */
	 public static Class<?> getGenericClass(Class<?> cls, int i)
	 {
		 try{
		     ParameterizedType parameterizedType = (ParameterizedType) cls.getGenericInterfaces()[i];
		     Type genericClass = parameterizedType.getActualTypeArguments()[0];
		     if (genericClass instanceof ParameterizedType)//nested generic type e.g. a<T> extends b<Map<T,S>>
		     {
		    	 return (Class<?>)((ParameterizedType) genericClass).getRawType(); // the result show as "interface Ljava/util/Map;"
		     } 
		     else if (genericClass instanceof GenericArrayType) // generic array 
		     {
		    	 return (Class<?>) ((GenericArrayType)genericClass).getGenericComponentType();
		     }
		     else {
		    	return (Class<?>)genericClass; 
		     }
		 } catch (Throwable t) {
			 throw new IllegalArgumentException(cls.getName() 
					 + " the generic type undefined!", t);
		 }
	 }
	 
	 /**
	  * Get the method description of string e.g.
	  * void do(int)   void do()
	  * int do(java.lang.String, boolean); 
	  * @param method
	  *         the method instance
	  * @return
	  *         the method string desc.
	  */
	 
	 public static String getName(final Method method)
	 {
		 if(method == null)
		 {
			 throw new IllegalArgumentException(
					 "the method must not be null!");
		 }
		 StringBuilder sb = new StringBuilder();
		 sb.append(getName(method.getReturnType())).append(' ');
		 sb.append(method.getName()).append('(');
		 Class<?>[] paramTypes = method.getParameterTypes();
	     for (int i = 0; i < paramTypes.length; i++)
	     {
	    	 if (i != 0)
	    	 {
	    		 sb.append(',');
	    	 }
	    	 sb.append(getName(paramTypes[i]));
	     }
	     sb.append(')');
	     return sb.toString();
	 }
	 
	 /**
	  * get the string desc of method signature(has no the return type).
	  * e.g. do(int), do(java.lang.String,boolean);
	  * @param methodName
	  *                the method string name.
	  * @param parameterTypes
	  *                the class array of parameter.
	  * @return
	  *               the method signature desc.
	  */
	 public static String getMethodSignature(String methodName, Class<?>[] parameterTypes)
	 {
		 StringBuilder sb = new StringBuilder(methodName);
		 sb.append("(");
		 if (parameterTypes !=null)
		 {
			 for (int i = 0 ; i < parameterTypes.length; i++)
			 {
				 if (i != 0)
				 {
					 sb.append(",");
				 }
				 sb.append(parameterTypes[i].getName());
			 }
		 }
		sb.append(")");
		return sb.toString();
	 }
	 
	 /**
	  * Get the constructor desc with the form
	  * (),(java.lang.String, int);
	  * @param c
	  *        the constructor
	  * @return
	  *        the constructor name with class name.
	  */      
	 public static String getName(final Constructor<?> c)
	 {
		 if (c == null)
		 {
			 throw new IllegalArgumentException(
					 "constructor argument must not be null!");
		 }
		 StringBuilder sb = new StringBuilder('(');
		 Class<?>[] classArray = c.getParameterTypes();
		 for (int i = 0; i < classArray.length; i++)
		 {
			 if (i != 0)
			 {
				 sb.append(',');
			 }
			 sb.append(classArray[i].getName());
		 }
		 sb.append(')');
		 return sb.toString();
	 }
	
	 /**
	  * Get class desc.
	  * boolean[] => "[Z"
	  * long[] => "[J"
	  * String[] => "[Ljava/lang/String;"
	  * @param cls
	  *        the class instance
	  * @return
	  *        the jvm internal desc.
	  * @throws Exception
	  *              if class is null.
	  */
	 public static String getDesc(Class<?> cls) throws Exception
	 {
		 if (cls == null)
		 {
			 throw new IllegalAccessException(
			 		"The class argument must not be null!");
		 }
		 StringBuilder sb = new StringBuilder();
		 while (cls.isArray())
		 {
			 sb.append('[');
			 cls = cls.getComponentType();
		 }
		 
		 if (cls.isPrimitive())
		 {
			 String s = cls.getName();
			 if ("int".equals(s))
				 sb.append(INTERNAL_INT);
			 else if ("byte".equals(s))
				 sb.append(INTERNAL_BYTE);
			 else if ("short".equals(s))
				 sb.append(INTERNAL_SHORT);
			 else if ("char".equals(s))
				 sb.append(INTERNAL_CHAR);
			 else if ("boolean".equals(s))
				 sb.append(INTERNAL_BOOLEAN);
			 else if ("float".equals(s))
				 sb.append(INTERNAL_FLOAT);
			 else if ("double".equals(s))
				 sb.append(INTERNAL_DOUBLE);
			 else if ("void".equals(s))
				 sb.append(INTERNAL_VOID);
			 else if ("long".equals(s))
				 sb.append(INTERNAL_LONG);
		 }
		 else
		 {
			 sb.append('L');
			 sb.append(cls.getName().replace('.', '/'));
			 sb.append(';');
		 }
		return sb.toString();
	 }
	 
	 /**
	  * Get the class array desc.
	  * [int.class, boolean[].class, Object.class] => I[ZLjava/lang/Object;
	  * @param cs
	  *         the class array.
	  * @return
	  *        the string desc
	  * @throws Exception
	  *                 if cs.length == 0.
	  */
	 public static String getDesc(Class<?>[] cs) throws Exception
	 {
		 if (cs.length == 0)
		 {
			 throw new IllegalArgumentException(
					 "the Class<?>[] length must be larger than 0!");
		 }
		 
		 StringBuilder sb = new StringBuilder(100);
		 for (Class<?> cls : cs)
			 sb.append(getDesc(cls));
		 return sb.toString();
	 }
	 
	 
	 /**
	  * Get the method desc in the jvm internal.
	  * int do(int arg1) => "do(I)I"
	  * void do(String arg1, boolean arg2) => "do(Ljava/lang/String;Z)V"
	  * @param method
	  *             the method instance.
	  * @return
	  *             the method string desc.
	  * @throws Exception
	  */
	 public static String getDesc(final Method method) throws Exception
	 {
		 if (method == null)
			 throw new IllegalArgumentException("the method must not be null !");
		 StringBuilder sb = new StringBuilder();
		 sb.append(method.getName()).append('(');
		 Class<?>[] parameterTypes = method.getParameterTypes();
		 for (int i = 0; i < parameterTypes.length; i++)
		 {
			 sb.append(getDesc(parameterTypes[i]));
		 }
		 sb.append(')').append(getDesc(method.getReturnType()));
		 return sb.toString();
	 }
	 
	 /**
	  * ()V, (Ljava/lang/String;I)V
	  * 
	  * Get the constructor string desc in the form
	  * of jvm internal.
	  * @param c
	  *         the constructor.
	  * @return
	  *         the string desc.
	  * @throws Exception
	  */
	 public static String getDesc(final Constructor<?> c) throws Exception
	 {
		 if (c == null)
			 throw new IllegalArgumentException("the constructor must not be null!");
		 
		 StringBuilder sb = new StringBuilder('(');
		 Class<?>[] parameterTypes = c.getParameterTypes();
		 for (int i = 0; i < parameterTypes.length; i++)
		 {
			 sb.append(getDesc(parameterTypes[i]));
		 }
		 sb.append(')').append('V');
		 return sb.toString();
		 
		 
	 }
	 
	 /**
	  * Get the method desc but without the method name.
	  * (I)V, "(Ljava/lang/String;)I"
	  * @param m
	  *         the method argument.
	  * @return
	  *         the string desc. 
	  * @throws Exception
	  */
	 public static String getDescWithoutMethodName(Method m) throws Exception
	 {
		 if (m == null)
			 throw new IllegalArgumentException("the method argument must not be null!");
		 StringBuilder sb = new StringBuilder('(');
		 Class<?>[] parameterTypes = m.getParameterTypes();
		 for (int i = 0; i < parameterTypes.length; i++)
		 {
			 sb.append(getDesc(parameterTypes[i]));
		 }
		 sb.append(')').append(getDesc(m.getReturnType()));
		 return sb.toString();
	 }
	 
	 /**
	  * get the class desc from the ctclass of javasist.
	  * the desc of internal desc of jvm.
	  * Object.class => Ljava/lang/Object;
	  * boolean[].class => [Z
	  * @param cls
	  *          the class of javasist form.
	  * @return
	  *          the class desc internal jvm
	  * @throws Exception
	  */
	 public static String getCtDesc(final CtClass cls) throws Exception
	 {
		 if (cls == null)
			 throw new IllegalArgumentException("the CtClass argument must not be null");
		 StringBuilder sb = new StringBuilder();
		 if (cls.isArray())
		 {
			 sb.append('[');
			 sb.append(getCtDesc(cls.getComponentType()));
		 }
		 else if (cls.isPrimitive())
		 {
			 String name = cls.getName();
			 if ("void".equals(name))
				 sb.append(INTERNAL_VOID);
			 else if ("boolean".equals(name))
				 sb.append(INTERNAL_BOOLEAN);
			 else if ("byte".equals(name))
				 sb.append(INTERNAL_BYTE);
			 else if ("char".equals(name))
				 sb.append(INTERNAL_CHAR);
			 else if ("short".equals(name))
				 sb.append(INTERNAL_SHORT);
			 else if ("int".equals(name))
				 sb.append(INTERNAL_INT);
			 else if ("float".equals(name))
				 sb.append(INTERNAL_FLOAT);
			 else if ("double".equals(name))
				 sb.append(INTERNAL_DOUBLE);
			 else if ("long".equals(name))
				 sb.append(INTERNAL_LONG);
		 }
		 else{
			 sb.append('L');
			 sb.append(cls.getName().replace('.', '/'));
			 sb.append(';');
		 }
		 return sb.toString();
	 }
	 
	 /**
	  * Get the method of javasisit desc.
	  * 
	  * @param method
	  *          the method javasist.
	  * @return
	  *         the method desc.
	  * @throws Exception
	  */
	 public static String getCtDesc(final CtMethod method) throws Exception
	 {
		 if (method == null)
			 throw new IllegalArgumentException("the Ctmethod argument can't be mull!");
		 CtClass[] parameterClasses = method.getParameterTypes();
		 StringBuilder sb = new StringBuilder('(');
		 for (int i = 0; i < parameterClasses.length; i++)
		 {
			 sb.append(getCtDesc(parameterClasses[i]));
		 }
		 sb.append(')').append(getCtDesc(method.getReturnType()));
		 return sb.toString();
	 }
	 
	 /**
	  * Get Constructor desc of javasist.
	  * ()V     (Ljava/lang/String;I)V
	  * @param c
	  *        The constructor argument.
	  * @return
	  *         the internal desc.
	  * @throws Exception
	  */
	 public static String getCtDesc(final CtConstructor c) throws Exception
	 {
		 if (c == null)
		 {
			 throw new IllegalArgumentException(""
			 		+ "The CtConstructor must not be null!");
		 }
		 StringBuilder sb = new StringBuilder('(');
		 CtClass[] parameterTypes = c.getParameterTypes();
		 for (int i = 0; i < parameterTypes.length; i++)
		 {
			 sb.append(getCtDesc(parameterTypes[i]));
		 }
		 sb.append(')').append('V');
		 return sb.toString();
	 }
	 
	 public static String getCtDescWithoutMethodName(final CtMethod method) throws Exception
	 {
		 if (method == null)
			 throw new IllegalArgumentException("The CtMethod argument must not be null!");
		 StringBuilder sb = new StringBuilder('(');
		 CtClass[] parameterTypes = method.getParameterTypes();
		 for (int i = 0; i < parameterTypes.length; i++)
		 {
			 sb.append(getCtDesc(parameterTypes[i]));
		 }
		 sb.append(')').append(getCtDesc(method.getReturnType()));
		 return sb.toString();
	 }
	 
	 /**
	  * the name to desc.
	  * java.util.Map[][] = > [[Ljava/util/Map;
	  * @param name
	  *           the desc name
	  * @return
	  *           the jvm desc.
	  */
	 public static String name2Desc(String name)
	 {
		 StringBuilder sb = new StringBuilder();
		 int c = 0, index = name.indexOf('[');//java.util.Map[][]
		 if (index > 0)
		 {
			 c = (name.length() - index) / 2; //get  number of the tuple of ('[]')
			 name = name.substring(0,index); // java.util.Map;
		 }
		 while (c-- > 0) sb.append('[');
		 if ("void".equals(name))  sb.append(INTERNAL_VOID);
		 else if ("boolean".equals(name)) sb.append(INTERNAL_BOOLEAN);
		 else if ("byte".equals(name)) sb.append(INTERNAL_BYTE);
		 else if ("char".equals(name)) sb.append(INTERNAL_CHAR);
		 else if ("short".equals(name)) sb.append(INTERNAL_SHORT);
		 else if ("int".equals(name)) sb.append(INTERNAL_INT);
		 else if ("float".equals(name)) sb.append(INTERNAL_FLOAT);
		 else if ("double".equals(name)) sb.append(INTERNAL_DOUBLE);
		 else if ("long".equals(name)) sb.append(INTERNAL_LONG);
		 else sb.append('L').append(name.replace('.', '/')).append(';');
		 return sb.toString();
		 
	 }
	 
	 /**
	  * get the desc to the name
	  * e.g. [L => int[] , [Ljava/lang/String =>java.lang.String[]
	  * @param desc
	  *           the desc jvm internal. 
	  * @return
	  *         the name
	  */
	 public static String desc2Name(String desc)
	 {
		 StringBuilder sb = new StringBuilder();
		 int index = desc.lastIndexOf('[') + 1;
		 if (desc.length() == index + 1) // the case of  [I primitive type.
		 {
			 switch (desc.charAt(index))
			 {
			     case INTERNAL_VOID : {sb.append("void"); break;}
			     case INTERNAL_BOOLEAN : {sb.append("boolean"); break;}
			     case INTERNAL_BYTE : {sb.append("byte"); break;}
			     case INTERNAL_CHAR : {sb.append("char"); break;}
			     case INTERNAL_SHORT : {sb.append("short"); break;}
			     case INTERNAL_INT : {sb.append("int"); break;}
			     case INTERNAL_FLOAT : {sb.append("float"); break;}
			     case INTERNAL_DOUBLE : {sb.append("double"); break;}
			     case INTERNAL_LONG : {sb.append("long"); break;}
			     default :
			    	 throw new RuntimeException();
			 }
		 }
		 else 
		 {
			 //Ljava/lamg/String;
			 sb.append(desc.substring(index + 1, desc.length() -1).replace('/', '.'));
		 }
		 while (index-- > 0) sb.append("[]");
		 return sb.toString();
	 }
	 
	 
	 public static Class<?> forName(String name) { 
		 try {
			return name2Class(name);
		} catch (Exception e) {
			throw new IllegalStateException("Not found class " + name +", cause: " + e.getMessage(), 
					e);
		}
	 }
	 
	 public static Class<?> name2Class(String name) throws Exception
	 {
			return name2Class(ClassUtil.getClassLoader(), name);
	 }
	 
	 /**
	  * the class desc name change to class.
	  * "boolean"  => "boolean.class"
	  * "java.util.Map[][]" => "java.util.Map[][].class"
	  * @param cl
	  *         the class loader instance.
	  * @param name
	  *          class name
	  * @return
	  *         class instance.
	  * @throws Exception
	  */
	 private static Class<?> name2Class(ClassLoader cl, String name) throws Exception
	 {
		 int c = 0, index = name.indexOf('[');
		 if (index > 0)
		 {
			 c = (name.length() - index) / 2;
			 name = name.substring(0, index);
		 }
		 if (c > 0)
		 {
			 StringBuilder sb = new StringBuilder();
			 while (c-- > 0)
				 sb.append('[');
			 if ("void".equals(name)) sb.append(INTERNAL_VOID);
			 else if ("boolean".equals(name)) sb.append(INTERNAL_BOOLEAN);
			 else if ("byte".equals(name)) sb.append(INTERNAL_BYTE);
			 else if ("short".equals(name)) sb.append(INTERNAL_SHORT);
			 else if ("char".equals(name)) sb.append(INTERNAL_CHAR);
			 else if ("int".equals(name)) sb.append(INTERNAL_INT);
			 else if ("float".equals(name)) sb.append(INTERNAL_FLOAT);
			 else if ("double".equals(name)) sb.append(INTERNAL_DOUBLE);
			 else if ("long".equals(name)) sb.append(INTERNAL_LONG);
			 else sb.append('L').append(name).append(';');// java.lang.Object => Ljava.lang.Object;
			 name = sb.toString();
		 }
		 else 
		 {
			 if ("void".equals(name)) return void.class;
			 else if ("boolean".equals(name)) return boolean.class;
			 else if ("byte".equals(name)) return byte.class;
			 else if ("char".equals(name)) return char.class;
			 else if ("short".equals(name)) return short.class;
			 else if ("int".equals(name)) return int.class;
			 else if ("float".equals(name)) return float.class;
			 else if ("double".equals(name)) return double.class;
			 else if ("long".equals(name)) return long.class;
		 }
		 
		 if (cl == null)
			 cl = ClassUtil.getClassLoader();
		 Class<?> clazz = CLASS_NAME_CACHE.get(name);
		/* if (clazz == null) {
			 synchronized (ReflectUtil.class) {
			      clazz = CLASS_NAME_CACHE.get(name);
			      if (clazz == null) {
			    	  clazz = Class.forName(name,true,cl);
			    	  CLASS_NAME_CACHE.put(name, clazz);
			      }
			 }
		 }
		 return clazz ;*/
		 if (clazz == null) {
			 clazz = Class.forName(name, true, cl);
			 CLASS_NAME_CACHE.put(name, clazz);
		 }
		 return clazz;
	 }
	 
	 
	 public static Class<?> desc2Class(String desc) throws Exception 
	 {
		 return desc2Class(ClassUtil.getClassLoader(), desc);
	 }
	 
	 /**
	  * the class desc to class.
	  * "[Z" = >  boolean[].class
	  * "[[java/util.Map;" => java.util.Map[][].class;
	  * @param cl
	  *        the class loader instance.
	  * @param desc
	  *        the class str desc.
	  * @return
	  *        class instance
	  * @throws Exception
	  *         ClassNotFoundException.
	  */
	 private static Class<?> desc2Class(ClassLoader cl, String desc) throws Exception
	 {
		 switch (desc.charAt(0))
		 {
		      case INTERNAL_VOID : return void.class;
		      case INTERNAL_BOOLEAN : return boolean.class;
		      case INTERNAL_BYTE : return byte.class;
		      case INTERNAL_CHAR : return char.class;
		      case INTERNAL_SHORT : return short.class;
		      case INTERNAL_INT : return int.class;
		      case INTERNAL_FLOAT : return float.class;
		      case INTERNAL_DOUBLE : return double.class;
		      case INTERNAL_LONG : return long.class;
		      case 'L' :
		    	  desc = desc.substring(1, desc.length() - 1).replace('/', '.');// "Ljava/lang/Object;" => "java.lang.Object"
		    	  break;
		      case '[' :
		          desc = desc.replace('/', '.'); // "[[Ljava/lang/Object;" => "[[Ljava.lang.Object;"
		          break;
		      default :
		    	  throw new ClassNotFoundException("Class not found: " + desc);
		 }
		 
	/*	 IF (CL == NULL)
			 CL = CLASSUTIL.GETCLASSLOADER();
		 CLASS<?> CLAZZ = CLASS_NAME_CACHE.GET(NAME);
		 IF (CLAZZ == NULL) {
			 SYNCHRONIZED (REFLECTUTIL.CLASS) {
			      CLAZZ = CLASS_NAME_CACHE.GET(NAME);
			      IF (CLAZZ == NULL) {
			    	  CLAZZ = CLASS.FORNAME(NAME,TRUE,CL);
			    	  CLASS_NAME_CACHE.PUT(NAME, CLAZZ);
			      }
			 }
		 }*/
		 
		 if (cl == null)
			 cl = ClassUtil.getClassLoader();
		 Class<?> clazz = CLASS_DESC_CACHE.get(desc);
		 if (clazz == null) 
		 {
			 clazz = Class.forName(desc, true, cl);
			 CLASS_DESC_CACHE.put(desc, clazz);
		 }
		 return clazz;
	 }
	 
	 public static Class<?>[]  desc2ClassaArray(String desc) throws Exception 
	 {
		 Class<?>[] clazzArray = desc2ClassArray(ClassUtil.getClassLoader(), desc);
		 return clazzArray;
	 }
	 
	 /**
	  * Get class array instance.
	  * 
	  * @param cl
	  *         the class loader instance.
	  * @param desc
	  *          desc of this.
	  * @return
	  *          Class[] class array.
	  * @throws Exception
	  *          ClassNotFoundException.
	  */
	 private static Class<?>[] desc2ClassArray(ClassLoader cl, String desc) throws Exception 
	 {
		 if (desc.length() == 0)
			 return EMPTY_CLASS_ARRAY;
		 List<Class<?>> cs = new ArrayList<Class<?>>();
		 Matcher m =  DATATYPE_PATTERN.matcher(desc);
		 while (m.find())
			 cs.add(desc2Class(cl, m.group()));
		 return cs.toArray(EMPTY_CLASS_ARRAY);
	 }
	 
	 public static Method findMethodByMethodSignature(Class<?> clazz, String methodName, String[] parameterTypes)
		        throws Exception {
		    String signature = methodName;
	        if(parameterTypes != null && parameterTypes.length > 0){
	            signature = methodName + StringUtil.join(parameterTypes);
	        }
	        Method method = METHOD_SIGNATURE_CACHE.get(signature);
	        if(method != null){
	            return method;
	        }
		    if (parameterTypes == null) {
	            List<Method> finded = new ArrayList<Method>();
	            for (Method m : clazz.getMethods()) {
	                if (m.getName().equals(methodName)) {
	                    finded.add(m);
	                }
	            }
	            if (finded.isEmpty()) {
	                throw new NoSuchMethodException("No such method " + methodName + " in class " + clazz);
	            }
	            if(finded.size() > 1) {
	                String msg = String.format("Not unique method for method name(%s) in class(%s), find %d methods.",
	                        methodName, clazz.getName(), finded.size());
	                throw new IllegalStateException(msg);
	            }
	            method = finded.get(0);
	        } else {
	            Class<?>[] types = new Class<?>[parameterTypes.length];
	            for (int i = 0; i < parameterTypes.length; i ++) {
	                types[i] = ReflectUtil.name2Class(parameterTypes[i]);
	            }
	            method = clazz.getMethod(methodName, types);
	            
	        }
		    METHOD_SIGNATURE_CACHE.put(signature, method);
	        return method;
		}

	    public static Method findMethodByMethodName(Class<?> clazz, String methodName)
	    		throws Exception {
	    	return findMethodByMethodSignature(clazz, methodName, null);
	    }
	    
	    public static Constructor<?> findConstructor(Class<?> clazz, Class<?> paramType) throws NoSuchMethodException {
	    	Constructor<?> targetConstructor;
			try {
				targetConstructor = clazz.getConstructor(new Class<?>[] {paramType});
			} catch (NoSuchMethodException e) {
				targetConstructor = null;
				Constructor<?>[] constructors = clazz.getConstructors();
				for (Constructor<?> constructor : constructors) {
					if (Modifier.isPublic(constructor.getModifiers()) 
							&& constructor.getParameterTypes().length == 1
							&& constructor.getParameterTypes()[0].isAssignableFrom(paramType)) {
						targetConstructor = constructor;
						break;
					}
				}
				if (targetConstructor == null) {
					throw e;
				}
			}
			return targetConstructor;
	    }

	    /**
	     * 检查对象是否是指定接口的实现。
	     * <p>
	     * 不会触发到指定接口的{@link Class}，所以如果ClassLoader中没有指定接口类时，也不会出错。
	     * 
	     * @param obj 要检查的对象
	     * @param interfaceClazzName 指定的接口名
	     * @return 返回{@code true}，如果对象实现了指定接口；否则返回{@code false}。
	     */
	    public static boolean isInstance(Object obj, String interfaceClazzName) {
	        for (Class<?> clazz = obj.getClass(); 
	                clazz != null && !clazz.equals(Object.class); 
	                clazz = clazz.getSuperclass()) {
	            Class<?>[] interfaces = clazz.getInterfaces();
	            for (Class<?> itf : interfaces) {
	                if (itf.getName().equals(interfaceClazzName)) {
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
	    
	    public static Object getEmptyObject(Class<?> returnType) {
	        return getEmptyObject(returnType, new HashMap<Class<?>, Object>(), 0);
	    }
	    
	    private static Object getEmptyObject(Class<?> returnType, Map<Class<?>, Object> emptyInstances, int level) {
	        if (level > 2)
	            return null;
	        if (returnType == null) {
	            return null;
	        } else if (returnType == boolean.class || returnType == Boolean.class) {
	            return false;
	        } else if (returnType == char.class || returnType == Character.class) {
	            return '\0';
	        } else if (returnType == byte.class || returnType == Byte.class) {
	            return (byte)0;
	        } else if (returnType == short.class || returnType == Short.class) {
	            return (short)0;
	        } else if (returnType == int.class || returnType == Integer.class) {
	            return 0;
	        } else if (returnType == long.class || returnType == Long.class) {
	            return 0L;
	        } else if (returnType == float.class || returnType == Float.class) {
	            return 0F;
	        } else if (returnType == double.class || returnType == Double.class) {
	            return 0D;
	        } else if (returnType.isArray()) {
	            return Array.newInstance(returnType.getComponentType(), 0);
	        } else if (returnType.isAssignableFrom(ArrayList.class)) {
	            return new ArrayList<Object>(0);
	        } else if (returnType.isAssignableFrom(HashSet.class)) {
	            return new HashSet<Object>(0);
	        } else if (returnType.isAssignableFrom(HashMap.class)) {
	            return new HashMap<Object, Object>(0);
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
	                        Object property = getEmptyObject(field.getType(), emptyInstances, level + 1);
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

	    public static boolean isBeanPropertyReadMethod(Method method) {
	        return method != null
	            && Modifier.isPublic(method.getModifiers())
	            && ! Modifier.isStatic(method.getModifiers())
	            && method.getReturnType() != void.class
	            && method.getDeclaringClass() != Object.class
	            && method.getParameterTypes().length == 0
	            && ((method.getName().startsWith("get") && method.getName().length() > 3)
	                    || (method.getName().startsWith("is") && method.getName().length() > 2));
	    }

	    public static String getPropertyNameFromBeanReadMethod(Method method) {
	        if (isBeanPropertyReadMethod(method)) {
	            if (method.getName().startsWith("get")) {
	                return method.getName().substring(3, 4).toLowerCase()
	                    + method.getName().substring(4);
	            }
	            if (method.getName().startsWith("is")) {
	                return method.getName().substring(2, 3).toLowerCase()
	                    + method.getName().substring(3);
	            }
	        }
	        return null;
	    }

	    public static boolean isBeanPropertyWriteMethod(Method method) {
	        return method != null
	            && Modifier.isPublic(method.getModifiers())
	            && ! Modifier.isStatic(method.getModifiers())
	            && method.getDeclaringClass() != Object.class
	            && method.getParameterTypes().length == 1
	            && method.getName().startsWith("set")
	            && method.getName().length() > 3;
	    }
	    
	    public static String getPropertyNameFromBeanWriteMethod(Method method) {
	        if (isBeanPropertyWriteMethod(method)) {
	            return method.getName().substring(3, 4).toLowerCase()
	                + method.getName().substring(4);
	        }
	        return null;
	    }

	    public static boolean isPublicInstanceField(Field field) {
	        return Modifier.isPublic(field.getModifiers())
	            && !Modifier.isStatic(field.getModifiers())
	            && !Modifier.isFinal(field.getModifiers())
	            && !field.isSynthetic();
	    }

	    @SuppressWarnings("rawtypes")
		public static Map<String, Field> getBeanPropertyFields(Class cl) {
	        Map<String, Field> properties = new HashMap<String, Field>();
	        for(; cl != null; cl = cl.getSuperclass()) {
	            Field[] fields = cl.getDeclaredFields();
	            for(Field field : fields) {
	                if (Modifier.isTransient(field.getModifiers())
	                    || Modifier.isStatic(field.getModifiers())) {
	                    continue;
	                }

	                field.setAccessible(true);

	                properties.put(field.getName(), field);
	            }
	        }

	        return properties;
	    }

	    @SuppressWarnings("rawtypes")
		public static Map<String, Method> getBeanPropertyReadMethods(Class cl) {
	        Map<String, Method> properties = new HashMap<String, Method>();
	        for(; cl != null; cl = cl.getSuperclass()) {
	            Method[] methods = cl.getDeclaredMethods();
	            for(Method method : methods) {
	                if (isBeanPropertyReadMethod(method)) {
	                    method.setAccessible(true);
	                    String property = getPropertyNameFromBeanReadMethod(method);
	                    properties.put(property, method);
	                }
	            }
	        }

	        return properties;
	    }

		private ReflectUtil(){}
	}

