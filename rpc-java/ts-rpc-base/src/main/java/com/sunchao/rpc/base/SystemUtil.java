package com.sunchao.rpc.base;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

/**
 * System utility.
 * The <i>Linux</i> platform selector debug is fixed until <i>JDK6u4</i>
 * 
 * @see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6403933">JDK-6403933 : (se) Selector doesn't block on Selector.select(timeout) (lnx)</a>
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public final class SystemUtil {
	
	public static final String OS_NAME = System.getProperty("os.name");
	
	private static boolean isLinuxPlatform = false;
	
	static {
		if (OS_NAME != null && OS_NAME.toLowerCase().indexOf("linux") >= 0)
			isLinuxPlatform = true;
	}
	
	public static final String JDK_VERSION = System.getProperty("java.version");
	
	private static boolean isAfterJava6u4Version = false;
	
	static {
		if (JDK_VERSION != null) {
			if (JDK_VERSION.indexOf("1.4.") >= 0
					|| JDK_VERSION.indexOf("1.5.") >= 0) {  //1.4 , 1.5
				isAfterJava6u4Version = false;
			} else if (JDK_VERSION.indexOf("1.6.") >= 0) {
				int index = JDK_VERSION.indexOf("_"); //get the sub version number.
				if (index > 0) {
					String subVersion = JDK_VERSION.substring(index + 1);
					if (subVersion != null && subVersion.length() > 0) {
						try {
							int subVersionNum = Integer.parseInt(subVersion);
							if (subVersionNum >= 4) {
								isAfterJava6u4Version = true;
							}
						} catch (NumberFormatException e) {
							//ignore.
						}
					}
				}
			} else { // > java6.
				isAfterJava6u4Version = true;
			}
		}
	}

	public static final boolean isLinuxPlatform() {
		return isLinuxPlatform;
	}
	
	public static final boolean isAfterJava6u4Version() {
		return isAfterJava6u4Version;
	}
	
	public static final int getCpuProcessorCount() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	/*public static void main(String args[]) {
		try {
			openSelector();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	public static final Selector openSelector() throws IOException {
		Selector ret = null;
		if (isLinuxPlatform()) {
			try {
				Class<?> providerClazz = Class
						.forName("sun.nio.ch.EPollSelectorProvider");
				if (providerClazz != null) {
					try {
						Method method = providerClazz.getMethod("provider");
						if (method != null) {
							SelectorProvider selectorProvider = (SelectorProvider) method.invoke(null);
							if (selectorProvider != null) {
								ret = selectorProvider.openSelector();
							}
						}
					} catch (Exception e) {
						//ignore.
					}
				}
			} catch (Exception e) {
				//ignore.
			}
		}
		if (ret == null)
			ret = Selector.open();
		return ret;
	}
}
