package com.sunchao.rpc.base.codec;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.sunchao.rpc.base.ReflectUtils;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Proxy.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public abstract class Proxy {

	private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);
	
	/**
	 * Get proxy.
	 * 
	 * @param classes
	 *              the interface class array.
	 * @return
	 *          proxy instance.
	 */
	public static Proxy getProxy(Class<?>... classes) {
		return getProxy(Proxy.class.getClassLoader(), classes);
	}
	
	/**
	 * Get proxy.
	 * 
	 * @param loader class Loader.
	 * @param classes  
	 *              the proxy interface class array.
	 * @return
	 *         proxy instance.
	 */
	public static Proxy getProxy(ClassLoader loader, Class<?>... classes) {
		if (classes.length > 65535) 
			throw new IllegalArgumentException("Interface limit exceeded!");
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < classes.length; i++) {
			String inter = classes[i].getName();
			if ( !classes[i].isInterface() ) 
				throw new RuntimeException(inter + " is not a interface!");
			
			Class<?> tmp = null;
			try {
			   tmp = Class.forName(inter, false, loader);
			} catch (ClassNotFoundException e) {
				LOGGER.warn("The interface[ " + inter + " ] could not found.",  e);
			}
			
			if (tmp != classes[i]) //two situation 1:loader cann't load the class, and tmp == null; 2:loader load the class, but not == the classes[i], because the different class 
				throw new IllegalArgumentException(classes[i] + " is not visible from class loader."); //loader load the same class file, and result not equals.
			sb.append(inter).append(';');
		}
		//the interfaces class name list(split by ';')
		String key = sb.toString();
		Map<String, Object> cache = PROXY_CACHE_MAP.get(loader);
		if (cache == null) {
			synchronized (PROXY_CACHE_MAP) {
				cache = PROXY_CACHE_MAP.get(loader);
				if (cache == null) {
					cache = new HashMap<String, Object>();
					PROXY_CACHE_MAP.put(loader, cache);
				}
			}
		}
		
		Proxy proxy = null;
		synchronized (cache) {
			do {
				Object value = cache.get(key);
				if (value instanceof Reference<?>) {
					proxy = (Proxy) ((Reference<?>) value).get(); 
					if (proxy != null) { //the proxy object already created! and not be GC, the weak reference will be gc when gc thread GC, also it will be some times to be clear.
						return proxy;
					}
				}
				
				if (value == PENDING_GENERATION_FLAG) { //the proxy now be creating , so we need wait for the completion. and be notified.
					try {
						cache.wait(); //wait.
					} catch (InterruptedException e) {
						LOGGER.warn(e.getMessage(), e);
					}
				} else { // the else situation . the value be null. or reference.get() be null.
					cache.put(key, PENDING_GENERATION_FLAG);  //set the blocking flag, and break out the while loop to create the proxy.
					break;
				}
			} while (true);
		}
		
		long id = PROXY_CLASS_COUNTER.getAndIncrement();
		String pkg = null; //package name.
		ClassCodec ccp = null, ccm = null; 
		try {
			ccp = ClassCodec.newInstance(loader);
			Set<String> worked = new HashSet<String>(); //store the interfaces's methods desc.
			List<Method> methods = new ArrayList<Method>();// store the interfaces's methods.
			
			for (int i = 0; i < classes.length; i++) {
				if (!Modifier.isPublic(classes[i].getModifiers())) { // the interface modifier is not public, and this is the package level access.
					String npkg = classes[i].getPackage().getName();// and the meaning that if the non public interfaces exists, they must be the same package.
					if (pkg == null) {
						pkg = npkg;
					} else {
						if (!pkg.equals(npkg)) 
							throw new IllegalArgumentException("Non-public interfaces from different packages");
					}
				}
				ccp.addInterface(classes[i]);
				for (Method method : classes[i].getMethods()) {
					String desc = ReflectUtils.getDesc(method); // methodName(Ljava/lang/String;I)V
					if (worked.contains(desc))
						continue;
					worked.add(desc);
					int ix = methods.size();
					Class<?> rt = method.getReturnType();
					Class<?>[] pts = method.getParameterTypes();
					
					StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
					for (int j = 0; j < pts.length; j++) 
						code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");// ($w) the warp type , if the argument not primitive, or skip the wrap flag ($w). because $0 represent
					code.append(" Object ret = handler.invoke(this, methods[" + ix + "], args);"); //the  <code>this</code>, so we need start will <i>$1</i>.
					if ( !Void.TYPE.equals(rt)) //has return value;
						code.append(" return ").append(asArgument(rt, "ret")).append(";");
					methods.add(method);
					ccp.addMethod(method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
				}
			}
			if (pkg == null)
				pkg = PACKAGE_NAME;
			
			//create ProxyInstance class.
			String pcn = pkg + ".proxy" + id;
			ccp.setClassName(pcn);
			ccp.addField("public static java.lang.reflect.Method[] methods;");
			ccp.addField("private " + InvocationHandler.class.getName() + " handler;");
			ccp.addConstructor(Modifier.PUBLIC, new Class<?>[]{ InvocationHandler.class}, new Class<?>[0], "handler=$1;");
			ccp.addDefaultConstructor();
			Class<?> clz = ccp.toClass();
			clz.getField("methods").set(null, methods.toArray(new Method[0]));
			
			//create proxy class.
			String fcn = Proxy.class.getName() + id;
			ccm = ClassCodec.newInstance(loader);
			ccm.setClassName(fcn);
			ccm.addDefaultConstructor();
			ccm.addSuperClass(Proxy.class);
			ccm.addMethod("public Object newInstance(" + InvocationHandler.class.getName() + " h){\n return new " + pcn + "($1); \n}");
			Class<?> pc = ccm.toClass();
			proxy = (Proxy)pc.newInstance();
			
		} catch (RuntimeException e) {
			LOGGER.warn(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (ccp != null) 
				ccp.release();
			if (ccm != null) 
				ccm.release();
			synchronized (cache) {
				if (proxy == null) 
					cache.remove(key);
				else 
					cache.put(key, new WeakReference<Proxy>(proxy));
				cache.notifyAll();
			}
		}
		return proxy;
	}
	
	public Object newInstance() {
		return newInstance(THROW_UNSUPPORT_INVOKE);
	}
	
	public abstract Object newInstance(InvocationHandler handler);
	
	protected Proxy() {}
	
	private static String asArgument(Class<?> clazz, String name) {
		if (clazz.isPrimitive()) {
			if (Boolean.TYPE == clazz) 
				return name + " == null ? false : ((Boolean)" + name + ").booleanValue()";
			if (Byte.TYPE == clazz) 
				return name + " == null ? (byte) 0 : ((Byte)" + name + ").byteValue()";
			if (Character.TYPE == clazz) 
				return name + " == null ? (char) 0 : ((Character)" + name + ").charValue()";
			if (Short.TYPE == clazz) 
				return name + " == null ? (short) 0 : ((Short)" + name + ").shortValue()";
			if (Integer.TYPE == clazz) 
				return name + " == null ? (int) 0 : ((Integer)" + name + ").intValue()";
			if (Float.TYPE == clazz) 
				return name + " == null ? (float) 0 : ((Float)" + name + ").floatValue()";
			if (Double.TYPE == clazz) 
				return name + " == null ? (double) 0 : ((Double)" + name + ").doubleValue()";
			if (Long.TYPE == clazz) 
				return name + " == null ? (long) 0 : ((Long)" + name + ").longValue()";
			throw new RuntimeException(name + " is unknown primitive type.");
		}
		return "(" + ReflectUtils.getName(clazz) + ")" + name;
	}
	
	private static final String PACKAGE_NAME = Proxy.class.getPackage().getName();
	
	private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);
	
	public static final InvocationHandler RETURN_NULL_INVOKER = new InvocationHandler() {
		
		public Object invoke(Object proxy, Method method, Object[] args)
		        throws Throwable {
			return null;
		}
	};
	
	public static final InvocationHandler THROW_UNSUPPORT_INVOKE = new InvocationHandler() {

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			throw new UnsupportedOperationException(
					new StringBuilder( 64 )
					.append("Method [" + ReflectUtils.getName(method) + "]")
					.append("unimplemented.")
					.toString());
		}	
	};
	
	/**
	 * Here use the WeakHashMap, protect from occur the more memory,
	 */
	private static final Map<ClassLoader, Map<String, Object>> PROXY_CACHE_MAP = new WeakHashMap<ClassLoader, Map<String, Object>>();
	
	/**
	 * When the value in {@link #PROXY_CACHE_MAP #Map} is the lock object, explain the proxy
	 * now be generated, so waited for a time.
	 */
	private static final Object PENDING_GENERATION_FLAG = new Object();
}
