package com.sunchao.rpc.base.serializer.support.varint.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.WeakHashMap;

/**
 * Reflect Helper class for class loader.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
class ASMClassLoader extends ClassLoader {
	

	/**
	 * Weak-reference to class loaders, to avoid perm gen memory leaks.
	 * The key is the parent class loader and the value is ASMClassLoader, both are weak-reference in the hash table.
	 */
	static private final WeakHashMap<ClassLoader, WeakReference<ASMClassLoader>> ACCESSCLASSLOADERS = 
			new WeakHashMap<ClassLoader, WeakReference<ASMClassLoader>>();
	//Fast-path for classes loaded in the same ClassLoader as this class.
	static private final ClassLoader selfContextParentClassLoader = getParentClassLoader(ASMClassLoader.class);
    static private volatile ASMClassLoader selfContextAccessClassLoader = new ASMClassLoader(selfContextParentClassLoader);
    
    static private volatile Method defineClassMethod;
    
    @SuppressWarnings("rawtypes")
	static ASMClassLoader get(Class type) {
    	ClassLoader parent = getParentClassLoader(type);
    	//1: fast-path:
    	if (selfContextParentClassLoader.equals(parent)) {
    		if (selfContextAccessClassLoader == null) {
    			synchronized (ACCESSCLASSLOADERS) {
    				if (selfContextAccessClassLoader == null) 
    					selfContextAccessClassLoader = new ASMClassLoader(selfContextParentClassLoader);
    			}
    		}
    		return selfContextAccessClassLoader;
    	}
    	//2: normal search
    	synchronized (ACCESSCLASSLOADERS) {
    		WeakReference<ASMClassLoader> ref = ACCESSCLASSLOADERS.get(parent);
    		if (ref != null) {
    			ASMClassLoader accessClassLoader = ref.get();
    			if (accessClassLoader != null)
    				return accessClassLoader;
    			else 
    				ACCESSCLASSLOADERS.remove(parent);
    		}
    		ASMClassLoader accessClassLoader = new ASMClassLoader(parent);
    		ACCESSCLASSLOADERS.put(parent, new WeakReference<ASMClassLoader>(accessClassLoader));
    		return accessClassLoader;
    	}
    }
    
    public static int activeAccessClassLoader() {
    	int size = ACCESSCLASSLOADERS.size();
    	if (selfContextAccessClassLoader != null)
    		size++;
    	return size;
    }
    
    public static void remove(ClassLoader parent) {
    	//1:fast-path.
    	if (selfContextParentClassLoader.equals(parent)) {
    		selfContextAccessClassLoader = null;
    	} else {
    		synchronized(ACCESSCLASSLOADERS) {
    			ACCESSCLASSLOADERS.remove(parent);
    		}
    	}
    }
    
    /**
     * "The runtime package of a class or interface is determined by the package name and defining class loader
     * of the class or interface."
     * @param t1
     * @param t2
     * @return
     */
    static boolean areInSameRuntimeClassLoader(@SuppressWarnings("rawtypes") Class t1, @SuppressWarnings("rawtypes") Class t2) {
    	if (t1.getPackage() != t2.getPackage()) 
    		return false;
    	ClassLoader loader1 = t1.getClassLoader();
    	ClassLoader loader2 = t2.getClassLoader();
    	ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    	if (loader1 == null) 
    		return (loader2 == null || loader2 == systemClassLoader);
    	if (loader2 == null)
    		return loader1 == systemClassLoader;
    	return loader1 == loader2;
    }
    
    protected synchronized java.lang.Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    	if (name.equals(ASMFieldAccess.class.getName())) return ASMFieldAccess.class;
    	return super.loadClass(name, resolve);
    }
    
    Class<?> defineClass(String name, byte[] bytes) throws ClassFormatError {
    	try {
    		return (Class<?>) getDefineClassMethod().invoke(getParent(), new Object[] {name, bytes, 0, bytes.length,
    			getClass().getProtectionDomain()});
    	} catch (Exception ignored) {}
    	return defineClass(name, bytes, 0, bytes.length, getClass().getProtectionDomain());
    }
    
    @SuppressWarnings("rawtypes")
	private static ClassLoader getParentClassLoader(Class type) {
    	ClassLoader parent = type.getClassLoader();
    	if (parent == null) parent = ClassLoader.getSystemClassLoader();
    	return parent;
    }
    
    private static Method getDefineClassMethod() throws Exception {
    	//DCL on volatile.
    	if (defineClassMethod == null) {
    		synchronized (ACCESSCLASSLOADERS) {
    			defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class,
    					int.class, ProtectionDomain.class});
    			try {
    				defineClassMethod.setAccessible(true);
    			} catch (Exception ignored) {}
    		}
    	}
    	return defineClassMethod;
    }
    
    private ASMClassLoader(ClassLoader parent) {
    	super(parent);
    }
}
