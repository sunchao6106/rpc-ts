package com.sunchao.rpc.common.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sunchao.rpc.common.Constants;
import com.sunchao.rpc.common.annotation.Utility;
import com.sunchao.rpc.common.extension.HotSwapLoader;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * The config utility.
 * 
 * @author sunchao
 *
 */
@Utility("ConfigUtil")
public class ConfigUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
	
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0
				|| "false".equalsIgnoreCase(str)
				|| "0".equalsIgnoreCase(str)
				|| "null".equalsIgnoreCase(str)
				|| "N/A".equalsIgnoreCase(str);
	}
	
	public static boolean isNotEmpty(String str) {
		return ! isEmpty(str);
	}
	
	/**
	 * the default flag 
	 * @param str
	 *         config property name.
	 * @return
	 *        flag
	 */
	public static boolean isDefaultValue(String str) {
		return "true".equalsIgnoreCase(str) 
				|| "default".equalsIgnoreCase(str);
	}
	
	/**
	 * insert default extension point in extension point list.
	 * <code><li>default</li></code>  explains the site with default extension point.
	 * <code><li>-</code></li> explains the delete. <code>-foo</code> delete add the default extension point(foo).
	 * <code>-default</code> delete add all default extension points.
	 * @param type
	 *        extension point type.
	 * @param cfg
	 *        extension point name list.
	 * @param def
	 *        default extension point list.
	 * @return
	 *        end up default extension point list.
	 */
	public static List<String> mergeValues(Class<?> type, String cfg, List<String> def) 
	{
		List<String> defaults = new ArrayList<String>();
		if (def != null) {
			for (String name : def) 
			{
				if (HotSwapLoader.getExtensionLoader(type).hasExtension(name)) {
					defaults.add(name);
				}
			}
		}
		List<String> names = new ArrayList<String>();
		// the default values.
		String[] configs = (cfg == null || cfg.trim().length() == 0) ? new String[0] :
			Pattern.compile("\\s*[,]+\\s*").split(cfg);
		
		for (String config : configs) {
			if (config != null && config.trim().length() > 0) {
				names.add(config);
			}
		}
		// not contains the -default.
		if (! names.contains("-" + "default")) {
			//  insert default extension point.
			int i = names.indexOf("default") ;
			if (i >  0) {
				names.addAll(i, defaults) ;
			} else {
				names.addAll(0, defaults);
			}
			names.remove("default");
		} else {
			names.remove("default");
		}
		// merge the '-' configuration property.
		for (String name : new ArrayList<String>(names)) {
			if (name.startsWith("-")) {
				names.remove(name);
				names.remove(name.substring(1));
			}
		}
		return names;
	}
	
	private static Pattern VARIABLE_PATTERN = Pattern.compile(
			"\\$\\s*\\{?\\s*([\\._0-9a-zA-Z]+)\\s*\\}?");
	
	/**
	 * 
	 * replace the property value with the
	 * params.
	 * 
	 * @param expression
	 *         the replaced expression.
	 * @param params
	 *        the key-value map.
	 * @return
	 *        the replaced result.
	 */
	public static String replaceProperty(String expression, Map<String, String> params) {
		if (expression == null || expression.length() == 0 || expression.indexOf('$') < 0) {
			return expression;
		}
		Matcher matcher = VARIABLE_PATTERN.matcher(expression);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = System.getProperty(key);
			if (value == null && params != null) {
				value = params.get(key);
			}
			if (value == null) {
				value = "";
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	private static volatile Properties PROPERTIES;
	
	/**
	 *  get properties instance.
	 * @return
	 */
	public static Properties getProperties() {
		if (PROPERTIES == null) {
			synchronized (ConfigUtil.class) {
				if (PROPERTIES == null) {
					String path = System.getProperty(Constants.PROPERTIES_KEY);
					if (path == null || path.length() == 0) {
						path =  System.getenv(Constants.PROPERTIES_KEY);
						if (path == null || path.length() == 0) {
							path = Constants.DEFAULT_RPC_PROPERTIES;
						}
					}
					PROPERTIES = ConfigUtil.loadProperties(path, false, true);
				}
			}
		}
		return PROPERTIES;
	}
	
	/**
	 * merge to a big one.
	 * @param properties
	 */
	public static void addProperties(Properties properties) {
		if (properties != null) {
			getProperties().putAll(properties);
		}
	}
	
	
	public static void setProperties(Properties properties) {
		if (properties != null) {
			PROPERTIES = properties;
		}
	}
	
	public static String getProperty(String key) {
		return getProperty(key, null);
	}
	
	/**
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getProperty(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value != null && value.length() > 0) {
			return value;
		}
		Properties properties = getProperties();
		return replaceProperty(properties.getProperty(key,defaultValue), (Map)properties);
	}
	
	public static Properties loadProperties(String fileName) {
		return loadProperties(fileName, false, false);
	}
	
	public static Properties loadProperties(String fileName, boolean allowMultiFile) {
		return loadProperties(fileName, allowMultiFile, false);
	}
	
	public static Properties loadProperties(String fileName, boolean allowMultiFile, boolean optional) {
		Properties properties = new Properties();
		if (fileName.startsWith("/")) {
			try {
				FileInputStream input = new FileInputStream(fileName);
				try {
					properties.load(input);
				} finally {
					input.close();
				}
			}  catch (Throwable t) {
				LOGGER.warn("Failed to load " + fileName + " file from " + fileName + "(ignore this file): " + t.getMessage(), t);
			}
			return properties;
		}
		
		List<java.net.URL> list = new ArrayList<java.net.URL>();
		try {
			Enumeration<java.net.URL> urls =  ClassUtil.getClassLoader().getResources(fileName);
			list = new ArrayList<java.net.URL>();
			while (urls.hasMoreElements()) {
				list.add(urls.nextElement()) ;
			}
		} catch (Throwable t) {
			LOGGER.warn("Fail to load " + fileName + " flie: " + t.getMessage(), t);
		}
		
		if (list.size() == 0) {
			if (! optional) {
				LOGGER.warn("No " + fileName + " found on the class path.");
			}
			return properties;
		}
		
		if (! allowMultiFile) {
			if (list.size() > 1) {
				String errMsg = String.format("only 1 %s file is expected, but %d rpc.properties files on class path: %s",
						fileName, list.size(), list.toString());
				LOGGER.warn(errMsg);
			}
			
			try {
				properties.load(ClassUtil.getClassLoader().getResourceAsStream(fileName));
			} catch (Throwable t) {
				LOGGER.warn("Fail to load " + fileName + " file from " + fileName + "(ignore this file):" + t.getMessage(), t );
			}
			return properties;
		}
		
		LOGGER.info("load " + fileName + " properties file from " + list);
		for (java.net.URL url  : list) {
			try {
				Properties p = new Properties();
				InputStream input = url.openStream();
				if (input != null) {
					try {
						 p.load(input);
						 properties.putAll(p);
					} finally {
						try{
						     input.close();
						} catch (Throwable t) {}
					}
				}
			} catch (Throwable t) {
				LOGGER.warn("Fail to load " + fileName + " file from " + url + "(ignore this file): " + t.getMessage(), t);
			}
		}
		return properties;
	}
	
	private static int PID = -1;
	
	public static int getPid() {
		if (PID < 0) {
			try {
				RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
				String name = runtime.getName();
				PID = Integer.parseInt(name.substring(0, name.indexOf('@')));
			} catch(Throwable e) {
				PID = 0;
			}
		}
		return PID;
	}
	
	private ConfigUtil() {
		
	}
}
