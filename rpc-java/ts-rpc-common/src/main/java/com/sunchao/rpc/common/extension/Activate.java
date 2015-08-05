package com.sunchao.rpc.common.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Activate
 * <p>
 * The annotation used to configure the auto activate
 * load condition. filter the extensions ,if there are many
 * implementation.
 * </p>
 * <li>{@link Activate#group()}</li>the group value is assigned by the spi;
 * <li>{@link Activate#value()}</li> if exists in the url ,valid.
 * <p>
 * @see ExtensionLoader#getActivateExtension(URL,String[], String);
 *</p>
 * 
 * @author sunchao
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Activate {

	/**
	 * the group filter condition.
	 * @return
	 */
	String[] group()  default {};
	
	/**
	 * get the value of url .
	 * @return
	 */
	String[] value() default {};
	/**
	 * 
	 * @return
	 */
	String[] before() default {};
	
	String[] after() default {};
	
	int order() default 0;
}
