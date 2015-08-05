package com.sunchao.rpc.common.utils;

import java.io.Serializable;

import org.apache.log4j.Level;


public class Log implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7533042576059708760L;
	
	private String logName;
	private Level logLevel;
	private String logMessage;
	private String logThread;
	
	public String getLogName() 
	{
		return this.logName;
	}
	
	public void setLogName(String logName)
	{
	   this.logName = logName;	
	}
	
	public Level getLogLevel()
	{
		return this.logLevel;
	}
	
	public void setLogLevel(Level logLevel)
	{
		this.logLevel = logLevel;
	}
	
	public String getLogMessage()
	{
		return this.logMessage;
	}
	
	public void setLogMessage(String logMessage)
	{
		this.logMessage = logMessage;
	}
	
	public String getLogThread()
	{
		return this.logThread;
	}
	
	public void setLogThread(String logThread)
	{
		this.logThread = logThread;
	}
			
	@Override
	public int hashCode() {
		final int seed = 31;
		int result = 1;
		result = seed * result + (logLevel == null ? 0 : logLevel.hashCode());
		result = seed * result + (logMessage == null ? 0 : logMessage.hashCode());
		result = seed * result + (logThread == null ? 0 : logThread.hashCode());
		result = seed * result + (logName == null ? 0 : logName.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (null == obj) return false;
		if (this == obj) return true;
		if ( ! (obj instanceof Log)) return false;
		Log other = (Log) obj;
		if (logLevel == null) {
			if (other.logLevel != null) return false;
		} else if ( ! logLevel.equals(other.logLevel)) return false;
		
		if (logName == null) {
			if (other.logName != null) return false;
		} else if ( ! logName.equals(other.logName)) return false;
		
		if (logThread == null) {
			if (other.logThread != null) return false;
		} else if ( ! logThread.equals(other.logThread)) return false;
		
		if (logMessage == null) {
			if (other.logMessage != null) return false;
		} else if ( ! logMessage.equals(other.logMessage)) return false;
		
		return true;
	}

}
