package com.sunchao.rpc.common.logger;

import java.io.File;

import com.sunchao.rpc.common.extension.Component;
import com.sunchao.rpc.common.logger.Logger.Level;

/**
 * the logger manager.
 * @author sunchao
 *
 */
@Component
public interface LoggerAdapter {
	/**
	 * get the logger by the key of class.
	 * @param key
	 *         the class key.
	 * @return
	 *        the logger.
	 */
	Logger getLogger(Class<?> key);
	
	/**
	 * get the logger by the string key.
	 * @param key
	 *         the string key.
	 * @return
	 *        the logger.
	 */
	Logger getLogger(String key);
	
	/**
	 * set the level of logger.
	 * @param level
	 *            the level of logger.
	 */
	void setLevel(Level level);
	
	/**
	 * get the level of logger.
	 * @return
	 *        the level of logger.
	 */
	Level getLevel();
	
	/**
	 * get the file of the log output.
	 * @return
	 *       the log file.
	 */
	File getFile();
	
	/**
	 *  set the file of log output.
	 * @param file
	 *           the log output file.
	 */
	void setFile(File file);

}
