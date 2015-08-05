package com.sunchao.rpc.common.compiler.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sunchao.rpc.common.compiler.Compiler;
import com.sunchao.rpc.common.utils.ClassUtil;


/**
 * Abstract compiler. (SPI, Prototype, ThreadSafe)
 * @author sunchao
 *
 */
public abstract class AbstractCompiler implements Compiler {

	private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");
	private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");
	
	public Class<?> compile(String code, ClassLoader classLoader) {
	   code = code.trim();
	   Matcher matcher = PACKAGE_PATTERN.matcher(code);
	   String pkg;
	   if (matcher.find()) {
		   pkg = matcher.group(1);
	   } else {
		   pkg = "";
	   }
	   matcher = CLASS_PATTERN.matcher(code);
	   String cls;
	   if (matcher.find()) {
		   cls = matcher.group(1);
	   } else {
		   throw new IllegalArgumentException("No such class name in " + code);
	   }
	   String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
	   try {
		   return Class.forName(className, true, ClassUtil.getCallerClassLoader(getClass()));//  static class loader.
	   } catch (ClassNotFoundException e) {
		   if (! code.endsWith("}")) {
			   throw new IllegalStateException("The java code not endsWith \"}\", code: \n" + code + "\n");
		   }
		   try {
			   return doCompile(className, code); //dynamic class byte code generate, and class load.
		   } catch (RuntimeException e1) {
			   throw e1;
		   } catch (Throwable t) {
			   throw new IllegalStateException("Failed to compile class, cause: " + className + ", code: \n" +code +
					   "\n, stack: "  + ClassHelper.toString(t));
		   }
	   }  
	}
	
	/**
	 * template method. sub class to implement this method, 
	 * @param name
	 * @param source
	 * @return
	 * @throws Throwable
	 */
	protected abstract Class<?> doCompile(String name, String source) throws Throwable;

}
