package com.sunchao.rpc.base.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.sunchao.rpc.common.annotation.RPCService;
import com.sunchao.rpc.common.extension.Component;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.utils.ConcurrentHashSet;
import com.sunchao.rpc.common.utils.HolderUtil;
import com.sunchao.rpc.common.utils.StringUtil;

public class ServiceLoader<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoader.class);
	
	private static final String SERVICES_DIRECTORY = "META-INF/services/";
	
	private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
	
	private static final ConcurrentMap<Class<?>, ServiceLoader<?>> SERVICE_LOADERS = 
			                                        new ConcurrentHashMap<Class<?>, ServiceLoader<?>>();
	
	
	private static final ConcurrentMap<Class<?>, Object> SERVICE_INSTANCES = 
			                                        new ConcurrentHashMap<Class<?>, Object>();
	
	/***************************************************************************************************/
	
	private final Class<?> type;
	
	//private final ServiceFactory serviceFactory;
	
	private final HolderUtil<Object> cachedAdaptiveInstance = new HolderUtil<Object>();
	
	/**
	 * List(the service implements name collection)
	 */
	private final ConcurrentHashSet<String> cacheNames = new ConcurrentHashSet<String>();
	
	/**
	 *   the service implement name === > the service implement's class .
	 */
	private final HolderUtil<Map<String, Class<?>>> name2Class = new HolderUtil<Map<String,Class<?>>>();
	
	/**
	 * the service implement name ====> the service implement instance.
	 */
	private final ConcurrentMap<String, HolderUtil<Object>> cachedInstances = new ConcurrentHashMap<String, HolderUtil<Object>>();
	
	private volatile Throwable createAdaptiveInstanceError;

	private String cachedDefaultName;
	
	private final HolderUtil<Map<Class<?>, String>> class2Name = new HolderUtil<Map<Class<?>,String>>();
	
	private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();
	
	
	private static <T> boolean withSPIAnnotation(Class<T> type) {
		return type.isAnnotationPresent(Component.class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ServiceLoader<T> getServiceLoader(Class<T> type) {
		if (type == null) 
			throw new IllegalArgumentException("Service type == null");
		if (! type.isInterface()) 
			throw new IllegalArgumentException("Service type(" + type + ") is not interface!");
		if (! withSPIAnnotation(type)) {
			throw new IllegalArgumentException("Service type(" + type  + 
					") is not service interface, because WITHOUT @" + Component.class.getSimpleName() + "Annotation!");
		}
		
		ServiceLoader<T> loader = (ServiceLoader<T>) SERVICE_LOADERS.get(type);
		if (loader == null) {
			SERVICE_LOADERS.putIfAbsent(type, new ServiceLoader<T>(type));
			loader = (ServiceLoader<T>) SERVICE_LOADERS.get(type);
		}
		return loader;
	}
	
	private ServiceLoader(Class<?> type) {
		this.type = type;
		//this.serviceFactory = (type == ServiceFactory.class ? null : ServiceLoader.getServiceLoader(ServiceFactory.class).getAdaptiveServiceImpl());
	}

	public String getServiceImplName(T serviceInstance) {
		return getServiceImplName(serviceInstance.getClass());
	}
	
	public String getServiceImplName(Class<?> serviceClass) {
		return class2Name.get().get(serviceClass);
	}
	
	/**
	 * Return Service implement instance, if don't sure the the service point or
	 * has no load(init), return <code>null</code> instead, this method do not
	 * invoke the service point be loaded.
	 * 
	 * always to invoke the method {@link #getService(Class<?>)) }.
	 * 
	 * @param name
	 *           the service alias name.
	 * @return
	 *           the service implement instance.
	 */
	@SuppressWarnings("unchecked")
	public T getLoadedServiceImpl(String name) {
		if (name == null || name.length() == 0) 
			throw new IllegalArgumentException("Service impl name  == null");
		HolderUtil<Object> holder = cachedInstances.get(name);
		if (holder == null) {
			cachedInstances.putIfAbsent(name, new HolderUtil<Object>());
			holder = cachedInstances.get(name);
		}
		return (T) holder.get();
	}
	
	public Set<String> getLoadedServiceImpls() {
		return Collections.unmodifiableSet(new TreeSet<String>(cacheNames));
	}
	
	/**
	 * return the defined name's service implement instance, if the defined name
	 * not existed , throw {@link IllegalStateException}.
	 * 
	 * @param name
	 *           the defined service name.
	 * @return
	 *           the implement instance.
	 */
	@SuppressWarnings("unchecked")
	public T getServiceImpl(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Service name  == null");
		if ("true".equals(name)) {
			return getDefaultServiceImpl();
		}
		HolderUtil<Object> holder = cachedInstances.get(name);
		if (holder == null) {
			cachedInstances.putIfAbsent(name, new HolderUtil<Object>());
			holder = cachedInstances.get(name);
		}
		Object instance = holder.get();
		if (instance == null) {
			synchronized(holder) {
				instance = holder.get();
				if (instance == null) {
					instance = createServiceImpl(name);
					holder.set(instance);
				}
			}
		}
		return (T) instance;
	}
	
	/**
	 * return the default service, if do not set, return <code>null</code>.
	 * 
	 */
	public T getDefaultServiceImpl() {
		getServiceImplClasses();
		if (null == cachedDefaultName || cachedDefaultName.length() == 0
				  || "true".equals(cachedDefaultName)) {
			return null;
		}
		return getServiceImpl(cachedDefaultName);
	}
	
	public boolean hasServiceImpl(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Serice name == null");
		try {
			return getServiceImplClass(name) != null;
		} catch(Throwable t) {
			return false;
		}
	}
	
	public Set<String> getSupportedServiceImpl() {
		Map<String, Class<?>> clazzes = getServiceImplClasses();
		return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
	}
	
	public String getDefaultServiceImplName() {
		getServiceImplClasses();
		return cachedDefaultName;
	}
	
	
	/**
	 * client point to add, not to load the location
	 * of "META-INFO/services/"
	 * 
	 * @param name
	 *            service name.
	 * @param clazz
	 *            service impl class.
	 */
	public void addServiceImpl(String name, Class<?> clazz) {
		getServiceImplClasses();
		
		if (!type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Input type "  + 
		              clazz + "not implement Service " + type);
		}
		if (clazz.isInterface()) {
			throw new IllegalStateException("Input type " +
		               clazz + "can not be interface!");
		}
		
		if (name2Class.get().containsKey(name)) {
			throw new IllegalStateException("Service name " +
		               name + "already existed(Service " + type + ")!");
		}
		if (class2Name.get().containsKey(clazz)) {
			throw new IllegalStateException("Service impl class name  " +
		               clazz.getSimpleName() + "already existed(Service " + type + ")!");
		}
		cacheNames.add(name);
		name2Class.get().put(name, clazz);
		class2Name.get().put(clazz, name);
	}
	
	private IllegalStateException findException(String name) {
		for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
			if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
				return entry.getValue();
			}
		}
		StringBuilder sb = new StringBuilder("No such service impl " + type.getName() + 
				                             " by name " + name);
		
		int i = 1;
		for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
			if (i == 1) {
				sb.append(", possible causes: ");
			}
			sb.append("\r\n(");
			sb.append(i ++);
			sb.append(") ");
			sb.append(entry.getKey());
			sb.append(":\r\n");
			sb.append(StringUtil.toString(entry.getValue()));
		}
		return new IllegalStateException(sb.toString());
	}
	
	@SuppressWarnings("unchecked")
	private T createServiceImpl(String name) {
		Class<?> clazz = getServiceImplClasses().get(name);
		if (clazz == null) {
			throw findException(name);
		}
		try {
			T instance = (T) SERVICE_INSTANCES.get(clazz);
			if (instance == null) {
				SERVICE_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
				instance = (T) SERVICE_INSTANCES.get(clazz);
			}
			return instance;
		} catch (Throwable t) {
			throw new IllegalStateException("Service instance(name: " + name + ", class: " + 
		                        type + ") could not to instantiated: " + t.getMessage(), t);
		}
	}
	
	private Class<?> getServiceImplClass(String name) {
		if (type == null)
			throw new IllegalArgumentException("Service interface type == null");
		if (name == null)
			throw new IllegalArgumentException("Service impl name == null");
		Class<?> clazz = getServiceImplClasses().get(name);
		if (clazz == null)
			throw new IllegalStateException("No such service \"" + name + "\" for " + type.getName() + "!");
		return clazz;
	}
	
	private Map<String, Class<?>> getServiceImplClasses() {
		Map<String, Class<?>> classes = name2Class.get();
		if (classes == null) {
			synchronized (name2Class) {
				classes = name2Class.get();
				if (classes == null) {
					classes = loadServiceImplClasses();
					name2Class.set(classes);
				}
			}
		}
		return classes;
	}
	
	private Map<String,Class<?>> loadServiceImplClasses() {
		final Component defaultAnnotation = type.getAnnotation(Component.class);
		if (defaultAnnotation != null) {
			String value = defaultAnnotation.value();
			if (value != null && (value = value.trim()).length() > 0) {
				String[] names = NAME_SEPARATOR.split(value);
				if (names.length > 1) {
					throw new IllegalStateException("more that one default service impl name on service interface " + 
				             type.getName() + ": " + Arrays.toString(names));
				}
				if (names.length == 1)  cachedDefaultName = names[0];	
			}
		}
		Map<String, Class<?>> serviceImplClasses = new HashMap<String, Class<?>>();
		loadFile(serviceImplClasses, SERVICES_DIRECTORY);
		return serviceImplClasses;
	}
	
	private void loadFile(Map<String, Class<?>> serviceImplClasses, String dir) {
		String filename = dir + type.getName();
		try {
			URL url;
			ClassLoader classLoader = findClassLoader();
			if(classLoader != null) {
				url = classLoader.getResource(filename);
			} else {
				url = ClassLoader.getSystemResource(filename);
			}
			if (url != null) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
					try {
						String line = null;
						while ((line = reader.readLine()) != null) {
							final int split = line.indexOf('#');
							if (split >= 0) line = line.substring(0, split);
							line = line.trim();
							if (line.length() > 0) {
						        String name = null;
						        int index = line.indexOf('=');
						        if (index > 0) {
						        	name = line.substring(0 , index);
						        	line = line.substring(index + 1).trim();
						        }
						        if (line.length() > 0) {
						        	Class<?> clazz = Class.forName(line, true, classLoader);
						        	if (! type.isAssignableFrom(clazz)) {
						        		throw new IllegalStateException("Error when load service impl class(interface: " +
						        	          type + ", class line: " + clazz.getName() + "), class "
						        	          + clazz.getName() + "is not subtype of interface.");
						        	}
						        	if (! clazz.isAnnotationPresent(RPCService.class)) {
						        		throw new IllegalStateException("Error when load service imple class(interface: " +
						        	          type + ", class line: " + clazz.getName() + "), class "
						        	          + clazz.getName() + "WITHOUT @" + RPCService.class.getSimpleName() + " Annotation!");
						        	}
						        	String[] names = NAME_SEPARATOR.split(name);
						        	if (names != null && names.length > 0) {
						        		for (String n : names) {
						        			if (! class2Name.get().containsKey(clazz)) {
						        				class2Name.get().put(clazz, n);
						        			}
						        			Class<?> c = name2Class.get().get(n);
						        			if (c == null) {
						        				name2Class.get().put(n, clazz);
						        			} else if (c != clazz) {
						        				throw new IllegalStateException("Duplicate service impl " + type.getName() + 
						        						" name " + n + " on " + c.getName() + " and " + clazz.getName());
						        			}
						        		}
						        	}
						        }
							}
						}
					} catch (Throwable t) {
						    throw  new IllegalStateException("Faild to load service impl class(interface: " + type + 
								") in " + url + ", cause: " + t.getMessage(), t);
					} finally {
						reader.close();
					}
				} catch (Throwable t) {
					LOGGER.error("Exception when load service class(interface: " + 
				                      type + ", class file: " + url + ") in " + url, t);
				} 
			}
			
		} catch (Throwable t) {
			LOGGER.error("Exception when load service impl class(interface: " +
		            type + ", description file: " + filename + "),", t);
		}
	}
	
	private static ClassLoader findClassLoader() {
		return ServiceLoader.class.getClassLoader();
	}
	

	
}


