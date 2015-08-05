package com.sunchao.rpc.common.compiler.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.sunchao.rpc.common.utils.ClassUtil;
/**
 * JdkCompiler. (SPI, Singleton, ThreadSafe)
 * 
 * 
 * which use the javax.tools.* to dynamic generate byte code. and load it.
 * @author sunchao
 *
 */
public class JdkCompiler extends AbstractCompiler {
	
	@SuppressWarnings("restriction")
	private final JavaCompiler compiler =ToolProvider.getSystemJavaCompiler();
	@SuppressWarnings("restriction")
	private final DiagnosticCollector<JavaFileObject> diagnosticCollector = new  DiagnosticCollector<JavaFileObject>();
	private final ClassLoaderImpl classLoader;
	private final JavaFileManagerImpl javaFileManager;
	
	private volatile List<String> options;
	
	@SuppressWarnings({ "restriction", "resource" })
	public JdkCompiler() {
		options = new ArrayList<String>();
		options.add("-target");
		options.add("1.6");
		StandardJavaFileManager manager = compiler.getStandardFileManager(diagnosticCollector, null, null);
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader instanceof URLClassLoader
				&& (! loader.getClass().getName().equals("sun.misc.Launcher$AppClassLoader"))) {
			try {
				URLClassLoader urlClassLoader = (URLClassLoader) loader;
				List<File> files = new ArrayList<File>();
				for (URL url : urlClassLoader.getURLs()) {
					files.add(new File(url.getFile()));
				}
				manager.setLocation(StandardLocation.CLASS_PATH, files);
			} catch (IOException  e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoaderImpl>() {

			public ClassLoaderImpl run() {
				 return new ClassLoaderImpl(loader);
			}   
		});
		javaFileManager = new JavaFileManagerImpl(manager, classLoader);
	}
	
	/**
	 * 
	 * 
	 * @author sunchao
	 *
	 */
    private final class ClassLoaderImpl extends ClassLoader {
    	@SuppressWarnings("restriction")
		private final Map<String, JavaFileObject> classes =  new HashMap<String, JavaFileObject>();
    	
    	ClassLoaderImpl(final ClassLoader parentClassLoader) {
    		super(parentClassLoader);
    	}
    	
    	@SuppressWarnings("restriction")
		Collection<JavaFileObject> files() {
    		return Collections.unmodifiableCollection(classes.values());
    	}
    	
    	/**
    	 * firstly to search the local cached.
    	 */
    	@SuppressWarnings("restriction")
		@Override
    	protected Class<?> findClass(final String qualifiedClassName) throws ClassNotFoundException {
            JavaFileObject	file = classes.get(qualifiedClassName);
            if (file != null) {
            	byte[] bytes = ((JavaFileObjectImpl)file).getByteCode();
            	return defineClass(qualifiedClassName, bytes, 0, bytes.length);
            }
            try {
            	return ClassUtil.forNameWithCallerClassLoader(qualifiedClassName, getClass());
            } catch (ClassNotFoundException nf) {
            	return super.findClass(qualifiedClassName);
            }
    	}
    	
    	@SuppressWarnings("restriction")
		void add(final String qulifiedClassName, final JavaFileObject javaFile) {
    		classes.put(qulifiedClassName, javaFile);
    	}
    	
    	@Override
    	protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
    		return super.loadClass(name, resolve);
    	}
	
	
	    @Override
	    public InputStream getResourceAsStream(final String name) {
		   if (name.endsWith(ClassHelper.CLASS_EXTENSION)) {
			    String qualifiedClassName = name.substring(0, name.length() - ClassHelper.CLASS_EXTENSION.length()).replace('/', '.');
			    JavaFileObjectImpl file = (JavaFileObjectImpl)classes.get(qualifiedClassName);
			    if (file != null) {
				     return new ByteArrayInputStream(file.getByteCode());
			    }
		   }
		   return super.getResourceAsStream(name);
	   }
	  
    }
    
    /**
     * <pre>
     * the implementation of the {@link JavaFileObject} and {@link FileObject}, which
     * hold the java code or compiled byte code.
     * </pre>
     * <p>
     * the class can be used with:
     * <ol>
     *   <li>store the code which need by compiler,{@link JavaFileObjectImpl#JavaFileObjectImpl(String, CharSequence)}</li>
     *   <li>store the byte code which the compiler compiled, {@link JavaFileObjectImpl #JavaFileObjectImpl(String, Kind)}</li>
     * </ol>
     * </p>
     * 
     * @author sunchao
     *
     */
    
    @SuppressWarnings("restriction")
	private static final class JavaFileObjectImpl extends SimpleJavaFileObject {
         /** if kind == class, store the byte code. get by{@link JavaFileObjectImpl #openInputStream()}   */
    	private ByteArrayOutputStream bytecode;
    	/**  if kind == Source, store the java code  */
    	private final CharSequence source;
    	
    	public JavaFileObjectImpl(final String baseName, final CharSequence source) {
    		super(ClassHelper.toURI(baseName + ClassHelper.JAVA_EXTENSION), Kind.SOURCE);
    		this.source = source;
    	}
    	
    	JavaFileObjectImpl(final String name, final Kind kind) {
			// TODO Auto-generated constructor stub
    		super(ClassHelper.toURI(name), kind);
    		source = null;
		}
    	
		public JavaFileObjectImpl(URI arg0, Kind arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
			source = null;
			
		}
		
		@Override
		public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
			if (source == null) {
				throw new UnsupportedOperationException("source == null");
			}
			return source;
		}
		
		@Override
		public InputStream openInputStream() {
			return new ByteArrayInputStream(getByteCode());
		}
		
		@Override
		public OutputStream openOutputStream() {
			return bytecode = new ByteArrayOutputStream();
		}
	
		public byte[] getByteCode() {
			return bytecode.toByteArray();
		}
    	
    }
    
    /**
     * <pre>
     * <b>{@link javaFileManager } instance, which use to manage the java code and byte code</b>.
     * </pre>
     * 
     *  all the code in the {@link CharSequence} from  store in the memory, and byte code with the
     *  byte array store in  the memory.
     * @author sunchao
     *
     */
    private static final class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

    	private final ClassLoaderImpl classLoader;
    	
    	private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();
    	
		public JavaFileManagerImpl(JavaFileManager arg0, ClassLoaderImpl classLoader) {
			super(arg0);
			this.classLoader = classLoader;
		}
    	
		@Override
		public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
			FileObject o = fileObjects.get(uri(location, packageName, relativeName));
			if (o != null) 
				return o;
			return super.getFileForInput(location, packageName, relativeName);
		}
		
		public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObject file) {
			fileObjects.put(uri(location, packageName, relativeName), file);
		}
		
		private URI uri(Location location, String packageName, String relativeName) {
			return ClassHelper.toURI(location.getName() + "/" + packageName + "/" + relativeName);
		}
		
		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind, FileObject outputFile)
		         throws IOException {
			JavaFileObject file = new JavaFileObjectImpl(qualifiedName, kind);
			classLoader.add(qualifiedName, file);
			return file;
		}
		
		@Override
		public ClassLoader getClassLoader(JavaFileManager.Location location) {
			return classLoader;
		}
		
		@Override
		public String inferBinaryName(Location loc, JavaFileObject file) {
			if (file instanceof JavaFileObjectImpl)
				return file.getName();
			return super.inferBinaryName(loc, file);
		}
		
		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
		         throws IOException {
			Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			List<URL> urlList = new ArrayList<URL>();
			Enumeration<URL> e = contextClassLoader.getResources("com");
			while (e.hasMoreElements()) {
				urlList.add(e.nextElement());
			}
			ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();
			if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
				for (JavaFileObject file : fileObjects.values()) {
					if (file.getKind() == Kind.CLASS && file.getName().startsWith(packageName)) {
						files.add(file);
					}
				}
				files.addAll(classLoader.files());
			}
		    else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
		       	for (JavaFileObject file  : fileObjects.values()) {
				    if (file.getKind() == Kind.SOURCE && file.getName().startsWith(packageName)) {
					    files.add(file);
				    }
		     	}
		    }
		    for (JavaFileObject file : result) {
		    	files.add(file);
		    }
		    return files;
        }
    }

	@Override
	protected Class<?> doCompile(String name, String source) throws Throwable {
		int i = name.lastIndexOf('.');
		String packageName = i < 0 ? "" : name.substring(0, i);
		String className = i < 0 ? name : name.substring(i + 1);
		JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, source);
		javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, 
				className + ClassHelper.JAVA_EXTENSION, javaFileObject);
		Boolean result = compiler.getTask(null, javaFileManager, diagnosticCollector, options,
				null, Arrays.asList(new JavaFileObject[]{javaFileObject})).call();
		if (result == null || ! result.booleanValue()) {
			throw new IllegalStateException("Compilation failed. class: " + name + ", diagnostics: " + diagnosticCollector);
			
		}
		return classLoader.loadClass(name);
	}

}
