package com.sunchao.rpc.base.transport;

/**
 * The service type factory, include syn, async in server.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Deprecated
public class ServerProcessorFactory {
	
    public static final byte SYN_SERVICE_PROCESSOR     = (byte) 0x00;
    
    public static final byte ASYNC_SERVICE_PROCESSOR   = (byte) 0x01;

}
