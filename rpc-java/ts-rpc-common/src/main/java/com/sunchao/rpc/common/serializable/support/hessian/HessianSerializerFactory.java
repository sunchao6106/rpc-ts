package com.sunchao.rpc.common.serializable.support.hessian;

import com.caucho.hessian.io.SerializerFactory;

public class HessianSerializerFactory extends SerializerFactory {
	
	
	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	private HessianSerializerFactory() {}
	
	public static HessianSerializerFactory getInstance() {
		return HessianSerializerFactoryHolder._instance;
	}
	
	private static class HessianSerializerFactoryHolder {
		static  HessianSerializerFactory _instance = new HessianSerializerFactory();
	}

}
