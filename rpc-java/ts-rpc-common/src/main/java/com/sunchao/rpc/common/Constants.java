package com.sunchao.rpc.common;

import java.util.regex.Pattern;

/**
 * Constants.
 * @author sunchao
 *
 */
public class Constants {

	public static final int       DEFAULT_IO_THREADS                          = Runtime.getRuntime()
			                                                                         .availableProcessors() + 1;
	public static final String    DEFAULT_PROXY                               = "javassist";
	public static final String    DEFAULT_KEY_PREFIX                          = "default";
	public static final String    REMOVE_VALUE_PREFIX                         = "-";
	public static final String    LOCAL_KEY                                   = "local";
	public static final String    LOCALHOST_KEY                               =  "localhost";
	public static final String    LOCALHOST_VALUE                             =  "127.0.0.1";
	public static final String    ANYHOST_KEY                                 =  "anyhost";
	public static final String    ANYHOST_VALUE                               =  "0.0.0.0";
	public static final String    ANY_VALUE                                   =  "*";
	public static final String    COMMA_SEPARATOR                             =  ",";
	public static final Pattern   COMMA_SPLIT_PATTERN                         =  Pattern.compile("\\s*[,]+\\s*");
	public static final String    PROPERTIES_KEY                              = "properties.file";
	public static final String    DEFAULT_RPC_PROPERTIES                      = "rpc.properties";
	/****************************************************      threads       ********************************************************************/
    public static final String    THREADPOOL_KEY                              = "threadpool";
    public static final String    THREAD_NAME_KEY                             = "threadname";
    public static final String    DEFAULT_THREADPOOL                          = "limited";
    public static final String    DEFAULT_CLIENT_THREADPOOL                   = "cached";
    public static final String    IO_THREADS_KEY                              = "iothreads";
    public static final String    CORE_THREADS_KEY                            = "corethreads";
    public static final String    THREADS_KEY                                 = "threads";
    public static final String    ALIVE_KEY                                   = "alive";
    public static final int       DEFAULT_ALIVE                               =  60 * 1000;
    public static final String    EXECUTORS_KEY                               = "executors";
    public static final String    DEFAULT_THREAD_NAME                         = "sunchao_rpc";
    public static final int       DEFAULT_THREADS                             = 200;
    public static final int       DEFAULT_CORE_THREADS                        = 0;
    public static final String    QUEUES_KEY                                  = "queues";
    public static final int       DEFAULT_QUEUES                              = 0;
    public static final String    SERVICE_NAME                                = "service_name";
} 
