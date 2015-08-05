package com.sunchao.rpc.common.logger.slf4j;

import java.io.File;

import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.Logger.Level;
import com.sunchao.rpc.common.logger.LoggerAdapter;

public class Slf4jLoggerAdapter implements LoggerAdapter {
	
	public Logger getLogger(String key) {
		return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(key));
	}

	public Logger getLogger(Class<?> key) {
		return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(key));
	}

	private Level level;
	private File file;

	public void setLevel(Level level) {
		this.level = level;
	}

	public Level getLevel() {
		return this.level;
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
