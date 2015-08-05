package com.sunchao.rpc.base.client;

import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javassist.Modifier;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.sunchao.rpc.common.utils.ReflectUtil;

import static org.objectweb.asm.Opcodes.*;

@Deprecated
public abstract class AsmProxy {
	
	private static final AtomicLong CLASS_GEN_COUNT = new AtomicLong(0);
	
	private static final String CLASS_NAME = AsmProxy.class.getName().replace('.', '/');
	private static final String GEN_NAME_PREFIX = CLASS_NAME + '$';
	
	private static final Map<ClassLoader, Map<String, Object>> PROXY_CACHE = new WeakHashMap<ClassLoader, Map<String, Object>>();
	
	private static final Object pendingGenerationMarker = new Object();
	
	public static AsmProxy getProxy(Class<?>...classes) throws Exception {
		return getProxy(AsmProxy.class.getClassLoader(), classes);
	}
	

	public static AsmProxy getProxy(ClassLoader loader, Class<?>... classes) throws Exception {
		if (classes.length > 65535) 
			throw new IllegalArgumentException("interface limit exceed.");
		
		ArrayList<String> interfaceNames = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < classes.length; i++) {
			String interfaceName = classes[i].getName();
			if (!classes[i].isInterface()) 
				throw new IllegalArgumentException("the type: " + interfaceName + " is not a interface.");
			
			Class<?> tmp = null;
			//test whether the interface is visible to the class loader.
			try {
				tmp = Class.forName(interfaceName, false, loader);
			} catch (ClassNotFoundException e) {}
			
			if (tmp != classes[i]) 
				throw new IllegalArgumentException(classes[i] + " is not visible from class loader.");
			
			sb.append(interfaceName).append(";");
		}
		
		//String[] interfaces = interfaceNames.toArray(new String[0]);
		
		String interfaceKey = sb.toString();
		
		//get cache by class loader.
		Map<String, Object> cache;
		
		synchronized (PROXY_CACHE) {
			cache = PROXY_CACHE.get(loader);
			if (cache == null) {
				cache = new HashMap<String, Object>();
				PROXY_CACHE.put(loader, cache);
			}
		}
		
		AsmProxy proxy = null;
		synchronized (cache) {
			do {
				Object value = cache.get(interfaceKey);
				if (value instanceof Reference<?>) {
					proxy = (AsmProxy) ((Reference<?>) value).get();
					if (proxy != null) 
						return proxy;
				}
				
				if (value == pendingGenerationMarker) {
					   try {
						cache.wait();
					} catch (InterruptedException e) {
						//ignore.
					}
				} else {
					cache.put(interfaceKey, pendingGenerationMarker);
					break;
				}
			} while (true);
		}
		
		//check the non-public interface whether come from the same package.
		String pkg = null;//package name.
		
		for (int i = 0; i < classes.length; i++) {
			if (!Modifier.isPublic(classes[i].getModifiers())) {
				String pkgx = classes[i].getPackage().getName();
				if (pkg == null) 
					pkg = pkgx;
				else {
					if (!pkg.equals(pkgx))
						throw new IllegalArgumentException("Non-public interfaces must be from the same package.");
				}
			}
			
			interfaceNames.add(classes[i].getName().replace('.', '/'));
		}
		
		String[] interfaces = interfaceNames.toArray(new String[0]);
		try {
		      long id = CLASS_GEN_COUNT.getAndIncrement();
		      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		      MethodVisitor mv;
		      cw.visit(V1_1, ACC_PUBLIC, GEN_NAME_PREFIX + id, null, CLASS_NAME, interfaces);
		      
		      {
			     //default constructor
		       	mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			    mv.visitCode();
			    mv.visitVarInsn(ALOAD, 0);
			    mv.visitMethodInsn(INVOKESPECIAL, CLASS_NAME, "<init>", "()V");
			    mv.visitInsn(RETURN);
			    mv.visitMaxs(0, 0);
			    mv.visitEnd();
		      }
		
		        //prevent the same method signature exists.
		      Set<String> methodDesc = new HashSet<String>();
		      List<Method> methods = new ArrayList<Method>();
		     
		      
	          {
			     for (int k = 0; k < classes.length; k++) {
				     for (Method method : classes[k].getMethods()) {
					      String desc = ReflectUtil.getDesc(method);
					      if (methodDesc.contains(desc))
					    	   continue;
					      methodDesc.add(desc);
					      methods.add(method);
					     
					      int index = methods.size();
					      Class<?>[] pts = method.getParameterTypes();
					      Class<?> rt = method.getReturnType();
					      mv = cw.visitMethod(ACC_PUBLIC, method.getName(), ReflectUtil.getDescWithoutMethodName(method),
					    		  null, null);
					      mv.visitCode();
					      StringBuilder buffer = new StringBuilder('(');
					      for (int i = 0; i < pts.length; i++) {
					    	  mv.visitVarInsn(ALOAD, i + 1);
					    	  mv.visitInsn(AALOAD);
					    	  Type paramType = Type.getType(pts[i]);
					    	  switch (paramType.getSort()) {
					    	  case Type.BOOLEAN:
					    			mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booeanVaule", "()Z");
									break;
								case Type.BYTE:
									mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
									break;
								case Type.CHAR:
									mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charVaule", "()C");
									break;
								case Type.SHORT:
									mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
									break;
								case Type.INT:
									mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intVaule", "()I");
									break;
								case Type.LONG:
									mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
									break;
								case Type.FLOAT:
									mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatVaule", "()F");
									break;
								case Type.DOUBLE:
									mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
									mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
									break;
								case Type.ARRAY:
									mv.visitTypeInsn(CHECKCAST, paramType.getDescriptor());
								//	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booeanVaule", "()Z");
									break;
								case Type.OBJECT:
									mv.visitTypeInsn(CHECKCAST, paramType.getInternalName());
									//mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
									break;
								}
                                buffer.append(paramType.getDescriptor());					    	  
					      }
					      buffer.append(')');
					      buffer.append(Type.getDescriptor(rt));
				     }
			     }
	          }
		return null;
	} finally {
		
	}
	}
}
