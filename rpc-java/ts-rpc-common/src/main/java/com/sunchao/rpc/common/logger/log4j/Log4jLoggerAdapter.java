package com.sunchao.rpc.common.logger.log4j;

import java.io.File;
import java.util.Enumeration;

import org.apache.log4j.*;

import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.Logger.Level;
import com.sunchao.rpc.common.logger.LoggerAdapter;

public class Log4jLoggerAdapter implements LoggerAdapter {

	private File file;
	
	@SuppressWarnings("unchecked")
	public Log4jLoggerAdapter() {
		try {
			org.apache.log4j.Logger logger = org.apache.log4j.LogManager.getRootLogger();
			if (logger != null) {
				Enumeration<Appender> appenders = logger.getAllAppenders();
				if (appenders != null) {
					while (appenders.hasMoreElements()) {
						Appender appender = appenders.nextElement();
						if (appender instanceof FileAppender) {
							FileAppender fileAppender = (FileAppender) appender;
							String filename = fileAppender.getFile();
							file = new File(filename);
							break;
						}
					}
				}
			}
		} catch (Throwable t) {
		}
	}

	public Logger getLogger(String key) {
		return new Log4jLogger(LogManager.getLogger(key));
	}

	public void setLevel(Level level) {
		LogManager.getRootLogger().setLevel(toLog4jLevel(level));
	}

	private org.apache.log4j.Level toLog4jLevel(Level level) {
		if (level == Level.ALL) {
			return org.apache.log4j.Level.ALL;
		} else if (level == Level.TRACE) {
			return org.apache.log4j.Level.TRACE;
		} else if (level == Level.DEBUG) {
			return org.apache.log4j.Level.DEBUG;
		} else if (level == Level.INFO) {
			return org.apache.log4j.Level.INFO;
		} else if (level == Level.WARN) {
			return org.apache.log4j.Level.WARN;
		} else if (level == Level.ERROR) {
			return org.apache.log4j.Level.ERROR;
		} else 
			return org.apache.log4j.Level.OFF;
		
	}

	public Level getLevel() {
		return fromLog4jLeval(LogManager.getRootLogger().getLevel());
	}

	private Level fromLog4jLeval(org.apache.log4j.Level level) {
		if (level == org.apache.log4j.Level.ALL) {
			return Level.ALL;
		} else if (level == org.apache.log4j.Level.TRACE) {
			return Level.TRACE;
		} else if (level == org.apache.log4j.Level.DEBUG) {
			return Level.DEBUG;
		} else if (level == org.apache.log4j.Level.INFO) {
			return Level.INFO;
		} else if (level == org.apache.log4j.Level.WARN) {
			return Level.WARN;
		} else if (level == org.apache.log4j.Level.ERROR) {
			return Level.ERROR;
		} else 
			return Level.OFF;
		}
		

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		
	}

	public Logger getLogger(Class<?> key) {
		return new Log4jLogger(LogManager.getLogger(key));
	}

}
