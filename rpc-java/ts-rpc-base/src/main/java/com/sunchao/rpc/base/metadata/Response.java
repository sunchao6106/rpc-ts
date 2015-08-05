package com.sunchao.rpc.base.metadata;

import java.util.Arrays;
import java.util.Map;

import com.sunchao.rpc.base.Config;

/**
 * Response.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class Response extends PacketHeader{


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Response [serial_id=" + serial_id + ", heartBeatFlag="
				+ heartBeatFlag + ", result=" + result + ", errorMsg="
				+ errorMsg + ", status_code=" + status_code + ", additional="
				+ additional + ", messageType=" + messageType
				+ ", serializerType=" + serializerType + ", serviceType="
				+ serviceType + ", version=" + version + ", magic="
				+ Arrays.toString(magic) + ", packetLen=" + packetLen + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((additional == null) ? 0 : additional.hashCode());
		result = prime * result
				+ ((errorMsg == null) ? 0 : errorMsg.hashCode());
		result = prime * result + (heartBeatFlag ? 1231 : 1237);
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + (int) (serial_id ^ (serial_id >>> 32));
		result = prime * result + status_code;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response other = (Response) obj;
		if (additional == null) {
			if (other.additional != null)
				return false;
		} else if (!additional.equals(other.additional))
			return false;
		if (errorMsg == null) {
			if (other.errorMsg != null)
				return false;
		} else if (!errorMsg.equals(other.errorMsg))
			return false;
		if (heartBeatFlag != other.heartBeatFlag)
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		if (serial_id != other.serial_id)
			return false;
		if (status_code != other.status_code)
			return false;
		return true;
	}

	/**
	 * @return the heartBeatFlag
	 */
	public boolean isHeartBeatFlag() {
		return heartBeatFlag;
	}

	/**
	 * @param heartBeatFlag the heartBeatFlag to set
	 */
	public void setHeartBeatFlag(boolean heartBeatFlag) {
		this.heartBeatFlag = heartBeatFlag;
	}

	/**
	 * @return the rId
	 */
	public long getrId() {
		return serial_id;
	}

	/**
	 * @param rId the rId to set
	 */
	public void setrId(long rId) {
		this.serial_id = rId;
	}

	/**
	 * @return the result
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}
	
	private long serial_id;
	
	private boolean heartBeatFlag = false;
	
	private Object result;
	
	private String errorMsg;
	
	private byte status_code = OK;
	
	private Map<String, String> additional;

	/**
	 * @return the additional
	 */
	public Map<String, String> getAdditional() {
		return additional;
	}

	/**
	 * @param additional the additional to set
	 */
	public void setAdditional(Map<String, String> additional) {
		this.additional = additional;
	}

	
	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * @param errorMsg the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * @return the status_code
	 */
	public byte getStatus_code() {
		return status_code;
	}

	/**
	 * @param status_code the status_code to set
	 */
	public void setStatus_code(byte status_code) {
		this.status_code = status_code;
	}


	public Response() {
		
	}
	
	public Response(long id) {
		this.serial_id = id;
	}
	
	/**
	 * common request-response.
	 */
	public static final byte OK               = 10;
	
	/**
	 * client-side-timeout
	 */
	public static final byte CLIENT_TIMEOUT   = 20;
	
	/**
	 * server-side-timeout.
	 */
	public static final byte SERVER_TIMEOUT   = 30;
	
	/**
	 * bad-request-format.
	 */
	public static final byte BAD_REQUEST      = 40;
	/**
	 * bad-response-format.
	 */
	public static final byte BAD_RESPONSE     = 50;
	/**
	 * service-not-exist.
	 */
	public static final byte SERVICE_LOST     = 60;
	/**
	 * server-internal-error.
	 */
	public static final byte SERVER_ERROR     = 70;
	/**
	 * client-internal-error.
	 */
	public static final byte CLIENT_ERROR     = 80;
	
	/**
	 * service-error.
	 */
	public static final byte SERVICE_ERROR    = 90;  
	
}
