package com.sunchao.rpc.common.utils;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.spi.LoggingEvent;

public class RPCAppender extends ConsoleAppender {

	public static boolean available = false;
	
	public static List<Log> logList = new ArrayList<Log>();
	
	public static void doStart() 
	{
		available = true;
	}
	
	public static void doStop()
	{
	   available = false;	
	}
	
	public static void clear()
	{
		logList.clear();
	}
	
	public void append(LoggingEvent event) 
	{
		super.append(event);
		if (available == true)
		{
			Log temp = parseLog(event);
			logList.add(temp);
		}
	}
	
	private Log parseLog(LoggingEvent event)
	{
		Log log = new Log();
		log.setLogLevel(event.getLevel());
		log.setLogName(event.getLogger().getName());
		log.setLogMessage(event.getMessage().toString());
		log.setLogThread(event.getThreadName());
		return log;
	}
}
