package com.sunchao.rpc.base.serializer.support.varint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * The annotation used to indicate a field of the serialized class
 * instance need to be ignored, which is equivalent to {@link Transient}
 * key.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignore {
	public String value() default "";
}
