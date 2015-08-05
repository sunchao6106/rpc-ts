package com.sunchao.rpc.common.logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sunchao.rpc.common.extension.HotSwapLoader;
import com.sunchao.rpc.common.logger.Logger.Level;
import com.sunchao.rpc.common.logger.jdk.JdkLoggerAdapter;
import com.sunchao.rpc.common.logger.log4j.Log4jLoggerAdapter;
import com.sunchao.rpc.common.logger.slf4j.Slf4jLoggerAdapter;
import com.sunchao.rpc.common.logger.support.FailsafeLogger;

/**
 * The factory of logger.
 * @author sunchao
 *
 */
public class LoggerFactory {

	private LoggerFactory(){
		
	}
	
	/**
	 * why use the keyword of 'volatile' , memory access? multiple threads?
	 */
	private static volatile LoggerAdapter LOGGER_ADAPTER;
	
	private static final ConcurrentHashMap<String, FailsafeLogger> LOGGERS = new ConcurrentHashMap<String, FailsafeLogger>();
	
	
	static {
		String logger = System.getProperty("application.logger");
		if ("slf4j" .equals(logger)) {
			setLoggerAdapter(new Slf4jLoggerAdapter());
		} else if ("log4j".equals(logger)) {
			setLoggerAdapter(new Log4jLoggerAdapter());
		} else if ("jdk".equals(logger)) {
			setLoggerAdapter(new JdkLoggerAdapter());
		} else {
			try {
				setLoggerAdapter(new Log4jLoggerAdapter());
			} catch (Throwable t) {
				try {
					 setLoggerAdapter(new Log4jLoggerAdapter());
				} catch (Throwable t1) {
					try{
						setLoggerAdapter(new Log4jLoggerAdapter());
					} catch (Throwable t2) {
						setLoggerAdapter(new Log4jLoggerAdapter());
					}
				}
			}
		}
	}    
	
	public static void setLoggerAdapter(String loggerAdapter) {
		if (loggerAdapter != null && loggerAdapter.length() > 0)
		{
			setLoggerAdapter(HotSwapLoader.getExtensionLoader(LoggerAdapter.class).getExtension(loggerAdapter));
		}
	}
	
	public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
		if (loggerAdapter != null) {
			Logger logger = loggerAdapter.getLogger(LoggerFactory.class.getName());
			logger.info("using logger: " + loggerAdapter.getClass().getName());
			LoggerFactory.LOGGER_ADAPTER = loggerAdapter;
			for (Map.Entry<String, FailsafeLogger> entry : LOGGERS.entrySet()) {
				entry.getValue().setLogger(LOGGER_ADAPTER.getLogger(entry.getKey()));
			}
		}
	}
	
	public static Logger getLogger(Class<?> key) {
		FailsafeLogger logger = LOGGERS.get(key.getName());
		if (logger == null) {
			LOGGERS.putIfAbsent(key.getName(), new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
			logger =  LOGGERS.get(key.getName());
		}
		return logger;
	}
	
	public static Logger getLogger(String key) {
	    FailsafeLogger logger = LOGGERS.get(key);
	    if (logger == null) {
	    	LOGGERS.putIfAbsent(key, new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
	    	logger = LOGGERS.get(key);
	    }
	    return logger;
	}
	
	public static void setLevel(Level level) {
		LOGGER_ADAPTER.setLevel(level);
	}
	
	public static Level getLevel(Level level) {
		return LOGGER_ADAPTER.getLevel();
	}
	
	public static File getFile() {
		return LOGGER_ADAPTER.getFile();
	}
}
