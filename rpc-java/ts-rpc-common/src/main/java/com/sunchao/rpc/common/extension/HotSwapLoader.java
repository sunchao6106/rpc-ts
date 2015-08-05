package com.sunchao.rpc.common.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.annotation.RPCService;
import com.sunchao.rpc.common.extension.support.ActivateComparator;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
import com.sunchao.rpc.common.utils.ConcurrentHashSet;
import com.sunchao.rpc.common.utils.HolderUtil;
import com.sunchao.rpc.common.utils.StringUtil;

/**
 * <p>
 * the extension object fetch.
 * </p>
 * <li>auto insert the extension point</li>
 * <li>auto wrap the extension point wrap class</li>
 * <li>the default extension point is an adaptive instance</li> 
 * @author sunchao
 */
public class HotSwapLoader<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(HotSwapLoader.class);
	private static final String SERVICES_DIRECTORY = "META-INF/services/";
	private static final String RPC_DIRECTORY = "META-INF/rpc/";
	private static final String RPC_INTERNAL_DIRETORY  = RPC_DIRECTORY + "internal/";
	private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
	/** the map cache include the form of  (class ,ExtensionLoad<Classs>) */
	private static final ConcurrentMap<Class<?>, HotSwapLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, HotSwapLoader<?>>();
	/**  the map of static class<?> of extension, and extension object */
	private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();
	
	/**
	 * *************************************************************************************************************
	 */
	private final Class<?> type; // explain the type which can be extension. (logger..)
	private final DynamicDeployFactory objectFactory; // the adaptive extension factory.
	private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();
	private final HolderUtil<Map<String, Class<?>>> cachedClasses = new HolderUtil<Map<String, Class<?>>>();
	private final Map<String, Activate> cachedActivates =  new ConcurrentHashMap<String, Activate>();
	private volatile Class<?> cachedAdaptiveClass = null; // the class with has @Adaptive;
	private final ConcurrentMap<String, HolderUtil<Object>> cachedInstance =  new ConcurrentHashMap<String, HolderUtil<Object>>();//load files with the key,
	private String cachedDefaultName; //"SPI("xxxx")";
	private final HolderUtil<Object> cachedAdaptiveInstance  = new HolderUtil<Object>(); // the adaptive instance.
	private volatile Throwable createAdaptiveInstanceError;
	
	private Set<Class<?>> cachedWrapperClasses; // the extension with the constructor has the type class. decorator.
	private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();
	
	/**
	 * ***************************************************************************************************************
	 */
	
	
	/**
	 * get the annotation of ("SPI").
	 * @param type
	 *          the class instance
	 * @return 
	 *          flag.
	 */
	private static <T> boolean withExtensionAnnotation(Class<T> type) 
	{
		return type.isAnnotationPresent(Component.class);
	}
	
	/**
	 * extension point load class utility.
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> HotSwapLoader<T> getExtensionLoader(Class<T> type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException("Extension type must not be null!");
		}
		if (!type.isInterface()) //the annotation of "SPI" is in the interface,
		{
			throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
		}
		if (!withExtensionAnnotation(type)) {
			throw new IllegalArgumentException("Exception type(" + type +
					") is not extension, but WITHOUT @" + Component.class.getSimpleName() + "Annotation!");
		}
		HotSwapLoader<T> loader = (HotSwapLoader<T>) EXTENSION_LOADERS.get(type);
		if (loader == null) {
			EXTENSION_LOADERS.putIfAbsent(type,  new HotSwapLoader<T>(type));
			loader = (HotSwapLoader<T>) EXTENSION_LOADERS.get(type);
		}
		return loader;
	}
	
	/**
	 * build new extension load correspond class type.
	 * and specify the adaptive extension factory.
	 * 
	 * @param type
	 */
	private HotSwapLoader(Class<?> type) {
		this.type = type;
		objectFactory = (type == DynamicDeployFactory.class ? null : HotSwapLoader.getExtensionLoader(DynamicDeployFactory.class).getAdaptiveExtension());
	}

	
	public String getExtensionName(T extensionInstance)
	{
		return getExtensionName(extensionInstance.getClass());
	}
	
	/**
	 * get the config extension name of extension class
	 * 
	 * @param extensionClass
	 *                extension class
	 * @return
	 *                extension name.
	 */
	public String getExtensionName(Class<?> extensionClass)
	{
	     return cachedNames.get(extensionClass);	
	}
	
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public List<T> getActivateExtension(String key)
	{
		return getActivateExtension(key);
	}
	
	/**
	 * get activate extensions.
	 * 
	 * @param values
	 *          extension point name
	 * @param group
	 * @return
	 */
	public List<T> getActivateExtension(String[] values)
	{
		List<T> exts = new ArrayList<T>();
		List<String> names = values == null ? new ArrayList<String>(0) : Arrays.asList(values);
		if (! names.contains("-" + "default")) {
			getExtensionClasses();
			for (Map.Entry<String, Activate> entry : cachedActivates.entrySet()) {
				String name = entry.getKey();
				T ext = getExtension(name);
				if (! names.contains(name)
							&& ! names.contains("-" + name)) {
					exts.add(ext);
					
				}
			}
			Collections.sort(exts, ActivateComparator.COMPARATOR);
		}
		List<T> usrs = new ArrayList<T>();
		for (int i = 0; i < names.size(); i++) 
		{
			String name = names.get(i);
			if (! name.startsWith("-")
					&& ! names.contains("-" + name))
			{
				if ("default".equals(name))
				{
					if (usrs.size() > 0) 
					{
						exts.addAll(0, usrs);
						usrs.clear();
					}
				} else {
					T ext = getExtension(name);
					usrs.add(ext);
				}
			}
		}
		if (usrs.size() > 0) {
			exts.addAll(usrs);
		}
		return exts;
	}
	
	/**
	 * return the instance of extension point.
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getLoadedExtension(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Extension name == null!");
		HolderUtil<Object> holder = cachedInstance.get(name);
		if (holder == null) {
			cachedInstance.putIfAbsent(name, new HolderUtil<Object>());
			holder = cachedInstance.get(name); 
		}
		return (T) holder.get();
	}
	
	public Set<String> getLoadedExtensions() {
		return Collections.unmodifiableSet(new TreeSet<String>(cachedInstance.keySet()));
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public T getExtension(String name)  {
		if (name == null || name.length() == 0) 
		{
			throw new IllegalArgumentException("Component name == null");
		}
		if ("true".equals(name)) {
			return getDefaultExtension();
		}
		HolderUtil<Object> holder = cachedInstance.get(name);
		if (holder == null) {
			cachedInstance.putIfAbsent(name, new HolderUtil<Object>());
			holder = cachedInstance.get(name);
		}
		Object instance = holder.get();
		if (instance ==  null) { // concurrent cache , classic
			synchronized (holder) {
				instance = holder.get();
				if (instance == null) {
					instance = createExtension(name);
					holder.set(instance);
				}
			}
		}
		return (T)instance;
	}
	
	public T getDefaultExtension() {
		getExtensionClasses();
		if (null == cachedDefaultName || cachedDefaultName.length() == 0
				|| "true".equals(cachedDefaultName)) { //true
			return null;
		}
		return getExtension(cachedDefaultName);
	}
	
	public boolean hasExtension(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Extension name == null");
		try {
			return getExtensionClass(name) != null;
		} catch (Throwable t) {
			return false;
		}
	}
	
	public Set<String> getSupportedExtensions() {
		Map<String, Class<?>> clazzes = getExtensionClasses();
		return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
	}
	
	public String getDefaultExtensionName() {
		getExtensionClasses();
		return cachedDefaultName;
	}
	
	public void addExtension(String name, Class<?> clazz) {
		getExtensionClasses();
		
		if (! type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Input type " + 
		          clazz + " not implement Component " + type);
		}
		if (clazz.isInterface()) {
			throw new IllegalStateException("Input type"  +
				  clazz + " can not be interface!");
		}
		
		if (! clazz.isAnnotationPresent(HotSwap.class)) {
			if (StringUtil.isBlank(name)) {
				throw new IllegalStateException("Extension name is blank (Component " + type + ")!");
			}
			if (cachedClasses.get().containsKey(name)) {
				throw new IllegalStateException("Component name " + 
			          name  + " already existed (Component " + type  + ")!");
			}
			cachedNames.put(clazz, name);
			cachedClasses.get().put(name, clazz);
		}
		else {
			if (cachedAdaptiveClass != null) {
				throw new IllegalStateException("HotSwap Component already existed ( Component " + type + ")!");
			}
			cachedAdaptiveClass = clazz;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getAdaptiveExtension() {
		Object instance = cachedAdaptiveInstance.get();
		if (instance == null) {
			if (createAdaptiveInstanceError == null) {
				synchronized (cachedAdaptiveInstance) {
					instance = cachedAdaptiveInstance.get();
					if (instance == null) {
						try {
							instance = createAdaptiveExtension();
							cachedAdaptiveInstance.set(instance);
						} catch (Throwable t) {
							createAdaptiveInstanceError = t;
							throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
						}
					}
				}
			}
			
		    else {
				   throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString()
							, createAdaptiveInstanceError);
			}
		}
		return (T) instance;
	}
	
	private IllegalStateException findException(String name)  {
		for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
			if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
				return entry.getValue();
			}
		}
		StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);
		int i = 1;
		for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
			if(i == 1) {
				buf.append(", possible causes: ");
			}
			buf.append("\r\n(");
			buf.append(i++);
			buf.append(") ");
			buf.append(entry.getKey());
			buf.append(":\r\n");
			buf.append(StringUtil.toString(entry.getValue()));
		}
		return new IllegalStateException(buf.toString());
	}
	
	
	/**
	 * get the extension instance, as the same time,
	 * inject the bean property for the extension instance.
	 *  warper the instance.
	 * @param name
	 *           the extension name.
	 * @return
	 *           the extension instance.
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	private T createExtension(String name) {
		Class<?> clazz = getExtensionClasses().get(name);
		if (clazz == null) {
			throw findException(name);
		}
		try {
			T instance = (T) EXTENSION_INSTANCES.get(clazz);
			if (instance == null) {
				EXTENSION_INSTANCES.putIfAbsent(clazz, (T)clazz.newInstance());
				instance = (T) EXTENSION_INSTANCES.get(clazz);
			}
			injectExtension(instance);
			Set<Class<?>> wrapperClasses = cachedWrapperClasses;
			if (wrapperClasses != null && wrapperClasses.size() > 0) {
				for (Class<?> wrapperClass : wrapperClasses) {
					instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
				}
			}
			return instance;
		} catch (Throwable t) {
			throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
		                     type + ") counld not be instantiated: " + t.getMessage(), t);
		}
	}
	
     /**
	 * insert or inject the bean property.
	 * via the setXX method.
	 * 
	 * @param instance
	 *         the instance which need injected.
	 * @return
	 *         the injected instance.
	 */
	private T injectExtension(T instance) {
	   try {
		      if (objectFactory != null) {
			       for (Method method  : instance.getClass().getMethods()) {
			    	   if (method.getName().startsWith("set")
			    			   && method.getParameterTypes().length == 1
			    			   && Modifier.isPublic(method.getModifiers())) {
			    		   Class<?> pt = method.getParameterTypes()[0];
			    		   try {
			    			   String property = method.getName().length() > 3 ? method.getName()
			    					   .substring(3, 4).toLowerCase() + method.getName().substring(4)
			    					   : "";
			    			   Object object = objectFactory.getExtension(pt, property);//via the extension factory get the bean value?
			    			   if (object != null) {
			    				   method.invoke(instance, object); // reflect the set method ,and setter the value.
			    			   }
			    		   } catch (Exception e) {
			    			   LOGGER.error("fail to inject via method " + method.getName()
			    					   + " of interface " + type.getName() + ": " + e.getMessage(), e);
			    		   }
			    	   }
			       }    	  
	          }
	   } catch (Exception e) {
		   LOGGER.error(e.getMessage(), e);
	   }
	   return instance;
	}
	
	/**
	 * simple 
	 * @param name
	 * @return
	 */
	private Class<?> getExtensionClass(String name) {
		if (type == null) 
			throw new IllegalArgumentException("Extension type == null");
		if (type == null)
			throw new IllegalArgumentException("Extension name == null");
		Class<?> clazz = getExtensionClasses().get(name);
		if (clazz == null)
			throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
		return clazz;
	}
	
	/**
	 * the classic cache implementation
	 * the cachedClasses is holder in a utility classs.
	 * @return
	 */
	private Map<String, Class<?>> getExtensionClasses() {
		Map<String, Class<?>> classes = cachedClasses.get();
		if (classes == null) {
			synchronized (cachedClasses) {
				if (classes == null) {
					classes = loadExtensionClasses();
					cachedClasses.set(classes);
				}
			}
		}
		return classes;
	}
	
	/**
	 * load extension classes and cache them.(ThreadSafe)
	 * @return
	 */
	private Map<String, Class<?>> loadExtensionClasses() {
		final Component defaultAnnotation = type.getAnnotation(Component.class);
		if (defaultAnnotation != null) {
			String value = defaultAnnotation.value();
			if (value != null && (value = value.trim()).length() > 0) {
				String[] names = NAME_SEPARATOR.split(value);
				if (names.length > 1) {
					throw new IllegalStateException("more that 1 default extension name on extension " + type.getName()
							+ ":" + Arrays.toString(names));
				}
				if (names.length == 1) {
					cachedDefaultName = names[0]; // "SPI("ZZZ")" default service name.
				}
			}
		}
		Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
		loadFile(extensionClasses, RPC_INTERNAL_DIRETORY);
		loadFile(extensionClasses, RPC_DIRECTORY);
		loadFile(extensionClasses, SERVICES_DIRECTORY);
		return extensionClasses;
	}
	
	/**
	 * the extension load core part.
	 * 
	 * @param extensionClasses
	 * @param dir
	 */
	private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
		String fileName = dir + type.getName();//the path of file + type.name => interface.getName();
		try {
			 Enumeration<java.net.URL> urls;
			 ClassLoader classLoader = findClassLoader();
			 if (classLoader != null) {  // class loader load the resource.
				 urls = classLoader.getResources(fileName);
			 } else {
				 urls = ClassLoader.getSystemResources(fileName);
			 }
			 if (urls != null) {
				 while (urls.hasMoreElements()) {
					 java.net.URL url = urls.nextElement();
					 try {// read the extension metadata.
						 BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
						 try {
							 String line = null;
							 while ((line = reader.readLine()) != null) {
								 final int ci = line.indexOf('#');// comments.
								 if (ci >= 0) line = line.substring(0,ci);
								 line = line.trim();
								 if (line.length() > 0) {
									 try {
										 String name = null;
										 int i = line.indexOf('=');
										 if (i > 0) {
											 name = line.substring(0, i).trim();
											 line = line.substring(i + 1).trim();
										 }
										 if (line.length() > 0 ) { // load the class
											 Class<?> clazz = Class.forName(line, true, classLoader);
											 if (! type.isAssignableFrom(clazz)) { //no implement the interface class.
												 throw new IllegalStateException("Error when load extension class(interface: " +
													  type + ", class line: "  + clazz.getName() +	"), class "
													  + clazz.getName() + " is not subtype of interface.");
											 }
											 
											 if (fileName.startsWith("META-INF/services/")) {
												boolean flag = clazz.isAnnotationPresent(RPCService.class);
												if (! flag) 
													throw new IllegalStateException("Error when load service impl class(interface: " +
												    type + ", class line: " + clazz.getName() + "),class "
												     +  clazz.getName() + " WITHOUT @" + RPCService.class.getSimpleName() + "Annotation");
											 }
											 if (clazz.isAnnotationPresent(HotSwap.class)) {//add the adaptive.
												 if (cachedAdaptiveClass == null) {
													 cachedAdaptiveClass = clazz;//the adaptive class.
												 } else if (! cachedAdaptiveClass.equals(clazz)) { //the cached adaptive class just only have one
													 throw new IllegalStateException("More that 1 adaptive class found: " 
															 + cachedAdaptiveClass.getClass().getName()
															 + ", "  +  clazz.getClass().getName());
												 }
											 } else {
												 try {
													 clazz.getConstructor(type); //wrapper class, decorate pattern.
													 Set<Class<?>> wrappers = cachedWrapperClasses;
													 if (wrappers == null) {
														 cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
														 wrappers = cachedWrapperClasses;
													 }
													 wrappers.add(clazz);
												 } catch (NoSuchMethodException e) {
													 clazz.getConstructor();
													 if (name == null || name.length() == 0) {
														 name = findAnnotationName(clazz);
														 if (name == null || name.length() == 0) { // type ="HelloWorld", class . name ="Impl1HelloWorld";
															 if (clazz.getSimpleName().length() > type.getSimpleName().length()
																	 && clazz.getSimpleName().endsWith(type.getSimpleName())) {
																 name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - 
																		 type.getSimpleName().length()).toLowerCase();
															 } else {
																 throw new IllegalStateException("No such extension name for the class " + clazz.getName() + 
																		 " in the config " + url);
															 }
														 }
													 }
													 
													 String[] names = NAME_SEPARATOR.split(name); 
													 if (names != null && names.length > 0) {  //when the class has many alias, just take the first one .
														 Activate activate = clazz.getAnnotation(Activate.class); //if the class is AnnotationPresent(Atcivate.class);
														 if (activate != null) {
															 cachedActivates.put(names[0], activate); // put the tuple(string, activate.annonatiton) why???
														 }
														 for (String n : names) { //has not the annotation, just general class.
															 if (! cachedNames.containsKey(clazz)) {
																 cachedNames.put(clazz, n); // the tuple of (class , name);
															 }
															 Class<?> c = extensionClasses.get(n); //the one name just has one implementation.
															 if (c == null) {
																 extensionClasses.put(n, clazz); // the tuple of (name, class)
															 } else if (c != clazz) {  // one name has more extension instance.
																 throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on "
																		 + c.getName() + " and " + clazz.getName());
															 }
														 }
													 }
												 }
											 }
										 }
									 } catch (Throwable t) {
										 IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " +
									 line + ") in " + url + ", cause: " + t.getMessage(), t);
										 exceptions.put(line, e);
									 }
								 }
							 }
						 } finally {
							 reader.close();
						 }
					 } catch (Throwable t) {
						 LOGGER.error("Exception when load extension class(interface: " +
					 type + ", class file: " + url + ") in " + url, t);
					 }
				 }
			 }
		} catch (Throwable t) {
			LOGGER.error("Exception when load extension class(interface: " +
		type + ", description file: " + fileName + ").",t);
		}
	}
	@Deprecated
	private String findAnnotationName(Class<?> clazz) {
		Component spi = clazz.getAnnotation(Component.class);
		if (spi == null) {
			String name = clazz.getSimpleName();
			if (name.endsWith(type.getSimpleName())) {
				name = name.substring(0, name.length() - type.getSimpleName().length());
			}
			return name.toLowerCase();
		}
		return spi.value();
	}
	
	
	@SuppressWarnings("unchecked")
	private T createAdaptiveExtension() {
		try {
			return injectExtension((T) getAdaptiveExtensionClass().newInstance());
		} catch (Exception e) {
			throw new IllegalStateException("can not create adaptive extention " + type + ", cause :" + e.getMessage(), e);
		} 
	}
	
	private Class<?> getAdaptiveExtensionClass() {
		getExtensionClasses();
		if (cachedAdaptiveClass != null) {
			return cachedAdaptiveClass;
		}
		return cachedAdaptiveClass = createAdaptiveExtensionClass();
	}
	
	
	private Class<?> createAdaptiveExtensionClass() {
		String code = createAdaptiveExtensionClassCode();
		ClassLoader classLoader = findClassLoader();
		com.sunchao.rpc.common.compiler.Compiler compiler = HotSwapLoader.getExtensionLoader(com.sunchao.rpc.common.compiler.Compiler.class).getAdaptiveExtension();
		return compiler.compile(code, classLoader);
	}
	
	private String createAdaptiveExtensionClassCode() {
		StringBuilder sb =  new StringBuilder();
		Method[] methods = type.getMethods();
		boolean hasAdaptiveAnnotation  = false;
		for (Method m : methods) {
			if (m.isAnnotationPresent(HotSwap.class)) {
				hasAdaptiveAnnotation = true;
				break;
			}
		}
		// has no adaptive method , throw exception.
		if (! hasAdaptiveAnnotation) 
			throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");
		sb.append("\n package " + type.getPackage().getName() + ";");
		sb.append("\n import " + HotSwapLoader.class.getName() + ";");
		sb.append("\n import com.sunchao.rpc.base.exception.*;" );
		sb.append("\n public class " + type.getSimpleName() + "$Adaptive"  + " implements " + type.getCanonicalName() + " {" );
		for (Method method : methods) 
		{
			Class<?> rt = method.getReturnType();
			Class<?>[] pts = method.getParameterTypes();
			Class<?>[] ets = method.getExceptionTypes();
			
			HotSwap adaptiveAnnotation = method.getAnnotation(HotSwap.class);
			StringBuilder code = new StringBuilder(512);
			if (adaptiveAnnotation == null) {
				code.append("throw new UnsupportedOperationException(\"method ")
				            .append(method.toString()).append(" of interface ")
				            .append(type.getName()).append(" is not adaptive method!\");");
			} else {
				//check the configuration whether or not null.
                int configIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(ClientConfig.class)) {
                        configIndex = i; //exists
                        break;
                    }
                }
             //   System.out.println(configIndex);
                // the condition exists.
                if (configIndex != -1) {
                    // Null Point check
                    String s = String.format("\n if (arg%d == null) throw new IllegalArgumentException(\"the client remote configuration cannot null\");",
                                    configIndex); //because the javassist argument flag '$' come from 1, and the '$0' represent the 'this'.
                    code.append(s);
                    
                    s = String.format("\n %s config = arg%d;", ClientConfig.class.getName(), configIndex); 
                    code.append(s);
                   
                }
                // the condition the client configuration not exists.
                else {
                    String getterMethod = null;
                    
                    // look up the parameter types getter method.
                    OUTTER:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if (name.startsWith("get") && name.length() > 3
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == ClientConfig.class) {
                                configIndex = i;
                                getterMethod = name;
                                break OUTTER;
                            }
                        }
                    }
                    
                    if(getterMethod == null) {
                        throw new IllegalStateException("fail to create adative class for interface " + type.getName()
                        		+ ": not found config parameter or config attribute in parameters of method " + method.getName());
                    }
                    
                    // check the client config whether or not null.
                    String s = String.format("\n if (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                                    configIndex, pts[configIndex].getName());
                    code.append(s);
                    //check the argument which has the getter method of client config.
                    s = String.format("\n if (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
                                    configIndex, getterMethod, pts[configIndex].getName(), getterMethod);
                    code.append(s);

                    s = String.format("\n %s config = arg%d.%s();", ClientConfig.class.getName(), configIndex, getterMethod); 
                    code.append(s);
                }
                
                String[] value = adaptiveAnnotation.value();
                // adaptive annotation's value == null
                if(value.length == 0) { //YyyInvokeWraper = {yyy.invoke.wrapper}
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb1 = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if(Character.isUpperCase(charArray[i])) {
                            if(i != 0) {
                                sb1.append(".");
                            }
                            sb1.append(Character.toLowerCase(charArray[i]));
                        }
                        else {
                            sb1.append(charArray[i]);
                        }
                    }
                    value = new String[] {sb1.toString()};
                }
                
                //check the Invocation
                //boolean hasInvocation = false;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].getName().equals("com.sunchao.rpc.api.Invocation")) {
                        // check the invocation argument whether or null
                        String s = String.format("\n if (arg%d == null) throw new IllegalArgumentException(\"invocation cannot be null.\");", i);
                        code.append(s);
                        //rpc method name.
                        s = String.format("\n String methodName = arg%d.getMethodName();", i); 
                        code.append(s);
                       // hasInvocation = true;
                        break;
                    }
                }
                
                //the annotation 'SPI("xxxx")'
                String defaultImplName = cachedDefaultName; //get the user-defined extension configuration.
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if(i == value.length - 1) {
                        if(null != defaultImplName) {
                            if(!"protocol".equals(value[i]))
                                getNameCode = String.format("config.getParameterOrDefault(\"%s\", \"%s\")", value[i], defaultImplName);
                            else
                                getNameCode = String.format("( config.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultImplName);
                        }
                        else { //the default implementation not exists. the annotation flag SPI("null");
                            if(!"protocol".equals(value[i])) 
                                getNameCode = String.format("config.getParameter(\"%s\")", value[i]);
                            else
                                getNameCode = "config.getProtocol()";
                        }
                    }
                    else {
                        if(!"protocol".equals(value[i]))
                            getNameCode = String.format("config.getParameterOrDefault(\"%s\", %s)", value[i], getNameCode);
                        else
                            getNameCode = String.format("config.getProtocol() == null ? (%s) : config.getProtocol()", getNameCode);
                    }
                }
                code.append("\n String implName = ").append(getNameCode).append(";");
                // check implName whether or not null.
                String s = String.format("\n if(implName == null) " +
                		"throw new IllegalStateException(\"Fail to get extension(%s) name from config(\" + config.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);
                
                s = String.format("\n %s extension = (%s)%s.getExtensionLoader(%s.class).getExtension(implName);",
                        type.getName(),type.getName(), HotSwapLoader.class.getSimpleName(), type.getName());
                code.append(s);
                
                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\n return ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }
            
            sb.append("\n public " + rt.getCanonicalName() + " " + method.getName() + "(");
            for (int i = 0; i < pts.length; i ++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(pts[i].getCanonicalName());
                sb.append(" ");
                sb.append("arg" + i);
            }
            sb.append(")");
            if (ets.length > 0) {
                sb.append(" throws ");
                for (int i = 0; i < ets.length; i ++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(ets[i].getCanonicalName());
                }
            }
            sb.append(" {");
            sb.append(code.toString());
            sb.append("\n}");
        }
        sb.append("\n}");
        if (LOGGER.isInfoEnabled()) {
            LOGGER.debug(sb.toString());
        }
        return sb.toString();
    }
	
	private static ClassLoader findClassLoader() {
		return HotSwapLoader.class.getClassLoader();
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + "[" + type.getName() + "]";
	}
}
