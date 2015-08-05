package com.sunchao.rpc.common.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link HotSwapLoader} create extension's adaptive instance
 * 
 * @author sunchao
 * @see ExtensionLoader.
 * @see URL
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface HotSwap {
	
	/**
	 * The annotation work the interface or class 
	 * or method. which denote the implementation
	 * can be swapped.
	 * 
	 * @return
	 */
	String[] value() default {};

}
