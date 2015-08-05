package com.sunchao.rpc.base.metadata;

import java.nio.ByteBuffer;
import java.util.Map;

import com.sunchao.rpc.base.serializer.TSerializer;

/**
 * RPC call meta data.
 * service name, and the method name, the argument types.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */

public class RPCMetaData {
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	/**
	 * +--len---+--serviceName-------+---len--+-methodName-----+--size(arguments)--+-len-+-(argument x1)-----+--len-+--(argument x2)----+
	 * |                             |                         |                   |                         |                          |
	 * +--------+--------------------+--------+----------------+-------------------+-----+-------------------+------+-------------------+
	 * @return
	 */
	public ByteBuffer serialize() {
		int length = this.serviceName.length() + 4 
				      + this.methodName.length() + 4 + 4;
		int size = parameterTypeName.length;
		for (int i = 0; i < size; i++) {
			length += 4 + parameterTypeName[i].length();
		}
		ByteBuffer buf = ByteBuffer.allocate(length);
		buf.putInt(this.serviceName.length());
		buf.put(this.serviceName.getBytes(TSerializer.SYS_CHARSET));
		buf.putInt(this.methodName.length());
		buf.put(this.methodName.getBytes(TSerializer.SYS_CHARSET));
		buf.putInt(size);
		for (int i = 0; i < size; i++) {
			buf.putInt(parameterTypeName[i].length());
			buf.put(parameterTypeName[i].getBytes(TSerializer.SYS_CHARSET));
		}
		
		buf.flip();
		return buf;
	}
	
	public void deserialize(ByteBuffer buf) {
		if (buf == null || buf.remaining() == 0) return;
		int size  = buf.getInt();
		byte[] buffer = new byte[size];
		buf.get(buffer);
		this.serviceName = new String(buffer, TSerializer.SYS_CHARSET);
		
		size = buf.getInt();
		buffer = new byte[size];
		buf.get(buffer);
		this.methodName = new String(buffer, TSerializer.SYS_CHARSET);
		size = buf.getInt();
		String[] argumentClassName = new String[size];
		for (int i = 0; i < size; i++) {
			size = buf.getInt();
			buffer= new byte[size];
			buf.get(buffer);
			argumentClassName[i] = new String(buffer, TSerializer.SYS_CHARSET);
		}
		this.parameterTypeName = argumentClassName;
		
	}

	
	/**
	 * @return the attachments
	 */
	public Map<String, String> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments the attachments to set
	 */
	public void setAttachments(Map<String, String> attachments) {
		this.attachments = attachments;
	}

	public String[] getParameterTypeName() {
		return this.parameterTypeName;
	}

	public void setParameterTypeName(String[] parameterTypeName) {
		this.parameterTypeName = parameterTypeName;
	}

	/**
	 * service name.
	 */
	private String serviceName;
	
	/**
	 * service method name.
	 */
	private String methodName;
	
	/**
	 * argument types class name,
	 */
	private String[] parameterTypeName;
	
	/**
	 * additional informations.
	 */
	private Map<String, String> attachments;

}
