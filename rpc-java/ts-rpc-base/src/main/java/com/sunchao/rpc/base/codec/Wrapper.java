package com.sunchao.rpc.base.codec;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

import com.sunchao.rpc.base.ReflectUtils;

/**
 * Wrapper.
 * The utility class is used to decrease the reflect call to <code>Class<?></code>
 * for the property and method to invoke.
 * we can {@link #getPropertyNames()} and call {@link #invokeMethod(Object, String, Class[], Object[])}
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public abstract class Wrapper {
	
	public static Wrapper getWrapper(Class<?> c) {
		while (ClassCodec.isDynamicGeneratedClass(c)) 
			c = c.getSuperclass();
		
		if (c == Object.class)
			return OBJECT_WRAPPER;
		
		Wrapper ret = WRAPPER_CACHE_MAP.get(c);
		if (ret == null) {
			ret = makeWrapper(c);
			WRAPPER_CACHE_MAP.put(c, ret);
		}
		return ret;
	}
	
	public abstract String[] getPropertyNames();
	
	/**
	 * Get the property type
	 * @param pn 
	 *        the property name.
	 * @return
	 *        the property type.
	 */
	public abstract Class<?> getPropertyType(String pn);
	
	/**
	 * Judge whether has the property by the specified property name.
	 * 
	 * @param name
	 *          the specified name.
	 * @return
	 */
	public abstract boolean hasProperty(String name);
	
	/**
	 * Get the property value by the specified instance and the 
	 * property name.
	 * 
	 * @param instance
	 *         the instance which to get from.
	 * @param pn
	 *         the property name.
	 * @return
	 *         the property value.
	 * @throws NoSuchPropertyException
	 * @throws IllegalArgumentException
	 */
	public abstract Object getPropertyValue(Object instance, String pn) 
			throws NoSuchPropertyException, IllegalArgumentException;
	
	/**
	 * Set the instance's property value by the specified property
	 * name.
	 * 
	 * @param instance
	 *         the instance which to be setter
	 * @param pn
	 *         the property name
	 * @param pv
	 *         the property value.
	 * @throws NoSuchPropertyException
	 * @throws IllegalArgumentException
	 */
	public abstract void setPropertyValue(Object instance, String pn, Object pv)
	        throws NoSuchPropertyException, IllegalArgumentException;
	
	/**
	 * Get the property values by the specified property name array.
	 * 
	 * @param instance
	 * @param pns
	 * @return
	 * @throws NoSuchPropertyException
	 * @throws IllegalArgumentException
	 */
	public Object[] getPropertyValues(Object instance, String[] pns) throws NoSuchPropertyException
	          ,IllegalArgumentException {
		 Object[] ret = new Object[pns.length];
		 for (int i = 0; i < ret.length; i++)
			 ret[i] = getPropertyValue(instance, pns[i]);
		 return ret;
	}
	
	/**
	 * Set the instance's property values.
	 * @param instance
	 * @param pns
	 * @param pvs
	 * @throws NoSuchPropertyException
	 * @throws IllegalArgumentException
	 */
	public void setPropertyValues(Object instance, String[] pns, Object[] pvs) throws NoSuchPropertyException,
	        IllegalArgumentException {
		if (pns.length != pvs.length) 
			throw new IllegalArgumentException("pns.length != pvs.length.");
		
		for (int i = 0; i < pns.length; i++)
			setPropertyValue(instance, pns[i], pvs[i]);
	}
	
	/**
	 * Get the method name array(public).
	 * @return
	 *       
	 */
	public abstract String[] getMethodNames();
	
	/**
	 * Get the method name array(all)
	 * 
	 * @return
	 */
	public abstract String[] getDeclaredMethodNames();
	
	/**
	 * check whether has the method by the method name.
	 * @param name
	 * @return
	 */
	public boolean hasMethod(String name) {
		for (String mn : getMethodNames()) 
			if (mn.equals(name)) return true;
		return false;
	}
	
	/**
	 * Invoke the method
	 * 
	 * @param instance
	 *        the instance which instance of the argument (makeWrapper(Class)).
	 * @param mn
	 *       the method name.
	 * @param type
	 *        the parameter types.
	 * @param args
	 *        the parameter instance.
	 * @return
	 *        the result value
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public abstract Object invokeMethod(Object instance, String mn, Class<?>[] type,
			Object[] args) throws NoSuchMethodException, InvocationTargetException;
	
	/**
	 * Get the wrapper instance.
	 * 
	 * @param c
	 * @return
	 */
	private static Wrapper makeWrapper(Class<?> c) {
		if (c.isPrimitive()) 
			throw new IllegalArgumentException("Cannot create wrapper for primitive type: " +  c);
		
		String name = c.getName();
		ClassLoader cl = c.getClassLoader();
		
		/** the setPropertyValue method string code */
		StringBuilder c1 = new StringBuilder("public void setPropertyValue(Object o, String n, Object v) {\n ");
		StringBuilder c2 = new StringBuilder("public void getPropertyValue(Object o, String n) {\n ");
		StringBuilder c3 = new StringBuilder("public Object invokeMethod(Object o, String n, Class[] p, Object[] v)"
				+ " throws " + InvocationTargetException.class.getName() + "{\n ");
		/**
		 * c.getName() c;
		 * try {
		 *       c = ((c.getName()) o);
		 * } catch (Throwable t) {
		 *      throw new IllegalArgumentException(e);
		 * }
		 */
		c1.append(name).append(" w\n; try {\n w = ((").append(name).append(")$1); \n} catch (Throwable t) {\n throw new IllegalArgumentException(t);\n}");
		c2.append(name).append(" w\n; try {\n w = ((").append(name).append(")$1); \n} catch (Throwable t) {\n throw new IllegalArgumentException(t);\n}");
		c3.append(name).append(" w\n; try {\n w = ((").append(name).append(")$1); \n} catch (Throwable t) {\n throw new IllegalArgumentException(t);\n}");
		
		/**
		 * property name => property type 
		 */
		Map<String, Class<?>> pts = new HashMap<String, Class<?>>();
		/**
		 * method desc => method instance.
		 */
		Map<String, Method> ms = new LinkedHashMap<String, Method>(); 
		/**
		 * method names.
		 */
		List<String> mns = new ArrayList<String>();
		/**
		 * declared method names.
		 */
		List<String> dmns = new ArrayList<String>();
		
		/**
		 * get the public Non-Static and Non-Transient field array of the <i>c</i>.
		 */
		for (Field f : c.getFields()) {
			String fn = f.getName();
			Class<?> ft = f.getType();
			if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) 
                  continue;				
			
			c1.append("\n  if( $2.equals(\"").append(fn).append("\") ){\n w.").append(fn).append(" = ").append(arg(ft, "$3")).append(";\n return; \n}") ;
			c2.append("\n  if( $2.equals(\"").append(fn).append("\") ){\n return ($w)w.").append(fn).append("; \n}");
			pts.put(fn, ft);
		}
		
		/**
		 * get the public methods of the <i>c</i>.
		 */
		Method[] methods = c.getMethods();
		boolean hasMethod = hasMethods(methods);
		if (hasMethod) {
			c3.append("\n  try {");
		} 
		for (Method m : methods) {
			if (m.getDeclaringClass() == Object.class)// include the toString(), hashCode(), equals(), getClass() an so on.
				continue;
			String mn = m.getName();
			c3.append("\n if (\"").append(mn).append("\".equals( $2 ) ");
			int len = m.getParameterTypes().length;
			c3.append(" && ").append(" $3.length == ").append(len);
			
			boolean override = false;
			for (Method m2 : methods) {
				if (m != m2 && m.getName().equals(m2.getName())) {
					override = true;
					break;
				}
			}
			if (override) {
				if (len > 0) {
					for (int l = 0; l < len; l++) {
						c3.append(" && ").append(" $3[").append(l).append("].getName().equals(\"")
						     .append(m.getParameterTypes()[l].getName()).append("\")");
					}
				}
			}
			
			c3.append(") {\n");
			if (m.getReturnType() == Void.TYPE) 
				c3.append(" w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");").append("\n return null;");
			else
				c3.append(" return ($w)w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");");
			
			c3.append("\n }");
			mns.add(mn);
			if (m.getDeclaringClass() == c) 
				dmns.add(mn);
			ms.put(ReflectUtils.getDesc(m), m);
		} 
		
		if (hasMethod) {
			c3.append("\n } catch (Throwable t) {");
			c3.append("\n  throw new java.lang.reflect.InvocationTargetException(t); ");
			c3.append("\n}");
		}
		
		c3.append("\n throw new " + NoSuchMethodException.class.getName() + "(\"Not found method \\\"\" + $2 + \"\\\" in class " + c.getName() + ".\"); \n}");
		
		Matcher matcher;
		for (Map.Entry<String, Method> entry : ms.entrySet()) {
			String md = entry.getKey();
			Method method = entry.getValue();
			if ((matcher = ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(md)).matches()) {
				String pn = propertyName(matcher.group(1));
				c2.append("\n if( $2.equals(\"").append(pn).append("\")) {\n return ($w)w.").append(method.getName()).append("(); \n}");
				pts.put(pn,  method.getReturnType());
			} else if ((matcher = ReflectUtils.IS_HAS_CAN_METHOD_DESC_PATTERN.matcher(md)).matches()) {
				String pn = propertyName(matcher.group(1));
				c2.append("\n if( $2.equals(\"").append(pn).append("\")) {\n return ($w)w.").append(method.getName()).append("(); \n}");
				pts.put(pn,  method.getReturnType());
			} else if ((matcher = ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(md)).matches()) {
				Class<?> pt = method.getParameterTypes()[0];
				String pn = propertyName(matcher.group(1));
				c1.append("\n if( $2.equals(\"").append(pn).append("\") ){\n w.").append(method.getName()).append("(").append(arg(pt,"$3")).append(");\n return; \n}");
				pts.put(pn, pt);
			}
		}
		c1.append("\n throw new " + NoSuchPropertyException.class.getName() + "(\"Not found property \\\"\" + $2 + \"\\\" getter or setter method in class " + c.getName() + ".\"); \n}");
		c2.append("\n throw new " + NoSuchPropertyException.class.getName() + "(\"Not found property \\\"\" + $2 + \"\\\" getter or setter method in class " + c.getName() + ".\"); \n}");
		
		//make class
		long id = WRAPPER_CLASS_COUNTER.getAndIncrement();
		ClassCodec codec = ClassCodec.newInstance(cl);
		codec.setClassName( ( Modifier.isPublic(c.getModifiers()) ? Wrapper.class.getName() : c.getName() + "$sw" ) + id);
        codec.addSuperClass(Wrapper.class);
        
        codec.addDefaultConstructor();
        codec.addField("\n public static String[] pns;"); //property name array.
        codec.addField("\n public static " + Map.class.getName() + " pts;");//property type map.
        codec.addField("\n public static String[] mns;");// all method name array.
        codec.addField("\n public static String[] dmns;");// declared method name array.
        for (int i = 0, len = ms.size(); i < len; i++) {
        	codec.addField("\n public static Class[] mts" + i + ";");//method types array.
        }
        
        codec.addMethod("\n public String[] getPropertyNames(){\n return pns; \n}");
        codec.addMethod("\n public boolean hasProperty(String n){\n return pts.containsKey($1); \n}");
        codec.addMethod("\n public Class getPropertyType(String n){\n return (Class)pts.get($1); \n}");
        codec.addMethod("\n public String[] getMethodNames(){\n return mns; \n}");
        codec.addMethod("\n public String[] getDeclaredMethodNames(){\n return dmns; \n}");
        codec.addMethod(c1.toString());
        codec.addMethod(c2.toString());
        codec.addMethod(c3.toString());
        
        try {
        	Class<?> wc = codec.toClass();
        	wc.getField("pts").set(null, pts);
        	wc.getField("pns").set(null, pts.keySet().toArray(new String[0]));
        	wc.getField("mns").set(null, mns.toArray(new String[0]));
        	wc.getField("dmns").set(null, dmns.toArray(new String[0]));
        	int index = 0;
        	for (Method m : ms.values()) {
        		wc.getField("mts" + index++).set(null, m.getParameterTypes());
        	}
        	return (Wrapper) wc.newInstance();
        } catch (RuntimeException e) {
        	throw e;
        } catch (Throwable t) {
        	throw new RuntimeException(t.getMessage(), t);
        } finally {
        	codec.release();
        	ms.clear();
        	mns.clear();
        	dmns.clear();
        }
	}
	
	private static String arg(Class<?> clazz, String name) {
		if (clazz.isPrimitive()) {
			if (clazz == Boolean.TYPE) 
				return "((Boolean)" + name + ").booleanValue()";
			if (clazz == Byte.TYPE) 
				return "((Byte)" + name + ").byteValue()";
			if (clazz == Character.TYPE) 
				return "((Character)" + name + ").charValue()";
			if (clazz == Double.TYPE) 
				return "((Number)" + name + ").doubleValue()";
			if (clazz == Float.TYPE) 
				return "((Number)" + name + ").floatValue()";
			if (clazz == Integer.TYPE) 
				return "((Number)" + name + ").intValue()";
			if (clazz == Long.TYPE) 
				return "((Number)" + name + ").longValue()";
			if (clazz == Short.TYPE) 
				return "((Short)" + name + ").shortValue()";
			throw new RuntimeException("Unknown primitive type: " + clazz.getName());
		}
		return "(" + ReflectUtils.getName(clazz) + ")" + name;
	}
	
	private static String args(Class<?>[] cs, String name) {
		int len = cs.length;
		if (len == 0) return "";
		StringBuilder sb =  new StringBuilder();
		for (int i = 0; i < len; i++) {
			if (i > 0) 
				sb.append(',');
			sb.append(arg(cs[i], name + "[" + i + "]"));
		}
		return sb.toString();
	}
	
	/**
	 * Convert the property setter/getter method to the property name.
	 * e.g. setName() => name;
	 * @param pn
	 *         the regex name with the first char high case.
	 * @return
	 */
	private static String propertyName(String pn) {
		return pn.length() == 1 || Character.isLowerCase(pn.charAt(1)) ? Character.toLowerCase(pn.charAt(0)) + pn.substring(1) : pn;
	}
	
	/**
	 * Judge the method belongs to the <code>Object</code>
	 * 
	 * @param methods
	 *             the method arrays.
	 * @return
	 */
	private static boolean hasMethods(Method[] methods) {
		if (methods == null || methods.length == 0) {
			return false;
		}
		for (Method m : methods) {
			if (m.getDeclaringClass() != Object.class) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * The default implement.
	 */
	private static final Wrapper OBJECT_WRAPPER = new Wrapper() {
		
		@Override
		public String[] getMethodNames() {return OBJECT_METHODS;}
		
		@Override
		public String[] getDeclaredMethodNames() {return OBJECT_METHODS;}
		
		@Override
		public String[] getPropertyNames() {return EMPTY_STRING_ARRAY;}
		
		@Override
		public Class<?> getPropertyType(String pn) {return null;}
		
		@Override
		public Object  getPropertyValue(Object instance, String pn) throws 
		     NoSuchPropertyException {throw new NoSuchPropertyException(
		    	"Property [" + pn + "] " + " not found.");}
		
		@Override
		public boolean hasProperty(String name) {return false;}
		
		@Override
		public Object invokeMethod(Object instance, String mn, Class<?>[] types,
				Object[] args) throws NoSuchMethodException {
			if ( "getClass".equals(mn) )  return instance.getClass();
			if ( "hashCode".equals(mn) )  return instance.hashCode();
			if ( "toString".equals(mn) )  return instance.toString();
			if ( "equals".equals(mn)   )  
			{
				if (args.length == 1) return instance.equals(args[0]);
				throw new IllegalArgumentException("Invoke method [" + mn + "] argument number error");
			}
			throw new NoSuchMethodException("method [" + mn +"] not found.");
		}
		
		@Override
		public void setPropertyValue(Object instance, String pn, Object pv)
				throws NoSuchPropertyException, IllegalArgumentException {
			throw new NoSuchPropertyException("Property [" + pn + "] not found.");
			
		}
	};
	/**
	 * <code>Object</code> common methods.
	 */
	private static final String[] OBJECT_METHODS = new String[]{"getClass", "hashCode", "toString", "equals"};

	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	/**
	 * the Class<?> => Wrapper map cache.
	 * the class is the dynamic generated class' super class which do not 
	 * be dynamic class.
	 */
	private static final Map<Class<?>, Wrapper> WRAPPER_CACHE_MAP = new ConcurrentHashMap<Class<?>, Wrapper>();

	/**
	 * generated wrapper counter
	 */
	private final static AtomicLong WRAPPER_CLASS_COUNTER = new AtomicLong(0);
}
