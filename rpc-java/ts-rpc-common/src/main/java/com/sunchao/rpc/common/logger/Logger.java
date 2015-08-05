package com.sunchao.rpc.common.logger;


/**
 * The Log interface.
 * 
 * @author sunchao
 *
 */
public interface Logger {
	
	public enum Level{
		/**
		 * ALL
		 */
		ALL,
		/**
		 * TRACE
		 */
		TRACE,
		/**
		 * DEBUG
		 */
		DEBUG,
		/**
		 * INFO
		 */
		INFO,
		/**
		 * ERROR
		 */
		ERROR,
		/**
		 * WARN
		 */
		WARN,
		/**
		 * OFF
		 */
		OFF
	}
	/**
	 * print out msg
	 * @param msg
	 *      
	 */
	void trace(String msg);
	
	/**
	 * print the throwable
	 * 
	 * @param e
	 */
	void trace(Throwable e);
	
	/**
	 * print the msg and the throwable
	 * @param msg
	 * @param e
	 */
	void trace(String msg, Throwable e);
	
	/**
	 * print the debug msg.
	 * @param msg
	 */
	void debug(String msg);
	
	/**
	 * print the debug throwable
	 * @param e
	 */
	void debug(Throwable e);
	
	/**
	 * print the debug msg and throwable
	 * @param msg
	 * @param e
	 */
	void debug(String msg, Throwable e);
	
	/**
	 * print the regular throwable
	 * @param e
	 */
	void info(Throwable e);
	
	/**
	 * print the regular msg and throwable
	 * @param msg
	 * @param e
	 */
	void info(String msg, Throwable e);
	
	/**
	 * print the warn msg.
	 * @param msg
	 */
	void warn(String msg);
	void warn(Throwable e);
	void warn(String msg, Throwable e);
	void error(String msg);
	void error(Throwable e);
	void error(String msg, Throwable e);
	boolean isTraceEnabled();
	boolean isDebugEnabled();
	boolean isInfoEnabled();
	boolean isWarnEnabled();
	boolean isErrorEnabled();

	void info(String string);
	

}
