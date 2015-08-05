package com.sunchao.rpc.base.serializer;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

/**
 * The Kryo Serializer Utility Class.
 * NOTE: when use kryo serialization to serializer and deseralizer the message for RPC.
 * The registration order is very important with the registration ID. Below is the official interpretation.
 * 
 * Here SomeClass is registered with Kryo, which associates the class with an int ID. When Kryo writes out an 
 * instance of SomeClass, it will write out this int ID. This is more efficient than writing out the class name, 
 * but requires the classes that will be serialized to be known up front. During deserialization, the registered 
 * classes must have the exact same IDs they had during serialization. The register method shown above assigns 
 * the next available, lowest integer ID, which means the order classes are registered is important. The ID can
 * also be specified explicitly to make order unimportant:
 * 
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class KSerializerHelper {
	
	/**
	 * The Collection which hold the client' message class, and include the 
	 * kryo registration in order by {@link #REGISTER_CLASS_COLLECTION},
	 * {@link #REGISTER_ID_COLLECTION} , {@link #SERIALIER_COLLECTION}}.
	 */
	@SuppressWarnings("rawtypes")
	private static final List<Serializer> SERIALIER_COLLECTION   = new ArrayList<Serializer>();
	private static final List<Integer>    REGISTER_ID_COLLECTION  = new ArrayList<Integer>();
	@SuppressWarnings("rawtypes")
	private static final List<Class>   REGISTER_CLASS_COLLECTION = new ArrayList<Class>();
	
	/**
	 * Register Message.
	 * 
	 * @param classObject
	 *               the message class object.
	 * @param serializer
	 *               the specified serializer.
	 * @param seqId
	 *               the number which represent the registration order.
	 */
	@SuppressWarnings("rawtypes")
	public static synchronized void registerMessage(Class<?> classObject, Serializer serializer, int seqId) {
		REGISTER_CLASS_COLLECTION.add(classObject);
		SERIALIER_COLLECTION.add(serializer);
		REGISTER_ID_COLLECTION.add(seqId);
	}
	
	/**
	 * Because the creation/initialization of Kryo instances is rather expensive, 
	 * in a multithreaded scenario you should pool Kryo instances. A very simple 
	 * solution is to bind Kryo instances to Threads using ThreadLocal, like this:
	 */
	private static final ThreadLocal<Kryo> KRYOS = new ThreadLocal<Kryo>() {
		@Override
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			int length = REGISTER_ID_COLLECTION.size();
			for (int i = 0; i < length; i++) {
				kryo.register(REGISTER_CLASS_COLLECTION.get(i), SERIALIER_COLLECTION.get(i), 
						      REGISTER_ID_COLLECTION.get(i));
			}
			kryo.setRegistrationRequired(true);
			/**
			 * By default, each appearance of an object in the graph after the first is
			 * stored as an integer ordinal. This allows multiple references to the same 
			 * object and cyclic graphs to be serialized. This has a small amount of 
			 * overhead and can be disabled to save space if it is not needed.
			 */
			kryo.setReferences(false);
			return kryo;
		}
		
	};
	
	public static Kryo getKryo() {
		return KRYOS.get();
	}
	
	private KSerializerHelper() {}
    
}
