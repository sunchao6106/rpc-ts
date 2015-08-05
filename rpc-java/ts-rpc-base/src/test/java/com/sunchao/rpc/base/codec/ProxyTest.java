package com.sunchao.rpc.base.codec;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import junit.framework.TestCase;

public class ProxyTest extends TestCase { 
	
/*	public void testMain() throws Exception {
		Proxy proxy = Proxy.getProxy(PTest.class, PTest.class);
		PTest instance  = (PTest) proxy.newInstance(new InvocationHandler(){

			public Object invoke(Object arg0, Method arg1, Object[] arg2)
					throws Throwable {
				if ("getName".equals(arg1.getName())) {
				     assertEquals(arg2.length, 0);
				} else if ("setName".equals(arg1.getName())) {
					assertEquals(arg2.length, 2);
					assertEquals(arg2[0], "tomsun");
					assertEquals(arg2[1], "hello");
				}
				return null;
			}
		});
		assertNull(instance.getName());
		instance.setName("tomsun", "hello");
	}*/
	
	@Test
	public void testCglibProxy() throws Exception {
		PTest test =  (PTest) Proxy.getProxy(PTest.class).newInstance(new InvocationHandler(){
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				System.out.println(method.getName());
				return null;
			}
		});
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(test.getClass());
		enhancer.setCallback(new MethodInterceptor() {
			public Object intercept(Object arg0, Method arg1, Object[] arg2,
					MethodProxy arg3) throws Throwable {
				// TODO Auto-generated method stub
				return null;
			}
		});
		try {
			PTest test1 = (PTest) enhancer.create();
			test1.setName("111", "222");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	
	public static interface PTest {
		String getName();
		void setName(String name, String name1);
	}
}


