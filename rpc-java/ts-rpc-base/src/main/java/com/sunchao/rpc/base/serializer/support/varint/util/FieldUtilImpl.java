package com.sunchao.rpc.base.serializer.support.varint.util;

import java.lang.reflect.Field;
import java.util.List;

import com.sunchao.rpc.base.serializer.support.varint.Builder;
import com.sunchao.rpc.base.serializer.support.varint.Builder.AbstractFieldBuilder;
import com.sunchao.rpc.base.serializer.support.varint.collection.IntArray;

import static com.sunchao.rpc.base.UnsafeUtil.unsafe;

/**
 * Helper class for object's fields to serializer using Unsafe-based approach.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
final class FieldUtilImpl {
	
   private AbstractFieldBuilder<?> builder;
   
   @SuppressWarnings("rawtypes")
public FieldUtilImpl(AbstractFieldBuilder builder) {
	   this.builder = builder;
   }
   
   @SuppressWarnings("restriction")
public void createUnsafeCacheFieldsAndRegions(List<Field> validFields, List<AbstractFieldBuilder<?>> fieldBuildes, 
		   int baseIndex, IntArray useASM) {
	   //Find adjacent fields of primitive types.
	   long startPrimitives = 0;
	   long endPrimitives = 0;
	   boolean lastWasprimitive = false;
	   int primitiveLength = 0;
	   int lastAccessIndex = -1;
	   Field lastField = null;
	   long fieldOffset = -1;
	   long fieldEndOffset = -1;
	   long lastFieldEndOffset = -1;
	   
	   for (int i = 0, n = validFields.size(); i< n; i++) {
		   Field field = validFields.get(i);
		   int accessIndex = -1;
		   if (builder.getAccess() != null && useASM.get(baseIndex + i) == 1) 
			   accessIndex = builder.getAccess().getIndex(field.getName());
		   fieldOffset = unsafe().objectFieldOffset(field);
		   fieldEndOffset = fieldOffset + fieldSizeOf(field.getType());
		   
		   if (!field.getType().isPrimitive() && lastWasprimitive) {
			   endPrimitives = lastFieldEndOffset;
			   lastWasprimitive = false;
			   if (primitiveLength > 1) {
				   
			   }
		   }
	   }
			   
   }
   
   @SuppressWarnings("restriction")
  private int fieldSizeOf(Class<?> clazz) {
	   if (clazz == int.class || clazz == float.class) return 4;
	   if (clazz == long.class || clazz == double.class) return 8;
	   if (clazz == byte.class || clazz == boolean.class) return 1;
	   if (clazz == short.class || clazz == char.class) return 2;
	   return unsafe().addressSize();
   }
   
   @SuppressWarnings("restriction")
  public long getObjectFieldOffset(Field field) {
	   return unsafe().objectFieldOffset(field);
   }

}
