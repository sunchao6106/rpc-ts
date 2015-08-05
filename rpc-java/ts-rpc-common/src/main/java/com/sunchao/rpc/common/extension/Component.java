package com.sunchao.rpc.common.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

	/**
	 * the component representation, which 
	 * the underlying implementation can be swap be the 
	 * {@link #value()}.
	 * the method return string represent the defined
	 * implementation which specified by the alias.
	 * 
	 * @return
	 */
	public String value() default "";
	
}
