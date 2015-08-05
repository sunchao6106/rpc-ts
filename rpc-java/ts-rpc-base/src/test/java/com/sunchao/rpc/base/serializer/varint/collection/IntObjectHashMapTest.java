package com.sunchao.rpc.base.serializer.varint.collection;
import static org.junit.Assert.*;
import junit.framework.TestCase;

import com.sunchao.rpc.base.serializer.support.varint.collection.IntObjectHashMap;

public class IntObjectHashMapTest extends TestCase {
 
	private IntObjectHashMap<String> intMap = new IntObjectHashMap<String>();
	
	public void testMain() {
		insertTest();
		assertNotEquals(intMap.get(10), "3333");
		assertEquals(intMap.get(0), "0000");
		assertEquals(intMap.get(1), "2222");
		assertEquals(intMap.get(34), "4444");
		assertEquals(intMap.get(21), null);
		assertEquals(intMap.get(10), "7777");
		intMap.put(10, "9999");
		assertEquals(intMap.get(10), "9999");
		
	}
	
	public void insertTest() {
		intMap.put(0, "0000");
		intMap.put(1, "2222");
		intMap.put(10, "3333");
		intMap.put(34, "4444");
		intMap.put(21, "6666");
		intMap.put(10, "7777");
		//intMap.containsKey(key)
		intMap.remove(21);
	}
}
