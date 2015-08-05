package com.sunchao.rpc.base.serializer.varint;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.sunchao.rpc.base.serializer.support.varint.Builder;
import com.sunchao.rpc.base.serializer.support.varint.Ignore;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;
import com.sunchao.rpc.common.io.ByteHelper;
import com.sunchao.rpc.common.logger.LoggerFactory;

import junit.framework.TestCase;

/**
 * null length = 0, length = x 
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class VarSerializerDeserializerTest extends TestCase {
	
	@Test
	public void testPrimitiveIntBox() throws IOException {
		
		Builder<Integer> builder = Builder.register(Integer.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		int a = Integer.MIN_VALUE;
		builder.writeTo(a, bbos);
		byte[] b = bbos.toByteArray();
		System.out.println("*********************Integer***********************");
		System.out.println(b.length + ":" + ByteHelper.bytes2hex(b));
		a = builder.parseFrom(b);
		System.out.println(a);

	}
	
	@Test
	public void testPrimitiveByteBox() throws IOException {
		
		Builder<Byte> builder = Builder.register(Byte.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		byte b = 92;
		builder.writeTo(b, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Byte***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		b = builder.parseFrom(by);
		System.out.println(b);

	}
	
	@Test
	public void testPrimitiveBooleanBox() throws IOException {
		
		Builder<Boolean> builder = Builder.register(Boolean.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		boolean flag = false;
		builder.writeTo(flag, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Boolean***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		flag = builder.parseFrom(by);
		System.out.println(flag);

	}
	
	@Test
	public void testPrimitiveShortBox() throws IOException {
		
		Builder<Short> builder = Builder.register(Short.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		short b = 1346;
		builder.writeTo(b, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Short***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		b = builder.parseFrom(by);
		System.out.println(b);

	}
	
	@Test
	public void testPrimitiveCharBox() throws IOException {
		
		Builder<Character> builder = Builder.register(Character.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		char b = 134;
		builder.writeTo(b, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Char***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		b = builder.parseFrom(by);
		System.out.println((int)b);

	}
	
	@Test
	public void testPrimitiveIFloatBox() throws IOException {
		
		Builder<Float> builder = Builder.register(Float.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		float b = 134.66F;
		builder.writeTo(b, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Float***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		b = builder.parseFrom(by);
		System.out.println(b);

	}
	
	@Test
	public void testPrimitiveDoubleBox() throws IOException {
		
		Builder<Double> builder = Builder.register(Double.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		double b = 666134.777777;
		builder.writeTo(b, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Double***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		b = builder.parseFrom(by);
		System.out.println(b);

	}
	
	@Test
	public void testPrimitiveLongBox() throws IOException {
		
		Builder<Long> builder = Builder.register(Long.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		long b = Long.MIN_VALUE;
		builder.writeTo(b, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Long***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		b = builder.parseFrom(by);
		System.out.println(b);

	}
	
	//string
	@Test
	public void testStringType() throws IOException {
		
		Builder<String> builder = Builder.register(String.class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		String testString = "xxxxxxxxxxxdsdfdddsbskhuadksadbasd";
		System.out.println("*********************String***********************");
		System.out.println(testString.length() + ":" + ByteHelper.bytes2hex(testString.getBytes()));
		builder.writeTo(testString, bbos);
		byte[] b = bbos.toByteArray();
		
		System.out.println(b.length + ":" + ByteHelper.bytes2hex(b));
		testString = builder.parseFrom(b);
		System.out.println(testString);
	//	builder.writeTo(testString, bbos);
	//	b = bbos.toByteArray();
		//System.out.println(b.length + ":" + ByteHelper.bytes2hex(b));
	}
	
	//public void testPrimive
    //array
	@Test
	public void testPrimitiveintArray() throws IOException {
		
		Builder<int[]> builder = Builder.register(int[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		int[] ints = {3,4,5,6};
		builder.writeTo(null, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************int array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		ints = builder.parseFrom(by);
		System.out.println(Arrays.toString(ints));

	}
	
	@Test
	public void testPrimitiveIntegerArray() throws IOException {
		
		Builder<Integer[]> builder = Builder.register(Integer[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Integer[] ints = {3,4,5,6};
		builder.writeTo(null, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Integer array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		ints = builder.parseFrom(by);
		System.out.println(Arrays.toString(ints));

	}
	
	@Test
	public void testPrimitivebyteArray() throws IOException {
		
		Builder<byte[]> builder = Builder.register(byte[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		byte[] bytes = {3,4,5,6};
		builder.writeTo(bytes, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************byte array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		bytes = builder.parseFrom(by);
		System.out.println(Arrays.toString(bytes));

	}
	
	@Test
	public void testPrimitiveByteArray() throws IOException {
		
		Builder<Byte[]> builder = Builder.register(Byte[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Byte[] bytes = {11,56,99,11};
		builder.writeTo(bytes, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Byte array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		bytes = builder.parseFrom(by);
		System.out.println(Arrays.toString(bytes));

	}
	
	@Test
	public void testPrimitiveBooleanArray() throws IOException {
		
		Builder<Boolean[]> builder = Builder.register(Boolean[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Boolean[] booleans = {true,false,true,false};
		builder.writeTo(booleans, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Boolean array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		booleans = builder.parseFrom(by);
		System.out.println(Arrays.toString(booleans));

	}
	
	@Test
	public void testPrimitivebooleanArray() throws IOException {
		
		Builder<boolean[]> builder = Builder.register(boolean[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		boolean[] booleans = {true,false,true,false};
		builder.writeTo(booleans, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************boolean array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		booleans = builder.parseFrom(by);
		System.out.println(Arrays.toString(booleans));

	}
	
	@Test
	public void testPrimitiveCharacterArray() throws IOException {
		
		Builder<Character[]> builder = Builder.register(Character[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Character[] chars = {'f', '%' , '6' , '*' , '.' , 'a'};
		builder.writeTo(chars, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Character array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		chars = builder.parseFrom(by);
		System.out.println(Arrays.toString(chars));

	}
	
	@Test
	public void testPrimitivecharArray() throws IOException {
		
		Builder<char[]> builder = Builder.register(char[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		char[] chars = {'f', '%' , '6' , '*' , '.' , 'a'};
		builder.writeTo(chars, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************char array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		chars = builder.parseFrom(by);
		System.out.println(Arrays.toString(chars));

	}
	
	@Test
	public void testPrimitiveShortArray() throws IOException {
		
		Builder<Short[]> builder = Builder.register(Short[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Short[] shorts = {456, 576 , 567 , 11 , 0 };
		builder.writeTo(shorts, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Short array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		shorts = builder.parseFrom(by);
		System.out.println(Arrays.toString(shorts));

	}
	
	@Test
	public void testPrimitiveshortArray() throws IOException {
		
		Builder<short[]> builder = Builder.register(short[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		short[] shorts = {456, 576 , 567 , 11 , 0 };
		builder.writeTo(shorts, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************short array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		shorts = builder.parseFrom(by);
		System.out.println(Arrays.toString(shorts));
	}
	
	@Test
	public void testPrimitiveFloatArray() throws IOException {
		
		Builder<Float[]> builder = Builder.register(Float[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Float[] floats = {456.21F, 576.21F , 567.00F , 11.35F , 0.0F };
		builder.writeTo(floats, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Float array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		floats = builder.parseFrom(by);
		System.out.println(Arrays.toString(floats));
	}
	
	@Test
	public void testPrimitiveDoubleArray() throws IOException {
		
		Builder<Double[]> builder = Builder.register(Double[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Double[] doubles = {456.21 , 576.21 , 567.00 , 11.35 , 0.0 };
		builder.writeTo(doubles, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Double array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		doubles = builder.parseFrom(by);
		System.out.println(Arrays.toString(doubles));
	}
	
	@Test
	public void testPrimitiveLongArray() throws IOException {
		
		Builder<Long[]> builder = Builder.register(Long[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Long[] longs = {456L , 576L , 567L, 11L , 0L};
		builder.writeTo(longs, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************Long array***********************");
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		longs = builder.parseFrom(by);
		assertEquals(longs.length, 5);
		System.out.println(Arrays.toString(longs));
	}
	
	@Test
	public void testPrimitiveStringArray() throws IOException {
		
		Builder<String[]> builder = Builder.register(String[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		String[] strings = {"hello", "world", "my name is tom sun." };
	//	String[] string = {};
		builder.writeTo(strings, bbos);
		byte[] by = bbos.toByteArray();
		System.out.println("*********************String array***********************");
		int len = 0;
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			len += s.length();
			sb.append(ByteHelper.bytes2hex(s.getBytes()));
		}
		System.out.println(len + ":" + sb.toString());
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		strings = builder.parseFrom(by);
		System.out.println(Arrays.toString(strings));
	}
	
	@Test
	public void test_Multi_dimensional_array() throws IOException {
		ByteBufferOutputStream bbos;
		byte[] by;
		
		Builder<Object[]> builder1 = Builder.register(Object[].class);
		bbos = new ByteBufferOutputStream();
		builder1.writeTo(new Object[]{new String[0]}, bbos);
		by = bbos.toByteArray();
		
		Builder<long[]> longs = Builder.register(long[].class);
		bbos = new ByteBufferOutputStream();
		longs.writeTo(new long[]{ -89, 67832221334567878L}, bbos);
		longs.writeTo(new long[]{1, 111111, 34, 76, 23455,-99999999999999999L, 7777777777777777777L}, bbos);
		longs.writeTo(new long[]{1, 2, 3 ,1234567, 1234567890L, -987654321L} , bbos);
		by = bbos.toByteArray();
		long[] p = longs.parseFrom(by);
		assertEquals(p.length, 2);
		
		
		Builder<int[][]> mdi = Builder.register(int[][].class);
		bbos = new ByteBufferOutputStream();
		mdi.writeTo(new int[][]{{1, 2, 3}, {5, 6}, {11, 12, 13, 14}, {1111, 1112, 1113}}, bbos);
		by = bbos.toByteArray();
		int[][] ii = mdi.parseFrom(by);
		assertEquals(ii.length, 4);
		
		Builder<int[][][]> iii = Builder.register(int[][][].class);
		bbos =  new ByteBufferOutputStream();
		iii.writeTo(new int[][][]{
				 {{1, 3, 5, 7}}
				,{{2, 4, 6, 8}}
				,{{11,12,13,14}}
				,{{21,22,23,24}}
		}, bbos);
		by = bbos.toByteArray();
		int[][][] intsss =  iii.parseFrom(by);
		assertEquals(intsss.length, 4);
	}
	
	
	@Test
	public void testEnum() throws IOException {
		Builder<Color[]> builder = Builder.register(Color[].class);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Color[] colors = new Color[3];
		colors[0] = Color.GREEN;
		colors[1] = Color.RED;
		colors[2] = Color.YELLOW;
		builder.writeTo(colors, bbos);
		byte[] b =  bbos.toByteArray();
		System.out.println("**********************Enum**********************************");
		System.out.println(b.length + ":" + ByteHelper.bytes2hex(b));
		colors = builder.parseFrom(b);
		System.out.println(Arrays.toString(colors));
	}
	
	@Test
	public void testPersonObject() throws IOException {
		byte[] by ;
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		
		Builder<Person> builder1 = Builder.register(Person.class);
		System.out.println("**********************Object**********************************");
		builder1.writeTo(new Person(), bbos);
		by = bbos.toByteArray();
		System.out.println(by.length + ":" + ByteHelper.bytes2hex(by));
		Person p = builder1.parseFrom(by);
		System.out.println(p);
	}
	
	public static interface TestBean {
		String getName();
		void setName(String name);
		List<Person> getPersons();
		void setPersons(List<Person> p);
	}
	
	public static class TestBeanImpl implements TestBean {

		public String getName() {
			return "tomsun";
		}

		public void setName(String name) {
			// TODO Auto-generated method stub
			
		}

		public List<Person> getPersons() {
			return new ArrayList<Person>();
		}

		public void setPersons(List<Person> p) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public enum Color {
		GREEN, YELLOW, RED;
	}
	
	public static class Person {
		public final int[] xxxxxxxxxxxxxxxxxxxxxxxxxxx = {1,2,3};
		@Ignore("123")
		public boolean[] xxxxxxxxxxxxxxxxxxx = {true, false};
		public String xxxxxxxxxxx = "xxxxxx";
		public float xxxxxxxx = 1.0F;
		public double xxxxxxxxx= 1.0;
		public boolean xxxxxxx = true;
		public char xxxxxx = 'a';
		public short xxxxx = 6;
		public byte xxxx = 7;
		public int xx = 8;
		public static int x = 9;
		public long xxx = 10;
		@Ignore
		String x1 = "ooooooooooo";
		String x3 = "yyyyyyyyyy";
		
		private int y = 2, z = -2, q[] = {1,2,3,-1,-2,-3};
		private float f = 1.0f, f1 = -1.0f, f2[] = {1.0F, -2.0F, 3.0F, -4.0F};
		private String s1 = "", s2 = "x0x0";
		private String[] s3 = {"hehe", "haha", "xixi"};
		private byte b = 2, b1 = -2, b3[] = {1,2,3,-1,-2,-3};
		@Ignore
		private char c = 't',c1[] = {'t', 'o','m','s','u','n'};
		private  short short1 = 56, short2 = -56, short3[] = {1,2,3,-1,-2,-3};
		private double d1 = -1.2, d2 = 1.2, d3[] = {1.0,2.0,3.0,-1.9,-2.0};
		private long  l1 = 11111, l2 = -33333,l3[] = {1,2,3,-1,-2,-3};
		private final Boolean bool = Boolean.TRUE;
		private Integer inter = Integer.MAX_VALUE;
		private Character cha = Character.MAX_VALUE;
		private Double dou = Double.MAX_VALUE;
		private List<String> list0 = new ArrayList<String>();
		{
			list0.add("sunchao");
			list0.add("hello");
			list0.add("world");
		}
		
		private Color color = Color.YELLOW;
		public Color getColor() {
			return color;
		}
		public void setColor(Color color) {
			this.color = color;
		}
		public Class<?> getClazz() {
			return clazz;
		}
		public void setClazz(Class<?> clazz) {
			this.clazz = clazz;
		}
		private java.util.Date date = null;
		Class<?> clazz = Color.class;
		private Color[] colors = {Color.GREEN,Color.RED};
		
		public String toString() {
			return new StringBuilder("Person:[xxxxxxxxxxxxxxxxxxxxxxxxxxx: " + Arrays.toString(this.xxxxxxxxxxxxxxxxxxxxxxxxxxx ) + "\n")
			.append("xxxxxxxxxxxxxxxxxxx: " + Arrays.toString(this.xxxxxxxxxxxxxxxxxxx) + "\n")
			.append("xxxxxxxxxxx: " + this.xxxxxxxxxxx + "\n")
			.append("xxxxxxxx: " + this.xxxxxxxx + "\n")
			.append("xxxxxxxxx: " + this.xxxxxxxxx)
			.append("xxxxxxx: " + this.xxxxxxxx + "\n")
			.append("xxxxxx: " + this.xxxxxx + "\n")
			.append("xxxxx: " + this.xxxxx + "\n")
			.append("xxxx: " + this.xxxx + "\n")
			.append("xx: " + this.xx + "\n")
			.append("x: " + this.x + "\n")
			.append("xxx: " + this.xxx + "\n")
			.append("x1: " + this.x1 + "\n")
			.append("x3: " + this.x3 + "\n")
			.append("y: " + this.y + " , z: " + this.z + " , q: " + Arrays.toString(this.q) + "\n" )
			.append("f: " + this.f + " , f1: " + this.f1 + " ,f2: " + Arrays.toString(this.f2) + "\n" )
			.append("s1: " + this.s1 + " ,s2: " +  this.s2 + "\n")
			.append("s3: " + Arrays.toString(this.s3) + "\n")
			.append("b: " + this.b + " ,b1: " + this.b1 + " ,b3: " + Arrays.toString(this.b3) + "\n")
			.append("c: " + this.c + " ,c1: " + Arrays.toString(this.c1) + "\n")
			.append("short1: " + this.short1 + " ,short2: " + this.short2 + " ,short3: " + Arrays.toString(this.short3) + "\n")
			.append("d1: " + this.d1 + " ,d2: " + this.d2 + " ,d3: " + Arrays.toString(this.d3) + "\n")
			.append("l1: " + this.l1 + " ,l2: " + this.l2 + " ,l3: " + Arrays.toString(this.l3) + "\n" )
			.append("bool: " + this.bool + "\n")
			.append("inter: " + this.inter + "\n")
			.append("char: " + this.cha + "\n" )
			.append("dou: " + this.dou + "\n")
			.append("list0: " + Arrays.toString(this.list0.toArray()) + "\n")
			.append("color: " + this.color + "\n")
			.append("clazz:" + this.clazz.getName() + "\n")
			.append("date: " + this.date + "\n")
			.append("colors: " + Arrays.toString(colors) + "]").toString();
		}
	}
	
	@Test
	public void testThrowable() throws IOException {
		Builder<Throwable> builder = Builder.register(Throwable.class);
		Throwable t = new Throwable("hello world");
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		builder.writeTo(t, bbos);
		byte[] by = bbos.toByteArray();
		Throwable t1 = builder.parseFrom(by);
		System.out.println(t1.getMessage());
	}
	
	@Test
	public void testInterfaceImpl() throws IOException {
		byte[] b;
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Builder<TestBean> builder = Builder.register(TestBean.class);
		TestBean bean = new TestBeanImpl();
		builder.writeTo(bean, bbos);
		b = bbos.toByteArray();
		bean = builder.parseFrom(b);
		assertEquals(bean.getName(), "tomsun");
		assertEquals(bean.getPersons().size(), 0);
	}
	
	@Test
	public void testClassNameAndId() throws IOException {
		Bean  bean = new Bean();
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Builder<ArrayList> builder = Builder.register(ArrayList.class);
		ArrayList<Bean> list = new ArrayList<Bean>();
		list.add(bean);
		//list.add(bean);
		//list.add(bean);
		builder.writeTo(list, bbos);
		byte[] b = bbos.toByteArray();
		
		System.out.println("***************************ArrayList Bean***********************************");
		System.out.println(b.length + ":" + ByteHelper.bytes2hex(b));
	}
	
	@Test
	public void testBean() throws IOException {
		Bean bean = new Bean();
		bean.setX(3);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Builder<Bean> builder = Builder.register(Bean.class);
		for (int i = 0; i < 10; i++) {
			builder.writeTo(bean, bbos);
		}
		byte[] b = bbos.toByteArray();
		System.out.println("********************************Reference and nameID**********************************************");
		System.out.println(b.length + ":" + ByteHelper.bytes2hex(b));
		Bean newBean = builder.parseFrom(b);
		assertEquals(newBean.x, 3);
		System.out.println("********************************Reference and nameID**********************************************");
	}
	
	public static class Bean {
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bean other = (Bean) obj;
			if (x != other.x)
				return false;
			return true;
		}

		private int x = 0;
		
		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			return result;
		}	
	}
	
	@Test
	public void testReference() throws IOException {
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		Leaf parent = new Leaf();
		Leaf son = new Leaf();
		parent.value = "ÄãºÃ";
		parent.son = son;
		son.value = "world";
		son.parent = parent;
		Builder<Leaf> builder = Builder.register(Leaf.class);
		builder.writeTo(parent, bbos);
	    byte[] b = bbos.toByteArray();
	    System.out.println(b.length + ":" + ByteHelper.bytes2hex(b));
	    
	    parent = builder.parseFrom(b);
	    assertEquals(parent.parent, parent);
	    assertEquals(parent.value, "ÄãºÃ");
	    assertEquals(parent.son, son);
	}
	
	public static class Leaf {
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Leaf other = (Leaf) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		
		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
		
		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}
		
		Leaf parent = this;
		Leaf son = this;
		String value = "tomsun";
		
	}
}
