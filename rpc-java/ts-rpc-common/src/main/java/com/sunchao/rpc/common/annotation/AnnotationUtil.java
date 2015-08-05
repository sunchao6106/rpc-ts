package com.sunchao.rpc.common.annotation;

import java.lang.annotation.Annotation;

import com.sunchao.rpc.common.extension.Component;

/**
 * <p>
 * The annotation utility with use by the reflection.
 * and get class, field, method, package metadata.
 * </p>
 * @author sunchao
 *
 */
public class AnnotationUtil {

	private static final Class<? extends Annotation> SPI_CLASS =
			Component.class;
	private static final Class<? extends Annotation> UTILITY_CLASS =
			Utility.class;
	
	private static final String FMT_OUT_SPI = "[" + "the spi name : %s ," + " id : %d "+ "]";
	
	private static final String FMT_OUT_UTIL ="[" + "the utility name : %s " + "]";
	
	private static void print(Object...objects)
	{
		if (objects == null || objects.length == 0) 
		{
			throw new IllegalArgumentException(
					"invalid arguments,and not match the need!");
		}
		if (objects.length == 1)
		{
	        System.out.printf(FMT_OUT_UTIL, objects[0]);	
		}
		else if (objects.length == 2)
		{
			System.out.printf(FMT_OUT_SPI, objects);
		}
		
		throw new IllegalArgumentException("the number arguments don't match, "
				+ "the number arguments : " + objects.length );
	}
	
	public static void getAnnotationInfo(Class<?> clazz) 
	{
		if (clazz == null)
		{
			throw new IllegalArgumentException("the class argument must not be null!");
		}
		
		if (clazz.isAnnotation())
		{
			if (clazz.isAnnotationPresent(SPI_CLASS))
			{
				Component spi = (Component) clazz.getAnnotation(SPI_CLASS);
				String spiName = spi.value();
			    print(spiName);	
			}
			else if (clazz.isAnnotationPresent(UTILITY_CLASS))
			{
				Utility util = (Utility) clazz.getAnnotation(UTILITY_CLASS);
				String utilName =  util.value();
				print(utilName);
			}
		}
	}
}
