package com.sunchao.rpc.common.utils;

public class Assert {

	public static void notNull(Object obj, String message)
	{
		if (obj == null)
			throw new IllegalArgumentException(message);
	}
}
