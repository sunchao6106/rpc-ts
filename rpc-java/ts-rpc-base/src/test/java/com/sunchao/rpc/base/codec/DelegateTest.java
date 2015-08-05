package com.sunchao.rpc.base.codec;

import com.sunchao.rpc.base.codec.Delegate.DelegateAware;
import static org.junit.Assert.*;
import junit.framework.TestCase;

public class DelegateTest extends TestCase {
	
	
	public void testMain() {
		Delegate delegate = Delegate.delegate(new Class<?>[]{Inter1.class, Inter2.class, Inter3.class, Inter4.class}, new Class<?>[] {
			Impl1.class, Impl2.class, Impl3.class});
		Object dummy = delegate.newInstance(new Object[] {new Impl1(), new Impl2(), new Impl3()});
		((Inter1)dummy).method1();
		((Inter2)dummy).method2();
		((Inter3)dummy).method3();
		((Inter4)dummy).method4();
		assertEquals(dummy instanceof Inter1, true);
		assertEquals(dummy instanceof Inter2, true);
		assertEquals(dummy instanceof Inter3, true);
		assertEquals(dummy instanceof Inter4, true);
	}
	
	public static class Impl3 {
		
		public void method4() {
			System.out.println("Impl3.method4()!");
		}
	}
	
	public static class Impl1 implements DelegateAware {

		public void method1() {
			System.out.println("Impl1.method1()!");
		}
		
		public void setDelegateInstance(Object instance) {
			System.out.println("I'm be delegated by the: " + instance);
		}	
	}
	
	public static class Impl2 implements DelegateAware {

		public void method2() {
			System.out.println("Impl2.method2()!");
		}
		
		public void method3() {
		   System.out.println("Impl2.method3()!");	
		}
		
		public void setDelegateInstance(Object instance) {
			// TODO Auto-generated method stub
			System.out.println("I'm be delegated by the: " + instance);
		}	
	}
	
	public static interface Inter1 {
		void method1();
	}
	
	public static interface Inter2 {
		void method2();
	}
	
	public static interface Inter3 {
		void method3();
	}
	
	public static interface Inter4 {
		void method4();
	}
	
}
