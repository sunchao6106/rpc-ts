package com.sunchao.rpc.common.status.support;

import com.sunchao.rpc.common.extension.Activate;
import com.sunchao.rpc.common.status.Status;
import com.sunchao.rpc.common.status.StatusChecker;

/**
 * Memory check.
 * @author sunchao
 *
 */
@Activate
public class MemoryStatusChecker implements StatusChecker {

	public Status check() {
		Runtime runtime = Runtime.getRuntime();
		long freeMemory = runtime.freeMemory();// free memory
	    long maxMemory = runtime.maxMemory();  // try to get max memory. 
	    long totalMemory = runtime.totalMemory();  // total memory
	    boolean ok = (maxMemory - (totalMemory - freeMemory) > 2 * 1024 * 1024); // the remaining memory less that 2M alarm;
	    String msg = "max: " + (maxMemory / 1024/ 1024) + "M, total: " 
	    + (totalMemory / 1024 / 1024) + "M, used: " + ((totalMemory / 1024 / 1024) - (freeMemory /1024 / 1024)) + "M, free: " +
	    (freeMemory / 1024 / 1024)	+ "M";
	    return new Status(ok ? Status.Level.OK : Status.Level.WARN, msg);
	}

}
