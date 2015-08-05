package com.sunchao.rpc.base.codec;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.sunchao.rpc.base.ReflectUtils;

/**
 * Delegate.
 * The delegate implement.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public abstract class Delegate {
	
	/**
	 * The interface array which need to be implement delegated by
	 * the <i>dcs</i> class array.
	 * 
	 * @param ics
	 *        the interface class array.
	 * @param dcs
	 *        the delegate class array.
	 * @param loader
	 *        the class loader.
	 * @return
	 */
	public static Delegate delegate(Class<?>[] ics, Class<?>[] dcs, ClassLoader loader) {
		assertInterfaceArray(ics);
		long id = DELEGATE_CLASS_COUNTER.getAndIncrement();
		String pkg = null;
		ClassCodec ccp = null, ccm = null;
		try {
		
			ccp = ClassCodec.newInstance(loader);
			//impl constructor .
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < dcs.length; i++) {
				if (! Modifier.isPublic(dcs[i].getModifiers())) {
					String npkg = dcs[i].getPackage().getName();
					if (pkg == null) {
						pkg = npkg;
					} else {
						if (!pkg.equals(npkg))
							throw new IllegalArgumentException("Non-public interfaces class from differnet packages!");
					}
				}
				ccp.addField("private " + dcs[i].getName() + "  d" + i + ";\n");
				//sb.append(" d").append(i).append(" = ").append(" $1[").append(i + "];\n");
			    sb.append("d").append(i).append(" = (").append(dcs[i].getName()).append(") $1[").append(i).append("];\n");
				//sb.append("d").append(i).append("= (").append(dcs[1].getName()).append(") $1[").append(0).append("]; \n");
				if (DelegateAware.class.isAssignableFrom(dcs[i]))  {
				   sb.append("d").append(i).append(".setDelegateInstance(this);\n");
				}
			}
			ccp.addConstructor(Modifier.PUBLIC, new Class<?>[]{Object[].class}, sb.toString());
			
			Set<String> worked = new HashSet<String>();
			for (int i = 0; i < ics.length; i++) {
				if (!Modifier.isPublic(ics[i].getModifiers())) {
					String npkg = ics[i].getPackage().getName();
					if (pkg == null) {
						pkg = npkg;
					} else {
						if (pkg != npkg) {
							throw new IllegalArgumentException("Non-public delegate class from different packages");
						}
					}
				}
				ccp.addInterface(ics[i]);
				
				for (Method method : ics[i].getMethods()) {
					if ("java.lang.Object".equals(method.getDeclaringClass().getName()))
						continue;
					String desc = ReflectUtils.getDesc(method);
					if (worked.contains(desc))
						continue;
					worked.add(desc);
					int idx = findMethod(dcs, desc);
					if (idx < 0) 
						throw new RuntimeException("Missing method [" + desc + "] implement.");
					Class<?> rt = method.getReturnType();
					String mn = method.getName();
					if (Void.TYPE.equals(rt)) {
						ccp.addMethod(mn, method.getModifiers(),rt, method.getParameterTypes(), method.getExceptionTypes(),
								"\n d" + idx + "." + mn +"($$);" );
					} else {
						ccp.addMethod(mn, method.getModifiers(), rt, method.getParameterTypes(), method.getExceptionTypes(),
								"\n return ($r)d" + idx + "." + mn + "($$);");
					}
				}
			}
			if (pkg == null)
				pkg = PACKAGE_NAME;
				
			String dele = pkg + ".dele" + id;
			ccp.setClassName(dele);
			ccp.toClass();
				
			String fcn = Delegate.class.getName() + id;
			ccm = ClassCodec.newInstance(loader);
			ccm.setClassName(fcn);
			ccm.addDefaultConstructor();
			ccm.addSuperClass(Delegate.class);
			ccm.addMethod("public Object newInstance(Object[] delegates){\n return new " + dele + "($1); }");
			Class<?> delegate = ccm.toClass();
			return (Delegate) delegate.newInstance();
			//return null;
		} catch (RuntimeException e) {
		//	e.printStackTrace();
			throw e;
		} catch (Exception e) {
			//e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (ccp != null)
				ccp.release();
			if (ccm != null)
				ccm.release();
		}
	}
	
	/**
	 * 
	 * @param ics
	 * @param dcs
	 *        the delegate class array.
	 * @return
	 */
	public static Delegate delegate(Class<?>[] ics, Class<?>[] dcs) {
		return delegate(ics, dcs, Delegate.class.getClassLoader());
	}
	
	/**
	 * the interface array which want to be implement delegate by
	 * the delegate class 
	 * @param ics
	 *          the interface class array.
	 * @param dc
	 *          the delegate class.
	 * @param loader
	 *          the class loader.
	 * @return
	 */
	public static Delegate delegate(Class<?>[] ics, Class<?> dc, ClassLoader loader) {
		return delegate(ics, new Class<?>[]{dc}, loader);
	}
	
	/**
	 * the interface array which want to be implement delegated by
	 * the delegate class.
	 * all the interface class must be public.
	 * 
	 * @param ics
	 *         the interface class array.
	 * @param dc
	 *         the delegate class.
	 * @return
	 */
	public static Delegate delegate(Class<?>[] ics, Class<?> dc) {
		return delegate(ics, new Class<?>[]{dc});
	}
	
	public static interface DelegateAware {
		
		void setDelegateInstance(Object instance);
	}
	
	/**
	 * new Delegate instance.
	 * @param dcs
	 *         the delegate instances.
	 * @return
	 *         the instance.
	 */
	public abstract Object newInstance(Object[] dcs);
	
	protected Delegate() {}
	
	private static int findMethod(Class<?>[] dcs, String desc) {
		Class<?> cl;
		Method[] methods;
		for (int i = 0; i < dcs.length; i++) {
			cl = dcs[i];
			methods = cl.getMethods();
			for (Method method : methods) {
				if (desc.equals(ReflectUtils.getDesc(method))) 
					return i;
			}
		}
		return -1;
	}
	
	private static void assertInterfaceArray(Class<?>[] ics) {
		for (int i = 0; i < ics.length; i++) {
			if (!ics[i].isInterface()) 
				throw new IllegalArgumentException("Class(" + ics[i].getName() + ") is not a interface.");
		}
	}
	
	private static final String PACKAGE_NAME = Delegate.class.getPackage().getName();
	
	private static AtomicLong DELEGATE_CLASS_COUNTER = new AtomicLong(0);

}
