package com.sunchao.rpc.base;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
public class TypesTest {  
	    Map<String, Integer> a;  
	    Inner<Float, Double> b;  
	    List<Set<String>[][]> c;  
	    List<String> d;  
	    Set<String> e;  
	    Outer<String>.Inner f;  
	  
	    private ParameterizedType mapStringInteger;  
	    private ParameterizedType innerFloatDouble;  
	    private ParameterizedType listSetStringArray;  
	    private ParameterizedType listString;  
	    private ParameterizedType setString;  
	    private ParameterizedType outerInner;  
	    private GenericArrayType setStringArray;  
	  
	    private String toString(ParameterizedType parameterizedType) {  
	        Type[] types = parameterizedType.getActualTypeArguments();  
	        String ret = "\n\t" + parameterizedType + "\n\t\t泛型个数：" + types.length + "==>";  
	        for (Type type : types) {  
	            ret += type + ", ";  
	        }  
	        return ret;  
	    }  
	  
	    @Test  
	    public void main() throws Exception {  
	        mapStringInteger = (ParameterizedType) getClass().getDeclaredField("a").getGenericType();  
	        innerFloatDouble = (ParameterizedType) getClass().getDeclaredField("b").getGenericType();  
	        listSetStringArray = (ParameterizedType) getClass().getDeclaredField("c").getGenericType();  
	        listString = (ParameterizedType) getClass().getDeclaredField("d").getGenericType();  
	        setString = (ParameterizedType) getClass().getDeclaredField("e").getGenericType();  
	        outerInner = (ParameterizedType) getClass().getDeclaredField("f").getGenericType();  
	        setStringArray = (GenericArrayType) listSetStringArray.getActualTypeArguments()[0];  
	          
	        System.out.println("a Map<String, Integer> 推导擦除后类型信息：" + toString(mapStringInteger));  
	        System.out.println();  
	        System.out.println("b Inner<Float, Double> 推导擦除后类型信息：" + toString(innerFloatDouble));  
	        System.out.println();  
	        System.out.println("c List<Set<String>[][]> 推导擦除后类型信息：" + toString(listSetStringArray));  
	        System.out.println();  
	        System.out.println("d List<String> 推导擦除后类型信息：" + toString(listString));  
	        System.out.println();  
	        System.out.println("e Set<String> 推导擦除后类型信息：" + toString(setString));  
	        System.out.println();  
	        System.out.println("f Outer<String>.Inner 推导擦除后类型信息：" + toString(outerInner));  
	        System.out.println();  
	        System.out.println("c List<Set<String>[][]> List第二层Set的泛型推导信息：" + setStringArray);  
	    }  
	  
	    class Inner<T1, T2> {  
	    }  
	  
	    static class Outer<T> {  
	        class Inner {  
	        }  
	    }  
	}  