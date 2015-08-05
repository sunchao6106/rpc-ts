package com.sunchao.rpc.common.utils;

import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Level;

import com.sunchao.rpc.common.annotation.Utility;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Log Utility.
 * @author sunchao
 *
 */
@Utility
public class LogUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);
	
	public static void start()
	{
		RPCAppender.doStart();
	}
	
	public static void stop()
	{
		RPCAppender.doStop();
	}
	
	public static boolean checkNoError() {
		if (findLevel(Level.ERROR) == 0) 
			return true;
		else 
			return false;
	}
	
	public static int findName(String expectedLogName)
	{
		int count = 0;
		List<Log> logList = RPCAppender.logList;
		for (int i = 0; i < logList.size(); i++)
		{
			String logName = logList.get(i).getLogName();
			if (logName.contains(expectedLogName))
				count++;
		}
		return count;
	}
	
	public static int findLevel(Level expectedLevel)
	{
		int count = 0;
		List<Log> logList = RPCAppender.logList;
		for (int i = 0; i < logList.size(); i++)
		{
			Log log = logList.get(i);
			if (log.getLogLevel().equals(expectedLevel))
				count++;
		}
		return count;
	}
	
	public static int findLevelWithThreadName(Level expectedLevel, String threadName)
	{
		int count = 0;
		List<Log> logList = RPCAppender.logList;
		for (int i = 0; i < logList.size(); i++)
		{
			Log log = logList.get(i);
			if (log.getLogLevel().equals(expectedLevel) && log.getLogThread().equals(threadName))
				count++;
		}
		return count;
	}
	
	public static int findThread(String expectedThread)
	{
		int count =0 ;
		List<Log> logList = RPCAppender.logList;
		for (int i = 0; i < logList.size(); i++)
		{
			String logThread = logList.get(i).getLogThread();
			if (logThread.contains(expectedThread))
				count++;
		}
		return count++;
	}
	
	public static int findMessage(String expectedMessage)
	{
		int count = 0;
		List<Log> logList = RPCAppender.logList;
		for (int i = 0; i < logList.size(); i++)
		{
			String logMessage = logList.get(i).getLogMessage();
			if (logMessage.contains(expectedMessage))
				count++;
		}
		return count;
	}
	
	public static int findMessage(Level expectedLevel, String expectedMessage)
	{
		int count = 0;
		List<Log> logList = RPCAppender.logList;
		for (int i = 0; i < logList.size(); i++)
		{
			Level level = logList.get(i).getLogLevel();
			if (level.equals(expectedLevel)) {
				String logMessage = logList.get(i).getLogMessage();
				if (logMessage.contains(expectedMessage))
					count++;
			}
		}
		return count;
	}
	
	public static <T> void printList(List<T> list)
	{
		LOGGER.info("printList: ");
		Iterator<T> it = list.iterator();
		while (it.hasNext()) {
			LOGGER.info(it.next().toString());
		}
	}
}
