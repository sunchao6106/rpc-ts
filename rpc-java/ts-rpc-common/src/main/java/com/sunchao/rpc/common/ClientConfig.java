package com.sunchao.rpc.common;

import java.util.Map;


/**
 * Simple remote server address and the service type configuration.
 * It must be not null.
 * 
 * NOTE : the service and network argument configuration which can
 * be configured in the <i>additional</i> {@link #additional}.
 * if not, system use the default configuration. And the system
 * common configuration's key which defined in the {@link com.sunchao.rpc.base.Config}.
 * and the key must be accordance,  <i>use-defined</i> key-value pairs except.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ClientConfig {

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClientConfig [protocol=" + protocol + ", host=" + host
				+ ", port=" + port + ", additional=" + additional + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((additional == null) ? 0 : additional.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
		result = prime * result + port;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientConfig other = (ClientConfig) obj;
		if (additional == null) {
			if (other.additional != null)
				return false;
		} else if (!additional.equals(other.additional))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (protocol == null) {
			if (other.protocol != null)
				return false;
		} else if (!protocol.equals(other.protocol))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the additional
	 */
	public Map<String, String> getAdditional() {
		return additional;
	}
	
	public ClientConfig setParameter(String key, String value) {
		this.additional.put(key, value);
		return this;
	}
	
	public ClientConfig setParameterIfAbsent(String key, String value) {
		if (this.additional.get(key) == null) 
			this.additional.put(key, value);
		return this;
	}

	/**
	 * Get the user-defined value. If not set,
	 * it return null.
	 * 
	 * @param key
	 * @return
	 */
	public String getParameter(String key) {
		return this.additional.get(key);
	}
	
	/**
	 * Get the configuration by the specified key, 
	 * if the value of user not set, and return the
	 * defaultValue, not null.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getParameterOrDefault(String key,
			String defaultValue) {
		String value = this.additional.get(key);
		if (value == null || value.length() == 0)
			return defaultValue;
		return value;
	}
	
	private final String protocol;
	
	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	private final String host;
	
	private final int port;
	
	private final Map<String, String> additional;
	
	public ClientConfig(String host, int port, Map<String, 
			String> additional, String protocol) {
		this.host = host;
		this.port = port;
		this.additional = additional;
		this.protocol = protocol; 
	}
}
