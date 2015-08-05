package com.sunchao.rpc.common.compiler.support;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import com.sunchao.rpc.common.compiler.support.AbstractCompiler;
import com.sunchao.rpc.common.utils.ClassUtil;
/**
 * JavassistCompiler (ThreadSafe, Singleton, SPI).
 * 
 * @author sunchao
 *
 */
public class JavassistCompiler extends AbstractCompiler {
    /** 
     * the regex pattern of import.
     *  e.g.  import java.util.*;   import java.util.List;   
     */
	private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");
	/**
	 * the regex pattern of extends and not 
	 * the expression of "xxx extends yyy { ".
	 * or "xxx extends yyy implements xxxx {"
	 * e.g.  xxx  extends xxxx.xxx.yyy;
	 */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");
	/**
	 * the regex expression of implements.
	 * e.g. implements com.xxx.yyyy , com.zzz.IIII {
	 * is this regex is right ???? e.g.  "Hello implements Serializable, Coneable { \n" 
	 */
   // private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w\\.]+)\\s*\\{\n");
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w\\.\\, ]+)\\s*\\{\n");
	/**
	 * the regex expression of method desc.
	 */
    private static final Pattern METHOD_PATTERN = Pattern.compile("\n(private|public|protected)\\s+");
	/**
	 * xxxx =  xxxx;
	 */
    private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;");
    
    public static void main(String args[]) {
    	String str = "Hello extends Hello{\n";
    	Matcher m =  EXTENDS_PATTERN.matcher(str);
    	while (m.find()) {
    		String s = m.group(1);
    		System.out.println(s);
    	}
    }
    
    public String hello() {
    	return "hello";
    }
	
	/**
	 * @param name
	 *       the class name.
	 * 
	 * @param source
	 *       the class text desc. 
	 */      
	@Override
	protected Class<?> doCompile(String name, String source) throws Throwable {
		int i = name.lastIndexOf('.'); // com.xxx.MyClass
		String className = i < 0 ? name : name.substring(i + 1);
		ClassPool pool = new ClassPool(true);
		pool.appendClassPath(new LoaderClassPath(ClassUtil.getCallerClassLoader(getClass())));// get the class loader of this class.
		Matcher matcher = IMPORT_PATTERN.matcher(source);
		List<String> importPackages = new ArrayList<String>();
		Map<String, String> fullNames = new HashMap<String, String>();
		while (matcher.find()) {
			String pkg = matcher.group(1);
			if (pkg.endsWith(".*")) {
				String pkgName = pkg.substring(0, pkg.length() - 2);
				pool.importPackage(pkgName); //import the import package.
				importPackages.add(pkgName);
			} else {
				int pi = pkg.lastIndexOf('.');
				if (pi > 0) {
					String pkgName = pkg.substring(0, pi);
					pool.importPackage(pkgName);
					importPackages.add(pkgName);
					fullNames.put(pkg.substring(pi + 1), pkg);
				}
			}
		}
		String[] packages = importPackages.toArray(new String[0]);
		matcher = EXTENDS_PATTERN.matcher(source);
		CtClass cls;
		if (matcher.find()) {
			String extend = matcher.group(1).trim();
			String extendClass;
			if (extend.contains(".")) {
				extendClass = extend;
			} else if (fullNames.containsKey(extend)) {
				extendClass = fullNames.get(extend);
			} else {
				extendClass = ClassHelper.forName(packages, extend).getName();
			}
			cls = pool.makeClass(name, pool.get(extendClass)); // the ctclass created form   classpool.makeclass(string, superclass).
		} else {
			cls = pool.makeClass(name); // has no super class.
		}
		
		matcher = IMPLEMENTS_PATTERN.matcher(source);
		if (matcher.find()) { // \w includes [a-zA-Z0-9_]
			String[] interfaces = matcher.group(1).trim().split("\\,"); // is there  possible
			for (String inter : interfaces) {
				inter = inter.trim();
				String interfaceClass;
				if (inter.contains(".")) {
					interfaceClass = inter;
				} else if (fullNames.containsKey(inter)) {
					interfaceClass = fullNames.get(inter);
				} else {
					interfaceClass = ClassHelper.forName(packages, inter).getName();
				} 
				cls.addInterface(pool.get(interfaceClass));
			}
		}
		String body = source.substring(source.indexOf("{") + 1, source.length() - 1); //public class xxxx {}
		
		String[] methods  = METHOD_PATTERN.split(body);
		for (String method : methods) {
			method = method.trim();
			if (method.length() > 0) {
				if (method.startsWith(className)) {
					cls.addConstructor(CtNewConstructor.make("public " + method, cls));
				} else if (FIELD_PATTERN.matcher(method).matches()) {
					cls.addField(CtField.make("private " + method, cls));
				} else {
					cls.addMethod(CtNewMethod.make("public " + method, cls));
				}
			}
		}
		return cls.toClass(ClassUtil.getCallerClassLoader(getClass()), JavassistCompiler.class.getProtectionDomain());
	}

}
