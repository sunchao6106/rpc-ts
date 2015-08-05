package com.sunchao.rpc.common.logger.jdk;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.Logger.Level;
import com.sunchao.rpc.common.logger.LoggerAdapter;

public class JdkLoggerAdapter implements LoggerAdapter {
	
	private static final String GLOBAL_LOGGER_NAME = "global";

	private File file;
	
	public JdkLoggerAdapter() {
		try {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties");
			if (in != null) {
				LogManager.getLogManager().readConfiguration(in);
			} else {
				System.err.println("No such logging.properties in classpath for jdk logging config!");
			}
		} catch (Throwable t) {
			System.err.println("failed to load logging.properties in classpath for jdk logging config, casue : " + t.getMessage());
		}
		
		try {
			Handler[] handlers = java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).getHandlers();
			for (Handler handler : handlers) {
				if (handler instanceof FileHandler) {
					FileHandler fileHandler = (FileHandler) handler;
					Field field =  fileHandler.getClass().getField("files");
					File[] files = (File[]) field.get(fileHandler);
					if (files != null && files.length > 0) {
						file = files[0];
					}
				}
			}
		} catch (Throwable t) {
		}
	}
	
	public Logger getLogger(Class<?> key) {
		return new JdkLogger(java.util.logging.Logger.getLogger(key == null ? "" : key.getName()));
	}

	public Logger getLogger(String key) {
		return new JdkLogger(java.util.logging.Logger.getLogger(key));
	}

	public void setLevel(Level level) {
		java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).setLevel(toJdkLevel(level));
	}

	public Level getLevel() {
		return fromJdkLevel(java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).getLevel());
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		
	}
	
	private static java.util.logging.Level toJdkLevel(Level level) {
		if (level == Level.ALL) {
			return java.util.logging.Level.ALL;
		} else if (level == Level.TRACE) {
			return java.util.logging.Level.FINER;
		} else if (level == Level.DEBUG) {
			return java.util.logging.Level.FINE;
		} else if (level == Level.INFO) {
			return java.util.logging.Level.INFO;
		} else if (level == Level.WARN) {
			return java.util.logging.Level.WARNING;
		} else if (level == Level.ERROR) {
			return java.util.logging.Level.SEVERE;
		} else 
			return java.util.logging.Level.OFF;
	}
	
	private static Level fromJdkLevel(java.util.logging.Level level) {
		if (level == java.util.logging.Level.ALL) {
			return Level.ALL;
		} else if (level == java.util.logging.Level.FINER) {
			return Level.TRACE;
		} else if (level == java.util.logging.Level.FINE) {
			return Level.DEBUG;
		} else if (level == java.util.logging.Level.INFO) {
			return Level.INFO;
		} else if (level == java.util.logging.Level.WARNING) {
			return Level.WARN;
		} else if (level == java.util.logging.Level.SEVERE) {
			return Level.ERROR;
		} else 
			return Level.OFF;
	}

}
