package com.sunchao.rpc.base.exception;

/**
 * The BASE ,Generic RPC Exception.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class RPCException extends Exception {

	private static final long serialVersionUID = 2570357413514669087L;
	
	public static final byte TIMEOUT = 0x00;
	
	public static final byte UNKNOWN = 0x01;
	protected byte type;
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public byte getType() {
		return this.type;
	}

	public RPCException() {
		super();
	}
	
	public RPCException(String message) {
		super(message);
	}
	
	public RPCException(Throwable throwable) {
		super(throwable);
	}
	
	public RPCException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public RPCException(byte code, String message) {
		super(message);
		this.type = code;
	}
}