package com.sunchao.rpc.common.status.support;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

import com.sunchao.rpc.common.extension.Activate;
import com.sunchao.rpc.common.status.Status;
import com.sunchao.rpc.common.status.StatusChecker;

/**
 * Load Status.
 * @author sunchao
 *
 */
@Activate
public class LoadStatusChecker implements StatusChecker  {

	/**
	 * @see java.lang.management.ManagementFactory#getOperatingSystemMXBean();
	 * @see java.lang.management.ManagementFactory#getClassLoadingMXBean();
	 * @see java.lang.management.ManagementFactory#getCompilationMXBean();
	 * @see java.lang.management.ManagementFactory#getGarbageCollectorMXBeans();
	 * @see java.lang.management.ManagementFactory#getMemoryManagerMXBeans();
	 * @see java.lang.management.ManagementFactory#getMemoryMXBean();
	 */
	public Status check()  {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
	    double load;
	    try {
			Method method = OperatingSystemMXBean.class.getMethod("getSystemLoadAverage", new Class<?>[0]);
		    load = (Double) method.invoke(operatingSystemMXBean, new Object[0]);
	    } catch (Throwable e) {
			load = -1;
		} 
	    int cpu = operatingSystemMXBean.getAvailableProcessors();
	    return new Status(load < 0 ? Status.Level.UNKNOWN :
	    	(load < cpu ? Status.Level.OK : Status.Level.WARN), (load < 0 ? "" : "load: " + load + ",") + "cpu: " + cpu);
	}

}
