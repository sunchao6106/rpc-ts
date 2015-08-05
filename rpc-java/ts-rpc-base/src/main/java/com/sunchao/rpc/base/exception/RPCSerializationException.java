package com.sunchao.rpc.base.exception;


/**
 * Serialization Exception.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class RPCSerializationException extends RPCException {

	private static final long serialVersionUID = -6504914013991200384L;
	
	public static final byte UNKNOWN = 0;
	/**the data is null*/
	public static final byte INVALID_DATA = 1;
	
	public static final byte NEGATIVE_SIZE = 2;
	
	public static final byte SIZE_LIMIT = 3;
	  
    private byte type = UNKNOWN;
    
    public RPCSerializationException() {
		super();
	}
	
	public RPCSerializationException(String message) {
		super(message);
	}
	
	public RPCSerializationException(Throwable throwable) {
		super(throwable);
	}
	
	public RPCSerializationException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public RPCSerializationException(byte type) {
		super();
		this.type = type;
	}
	
	public RPCSerializationException(byte type, String message) {
		super(message);
		this.type = type;
	}
	
	public RPCSerializationException(byte type, Throwable throwable) {
		super();
		this.type = type;
	}
	
	public RPCSerializationException(byte type, String message, Throwable throwable) {
		super(message, throwable);
		this.type = type;
	}
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public byte getType() {
		return this.type;
	}
	
	public boolean isInvalidData() {
		return this.type == INVALID_DATA;
	}
	
	public boolean isNegativeSize() {
		return this.type == NEGATIVE_SIZE;
	}
	
	public boolean isSizeLimit() {
		return this.type == SIZE_LIMIT;
	}
	
}