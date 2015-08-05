package com.sunchao.rpc.base.codec;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.junit.Test;

public class WrapperTest extends TestCase {
	
	@Test
	public void test_CommonClass() throws Exception {
		Wrapper w = Wrapper.getWrapper(CommonClass.class);
		/**
		 * Common class test.
		 */
		String[] mns = w.getMethodNames();
		assertEquals(mns.length, 4);
		String[] pns = w.getPropertyNames();
		assertEquals(pns.length, 2);
		CommonClass cc = new CommonClass();
		w.invokeMethod(cc, "setName", new Class<?>[]{String.class}, new Object[]{"tomsun"});
		assertEquals(w.invokeMethod(cc, "getName", new Class<?>[0], new Object[0]), "tomsun");
		
	}
	
	@Test
	public void test_Interface() {
		Wrapper w = Wrapper.getWrapper(Inter1.class);
		String[] mns = w.getMethodNames();
		assertEquals(mns.length, 4);
		mns = w.getDeclaredMethodNames();
		assertEquals(mns.length, 3);
	}
	
	@Test
	public void test_Interface_Impl() {
		Wrapper w = Wrapper.getWrapper(Inter1.class);
		Inter1 impl = new Impl0();  
		try {
			w.invokeMethod(impl, "setName", new Class<?>[]{String.class}, new Object[]{"tomsun"});
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			
		}
		
	}
	@Test
	public void test_Empty_Service() {
		Wrapper w = Wrapper.getWrapper(ServiceImpl.class);
		String[] mns = w.getDeclaredMethodNames();
		assertEquals(mns.length, 0);
		mns = w.getPropertyNames();
		assertEquals(mns.length, 0);
	}
	
	@Test
	public void test_Sub_father_Method() {
		assertArrayEquals(new String[]{"fatherName", "motherName"}, Wrapper.getWrapper(Son.class).getMethodNames());
		assertArrayEquals(new String[0], Wrapper.getWrapper(Son.class).getDeclaredMethodNames());
		
	}
	
	
	public static class CommonClass {
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		private int age;
		private String name;
		
	}
	
	/**
	 * Common example
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 */
	public static interface Inter0 {
		String name();
	}
	
	public static interface Inter1 extends Inter0 {
		void setName(String name);
		int age();
		void setAge(int age);
	}
	
	public static class Impl0 implements Inter1 {
		
		private String name = "tomsun";
		
		private int age = 25;

		public String name() {
			// TODO Auto-generated method stub
			return name;
		}

		public void setName(String name) {
			// TODO Auto-generated method stub
			this.name = name;
		}

		public int age() {
			// TODO Auto-generated method stub
			return age;
		}

		public void setAge(int age) {
			// TODO Auto-generated method stub
			this.age = age;
		}
		
	}
	
	/**
	 * No - Op
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 */
	public static interface Service {
		
	}
	
	public static class ServiceImpl implements Service {
		
	}
	
	
	/**
	 * Mutil-Interface.
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 */
	public static interface Father {
		String fatherName();
	}
	
	public static interface Mother {
		String motherName();
	}
	
	public static interface Son extends Father, Mother {
		
	}

}
