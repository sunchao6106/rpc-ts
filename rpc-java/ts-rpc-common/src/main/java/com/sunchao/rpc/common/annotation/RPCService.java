package com.sunchao.rpc.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * The Service impl annotation.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCService {
	/**
	 * 
	 * @return
	 *       The Service interface.
	 */
      Class<?> value();
      
}
