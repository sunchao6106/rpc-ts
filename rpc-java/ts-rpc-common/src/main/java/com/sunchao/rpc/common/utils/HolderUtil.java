package com.sunchao.rpc.common.utils;

import com.sunchao.rpc.common.annotation.Utility;

@Utility("HolderUtil")
public class HolderUtil<T> {

	private volatile T value;
	
	public void set(T value)
	{
		this.value = value;
	}
	
	public T get() {
		return this.value;
	}
}
