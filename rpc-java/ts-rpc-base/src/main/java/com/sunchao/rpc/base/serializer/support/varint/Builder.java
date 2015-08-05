package com.sunchao.rpc.base.serializer.support.varint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

import com.sunchao.rpc.base.ReflectUtils;
import com.sunchao.rpc.base.codec.ClassCodec;
import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.serializer.support.varint.generic.GenericStructure;
import com.sunchao.rpc.base.serializer.support.varint.util.ASMFieldAccess;
import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.serializable.Serialization;
import com.sunchao.rpc.common.serializable.support.java.CompactedObjectInputStream;
import com.sunchao.rpc.common.serializable.support.java.CompactedObjectOutputStream;

import static com.sunchao.rpc.base.serializer.support.varint.ClassResolve.*;
/**
 * 
 * The class create the serializer/de-serializer with {@link #writeTo(Object, Output)}, {@link #parseFrom(Input)}}
 * for specified class type, which based on the dynamic byte code generate. {@code javassist}.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 * @param <T> The class type which need to serialize/ deserialize
 */
public abstract class Builder<T> {
	
	
    protected static Logger LOGGER = LoggerFactory.getLogger(Builder.class);
	
	private static final String BUILD_CLASS_NAME = Builder.class.getName();
	
    private static final AtomicLong BUILD_CLASS_COUNTER = new AtomicLong(0);
	
	private static final ConcurrentMap<Class<?>, Builder<?>> BUILD_MAP = new ConcurrentHashMap<Class<?>, Builder<?>>();

	@SuppressWarnings("unused")
	private static final Map<Class<?>, Builder<?>> GENERIC_MAP = new ConcurrentHashMap<Class<?>, Builder<?>>();
	//Fields are sorted by alpha so the order of the data is known.
	private static final Comparator<Field> FIELD_COMPARATOR = new Comparator<Field>() {
		
		public int compare(Field o1, Field o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	//Constructor order by the number of constructor's arguments
	@SuppressWarnings("rawtypes")
	private static final Comparator<Constructor> CON_COMPARATOR = new Comparator<Constructor>() {

		public int compare(Constructor o1, Constructor o2) {
			return o1.getParameterTypes().length - o2.getParameterTypes().length;
		}
	};
	
	@SuppressWarnings("unchecked")
	public static <T> Builder<T> register(Class<T> c) {
		if (c == Object.class || c.isInterface()) 
			return (Builder<T>) GENERIC_BUILDER;
		if (c == Object[].class) 
			return (Builder<T>) GENERIC_ARRAY_BUILDER;
		
		Builder<T> builder = (Builder<T>) BUILD_MAP.get(c);
		if (builder != null)
			return builder;
		builder = newBuilder(c);
		BUILD_MAP.put(c, builder);
		return builder;
	}
	
	public static <T> void register(Class<T> c, Builder<T> b) {
		BUILD_MAP.put(c, b);
	}

	protected Builder() {}
	
	//public abstract void setClassResolve(ClassResolve resolve);
	
	public abstract Class<T> getType();
	
	public abstract void writeTo(T obj, Output out) throws IOException;
	
	public abstract T parseFrom(Input in) throws IOException;
	
	//decorate pattern.
	public void writeTo(T object, OutputStream os) throws IOException {
		Output out = new Output(os, new DefaultClassResolve());
		writeTo(object, out);
		out.flush();
		
	}
	
	public T parseFrom(InputStream in) throws IOException {
		return parseFrom(new Input(in, new DefaultClassResolve()));
	}
	
	public T parseFrom(byte[] b) throws IOException {
		return parseFrom(new ByteBufferInputStream(b));
	}
	
	private static Builder<?> newObjectBuilder(final Class<?> c) {
		if (c.isEnum()) 
			return newEnumBuilder(c);
		
		if (c.isAnonymousClass()) //anonymous class
			throw new RuntimeException("Cannot instantiation anonyous class: " + c);
		//Non-static inner class.
		if (c.getEnclosingClass() != null && !Modifier.isStatic(c.getModifiers())) 
			throw new RuntimeException("Cannot instantiation inner and non-static class: " + c);
		
		if (Throwable.class.isAssignableFrom(c)) 
			return SERIALIZABLE_BUILDER;
		
		ClassLoader loader = Builder.class.getClassLoader();
		//is same package.
		boolean isp;
		String cn = c.getName(), bcn;
		if (c.getClassLoader() == null) //is system class. if (cn.startsWith("java.") || cn.startsWith("javax.") || cn.startsWith("sun."))
		{
			isp = false;
			bcn = BUILD_CLASS_NAME + "$dg" + BUILD_CLASS_COUNTER.getAndIncrement();
		} else {
			isp = true;
			bcn = cn + "$dg" + BUILD_CLASS_COUNTER.getAndIncrement();
		}
		
		//is Collection, is Map, is Serializable.
		boolean isc = Collection.class.isAssignableFrom(c);
		boolean ism = !isc && Map.class.isAssignableFrom(c);
		boolean iss = !(isc || ism) && Serializable.class.isAssignableFrom(c);
		
		//deal with fields.
	 
		List<Field> fieldList = new ArrayList<Field>();
		Class<?> nextClass = c;
		while (nextClass != Object.class) {
			Field[] decalredFields = nextClass.getDeclaredFields();
			if (decalredFields != null) {
				for (Field f : decalredFields) {
					int modifier = f.getModifiers();
					if (Modifier.isStatic(modifier) //skip static field.
							|| Modifier.isFinal(modifier) //skip final field
							|| f.isSynthetic()//f.getName().equals("this$0") //skip the compiler-added in the non-static & inner class's field reference to the outer class
							|| ! Modifier.isPublic(f.getType().getModifiers())) continue; // but the "this$1, this$2????", so here to use Synthetic()
					if (Modifier.isTransient(modifier)) {
						if (iss) 
							return SERIALIZABLE_BUILDER;
						continue;
					}
					if (f.isAnnotationPresent(Ignore.class)) //ignore the field which has the annotation of <i>Ignore</i>
						continue;
					f.setAccessible(true);
					fieldList.add(f);
				}
			}
			nextClass = nextClass.getSuperclass();
		}
		
		Field[] fields = fieldList.toArray(new Field[0]);
		if (fields.length > 1) 
			Arrays.sort(fields, FIELD_COMPARATOR);
		//System.out.println(Arrays.toString(fields));
		//for (Field field : fields) {
		//	System.out.println(field.getName());
		//}
		//deal with constructor
		Constructor<?>[] constructors = c.getDeclaredConstructors();
		if (constructors == null || constructors.length == 0) {
			nextClass = c;
			do {
				nextClass = nextClass.getSuperclass();
				if (nextClass == null) 
					throw new RuntimeException("Cannot found the Constructor: " + c.getName());
				constructors = nextClass.getDeclaredConstructors();
			} while (constructors == null || constructors.length == 0);
		}
		if (constructors.length > 1) 
			Arrays.sort(constructors, CON_COMPARATOR);
		
		//writeObject
		StringBuilder cwo = new StringBuilder("protected void writeObject(Object obj, ").append(Output.class.getName()).append(" out) throws java.io.IOException {\n ");
		cwo.append(cn).append(" v = (").append(cn).append(")$1; ");
		cwo.append("\n $2.writeVarIntNonZigZag(fields.length); \n");
		
		//readObject
		StringBuilder cro = new StringBuilder("protected void readObject(Object ret, ").append(Input.class.getName()).append(" in) throws java.io.IOException{\n ");
		cro.append("int fieldCount  = $2.readVarIntNonZigZag();\n ");
		cro.append("if (fieldCount != ").append(fields.length).append(" )\n throw new IllegalStateException(\"Deserialize class[").append(cn).append("], field count not matched. Expect ").append(fields.length)
		.append(" but get \" + fieldCount + \".\"); \n");
		cro.append(cn).append(" ret = (").append(cn).append(")$1; \n");
		
		//newInstance .
		StringBuilder cni = new StringBuilder("protected Object newInstance(").append(Input.class.getName()).append(" in) {\n return ");
		Constructor<?> constructor = constructors[0]; //the parameter argument least.
		int modifier = constructor.getModifiers();
		boolean dn = Modifier.isPublic(modifier) || (isp && !Modifier.isPrivate(modifier)); //public or non-private access.
		//boolean len = (constructor.getParameterTypes().length > 0);
		int argumentLen = constructor.getParameterTypes().length;
		boolean hasArgm = argumentLen > 0 ? true : false;
		
		if (dn) {
			cni.append("new ").append(cn).append("(");
		} else {
			constructor.setAccessible(true);
			if (!hasArgm) {
				cni.append("constructor.newInstance(new Object[0]");
			} else {
			    cni.append("constructor.newInstance(new Object[]{");
			}
		}
		//parameter types.
		Class<?>[] pts = constructor.getParameterTypes();
		for (int i = 0; i < pts.length; i++) {
			if (i > 0)
				cni.append(",");
			cni.append(defaultValue(pts[i]));
		}
		if (!dn && hasArgm) 
			cni.append("}"); //close object array.
		cni.append("); \n }");
		
		//get bean-style property meta data.
		Map<String, PropertyMetadata> propertyMetadatas = propertyMetadata(c);
		//the field builder.
		List<Builder<?>> builders = new ArrayList<Builder<?>>(fields.length);
		String fieldName, fieldTypeName; //field name , field type name
		Class<?> fieldType; //field type
		boolean directAccess; 
		PropertyMetadata pm;
		Field field;
		for (int i = 0; i < fields.length; i++) {
			field = fields[i];
			fieldName = field.getName();
			fieldType = field.getType();
			fieldTypeName = ReflectUtils.getName(fieldType);
			directAccess = isp && (field.getDeclaringClass() == c) && (Modifier.isPrivate(field.getModifiers()) == false);
			if (directAccess) {
				pm = null;
			} else {
				pm = propertyMetadatas.get(fieldName);
				if (pm != null && (pm.type != fieldType || pm.setter == null || pm.getter == null))
					pm = null;
			}
			cro.append("\n if (fieldCount == ").append(i).append(") return; \n");
			if (fieldType.isPrimitive()) {
				if (fieldType == boolean.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.writeBoolean(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readBoolean();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.writeBoolean(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readBoolean());");
					} else { //reflect.
						cwo.append("\n $2.writeBoolean(((Boolean)fields[").append(i).append("].get($1)).booleanValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readBoolean());");
					}
				} else if (fieldType == byte.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.write(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readByte();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.write(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readByte());");
					} else { //reflect.
						cwo.append("\n $2.write(((Byte)fields[").append(i).append("].get($1)).byteValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readByte());");
					}
				} else if (fieldType == short.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.writeShort(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readShort();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.writeShort(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readShort());");
					} else { //reflect.
						cwo.append("\n $2.writeShort(((Short)fields[").append(i).append("].get($1)).shortValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readShort());");
					}
				}  else if (fieldType == char.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.writeChar(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readChar();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.writeChar(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readChar());");
					} else { //reflect.
						cwo.append("\n $2.writeChar(((Character)fields[").append(i).append("].get($1)).charValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readChar());");
					}
				}  else if (fieldType == int.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.writeVarInt(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readVarInt();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.writeVarInt(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readVarInt());");
					} else { //reflect.
						cwo.append("\n $2.writeVarInt(((Integer)fields[").append(i).append("].get($1)).intValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readVarInt());");
					}
				}  else if (fieldType == float.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.writeFloat(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readFloat();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.writeFloat(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readFloat());");
					} else { //reflect.
						cwo.append("\n $2.writeFloat(((Float)fields[").append(i).append("].get($1)).floatValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readFloat());");
					}
				}  else if (fieldType == double.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.writeDouble(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readDouble();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.writeDouble(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readDouble());");
					} else { //reflect.
						cwo.append("\n $2.writeDouble(((Double)fields[").append(i).append("].get($1)).doubleValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readDouble());");
					}
				} else if (fieldType == long.class) {
					if (directAccess) { //direct access.
						cwo.append("\n $2.writeVarLong(v.").append(fieldName).append(");");
						cro.append("\n ret.").append(fieldName).append(" = $2.readVarLong();");
					} else if (pm != null) { //setter/getter access.
						cwo.append("\n $2.writeVarLong(v.").append(pm.getter).append("());");
						cro.append("\n ret.").append(pm.setter).append("($2.readVarLong());");
					} else { //reflect.
						cwo.append("\n $2.writeVarLong(((Long)fields[").append(i).append("].get($1)).longValue());");
						cro.append("\n  fields[").append(i).append("].set(ret, ($w)$2.readVarLong());");
					}
				} 
			} else if (fieldType == c) {
				if (directAccess) {
					cwo.append("\n this.writeTo(v.").append(fieldName).append(", $2);");
					cro.append("\n ret.").append(fieldName).append(" = (").append(fieldTypeName).append(")this.parseFrom($2);");
				} else if (pm != null) {
					cwo.append("\n this.writeTo(v.").append(pm.getter).append("(), $2);");
					cro.append("\n ret.").append(pm.setter).append("((").append(fieldTypeName).append(")this.parseFrom($2));");
				} else {
					cwo.append("\n this.writeTo((").append(fieldTypeName).append(")fields[").append(i).append("].get($1), $2);");
					cro.append("\n fields[").append(i).append("].set(ret, this.parseFrom($2));");
				}
			} else {
				int builderCount = builders.size();
				builders.add(register(fieldType));
				
				if (directAccess) {
					cwo.append("\n builders[").append(builderCount).append("].writeTo(v.").append(fieldName).append(", $2);");
					cro.append("\n ret.").append(fieldName).append(" = (").append(fieldTypeName).append(")builders[").append(builderCount).append("].parseFrom($2);");
				} else if (pm != null) {
					cwo.append("\n builders[").append(builderCount).append("].writeTo(v.").append(pm.getter).append("(), $2);");
					cro.append("\n ret.").append(pm.setter).append("((").append(fieldTypeName).append(")builders[").append(builderCount).append("].parseFrom($2));");
				} else {
					cwo.append("\n builders[").append(builderCount).append("].writeTo((").append(fieldTypeName).append(")fields[").append(i).append("].get($1), $2);");
					cro.append("\n fields[").append(i).append("].set(ret, builders[").append(builderCount).append("].parseFrom($2));");
				}
			}
		}
		
		//skip any fields
		//cro.append("\n for (int i = ").append(fields.length).append("; i < fieldCount; i++) \n $2.skipAny();");
		//collection or map.
		if (isc) {
			cwo.append("\n $2.writeVarIntNonZigZag(v.size()); \n for (java.util.Iterator iter = v.iterator(); iter.hasNext();) {\n $2.writeObject(iter.next()); \n}");
			cro.append("\n int len = $2.readVarIntNonZigZag(); \n for (int i = 0; i < len; i++) \n ret.add($2.readObject());");
		} else if (ism) {
			cwo.append("\n $2.writeVarIntNonZigZag(v.size()); \n for (java.util.Iterator iter = v.entrySet().iterator(); iter.hasNext();) {\n java.util.Map.Enrty entry = (java.util.Map.Entry)iter.next(); \n $2.writeObject(entry.getKey()); \n $2.writeObject(entry.getValue()); \n}");
			cro.append("\n int len = $2.readVarIntNonZigZag(); \n for (int i = 0; i < len; i++) \n ret.put($2.readObject(), $2.readObject());");
		}
		cwo.append(" }");//over
		cro.append(" }");//over
		
		ClassCodec codec = ClassCodec.newInstance(loader);
		codec.setClassName(bcn);
		codec.addSuperClass(AbstractObjectBuilder.class);
		codec.addDefaultConstructor();
		codec.addField("public static java.lang.reflect.Field[] fields;");
		codec.addField("public static " + BUILD_CLASS_NAME + "[] builders;");
		if (!dn) {
			codec.addField("public static java.lang.reflect.Constructor constructor;");
		}
		codec.addMethod("public Class getType() {\n return " + cn + ".class; }");
		codec.addMethod(cwo.toString());
		codec.addMethod(cro.toString());
		codec.addMethod(cni.toString());
		try {
			Class<?> dg = codec.toClass();
			//set static field;
			dg.getField("fields").set(null, fields);
			dg.getField("builders").set(null, builders.toArray(new Builder<?>[0]));
			if (!dn) {
				dg.getField("constructor").set(null, constructor);
			}
			return (Builder<?>) dg.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} finally {
			codec.release();
		}
	}
	
	private static Builder<?> newArrayBuilder(Class<?> c) {
		Class<?> cc = c.getComponentType();
		if (cc.isInterface()) 
			return GENERIC_ARRAY_BUILDER;
	/*	if (cc == Boolean.class       ||
				cc == Byte.class      ||
				cc == Short.class     ||
				cc == Character.class ||
				cc == Integer.class   ||
				cc == Float.class     ||
				cc == Double.class    ||
				cc == Long.class) {
			cc = wrapClassConvert(cc);
			return register(cc);
		}*/
		ClassLoader loader = Builder.class.getClassLoader();
		
		String cn = ReflectUtils.getName(c), ccn = ReflectUtils.getName(cc);
		String bcn = BUILD_CLASS_NAME + "$dg" + BUILD_CLASS_COUNTER.getAndIncrement();
		
		int idx = cn.indexOf(']');
		//if name = 'int[][]' then s1 = 'int[', s2 = '][]'
		String s1 = cn.substring(0, idx), s2 = cn.substring(idx);
		//writeTo.
		StringBuilder cwt = new StringBuilder("public void writeTo(Object obj, ").append(Output.class.getName()).append(" out) throws java.io.IOException{");
		//parseFrom
		StringBuilder cpf = new StringBuilder("public Object parseFrom(").append(Input.class.getName()).append(" in) throws java.io.IOException{");
		
		cwt.append("\n if($1 == null) {\n $2.writeVarIntNonZigZag(").append(DefaultClassResolve.class.getName()).append(".FLAG_NULL); \n return; \n}");
		cwt.append("\n ").append(cn).append(" v = (").append(cn).append(")$1; \n int len = v.length; \n $2.writeVarIntNonZigZag(len + 1); \n for(int i = 0; i < len; i++) { ");
		
		cpf.append("\n int len = $1.readVarIntNonZigZag(); \n if ( len == " + DefaultClassResolve.class.getName()+".FLAG_NULL) return null;");
		cpf.append("\n if (len - 1 == 0) return new ").append(s1).append('0').append(s2).append("; ");
		cpf.append(cn).append(" ret = new ").append(s1).append("len - 1").append(s2).append("; \n for (int i = 0; i < len - 1; i++){ ");
		
		Builder<?> builder = null;
		if (cc.isPrimitive()) {
			if (cc == boolean.class) {
				cwt.append("\n $2.writeBoolean(v[i]);");
				cpf.append("\n ret[i] = $1.readBoolean();");
			} else if (cc == byte.class) {
				cwt.append("\n $2.write(v[i]);");
				cpf.append("\n ret[i] = $1.readByte();");
			} else if (cc == char.class) {
				cwt.append("\n $2.writeChar(v[i]);");
				cpf.append("\n ret[i] = $1.readChar();");
			} else if (cc == short.class) {
				cwt.append("\n $2.writeShort(v[i]);");
				cpf.append("\n ret[i] = $1.readShort();");
			} else if (cc == int.class) {
				cwt.append("\n $2.writeVarInt(v[i]);");
				cpf.append("\n ret[i] = $1.readVarInt();");
			} else if (cc == float.class) {
				cwt.append("\n $2.writeFloat(v[i]);");
				cpf.append("\n ret[i] = $1.readFloat();");
			} else if (cc == double.class) {
				cwt.append("\n $2.writeDouble(v[i]);");
				cpf.append("\n ret[i] = $1.readDouble();");
			} else if (cc == long.class) {
				cwt.append("\n $2.writeVarLong(v[i]);");
				cpf.append("\n ret[i] = $1.readLong();");
			}
		} else if (cc.equals(String.class)) {
			cwt.append("\n $2.writeString(v[i]);");
			cpf.append("\n ret[i] = $1.readString();");
		} else {
			builder = register(cc);
			
			cwt.append("\n builder.writeTo(v[i], $2);");
			cpf.append("\n ret[i] = (").append(ccn).append(")builder.parseFrom($1);");
		}
		cwt.append("\n } \n }");
		cpf.append("\n} \n return ret; \n }");
		
		ClassCodec codec = ClassCodec.newInstance(loader);
		codec.setClassName(bcn);
		codec.addSuperClass(Builder.class);
		codec.addDefaultConstructor();
		if (builder != null)
			codec.addField("public static " + BUILD_CLASS_NAME + " builder;");
		codec.addMethod(cwt.toString());
		codec.addMethod(cpf.toString());
		codec.addMethod("public Class getType() {\n return " + cn + ".class; \n}");
		try {
			Class<?> ow = codec.toClass();
			if (builder != null)
			     ow.getField("builder").set(null, builder);
			return (Builder<?>)ow.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			codec.release();
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static <T> Builder<T> newBuilder(Class<T> c) {
		if (c.isPrimitive()) { 
			throw new RuntimeException("can not create builder for primitive type: " + c);
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("create Builder for class: " + c);
		}
		Builder<?> builder;
		if (c.isArray())
			builder = newArrayBuilder(c);
		else 
			builder = newObjectBuilder(c);
		return (Builder<T>) builder;
	}
	
	/**
	 * Builds Enum type Builder utility.
	 * 
	 * @param c the enum class.
	 * @return  the builder of enum class.
	 * 
	 */
	private static Builder<?> newEnumBuilder(Class<?> c) {
		ClassLoader loader = Builder.class.getClassLoader();
		String cn = c.getName();
		Object[] enumConstants = c.getEnumConstants();
		String bcn = BUILD_CLASS_NAME + "$dg" + BUILD_CLASS_COUNTER.getAndIncrement();
		//enum ordinal.
		StringBuilder ecf = new StringBuilder("public static Object[] enumConstants; \n");
		//writeTo
		StringBuilder ewt = new StringBuilder("public void writeTo(Object obj,  ").append(Output.class.getName()).append(" out) throws java.io.IOException {");
		ewt.append("\n " + cn).append(" v = (").append(cn).append(")$1;");
		ewt.append("\n if (v == null){\n $2.writeVarIntNonZigZag(" + DefaultClassResolve.class.getName() + ".FLAG_NULL); \n return; \n} else {\n $2.writeVarIntNonZigZag(v.ordinal() + 1); \n} \n}");
		//parseFrom.
		StringBuilder epf = new StringBuilder("public Object parseFrom(").append(Input.class.getName()).append(" in) throws java.io.IOException{");
		epf.append("\n int ordinal = $1.readVarIntNonZigZag();\n if (ordinal == " + DefaultClassResolve.class.getName() + ".FLAG_NULL) return null;\n ordinal--;\n").append(" if (ordinal < 0 || ordinal > enumConstants.length - 1){\n").append("throw new java.io.IOException(\"Invalid ordinal for enum ").append(cn).append(" : \" + ordinal + \".\"); \n }")
		.append("\n Object constant = enumConstants[ordinal];\n").append(" return (").append(cn).append(")constant; \n}");
		
		ClassCodec codec = ClassCodec.newInstance(loader);
		codec.setClassName(bcn);
		codec.addSuperClass(Builder.class);
		codec.addDefaultConstructor();
		codec.addField(ecf.toString());
		codec.addMethod(ewt.toString());
		codec.addMethod(epf.toString());
		
		try {
			Class<?> dg = codec.toClass();
			dg.getField("enumConstants").set(null, enumConstants);
			return (Builder<?>)dg.newInstance();
		} catch (InstantiationException e) {
			//LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			codec.release();
		}
	}
	
	static {
		//boolean
		register(Boolean.class, new Builder<Boolean>() {
			@Override
			public Class<Boolean> getType() {
				return Boolean.class;
			}

			@Override
			public void writeTo(Boolean obj, Output out) throws IOException {
				out.writeBoolean(obj);
			}

			@Override
			public Boolean parseFrom(Input in) throws IOException {
				return in.readBoolean();
			}

		});
		//void
		register(Void.class, new Builder<Void>() {
			@Override
			public Class<Void> getType() {
				return Void.class;
			}

			@Override
			public void writeTo(Void obj, Output out) throws IOException {}

			@Override
			public Void parseFrom(Input in) throws IOException {
				return null;
			}

		});
		//byte
		register(Byte.class, new Builder<Byte>() {
			@Override
			public Class<Byte> getType() {
				return Byte.class;
			}

			@Override
			public void writeTo(Byte obj, Output out) throws IOException {
				out.write(obj);
			}

			@Override
			public Byte parseFrom(Input in) throws IOException {
				return in.readByte();
			}

		});
		//character
		register(Character.class, new Builder<Character> () {
			@Override
			public Class<Character> getType() {
				return Character.class;
			}

			@Override
			public void writeTo(Character obj, Output out) throws IOException {
				out.writeChar(obj);
			}

			@Override
			public Character parseFrom(Input in) throws IOException {
				return in.readChar();
			}

		});
		//short
		register(Short.class, new Builder<Short>() {
			@Override
			public Class<Short> getType() {
				return Short.class;
			}

			@Override
			public void writeTo(Short obj, Output out) throws IOException {
				out.writeShort(obj);
			}

			@Override
			public Short parseFrom(Input in) throws IOException {
				return in.readShort();
			}

		});
		//int
		register(Integer.class, new Builder<Integer>() {
			@Override
			public Class<Integer> getType() {
				return Integer.class;
			}

			@Override
			public void writeTo(Integer obj, Output out) throws IOException {
				out.writeVarInt(obj);
			}

			@Override
			public Integer parseFrom(Input in) throws IOException {
				return in.readVarInt();
			}

		});
		//long
		register(Long.class, new Builder<Long>() {
			@Override
			public Class<Long> getType() {
				return Long.class;
			}

			@Override
			public void writeTo(Long obj, Output out) throws IOException {
				out.writeVarLong(obj);
			}

			@Override
			public Long parseFrom(Input in) throws IOException {
				return in.readVarLong();
			}

		});
		//float
		register(Float.class, new Builder<Float>() {
			@Override
			public Class<Float> getType() {
				return Float.class;
			}

			@Override
			public void writeTo(Float obj, Output out) throws IOException {
				out.writeFloat(obj);
			}

			@Override
			public Float parseFrom(Input in) throws IOException {
				return in.readFloat();
			}

		});
		//Double
		register(Double.class, new Builder<Double>() {
			@Override
			public Class<Double> getType() {
				return Double.class;
			}

			@Override
			public void writeTo(Double obj, Output out) throws IOException {
				out.writeDouble(obj);
			}

			@Override
			public Double parseFrom(Input in) throws IOException {
				return in.readDouble();
			}
	
		});
		//String
		register(String.class, new Builder<String>() {
			@Override
			public Class<String> getType() {
				return String.class;
			}

			@Override
			public void writeTo(String obj, Output out) throws IOException {
				out.writeString(obj);
			}

			@Override
			public String parseFrom(Input in) throws IOException {
				return in.readString();
			}

		});
		//StringBuilder.
		register(StringBuilder.class, new Builder<StringBuilder>() {
			@Override
			public Class<StringBuilder> getType() {
				return StringBuilder.class;
			}

			@Override
			public void writeTo(StringBuilder obj, Output out)
					throws IOException {
				out.writeString(obj.toString());
			}

			@Override
			public StringBuilder parseFrom(Input in) throws IOException {
				String value = in.readString();
				if (value == null) return null;
				return new StringBuilder(value);
			}

		});
		//StringBuffer.
		register(StringBuffer.class, new Builder<StringBuffer>() {
			@Override
			public Class<StringBuffer> getType() {
				return StringBuffer.class;
			}

			@Override
			public void writeTo(StringBuffer obj, Output out)
					throws IOException {
				out.writeString(obj.toString());
			}

			@Override
			public StringBuffer parseFrom(Input in) throws IOException {
				String value = in.readString();
				if (value == null) return null;
				return new StringBuffer(value);
			}
	
		});
		//BigInteger
		register(BigInteger.class, new Builder<BigInteger>() {
			@Override
			public Class<BigInteger> getType() {
				return BigInteger.class;
			}

			@Override
			public void writeTo(BigInteger obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL); //0
					return;
				}
				
				if (obj == BigInteger.ZERO) {
					out.writeVarIntNonZigZag(2); //
					out.write(0);
					return;
				}
				
				byte[] bytes = obj.toByteArray();
				out.writeVarIntNonZigZag(bytes.length + 1); // prevent the length = 0 ,and conflict with the <code>FLAG_NULL</code>, so here length + 1;
				out.write(bytes);
			}

			@Override
			public BigInteger parseFrom(Input in) throws IOException {
				int length = in.readVarIntNonZigZag();
				if (length == FLAG_NULL) return null;
				byte[] bytes = in.readBytes(length - 1);
				if (length == 2) {
					switch (bytes[0]) {
					case 0:
						return BigInteger.ZERO;
					case 1:
						return BigInteger.ONE;
					case 2:
						return BigInteger.TEN;
					}
				}
				return new BigInteger(bytes);
			}
            	
		});
		//java.util.date
		register(Date.class, new Builder<Date>() {
			@Override
			public Class<Date> getType() {
				return Date.class;
			}

			@Override
			public void writeTo(Date obj, Output out) throws IOException {
				if (obj == null) {
					out.write(FLAG_NULL);
					return;
				}
				out.write(FLAG_NOT_NULL);
				out.writeLong(obj.getTime());
			}

			@Override
			public Date parseFrom(Input in) throws IOException {
				byte b = in.readByte();
				if (b == FLAG_NULL) return null;
				if (b != FLAG_NOT_NULL)
					 throw new IOException("Input format error, expect FLAG_NULL|FLAG_NOT_NULL!");
				return new Date(in.readLong());
			}

		});
		//java.sql.Date
		register(java.sql.Date.class, new Builder<java.sql.Date>() {
			@Override
			public Class<java.sql.Date> getType() {
				return java.sql.Date.class;
			}

			@Override
			public void writeTo(java.sql.Date obj, Output out)
					throws IOException {
			    if (obj == null) {
			    	out.write(FLAG_NULL);
			    	return;
			    }
			    out.write(FLAG_NOT_NULL);
			    out.writeLong(obj.getTime());
			}

			@Override
			public java.sql.Date parseFrom(Input in) throws IOException {
				byte b = in.readByte();
				if (b == FLAG_NULL) return null;
				if (b != FLAG_NOT_NULL) 
					throw new IOException("Input format error.");
				return new java.sql.Date(in.readLong());
			}
		
		});
		//java.sql.Timestamp
		register(java.sql.Timestamp.class, new Builder<java.sql.Timestamp>() {
			@Override
			public Class<Timestamp> getType() {
				return java.sql.Timestamp.class;
			}

			@Override
			public void writeTo(Timestamp obj, Output out) throws IOException {
				if (obj == null) { 
					out.write(FLAG_NULL);
					return;
				}
				out.write(FLAG_NOT_NULL);
				out.writeLong(obj.getTime());
			}

			@Override
			public Timestamp parseFrom(Input in) throws IOException {
				byte b = in.readByte();
				if (b == FLAG_NULL) return null;
				if (b != FLAG_NOT_NULL) 
					throw new IOException("Input format error!");
				return new java.sql.Timestamp(in.readLong());
			}
	
		});
		//java.sql.Time
		register(java.sql.Time.class, new Builder<java.sql.Time>() {
			@Override
			public Class<Time> getType() {
				return java.sql.Time.class;
			}

			@Override
			public void writeTo(Time obj, Output out) throws IOException {
				if (obj == null) {
					out.write(FLAG_NULL);
					return;
				}
				out.write(FLAG_NOT_NULL);
				out.writeLong(obj.getTime());
			}

			@Override
			public Time parseFrom(Input in) throws IOException {
				byte b = in.readByte();
				if (b == FLAG_NULL) return null;
				if (b != FLAG_NOT_NULL)
					throw new IOException("Input format error!");
				return new java.sql.Time(in.readLong());
			}

		});
		//array list
		register(ArrayList.class, new Builder<ArrayList>() {
			@SuppressWarnings("rawtypes")
			@Override
			public Class<ArrayList> getType() {
				return ArrayList.class;
			}

			@SuppressWarnings({ "rawtypes" })
			@Override
			public void writeTo(ArrayList obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL); //null
					return;
				}
				//out.write(FLAG_NOT_NULL);
				out.writeVarIntNonZigZag(obj.size() + 1);
				for (Object element : obj) {
					//out.writeClass(obj.getClass());
					//Builder b = Builder.register(element.getClass());
					//b.writeTo(element, out);
					out.writeObject(element);
				}
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public ArrayList parseFrom(Input in) throws IOException {
				//byte b = in.readByte();
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				ArrayList list = new ArrayList(len - 1);
				for (int i = 0; i < len - 1; i++) {
					//Class<?> c = in.readClass();
					list.add(in.readObject());
				}	
				return list;
			}
		});
		//hash map
		register(HashMap.class, new Builder<HashMap>() {
			@SuppressWarnings("rawtypes")
			@Override
			public Class<HashMap> getType() {
				return HashMap.class;
			}

			@SuppressWarnings({ "rawtypes"})
			@Override
			public void writeTo(HashMap obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
			//	out.write(FLAG_NOT_NULL);
				out.writeVarIntNonZigZag(obj.size() + 1);
				for (Iterator iter = obj.entrySet().iterator(); iter.hasNext();) {
					Entry entry = (Entry) iter.next();
					//key
					out.writeObject(entry.getKey());
					//out.writeClass(entry.getKey().getClass());
				//	Builder b_K = Builder.register(entry.getKey().getClass());
					//b_K.writeTo(entry.getKey(), out);
					//value
					out.writeObject(entry.getValue());
					//out.writeClass(entry.getValue().getClass());
				//	Builder b_V = Builder.register(entry.getValue().getClass());
				//	b_V.writeTo(entry.getValue(), out);
				}
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public HashMap parseFrom(Input in) throws IOException {
				//byte b = in.readByte();
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
			//	int len = in.readVarIntNonZigZag();
				HashMap map = new HashMap(len - 1);
				for (int i = 0; i < len - 1; i++) {
				//	Class<?> key = in.readClass();
				//	Object keyV = Builder.register(key).parseFrom(in);
				//	Class<?> value = in.readClass();
				//	Object valueV = Builder.register(value).parseFrom(in);
					map.put(in.readObject(), in.readObject());
				}
				return map;
			}
		});
		//hash set 
		register(HashSet.class, new Builder<HashSet>() {
			@SuppressWarnings("rawtypes")
			@Override
			public Class<HashSet> getType() {
				return HashSet.class;
			}

			@SuppressWarnings({ "rawtypes"})
			@Override
			public void writeTo(HashSet obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
			//	out.write(FLAG_NOT_NULL);
				out.writeVarIntNonZigZag(obj.size() + 1);
				for (Object element : obj) {
					//out.writeClass(element.getClass());
				//	Builder b = Builder.register(element.getClass());
					//b.writeTo(element, out);
					out.writeObject(element);
				}	
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public HashSet parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
			//	int len = in.readVarIntNonZigZag();
				HashSet set = new HashSet(len - 1);
				for (int i = 0; i < len - 1; i++) {
					//Class<?> c = in.readClass();
					set.add(in.readObject());
				}
				return set;
			}
		});
		
		register(byte[].class, new Builder<byte[]>() {
			@Override
			public Class<byte[]> getType() {
				return byte[].class;
			}

			@Override
			public void writeTo(byte[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				out.write(obj);
			}

			@Override
			public byte[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL) return null;
				return in.readBytes(len - 1);
			}
		});
		//boolean[]
		register(boolean[].class, new Builder<boolean[]>() {
			@Override
			public Class<boolean[]> getType() {
				return boolean[].class;
			}

			@Override
			public void writeTo(boolean[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (boolean b : obj) {
					out.writeBoolean(b);
				}
			}

			@Override
			public boolean[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				boolean[] bs = new boolean[len - 1];
				for (int i = 0; i < len - 1; i++) {
					bs[i] = in.readBoolean();
				}
				return bs;
			}	
		});
		//short[]
		register(short[].class, new Builder<short[]>() {
			@Override
			public Class<short[]> getType() {
				return short[].class;
			}

			@Override
			public void writeTo(short[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (short s : obj) {
					out.writeShort(s);
				}
			}

			@Override
			public short[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				short[] shorts = new short[len - 1];
				for (int i = 0; i < len - 1; i++) {
					shorts[i] = in.readShort();
				}
				return shorts;
			}
		});
		//char[]
		register(char[].class, new Builder<char[]>() {
			@Override
			public Class<char[]> getType() {
				return char[].class;
			}

			@Override
			public void writeTo(char[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (char c : obj) {
					out.writeChar(c);
				}
			}

			@Override
			public char[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				char[] chars = new char[len - 1];
				for (int i = 0; i < len - 1; i++) {
					chars[i] = in.readChar();
				}
				return chars;
			}
		});
		//int[]
		register(int[].class, new Builder<int[]>() {
			@Override
			public Class<int[]> getType() {
				return int[].class;
			}

			@Override
			public void writeTo(int[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (int i : obj) {
					out.writeVarInt(i);
				}
			}

			@Override
			public int[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				int[] ints = new int[len - 1];
				for (int i = 0; i < len - 1; i++) {
					ints[i] = in.readVarInt();
				}
				return ints;
			}
		});
		//float[]
		register(float[].class, new Builder<float[]>() {
			@Override
			public Class<float[]> getType() {
				return float[].class;
			}

			@Override
			public void writeTo(float[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (float f : obj) {
					out.writeFloat(f);
				}
			}

			@Override
			public float[] parseFrom(Input in) throws IOException {
			    int len = in.readVarIntNonZigZag();
			    if (len == FLAG_NULL)
			    	return null;
			    float[] floats = new float[len - 1];
			    for (int i = 0; i < len - 1; i++) {
			    	floats[i] = in.readFloat();
			    }
			    return floats;
			}	
		});
		//double[]
		register(double[].class, new Builder<double[]>() {
			@Override
			public Class<double[]> getType() {
				return double[].class;
			}

			@Override
			public void writeTo(double[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (double d : obj) {
					out.writeDouble(d);
				}
			}

			@Override
			public double[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				double[] doubles = new double[len - 1];
				for (int i = 0; i < len - 1; i++) {
					doubles[i] = in.readDouble();
				}
				return doubles;
			}
		});
		//string[]
		register(String[].class, new Builder<String[]>() {
			@Override
			public Class<String[]> getType() {
				return String[].class;
			}

			@Override
			public void writeTo(String[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (String s : obj) {
					out.writeString(s);
				}	
			}

			@Override
			public String[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				String[] ss = new String[len - 1];
				for (int i = 0; i < len -1; i++) {
					ss[i] = in.readString();
				}	
				return ss;
			}	
		});
		
		//long[]
		register(long[].class, new Builder<long[]>() {
			@Override
			public Class<long[]> getType() {
				return long[].class;
			}

			@Override
			public void writeTo(long[] obj, Output out) throws IOException {
				if (obj == null) {
					out.writeVarIntNonZigZag(FLAG_NULL);
					return;
				}
				out.writeVarIntNonZigZag(obj.length + 1);
				for (long l : obj) {
					out.writeVarLong(l);
				}
			}

			@Override
			public long[] parseFrom(Input in) throws IOException {
				int len = in.readVarIntNonZigZag();
				if (len == FLAG_NULL)
					return null;
				long[] longs = new long[len - 1];
				for (int i = 0; i < len -1; i++) {
					longs[i] = in.readVarLong();
				}
				return longs;	
			}	
		});
		
		//Class
		register(Class.class, new Builder<Class>() {
			@SuppressWarnings("rawtypes")
			@Override
			public Class<Class> getType() {
				return Class.class;
			}

			@SuppressWarnings("rawtypes")
			@Override
			public void writeTo(Class obj, Output out) throws IOException {
				out.writeClass(obj);
				out.write((obj != null && obj.isPrimitive()) ? 1 : 0);
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Class parseFrom(Input in) throws IOException {
				Class clazz = in.readClass();
				int isPrimitive = in.readByte();
				if (clazz == null || !clazz.isPrimitive()) return clazz;
				return (isPrimitive == 1) ? clazz : ReflectUtils.getWrapper(clazz);
			}
		});		
	}
	
	public static abstract class AbstractObjectBuilder<T> extends Builder<T> {
	
		@Override
		public void writeTo(T obj, Output out) throws IOException {
			if (obj == null) {
				out.write(FLAG_NULL);
				return;
			}
			//out.write(FLAG_NOT_NULL);
			//out.writeClass(obj.getClass());
	      //  Builder b = Builder.register(obj.getClass());
	      //  b.writeTo(obj, out);
			int referenceId = out.getResolve().getReference(obj);
			//if(LOGGER.isInfoEnabled()) {
			//	System.out.println("**************reference***********" + referenceId + " : " + obj.toString());
			//}
			if (referenceId < 0) {
				out.getResolve().addReference(obj);
				//System.out.println(out.getResolve().getReference(obj));
				out.write(FLAG_NOT_NULL);
				writeObject(obj, out);
			} else {
				out.write(FLAG_REFERENCE);
				out.writeVarIntNonZigZag(referenceId);
				if(LOGGER.isInfoEnabled()) {
					System.out.println("**************reference***********" + referenceId + " : " + obj.toString());
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T parseFrom(Input in) throws IOException {
			byte b = in.readByte();
			switch (b) {
			case FLAG_NULL:
				return null;
			case FLAG_NOT_NULL: {
				T t = newInstance(in);
				in.getResolve().addReference(t);
				readObject(t, in);
				return t;
			}
			case FLAG_REFERENCE:
				return (T) in.getResolve().getReference(in.readVarIntNonZigZag());
			default:
				throw new IOException("Input format error.");
			}
		}

		public abstract Class<T> getType();
		
		protected abstract void writeObject(T obj, Output out) throws IOException;
		
		protected abstract T newInstance(Input in) throws IOException;
		
		protected abstract void readObject(T ret, Input in) throws IOException;
		
	}
	
	//Object.
	static final Builder<Object> GENERIC_BUILDER = new Builder<Object>() {

		@Override
		public Class<Object> getType() {
			return Object.class;
		}

		@Override
		public void writeTo(Object obj, Output out) throws IOException {
		     //out.writeClass(Object.class);
			
			//out.write(FLAG_NOT_NULL);
			out.writeObject(obj);
		}

		@Override
		public Object parseFrom(Input in) throws IOException{
			//Class clazz = in.readClass();
			//return (Object)clazz.newInstance();
			return in.readObject();
		}
	};
	
	//Object[]
	static final Builder<Object[]> GENERIC_ARRAY_BUILDER = new AbstractObjectBuilder<Object[]>() {
		@Override
		public Class<Object[]> getType() {
			return Object[].class;
		}

		@Override
		protected void writeObject(Object[] obj, Output out) throws IOException {
			/*if (obj == null) {
				out.writeVarIntNonZigZag(FLAG_NULL);
				return;
			}*/
			out.writeVarIntNonZigZag(obj.length);
			for (Object object : obj) {
				/*out.writeClass(object.getClass());
				Builder b = Builder.register(object.getClass());
				b.writeTo(object, out);*/
				out.writeObject(object);
			}
				
		}

		@Override
		protected Object[] newInstance(Input in) throws IOException {
			int len = in.readVarIntNonZigZag();
			/*if (len == FLAG_NULL)
				return null;*/
			return new Object[len];
			
		}

		@Override
		protected void readObject(Object[] ret, Input in) throws IOException {
			for (int i = 0; i < ret.length; i++) {
				/*Class clazz = in.readClass();
				Builder b = Builder.register(clazz);
				ret[i] = b.parseFrom(in);*/
				ret[i] = in.readObject();
			}
				
		}
	};
	
	static final Builder<Serializable> SERIALIZABLE_BUILDER = new Builder<Serializable>() {
		@Override
		public Class<Serializable> getType() {
			return Serializable.class;
		}

		@SuppressWarnings("resource")
		@Override
		public void writeTo(Serializable obj, Output out) throws IOException {
			if (obj == null) {
				out.writeVarIntNonZigZag(FLAG_NULL);
				return;
			} 
			//out.write(FLAG_NOT_NULL);
			ByteBufferOutputStream bbos = new ByteBufferOutputStream();
			CompactedObjectOutputStream oos = new CompactedObjectOutputStream(bbos);
			oos.writeObject(obj);
			oos.flush();
			byte[] b = bbos.toByteArray();
			out.writeVarIntNonZigZag(b.length + 1);
			out.write(b, 0, b.length);
		}

		@SuppressWarnings("resource")
		@Override
		public Serializable parseFrom(Input in) throws IOException {
			int len = in.readVarIntNonZigZag();
			if (len == FLAG_NULL)
				return null;
			ByteBufferInputStream bbis = new ByteBufferInputStream(in.readBytes(len - 1));
			CompactedObjectInputStream ois = new CompactedObjectInputStream(bbis);
			try {
				return (Serializable) ois.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage());
			}
		}
		
	};
	
	public void setGenericStructure(GenericStructure gs) {
		this.genericStructure = gs;
	}
	
	public GenericStructure getGenericStructure() {
		return this.genericStructure;
	}
	
/*	private static Class<?> wrapClassConvert(Class<?> clazz) {
		if (clazz == Byte.class)
			return byte[].class;
		else if (clazz == Boolean.class)
			return boolean[].class;
		else if (clazz == Short.class)
			return short[].class;
		else if (clazz == Character.class)
			return char[].class;
		else if  (clazz == Integer.class)
			return int[].class;
		else if (clazz == Float.class)
			return float[].class;
		else if (clazz == Double.class)
			return double[].class;
		else if (clazz == Long.class)
			return long[].class;
		return clazz;
	}*/
		
	/**
	 * Get the constructor's parameter type array's default value.
	 * 
	 * @param clazz  the parameter type.
	 * @return
	 */
	private static String defaultValue(Class<?> clazz) {
		if (boolean.class == clazz) 
			return "false";
		else if (int.class == clazz) 
			return "0";
		else if (long.class == clazz) 
			return "0L";
		else if (double.class == clazz)
			return "(double)0";
		else if (float.class == clazz)
			return "(float)0";
		else if (short.class == clazz)
			return "(short)0";
		else if (char.class == clazz)
			return "(char)0";
		else if (byte.class == clazz)
			return "(byte)0";
		else if (byte[].class == clazz)
			return "new byte[]{0}";
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Get the mapping of the filed which has the setter/getter method.
	 * the fieldName => the structure of parameter/return type, and the 
	 * getter/setter method's desc.
	 * 
	 * @param clazz
	 * @return
	 */
	private static Map<String, PropertyMetadata> propertyMetadata(Class<?> clazz) {
		Map<String, Method> methodMap = new HashMap<String, Method>();
		Map<String, PropertyMetadata> ret = new HashMap<String, PropertyMetadata>();
		
		//all public method
		for (Method m : clazz.getMethods()) {
			if (m.getDeclaringClass() == Object.class) //Ignore Object's method.
				continue;
			methodMap.put(ReflectUtils.getDesc(m), m);
		}
		
		Matcher matcher;
		for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
			String desc = entry.getKey();
			Method method = entry.getValue();
			if ((matcher = ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(desc)).matches() ||
					(matcher = ReflectUtils.IS_HAS_CAN_METHOD_DESC_PATTERN.matcher(desc)).matches()) {
				String propertyName = propertyName(matcher.group(1));
				Class<?> returnType = method.getReturnType();
				PropertyMetadata pm = ret.get(propertyName);
				if (pm == null) {
					pm = new PropertyMetadata();
					pm.type = returnType;
					ret.put(propertyName,pm);
				} else {
					if (pm.type != returnType)
						continue;
				}
				pm.getter = method.getName();
			} else if ((matcher = ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(desc)).matches()) {
				String proertyName = propertyName(matcher.group(1));
				Class<?> parameterType = method.getParameterTypes()[0];
				PropertyMetadata pm = ret.get(proertyName);
				if (pm == null) {
					pm = new PropertyMetadata();
					pm.type = parameterType;
					ret.put(proertyName, pm);
				} else {
					if (pm.type != parameterType)
						continue;
				}
				pm.setter = method.getName();
			}
		}
		return ret;
	}
	
	private static String propertyName(String s) {
		return s.length() == 1 || Character.isLowerCase(s.charAt(1)) ? Character.toLowerCase(s.charAt(0)) + s.substring(1) : s; 
	}
	
	private GenericStructure genericStructure;
	
	/**
	 * The field Builder.
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 * @param <T> the value class.
	 */
	public static abstract class AbstractFieldBuilder<T> extends Builder<T> {
		Field field;
		ASMFieldAccess access;
		@SuppressWarnings("rawtypes")
		Class valueClass;
	//	Builder builder;
		int accessIndex = -1;
		long offset = -1;
		
		@SuppressWarnings("rawtypes")
		public void setClass(Class valueClass) {
			this.valueClass = valueClass;
			//this.builder = null;
		}
		
		public Field getField() {
			return field;
		}
		
		public String toString() {
			return field.getName();
		}
		
		public ASMFieldAccess getAccess() {
			return access;
		}

		public void setAccess(ASMFieldAccess access) {
			this.access = access;
		}
	}
	
	static class PropertyMetadata {
		Class<?> type;
		String setter, getter;
	}
}
