package com.sunchao.rpc.base.exception;
/**
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class RPCTransportException extends RPCException {

	private static final long serialVersionUID = 4845568681042724203L;
 
	public static final byte UNKNOWN = 0;
	
	public static final byte NOT_OPEN = 1;
	
	public static final byte ALREADY_OPEN = 2;
	
	public static final byte TIMED_OUT = 3;
	

	private byte type = UNKNOWN;

	public RPCTransportException() {
	   super();
	}

	public RPCTransportException(byte type) {
	   super();
	   this.type = type;
	}

	public RPCTransportException(byte type, String message) {
	   super(message);
	   this.type = type;
	}

	public RPCTransportException(String message) {
	   super(message);
	}

	public RPCTransportException(byte type, Throwable cause) {
	   super(cause);
	   this.type = type;
	}

	public RPCTransportException(Throwable cause) {
	   super(cause);
	}

	public RPCTransportException(String message, Throwable cause) {
	   super(message, cause);
	}

	public RPCTransportException(byte type, String message, Throwable cause) {
	   super(message, cause);
	   this.type = type;
	}

	public byte getType() {
	   return this.type;
	}
	
	public boolean isNotOpen() {
		return this.type == NOT_OPEN;
	}
	
	public boolean isAlreayOpen() {
		return this.type == ALREADY_OPEN;
	}
	
	public boolean isTimeout() {
		return this.type == TIMED_OUT;
	}

}
