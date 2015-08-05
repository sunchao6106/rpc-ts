package com.sunchao.rpc.base.transport;

import com.sunchao.rpc.common.ClientConfig;

public interface Resetable {
	
	/**
	 * Server reset;
	 * 
	 * @param config
	 */
	void  reset(ClientConfig config);

}
