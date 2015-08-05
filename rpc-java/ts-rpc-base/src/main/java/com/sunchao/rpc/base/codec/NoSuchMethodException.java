package com.sunchao.rpc.base.codec;

/**
 * NoSuchMethodException.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class NoSuchMethodException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7340941281754299049L;
	
	public NoSuchMethodException() {
		super();
	}
	
	public NoSuchMethodException(String message) {
		super(message);
	}

}
