package com.sunchao.rpc.base;

public interface Config {
	
	public static final short RPC_VERSION =  1;
	
	public static final byte[] RPC_MAGIC = new byte[]{0x72, 0x70, 0x60};
	
	public static final int CONNECT_TIMEOUT =  3 * 1000; //3s
	
	public static final int RECONNECT_COUNT = 3;
	
	public static final int RECONNECT_INTERVAL = 2 * 1000; //2s
	
	public static final int RESPONSE_TIMEOUT = 3 * 1000; //3s
	
	public static final String SERIALIZATION_KEY = "serialization";
	
	public static final String DEFAULT_SERIALIZATION = "0x00";
	
	public static final String HEARTBEAT_KEY = "heartbeat";
	
	public static final String DEFAULT_HEARTBEAT = "60 * 1000"; //1 min.
	
	public static final String HEARTBEAT_TIMEOUT_KEY = "heartbeat.timeout";
	
	public static final String DEFAULT_HEARTBEAT_TIMEOUT = "3 * " + DEFAULT_HEARTBEAT; 

}
