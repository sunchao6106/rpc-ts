package com.sunchao.rpc.base.codec;

import java.lang.reflect.Field;

import junit.framework.TestCase;

public class ClassCodecTest extends TestCase {
	
	@SuppressWarnings({ "unchecked", "static-access" })
	public void testMain() throws Exception {
		
		/*ClassCodec codec = ClassCodec.newInstance();
		codec.setClassName(getClass().getName() + "$Builder");
		codec.addField("private String name = \"tomsun\";");
		codec.addMethod("public void  getName(){" +
		"\n System.out.println($0.name);"
		+ "\n return;}");
		codec.addDefaultConstructor();
		Class<?> cls = codec.toClass();
		//ClassCodecTest$Builder builder = cls.newInstance();
*/	
		
		Person p = new Person();
		Field field = null, fields[] = Person.class.getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			if (f.getName().equals("name")) {
				field = f;
			}
		}
		
		ClassCodec codec = ClassCodec.newInstance();
		codec.setClassName(Person.class.getName() + "$Builder")
		.addInterface(Builder.class).addField("public static java.lang.reflect.Field NAME;")
		.addMethod("public Object getName(" + Person.class.getName() + " i){\n"
		+ "System.out.println(\"hello world\");" + "\n return (String)NAME.get($1); }")
		.addMethod("public void setName(" + Person.class.getName() + " i, Object name){\n"
				+ "NAME.set($1, $2);\n}");
		
		codec.addDefaultConstructor();
		Class<?> cls = codec.toClass();
		cls.getField("NAME").set(null, field);
		
		System.out.println(cls.getName());
		Builder<String> builder = (Builder<String>) cls.newInstance();
		System.out.println(p.getName());
		builder.setName(p, "ok");
		System.out.println(p.getName());
		System.out.println(codec.isDynamicGeneratedClass(cls));
	}
	
	
	class Person {
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		
		private String name = "tomsun";
		private int age =  26;	
	}
	
	interface Builder<T> {
		
		T getName(Person person);
		
		void setName(Person person, T name);
	}

}
