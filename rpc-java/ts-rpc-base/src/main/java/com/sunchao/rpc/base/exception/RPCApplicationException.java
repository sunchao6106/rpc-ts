package com.sunchao.rpc.base.exception;

import com.sunchao.rpc.base.transport.Channel;

/**
 * Application level.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class RPCApplicationException extends RPCException {

	private static final long serialVersionUID = 3300634516590896428L;
	
	private final Object request;

	private final Channel channel;
	/**
	 * @return the request
	 */
	public Object getRequest() {
		return request;
	}

	public static final byte UNKNOWN = 0;
	/**The service method not exists */
	public static final byte UNKNOWN_METHOD = 1;
	/** invalid message type    {@link MessageType}*/
	public static final byte INVALID_MESSAGE_TYPE = 2;
	
	public static final byte MISSING_RESULT = 3;
	
	public static final byte INTERNAL_ERROR = 4;
	
	
	public RPCApplicationException(Object request, Channel channel, String message) {
		super(message);
		this.request = request;
		this.channel = channel;
	}
	
	
	public RPCApplicationException(Object request, Channel channel, Throwable throwable) {
		super(throwable);
		this.channel = channel;
		this.request = request;
	}
	
	public RPCApplicationException(Object request, Channel channel, String message, Throwable throwable) {
		super(message, throwable);
		this.request = request;
		this.channel = channel;
	}
	
	public RPCApplicationException(byte type, Object request, Channel channel) {
		super();
		this.type = type;
		this.channel = channel;
		this.request = request;
	}
	
	public RPCApplicationException(byte type, String message,Object request, Channel channel) {
		super(message);
		this.type = type;
		this.channel = channel;
		this.request = request;
	}
	
	public RPCApplicationException(byte type, Throwable throwable, Object request, Channel channel) {
		super(throwable);
		this.type = type;
		this.channel = channel;
		this.request = request;
	}
	
	public RPCApplicationException(byte type, String message, Throwable throwable, Channel channel, Object request) {
		super(message, throwable);
		this.type = type;
		this.channel = channel;
		this.request = request;
	}
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public byte getType() {
		return this.type;
	}
	
	public boolean isUnknownMethod() {
		return this.type == UNKNOWN_METHOD;
	}
	
	public boolean isInvalidMessageType() {
		return this.type == INVALID_MESSAGE_TYPE;
	}
	
	public boolean isMissingResult() {
		return this.type == MISSING_RESULT;
	}
	
	public boolean isInternalError() {
		return this.type == INTERNAL_ERROR;
	}
	
}