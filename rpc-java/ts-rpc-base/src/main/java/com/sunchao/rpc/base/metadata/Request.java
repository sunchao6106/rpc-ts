package com.sunchao.rpc.base.metadata;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.sunchao.rpc.base.Config;

/**
 * Request
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class Request extends PacketHeader {
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Request [serial_id=" + serial_id + ", data=" + data
				+ ", oneway=" + oneway + ", heartBeatFlag=" + heartBeatFlag
				+ ", additional=" + additional + ", messageType=" + messageType
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
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + (heartBeatFlag ? 1231 : 1237);
		result = prime * result + (oneway ? 1231 : 1237);
		result = prime * result + (int) (serial_id ^ (serial_id >>> 32));
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
		Request other = (Request) obj;
		if (additional == null) {
			if (other.additional != null)
				return false;
		} else if (!additional.equals(other.additional))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (heartBeatFlag != other.heartBeatFlag)
			return false;
		if (oneway != other.oneway)
			return false;
		if (serial_id != other.serial_id)
			return false;
		return true;
	}



	/**
	 * @return the oneway
	 */
	public boolean isOneway() {
		return oneway;
	}



	/**
	 * @param oneway the oneway to set
	 */
	public void setOneway(boolean oneway) {
		this.oneway = oneway;
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


	private static final AtomicLong MESSAGE_ID = new AtomicLong(0);
	
	private final long serial_id;
	
	private Object data;
	
	private boolean oneway = false;
	
	private boolean heartBeatFlag = false;
	
	private Map<String, String> additional;
	
	private boolean errorRequest = false;
	
	/**
	 * @return the errorRequest
	 */
	public boolean isErrorRequest() {
		return errorRequest;
	}



	/**
	 * @param errorRequest the errorRequest to set
	 */
	public void setErrorRequest(boolean errorRequest) {
		this.errorRequest = errorRequest;
	}



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
	 * @return the data
	 */
	public Object getData() {
		return data;
	}



	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}



	public Request() {
		serial_id = nextMessageId();
	}
	
	public Request(long id) {
		this.serial_id = id;
	}
	
	/**
	 * Gets the request id.
	 * 
	 * @return
	 */
	public long getId() {
		return this.serial_id;
	}

	/**
	 * Gets the request id.
	 * @return
	 */
	public static long nextMessageId() {
		return MESSAGE_ID.getAndIncrement();
	}
	
	
	
    static String getDataString(Object data) {
		if (data == null) 
			return "null";
		else {
			return data.toString();
		}
	}
}
