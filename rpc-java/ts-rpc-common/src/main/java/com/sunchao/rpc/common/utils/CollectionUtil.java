package com.sunchao.rpc.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunchao.rpc.common.annotation.Utility;
/**
 * The collection utility.
 * 
 * @author sunchao
 *
 */

@Utility("CollectionUtil")
public class CollectionUtil {

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<T> sort(List<T> list) {
		if (list != null && list.size() > 0)
		{
			Collections.sort((List)list);
		}
		return list;
	}
	
	/**
	 * the class name comparator, and substring the last index of '.'
	 */
	private static final Comparator<String> SIMPLE_NAME_COMPARATOR  = new Comparator<String>() {

		public int compare(String s1, String s2) {
			if (s1 == null && s2 == null) 
			{
				return 0;
			}
			if (s1 == null)
			{
				return -1;
			}
			if (s2 == null)
			{
				return 1;
			}
			int i1 = s1.lastIndexOf('.');
			if (i1 >= 0)
			{
				s1 = s1.substring(i1 + 1);
			}
			int i2 = s2.lastIndexOf('.');
			if (i2 >= 0)
			{
				s2 = s2.substring(i2 + 1);
			}
			return s1.compareToIgnoreCase(s2);
		}
	};
	
	/**
	 * Compare the list with the simple-name-comparator.
	 * @param list
	 *          the list argument.
	 * @return
	 *          the sorted list.
	 */
	public static List<String> sortSimpleName(List<String> list)
	{
		if (list != null && list.size() > 0)
		{
			Collections.sort(list,SIMPLE_NAME_COMPARATOR);
		}
		return list;
	}
	
	/**
	 * split all the map<String,List> with the separator char sequence.
	 * with the key
	 * @param list
	 * @param separator
	 * @return
	 */
	public static Map<String, Map<String, String>> splitAll(Map<String, List<String>> list, String separator)
	{
		if (list == null)
		{
			return null;
		}
		Map<String, Map<String, String>> result =  new HashMap<String, Map<String, String>>();
		for (Map.Entry<String, List<String>> entry : list.entrySet())
		{
			result.put(entry.getKey(), split(entry.getValue(), separator));
		}
		return result;
	}
	
	/**
	 * equals to the join()
	 * please to @see join();
	 * @param map
	 * @param separator
	 * @return
	 */
	public static Map<String, List<String>> joinAll(Map<String, Map<String, String>> map, String separator)
	{
		if (map == null)
		{
			return null;
		}
		Map<String, List<String>> result =  new HashMap<String, List<String>>();
		for (Map.Entry<String, Map<String, String>> entry : map.entrySet())
		{
			result.put(entry.getKey(), join(entry.getValue(), separator));
		}
		return result;
	}
	
	/**
	 * use the  keyword 'separator' to split the every
	 * element of list, and store in the map,
	 * 
	 * @param list
	 *          the list store string.
	 * @param separator
	 *          the split char sequence.
	 * @return 
	 *         the map with the key split first and value split second.
	 */
	public static Map<String, String> split(List<String> list, String separator)
	{
		if (list == null)
		{
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		if (list == null || list.size() == 0)
		{
			return map;
		}
		for (String item : list)
		{
			int index = item.indexOf(separator);
			if (index == -1)
			{
				map.put(item, "");
			} else {
				map.put(item.substring(0, index), item.substring(index + 1));
			}
		}
		return map;
	}
	
	/**
	 * join the string tuple with the separator char sequence.
	 * if the value of map is null ,just store the key.
	 * @param map
	 *           the key,value tuple.
	 * @param separator
	 *          the join string
	 * @return
	 *         the joined string list.
	 */
	public static List<String> join(Map<String, String> map, String separator)
	{
		if (map == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		if (map == null || map.size() == 0)
		{
			return result;
		}
		for (Map.Entry<String, String> entry : map.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null || value.length() == 0)
			{
				result.add(key);
			} else {
				result.add(key + separator + value);
			}
		}
		return result;
	}
	
	/**
	 * join the string list with the join char sequence.
	 * 
	 * @param list
	 *           the join sequence.
	 * @param separator
	 *           the join key word;
	 * @return
	 *          joined string.
	 */
	public static String join(List<String> list, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String ele : list)
		{
			if (sb.length() > 0)
			{
				sb.append(separator);
			}
			sb.append(ele);
		}
		return sb.toString();
	}
	
	/**
	 * assert the two map whether equals or not?
	 * first: if two maps all null , return true.
	 * second: if one of maps is null return false;
	 * three : if the two maps' s size are not ==, return false;
	 * four : every value with the key in map1,and with the key to 
	 * search the map2, if map2'value is null or not equals , 
	 * return false;
	 * 
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static boolean mapEquals(Map<?, ?> map1, Map<?, ?> map2)
	{
		if (map1 == null && map2 == null)
		{
			return true;
		}
		if (map1 == null || map2 == null)
		{
			return false;
		}
		if (map1.size() != map2.size())
		{
			return false;
		}
		for (Map.Entry<?, ?> entry : map1.entrySet())
		{
			Object key = entry.getKey();
			Object value1 = entry.getValue();
			Object value2 = map2.get(key);
			if (! objectEquals(value1, value2))
			{
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * assert two objects whether equals or not?
	 * @param obj1
	 *           the obj1 argument.
	 * @param obj2
	 *           the obj2 argument;
	 * @return
	 *         flag .
	 */
	private static boolean objectEquals(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null)
		{
			return true;
		}
		if (obj1 == null || obj2 == null)
		{
			return false;
		}
		return obj1.equals(obj2);
	}
	
	/**
	 * make the not fixed length string array to
	 * <string, String> tuple, so the length must
	 * even, we firstly must assert the length 
	 * of string length.
	 * 
	 * 
	 * @param pairs 
	 *           the (not fixed length)variables
	 * @return
	 *           the tuple map.
	 */
	public static Map<String, String> toStringMap(String...pairs)
	{
		Map<String, String> parameters = new HashMap<String, String>();
		if (pairs.length > 0)
		{
			if ((pairs.length & 0x1) != 0)
			{
				throw new IllegalArgumentException("pairs must be even!");
			}
			for (int i = 0; i < pairs.length; i = i + 2)
			{
				parameters.put(pairs[i], pairs[i + 1]);
			}
		}
		return parameters;
	}
	
	/**
	 * equals to the above 
	 * 
	 * @param pairs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V>  toMap(Object ...pairs)
	{
		
		Map<K,V> result  = new HashMap<K,V>();
		if (pairs == null || pairs.length == 0)
		{
			return result;
		}
		int len = pairs.length / 2;
		for (int i = 0 ; i < len ; i++)
		{
			result.put((K)pairs[2 * i], (V)pairs[2 * i + 1]);
		}
		return result;
	}
	
	public static boolean isEmpty(Collection<?> collection)
	{
		return collection.isEmpty() || collection.size() == 0;
	}
	
	public static boolean isNotEmpty(Collection<?> collection)
	{
		return collection != null && collection.size() > 0;
	}
	
	private CollectionUtil() {
		
	}
}
