package com.sunchao.rpc.common.compiler.support;

import com.sunchao.rpc.common.extension.HotSwap;
import com.sunchao.rpc.common.extension.HotSwapLoader;
import com.sunchao.rpc.common.compiler.Compiler;
/**
 * AdaptiveCompiler. (SPI, Singleton, ThreadSafe)
 * @author sunchao
 *
 */
@HotSwap
public class AdaptiveCompiler implements Compiler {
	
	private static volatile String DEFAULT_COMPILER;
	
	public static void setDefaultCompiler(String compiler) {
		DEFAULT_COMPILER = compiler;
	}

	public Class<?> compile(String code, ClassLoader classLoader) {
		Compiler compiler;
		HotSwapLoader<Compiler> loader = HotSwapLoader.getExtensionLoader(Compiler.class);
		String name = DEFAULT_COMPILER;
		if (name != null && name.length() > 0) {
			compiler = loader.getExtension(name);
		} else {
			compiler = loader.getDefaultExtension();
		}
		return  compiler.compile(code, classLoader);
	}

}
