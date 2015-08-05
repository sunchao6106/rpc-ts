package com.sunchao.rpc.common.compiler;

import com.sunchao.rpc.common.extension.Component;

/**
 * Compiler. (SPI.Singleton, ThreadSafe)
 * @author sunchao
 *
 */
@Component("javassist")
public interface Compiler {

	/**
	 * compile java source code dynamic.(bcel,asm,javassist, cglib)
	 * @param code
	 *           java source code.
	 * @param classLoader
	 *            class loader.
	 * @return
	 *        compiled class object.
	 */
	Class<?> compile(String code, ClassLoader classLoader);
}
