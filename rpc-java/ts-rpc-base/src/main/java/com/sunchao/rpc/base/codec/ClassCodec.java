package com.sunchao.rpc.base.codec;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.sunchao.rpc.base.ReflectUtils;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * Javassist class dynamic generate.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class ClassCodec {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassCodec.class);
	
	private static final AtomicLong CLASS_NAME_COUNTER = new AtomicLong(0);
	
	private static final String CONSTRUCTOR_TAG = "<init>"; 
	
	public static interface DyInter{}; //dynamic generated class common interface flag.
	
	/**
	 * the classLoader => classPool map_cache, because the <code>ClassPool</code> creation is expensive.
	 */
	private static final Map<ClassLoader, ClassPool> POOL_CACHE = new ConcurrentHashMap<ClassLoader, ClassPool>();
	
	/**
	 * Get the ClassPool mapped to the ClassLoader
	 * 
	 * @param classLoader
	 * @return
	 */
	public static ClassPool getClassPool(ClassLoader classLoader) {
		if (classLoader == null) {
			return ClassPool.getDefault();
		} 
		ClassPool pool = POOL_CACHE.get(classLoader);
		if (pool == null) {
			 pool = new ClassPool(true);
			 pool.appendClassPath(new LoaderClassPath(classLoader)); // add the search path; if use the ClassPool.getDefault(), it will work on JVM environment.
			 POOL_CACHE.put(classLoader, pool);
		}
		return pool;
	}
	
	public static ClassCodec newInstance() {
		return new ClassCodec(getClassPool(Thread.currentThread().getContextClassLoader()));
	}
	
	public static ClassCodec newInstance(ClassLoader classLoader) {
		return new ClassCodec(getClassPool(classLoader));
	}
	
	/**
	 * Judge the specified class whether or not belong to the Dynamic generated class.
	 * @param cls
	 * @return
	 */
	public static boolean isDynamicGeneratedClass(Class<?> cls) {
		return ClassCodec.DyInter.class.isAssignableFrom(cls);
	}
	
	
	public String getClassName() {
		return this.mClassName;
	}
	
	/**
	 * Fluent style.
	 * 
	 * @param className
	 * @return
	 */
	public ClassCodec setClassName(String className) {
		this.mClassName = className;
		return this;
	}
	
	public ClassCodec addInterface(String it) {
		if (mInterfaces == null) 
			 mInterfaces = new HashSet<String>();
		mInterfaces.add(it);
		return this;
	}
	
	public ClassCodec addInterface(Class<?> cls) {
		return addInterface(cls.getName());
	}
	
	public ClassCodec addSuperClass(String sn) {
		mSuperClass = sn;
		return this;
	}
	
	public ClassCodec addSuperClass(Class<?> cls) {
		return addSuperClass(cls.getName());
	}
	
	public ClassCodec addField(String code) {
		if (mFields == null) {
			mFields = new ArrayList<String>();
		}
		mFields.add(code);
		return this;
	}
	
	/****************************************field******************************************************************/
	public ClassCodec addField(String name, int mod, Class<?> type) {
		return addField(name, mod, type ,null);
	}
	
	public ClassCodec addField(String name, int mod, Class<?> type, String defa) {
		StringBuilder sb =  new StringBuilder( 64 )
		.append(getModifier(mod)).append(' ')
		.append(ReflectUtils.getName(type)).append(' ')
		.append(name);
		if (defa != null && defa.length() > 0) {
			sb.append('=');
			sb.append(defa);
		}
		sb.append(';');
		return addField(sb.toString());
	}
	
	/**************************************method**************************************************************/
	
	public ClassCodec addMethod(String code) {
		if (mMethods == null) 
			mMethods = new ArrayList<String>();
		mMethods.add(code);
		return this;
	}
	
	public ClassCodec addMethod(String name, int mod, Class<?> rt, Class<?>[] pts, 
			String bodycode) {
		return addMethod(name, mod, rt, pts, null, bodycode);
	}
	
	public ClassCodec addMethod(String name, int mod, Class<?> rt, Class<?>[] pts,
			Class<?>[] ets, String bodycode) {
		StringBuilder sb = new StringBuilder( 256 )
		.append(getModifier(mod)).append(' ')
		.append(ReflectUtils.getName(rt)).append(' ')
		.append(name).append('(');
		for (int i = 0; i < pts.length; i++) {
			if (i > 0)
				sb.append(',');
			sb.append(ReflectUtils.getName(pts[i]));
			sb.append(" arg").append(i);
		}
		sb.append(')');
		if (ets != null && ets.length > 0) {
			sb.append(" throws ");
			for (int i = 0; i < ets.length; i++) {
				if (i > 0)
					sb.append(',');
				sb.append(ReflectUtils.getName(ets[i]));
			}
		}
		sb.append('{').append(bodycode).append('}');
		return addMethod(sb.toString());
	}
	
	public ClassCodec addMethod(Method m) {
		addMethod(m.getName(), m);
		return this;
	}
	
	public ClassCodec addMethod(String name, Method m) {
		String desc = name + ReflectUtils.getDescWithoutMethodName(m);
		addMethod('?' + desc);
		if (mCopyMethods == null) 
			mCopyMethods = new ConcurrentHashMap<String, Method>(8);
		mCopyMethods.put(desc, m);
		return this;
	}
	
	
	/**********************************constructor*********************************************************/
	public ClassCodec addConstructor(String code) {
		if (mConstructors == null) 
			   mConstructors = new LinkedList<String>();
		mConstructors.add(code);
		return this;
	}
	
	public ClassCodec addConstructor(int mod, Class<?>[] pts, String bodycode) {
		return addConstructor(mod, pts, null, bodycode);
	}
	
	public ClassCodec addConstructor(int mod, Class<?>[] pts, Class<?>[] ets,
			String bodycode) {
		StringBuilder sb = new StringBuilder( 128 )
		.append(getModifier(mod)).append(' ').append(CONSTRUCTOR_TAG);
		sb.append('(');
		for (int i = 0; i < pts.length; i++) {
			if (i > 0) 
				sb.append(',');
			sb.append(ReflectUtils.getName(pts[i]));
			sb.append(" arg").append(i);
		}
		sb.append(')');
		if (ets != null && ets.length > 0) {
			sb.append(" throws ");
			for (int i = 0; i < ets.length; i++) {
				if (i > 0) 
					sb.append(',');
				sb.append(ReflectUtils.getName(ets[i]));
			}
		}
		sb.append('{').append(bodycode).append('}');
		return addConstructor(sb.toString());
	}
	
	public ClassCodec addConstructor(Constructor<?> c) {
		String desc = ReflectUtils.getDesc(c);
		addConstructor('?' + desc);
		if (mCopyConstructors == null) 
			mCopyConstructors = new ConcurrentHashMap<String, Constructor<?>>(4);
		mCopyConstructors.put(desc, c);
		return this;
	}
	
	public ClassCodec addDefaultConstructor() {
		this.mDefaultConstructor = true;
		return this;
	}
	
	public ClassPool getClassPool() {
		return this.mPool;
	}
	
	public Class<?> toClass() {
		return toClass(getClass().getClassLoader(), getClass().getProtectionDomain());
	}
	
	public Class<?> toClass(ClassLoader loader, ProtectionDomain pd) {
		if (mClass != null) 
			mClass.detach(); //remove from the Class Pool.
		long id = CLASS_NAME_COUNTER.getAndIncrement();
		try {
			CtClass ctcs = this.mSuperClass == null ? null : mPool.get(mSuperClass); //get the super Ctclass from the pool.
			if (mClassName == null) 
				mClassName = (mSuperClass == null || javassist.Modifier.isPublic(ctcs.getModifiers())
				            ? ClassCodec.class.getName() : mSuperClass + "$sc") + id;
			mClass = mPool.makeClass(mClassName);
			if (mSuperClass != null) 
				mClass.setSuperclass(ctcs);
			mClass.addInterface(mPool.get(DyInter.class.getName())); //add the dynamic generated tag.
			
			if (mInterfaces != null) {
				for (String desc : mInterfaces)
					mClass.addInterface(mPool.get(desc));
			}
			
			if (mFields != null) {
				for (String code : mFields)
					mClass.addField(CtField.make(code, mClass));
			}
			
			if (mMethods != null) {
				for (String code : mMethods) {
					if (code.charAt(0) == '?') {
						mClass.addMethod(CtNewMethod.copy(getCtMethod(mCopyMethods.get(code.substring(1))), 
								code.substring(1, code.indexOf('(')), mClass, null));
					} else {
						mClass.addMethod(CtNewMethod.make(code, mClass));
					}
				}
			}
			
			if (this.mDefaultConstructor) {
				mClass.addConstructor(CtNewConstructor.defaultConstructor(mClass));
			}
			
			if (this.mConstructors != null) {
				for (String code : this.mConstructors) {
					if (code.charAt(0) == '?') {
						mClass.addConstructor(CtNewConstructor.copy(getCtConstructor(mCopyConstructors.get(
								code.substring(1))), mClass, null));
					} else {
						String[] sn = mClass.getSimpleName().split("\\$+"); //inner class name include $.
						mClass.addConstructor(CtNewConstructor.make(code.replaceFirst(CONSTRUCTOR_TAG, sn[sn.length - 1]), mClass));
					}
				}
			}
			return mClass.toClass(loader, pd);
		} catch (RuntimeException e) {
			throw e;
		} catch (NotFoundException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} catch (CannotCompileException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
			
	}
	
	
	private static String getModifier(int mod) {
		if (Modifier.isPublic(mod))
			return "public";
		else if (Modifier.isProtected(mod))
			return "protected";
		else if (Modifier.isPrivate(mod))
			return "private";
		else 
			return "";
	}
	
	/**
	 * release operation .
	 * because the class pool is heavy resource, so to prevent the memory leak.
	 * after the <code>toClass</code> operation, to clear the memory footprint.
	 */
	public void release() {
		if (this.mClass != null)
			this.mClass.detach();
		if (this.mInterfaces != null)
			this.mInterfaces.clear();
		if (this.mFields != null)
			this.mFields.clear();
		if (this.mMethods != null)
			this.mMethods.clear();
		if (this.mConstructors != null)
			this.mConstructors.clear();
		if (this.mCopyConstructors != null)
			this.mCopyConstructors.clear();
		if (this.mCopyMethods != null) {
			this.mCopyMethods.clear();
		}
	}
	
	private CtClass getCtClass(Class<?> c) throws Exception {
		return this.mPool.get(c.getName());
	}
	
	private CtMethod getCtMethod(Method m) throws Exception {
		return getCtClass(m.getDeclaringClass()).getMethod(m.getName(), ReflectUtils.getDescWithoutMethodName(m));
	}
	
	private CtConstructor getCtConstructor(Constructor<?> c) throws Exception {
		return getCtClass(c.getDeclaringClass()).getConstructor(ReflectUtils.getDesc(c));
	}
	
	/**
	 * <code>Javassist</code> class generated specified arguments.
	 * 
	 */
	
	private ClassPool mPool;
	
	private CtClass mClass;
	
	private String mClassName, mSuperClass;
	
	private Set<String> mInterfaces;
	
	private List<String> mFields, mConstructors, mMethods;
	
	private Map<String, Method> mCopyMethods;//<method desc, method instance>
	
	private Map<String, Constructor<?>> mCopyConstructors; //<constructor desc, constructor instance>
	
	private boolean mDefaultConstructor = false;
	
	private ClassCodec() {}
	
	private ClassCodec(ClassPool pool) {
		this.mPool = pool;
	}
}
