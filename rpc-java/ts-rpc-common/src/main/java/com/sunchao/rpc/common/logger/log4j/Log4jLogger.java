package com.sunchao.rpc.common.logger.log4j;

import java.io.Serializable;

import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.support.FailsafeLogger;

public class Log4jLogger implements Logger, Serializable {

	private static final long serialVersionUID = -5599033378439915741L;

	private static final String FQCN = FailsafeLogger.class.getName();
	
	private final org.apache.log4j.Logger logger ;
	
	public Log4jLogger(org.apache.log4j.Logger logger) {
		this.logger = logger;
	}
	
	
	public void trace(String msg) {
		logger.log(FQCN, org.apache.log4j.Level.TRACE, msg, null);
	}

	public void trace(Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.TRACE, e == null ? null : e.getMessage(), e);
	}

	public void trace(String msg, Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.TRACE, msg, e);
	}

	public void debug(String msg) {
		logger.log(FQCN, org.apache.log4j.Level.DEBUG,  msg, null);
		
	}

	public void debug(Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.DEBUG, e == null ? null : e.getMessage(), e);
	}

	public void debug(String msg, Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.DEBUG, msg, e);
	}

	public void info(Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.INFO, e == null ? null : e.getMessage(), e);
		
	}

	public void info(String msg, Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.INFO, msg, e);
	}

	public void warn(String msg) {
		logger.log(FQCN, org.apache.log4j.Level.WARN, msg, null);
		
	}

	public void warn(Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.WARN, e == null ? null : e.getMessage(), e);
		
	}

	public void warn(String msg, Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.WARN, msg, e);
	}

	public void error(String msg) {
		logger.log(FQCN, org.apache.log4j.Level.ERROR, msg, null);
	}

	public void error(Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.ERROR, e == null ? null : e.getMessage(), e);
	}

	public void error(String msg, Throwable e) {
		logger.log(FQCN, org.apache.log4j.Level.ERROR, msg, e);
	}

	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	public boolean isWarnEnabled() {
		return logger.isEnabledFor(org.apache.log4j.Level.WARN);
	}

	public boolean isErrorEnabled() {
		return logger.isEnabledFor(org.apache.log4j.Level.ERROR);
	}

	public void info(String string) {
		logger.log(FQCN, org.apache.log4j.Level.INFO, string, null);
		
	}

}
