package com.sunchao.rpc.base.codec;

/**
 * NoSuchPropertyException
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class NoSuchPropertyException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2957963031934866270L;
	
	public NoSuchPropertyException() {
		super();
	}
	
	public NoSuchPropertyException(String message) {
		super(message);
	}

}
