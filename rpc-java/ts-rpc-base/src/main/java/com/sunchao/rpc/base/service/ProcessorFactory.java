package com.sunchao.rpc.base.service;

import java.util.HashMap;
import java.util.Map;

public class ProcessorFactory {
	
	/**
	 * process the message in the caller thread.
	 */
	public static final byte INLINE_PROCESS = (byte) 0x00;
	
	//public static final byte 
	
	private static final Map<Byte, Processor> PROCESSOR_MAP = 
			new HashMap<Byte, Processor>();
	
	public static void registerProcessor(byte value, Processor handler) {
		PROCESSOR_MAP.put(value, handler);
	}
	
	public static Processor getProcessor(byte index) {
		return PROCESSOR_MAP.get(index);
	}

}
