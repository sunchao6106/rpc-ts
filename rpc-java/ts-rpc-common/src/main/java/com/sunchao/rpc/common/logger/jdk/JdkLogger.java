package com.sunchao.rpc.common.logger.jdk;

import java.io.Serializable;

import com.sunchao.rpc.common.logger.Logger;

public class JdkLogger implements Logger,Serializable{
	
    private final java.util.logging.Logger logger;
	
	private static final long serialVersionUID = 4085793687234299349L;
	
	public JdkLogger(java.util.logging.Logger logger) {
		this.logger = logger;
	}

	public void trace(String msg) {
	     logger.log(java.util.logging.Level.FINER, msg);	
	}

	public void trace(Throwable e) {
		logger.log(java.util.logging.Level.FINER,e.getMessage(), e);
	}

	public void trace(String msg, Throwable e) {
		logger.log(java.util.logging.Level.FINER, msg, e);
	}

	public void debug(String msg) {
		logger.log(java.util.logging.Level.FINE,msg);
	}

	public void debug(Throwable e) {
		logger.log(java.util.logging.Level.FINE, e.getMessage(), e);
	}

	public void debug(String msg, Throwable e) {
		logger.log(java.util.logging.Level.FINE, msg, e);
	}

	public void info(Throwable e) {
		logger.log(java.util.logging.Level.INFO, e.getMessage(), e);
	}

	public void info(String msg, Throwable e) {
		logger.log(java.util.logging.Level.INFO, e.getMessage(), e);
	}

	public void warn(String msg) {
	    logger.log(java.util.logging.Level.WARNING, msg);
	}

	public void warn(Throwable e) {
		logger.log(java.util.logging.Level.WARNING, e.getMessage(), e);
		
	}

	public void warn(String msg, Throwable e) {
		logger.log(java.util.logging.Level.WARNING, msg, e);
		
	}

	public void error(String msg) {
		logger.log(java.util.logging.Level.SEVERE, msg);
	}

	public void error(Throwable e) {
		logger.log(java.util.logging.Level.SEVERE, e.getMessage(), e);
	}

	public void error(String msg, Throwable e) {
		logger.log(java.util.logging.Level.SEVERE, msg, e);
		
	}

	public boolean isTraceEnabled() {
		return logger.isLoggable(java.util.logging.Level.FINER);
	}

	public boolean isDebugEnabled() {
		return logger.isLoggable(java.util.logging.Level.FINE);
	}

	public boolean isInfoEnabled() {
		return logger.isLoggable(java.util.logging.Level.INFO);
	}

	public boolean isWarnEnabled() {
		return logger.isLoggable(java.util.logging.Level.WARNING);
	}

	public boolean isErrorEnabled() {
		return logger.isLoggable(java.util.logging.Level.SEVERE);
	}

	public void info(String string) {
		logger.log(java.util.logging.Level.INFO, string);
	}

}
