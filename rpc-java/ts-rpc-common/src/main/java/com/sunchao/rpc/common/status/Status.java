package com.sunchao.rpc.common.status;

/**
 * Status
 * @author sunchao
 *
 */
public class Status {

	/**
	 * Level
	 */
	public static enum Level {
		/**
		 * OK
		 */
		OK,
		/**
		 * WARN
		 */
		WARN,
		/**
		 * ERROR
		 */
		ERROR,
		/**
		 * UNKNOWN
		 */
		UNKNOWN
	}
	
	private final Level level;
	private final String message;
	private final String description;
	
	public Status(Level level) {
		this(level, null, null);
	}
	
	public Status(Level level, String message) {
		this(level, message, null);
	}
	
	public Status(Level level, String message, String description) {
		this.level = level;
		this.message = message;
		this.description = description;
	}
	
	public Level getLevel() {
		return this.level;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public String getDescription() {
		return this.description;
	}
}
