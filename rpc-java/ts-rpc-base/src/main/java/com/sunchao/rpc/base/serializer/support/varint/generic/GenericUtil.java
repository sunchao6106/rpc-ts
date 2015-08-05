package com.sunchao.rpc.base.serializer.support.varint.generic;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sunchao.rpc.base.serializer.support.varint.Builder;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;


/**
 * The generic helper for class and field.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class GenericUtil {
	
	@SuppressWarnings("rawtypes")
	public GenericUtil(Builder builder) {
		this.builder = builder;
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericUtil.class);
	
	//public static final ConcurrentMap<Class<?>,Map<String, Class<?>>> CLASS_TYPE_VARIABLE_CONCRETE = 
		//	new ConcurrentHashMap<Class<?>, Map<String, Class<?>>>();
	
	/**
	 * Create a mapping from type variable names (which are declared as type parameters of a generic class)
	 * to the concrete classes used for type instantiation.
	 * 
	 * @param clazz the class with generic type arguments.
	 * @param generics concrete types used to instantiate the class.
	 * @return 
	 */
	@SuppressWarnings("rawtypes")
	public GenericStructure buildGenericPairs(Class clazz, Class[] generics) {
		if (clazz == null) 
			throw new IllegalArgumentException("the class cannot be null.");
		
		if (clazz.isPrimitive()) 
			throw new IllegalArgumentException("the primitive cannot to build the generic type.");
		
		Class type = clazz;
		TypeVariable[] typeParamters = null; //type variable.
	//	Class[] actualClass = null;
		
		if (type.isArray()) {
			type = type.getComponentType(); //get the array component type.
		}
		
		while (type != null) {
			typeParamters = type.getTypeParameters(); //get the class's type variable array.
			if (typeParamters == null || typeParamters.length == 0) { //the class is not generic type.
				Type superclass = null;// try to get the generic information from the super class.
				do {
					superclass = type.getGenericSuperclass();
					type = type.getSuperclass();
				} while (superclass != null && ! (superclass instanceof ParameterizedType));
				if (superclass == null) { 
					break;
				}
				
				ParameterizedType pt = (ParameterizedType)superclass;
				Type[] typeArgs = pt.getActualTypeArguments();
				typeParamters = type.getTypeParameters(); //get the class declare type variables.
				generics = new Class[typeArgs.length];
				for (int i = 0; i < typeArgs.length; i++) {
					generics[i] = (typeArgs[i] instanceof Class) ? (Class)typeArgs[i] : Object.class;
				}
				break;
			} else 
				break;
		}
		
		if (typeParamters != null && typeParamters.length > 0) {
			int typeVarNum = 0;
			GenericStructure gs;
			Map<String, Class> typeVar2concreteClass = new HashMap<String, Class>();
			for (TypeVariable typeVar : typeParamters) {
				String typeVarName = typeVar.getName();
				final Class concreteClass = getTypeVarConcreteClass(generics, typeVarNum, typeVarName);
				if (concreteClass != null) {
				    typeVar2concreteClass.put(typeVarName, concreteClass);
				}
				typeVarNum++;
			}
			gs = new GenericStructure(typeVar2concreteClass);
			return gs;
		} else 
			return null;
	}
	
	@SuppressWarnings("rawtypes")
	private Class getTypeVarConcreteClass(Class[] generics, int typeVarNum, String typeVarName) {
		if (generics != null && generics.length > typeVarNum) 
			return generics[typeVarNum];
		else {
			GenericStructure gs = builder.getGenericStructure();
			if (gs != null) {
				return gs.getConcreteClass(typeVarName);
			}
		}
		return null;
	}
	
	
	

	/**
	 * Get the first level of classes or interface for a generic type.
	 * 
	 * @param genericType the type which is generic type.
	 * @param builder the builder.
	 * @return  null if the specified type is not generic or its generic types are not classes.
	 */
	@SuppressWarnings("rawtypes")
	public static Class[] getGeneric(Type genericType, Builder builder) {
		if (genericType instanceof GenericArrayType) { //generic array's component type.
			Type componentType = ((GenericArrayType)genericType).getGenericComponentType();
			if (componentType instanceof Class) 
				return new Class[] {(Class) componentType};
			else 
				return getGeneric(componentType, builder);
		}
		
		if (!(genericType instanceof ParameterizedType)) return null; //Non-generic. e.g type variable, wildType
		Type[] actualTypes = ((ParameterizedType)genericType).getActualTypeArguments();
		Class[] generics = new Class[actualTypes.length];
		int count = 0;
		for (int i = 0, n = actualTypes.length; i < n; i++) {
			Type actualType = actualTypes[i];
			generics[i] = Object.class;
			if (actualType instanceof Class)
				generics[i] = (Class)actualType;
			else if (actualType instanceof ParameterizedType) 
				generics[i] = (Class)((ParameterizedType) actualType).getRawType();
			else if (actualType instanceof TypeVariable) { 
				//Class clazz = CLASS_TYPE_VARIABLE_CONCRETE.get(actualType).get(((TypeVariable) actualType).getName());
				GenericStructure gs = builder.getGenericStructure();
				if (gs != null) {
					Class clazz = gs.getConcreteClass(((TypeVariable)actualType).getName());
				    if (clazz != null) {
					    generics[i] = clazz;
				    } else
					    continue;
				} else 
					continue;
			} else if (actualType instanceof GenericArrayType) {
				Type componnentType = ((GenericArrayType)actualType).getGenericComponentType();
				if (componnentType instanceof Class) 
					generics[i] = Array.newInstance((Class)componnentType, 0).getClass();
				else if (componnentType instanceof TypeVariable) {
					//Class clazz = CLASS_TYPE_VARIABLE_CONCRETE.get(componnentType).get(((TypeVariable) componnentType).getName());
					GenericStructure gs = builder.getGenericStructure();
					if (gs != null) {
						Class clazz = gs.getConcreteClass(((TypeVariable)componnentType).getName());
				     	if (clazz != null) {
						 generics[i] = Array.newInstance(clazz, 0).getClass();
					    }
					}
				} else {
					Class[] componentGenerics = getGeneric(componnentType, builder); //ParameteredType, GenericArrayType
					if (componentGenerics != null) generics[i] = componentGenerics[0];
				}
			} else 
				continue;
			count++;
		}
		if (count == 0) {
			LOGGER.info("the generic array is empty");
			return null;
		}
		return generics;
	}
	
	@SuppressWarnings("rawtypes")
	private Builder builder;
}
