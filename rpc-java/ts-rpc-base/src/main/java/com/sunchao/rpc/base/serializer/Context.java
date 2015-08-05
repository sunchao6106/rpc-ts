package com.sunchao.rpc.base.serializer;

import org.apache.avro.Schema;

import com.sunchao.rpc.base.MessageType;
import com.sunchao.rpc.base.metadata.Packet;
import com.sunchao.rpc.base.metadata.RPCMetaData;

/**
 * Serialization context use for JSON, Protobuf, Thrift, Avro, don't be serialized or <code>deserialized</code>.
 * Just set the argument type , so JSON can <code>>deserialize</code> the type, this contains:
 * arguments type; return class, exception class.
 * 
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class Context {
	
	public void parsePacket(Packet packet) {
	//	this.seqId = packet.getMSN();
		byte flag = packet.getMessageType();
		if (flag == MessageType.ONEWAY) {
			this.setOneWay(true);
		} else {
			this.setOneWay(false);
		}
		
	}
	
	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public int getSeqId() {
		return seqId;
	}

	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public Class<?> getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(Class<?> exceptionType) {
		this.exceptionType = exceptionType;
	}

	
	public Class<?>[] getArgumentTypes() {
		return argumentTypes;
	}

	public void setArgumentTypes(Class<?>[] argumentTypes) {
		this.argumentTypes = argumentTypes;
	}

	public RPCMetaData getCallMeta() {
		return callMeta;
	}

	public void setCallMeta(RPCMetaData callMeta) {
		this.callMeta = callMeta;
	}

	
	public void setReturnClass(Class<?> returnClass) {
		this.returnClass = returnClass;
	}
	
	public Class<?> getReturnClass() {
		return this.returnClass;
	}
	
	public boolean isRequest() {
		return isRequest;
	}

	public void setRequest(boolean isRequest) {
		this.isRequest = isRequest;
	}

    private boolean isResultNormally;
    
	public boolean isResultNormally() {
		return isResultNormally;
	}

	public void setResultNormally(boolean isResultNormally) {
		this.isResultNormally = isResultNormally;
	}

	private int seqId;
	
	private Exception exception;
	
	private boolean isRequest;
	
	private Class<?>[] argumentTypes;
	
	private RPCMetaData callMeta;
	
	private Class<?> returnClass;
	
	private Class<?> exceptionType;
	
	private Schema schema;

	private boolean isOneWay;
	
	private Object[] arguments;
	
	private Object result;
	
	public boolean isOneWay() {
		return isOneWay;
	}

	public void setOneWay(boolean isOneWay) {
		this.isOneWay = isOneWay;
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}
	
	
}
