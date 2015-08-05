package com.sunchao.rpc.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.sunchao.rpc.common.utils.IOUtil;
/**
 * Byte Helper Class.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ByteHelper {

	/**
	 * Base64 contains the char 'A-Za-z0-9+/' and the pad char '='
	 * 
	 *  3 * 8 = 4 * 6 ;
	 *  
	 *  may can contains one or two '=';
	 */
	private static final String C64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="; //default base64.
	/**the hex char identifier*/
	private static final char[] BASE16 = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'}, BASE64 = C64.toCharArray();
	/**the mask of base64   */
	private static final int MASK4 = 0x0F, MASK6 = 0x3F, MASK8 = 0xFF;
	
	private static final Map<Integer, byte[]> DECODE_TABLE_MAP =  new ConcurrentHashMap<Integer, byte[]>();
	/**Thread Local  */
	private static ThreadLocal<MessageDigest> MD = new ThreadLocal<MessageDigest>();
	
	/**
	 * @see java.lang.System #arraycopy(Object, int, Object, int, int) 
	 * @param src
	 *         src array
	 * @param length
	 *         the copy length.
	 * @return
	 *        the copied array.
	 */
	public static byte[] copyOf(byte[] src, int length) 
	{
		byte[] dest = new byte[length];
		System.arraycopy(src, 0, dest, 0, Math.min(src.length, length));
		return dest;
	}
	
	/**
	 * 
	 * @param v
	 *       the short value.
	 * @return
	 *       the byte array.
	 */
	public static byte[] short2bytes(short v) 
	{
		byte[] ret = {0, 0};
		short2bytes(v, ret);
		return ret;
	}
	
	/**
	 * the array is reference pass.
	 * 
	 * @param v
	 * @param b
	 */
	public static void short2bytes(short v, byte[] b) 
	{
		short2bytes(v, b, 0);
	}
	
	/**
	 * short to byte (Big-Endian)(2).
	 * @param v
	 *        the short value.
	 * @param b
	 *        the byte array.
	 * @param off
	 *        the offset value.
	 */
	public static void short2bytes(short v, byte[] b, int off)
	{
		b[off + 1] = (byte) v;
		b[off + 0] = (byte) (v >>> 8);
	}
	
	
	public static byte[] int2bytes(int v) 
	{
		byte[] ret =  {0, 0, 0, 0};
		int2bytes(v, ret);
		return ret;
	}
	
	public static void int2bytes(int v, byte[] b)
	{
		int2bytes(v, b, 0);
	}
	
	/**
	 * the int invert to byte array(4).
	 * @param v
	 *        the int value.
	 * @param b
	 *        the byte array.
	 * @param off
	 *        the offset value.
	 */
	public static void int2bytes(int v, byte[] b, int off) 
	{
		b[off + 3] = (byte)v;
		b[off + 2] = (byte) (v >>> 8);
		b[off + 1] = (byte) (v >>> 16);
		b[off + 0] = (byte) (v >>> 24);
	}
	
	public static byte[] float2bytes(float v) 
	{
		byte[] ret = {0, 0, 0, 0};
		float2bytes(v, ret);
		return ret;
	}
	
	public static void float2bytes(float v, byte[] b)
	{
		float2bytes(v, b, 0);
	}
	
	/**
	 *  float invert to byte array(4).
	 * @param v
	 * @param b
	 * @param off
	 * 
	 * @see java.lang.Float #floatToIntBits(float)
	 * 0x80000000 - 0x7fffffff.
	 */
	public static void float2bytes(float v, byte[] b, int off) 
	{
	    int i = Float.floatToIntBits(v);
	    b[off + 3] = (byte) i;
	    b[off + 2] = (byte) (i >>> 8);
	    b[off + 1] = (byte) (i >>> 16);
	    b[off + 0] = (byte) (i >>> 24);
	}
	
	public static byte[] long2bytes(long v) 
	{
		byte[] ret = {0, 0, 0, 0, 0, 0, 0, 0};
		long2bytes(v, ret);
		return ret;
	}
	
	public static void long2bytes(long v, byte[] b) 
	{
		long2bytes(v, b, 0);
	}
	
	/**
	 * long invert to byte byte array (8)
	 * @param v
	 * @param b
	 * @param off
	 */
	public static void long2bytes(long v, byte[] b, int off)
	{
		b[off + 7] = (byte)v;
		b[off + 6] = (byte) (v >>> 8);
		b[off + 5] = (byte) (v >>> 16);
		b[off + 4] = (byte) (v >>> 24);
		b[off + 3] = (byte) (v >>> 32);
		b[off + 2] = (byte) (v >>> 40);
		b[off + 1] = (byte) (v >>> 48);
		b[off + 0] = (byte) (v >>> 56);
	}
	
	public static byte[] double2bytes(double v) 
	{
		byte[] ret = {0, 0, 0, 0, 0, 0, 0, 0};
		double2bytes(v, ret);
		return ret;
	}
	
	public static void double2bytes(double v, byte[] b)
	{
		double2bytes(v, b, 0);
	}
	
	/**
	 * double invert to byte array(8)
	 * @param v
	 * @param b
	 * @param off
	 * 
	 * @see java.lang.Double #doubleToLongBits(double);
	 */
	public static void double2bytes(double v, byte[] b, int off) 
	{
		long j = Double.doubleToLongBits(v);
		b[off + 7] = (byte) v;
		b[off + 6] = (byte) (j >>> 8);
		b[off + 5] = (byte) (j >>> 16);
		b[off + 4] = (byte) (j >>> 24);
		b[off + 3] = (byte) (j >>> 32);
		b[off + 2] = (byte) (j >>> 40);
		b[off + 1] = (byte) (j >>> 48);
		b[off + 0] = (byte) (j >>> 56);
		
	}
	
	public static short bytes2short(byte[] b) 
	{
		return bytes2short(b, 0);
	}
	
	
	public static short bytes2short(byte[] b, int off) 
	{
		return (short) (((b[off + 1] & 0xFF) << 0) + 
			((b[off + 0]) << 8));
	}
	
	public static int bytes2int(byte[] b) 
	{
		return bytes2int(b, 0);
	}
	
	public static int bytes2int(byte[] b, int off) 
	{
		return (int)(((b[off + 3] & 0xFF) << 0) +
				((b[off + 2] & 0xFF) << 8) +
				((b[off + 1] & 0xFF) << 16) +
				((b[off + 0] & 0xFF) << 24));
	}
	
	public static float bytes2float(byte[] b) 
	{
		return bytes2float(b, 0);
	}
	
	/**
	 * @see java.lang.Float #intBitsToFloat(int);
	 * @param b
	 * @param off
	 * @return
	 */
	public static float bytes2float(byte[] b, int off) 
	{
		int i = (int)(((b[off + 3] & 0xFF) << 0) +
				  ((b[off + 2] & 0xFF) << 8) +
				  ((b[off + 1] & 0xFF) << 16) +
				  ((b[off + 0] & 0xFF) << 24)
				);
		return Float.intBitsToFloat(i);
	}
	
	public static long bytes2long(byte[] b)
	{
		return bytes2long(b, 0);
	}
	
	public static long bytes2long(byte[] b, int off)
	{
		return (long)(((b[off + 7] & 0xFFL) << 0) +
				 ((b[off + 6] & 0xFFL) << 8) +
				 ((b[off + 5] & 0xFFL) << 16) +
				 ((b[off + 4] & 0xFFL) << 24) +
				 ((b[off + 3] & 0xFFL) << 32) +
				 ((b[off + 2] & 0xFFL) << 40) +
				 ((b[off + 1] & 0xFFL) << 48) +
				 ((b[off + 0] & 0xFFL) << 56)
				);
	}
	
	public static double bytes2double(byte[] b, int off)
	{
		long j = (long)( ((b[off + 7] & 0xFFL) << 0) +
				 ((b[off + 6] & 0xFFL) << 8) +
				 ((b[off + 5] & 0xFFL) << 16) +
				 ((b[off + 4] & 0xFFL) << 24) +
				 ((b[off + 3] & 0xFFL) << 32) +
				 ((b[off + 2] & 0xFFL) << 40) +
				 ((b[off + 1] & 0xFFL) << 48) +
				 ((b[off + 0] & 0xFFL) << 56)
				);
		return Double.longBitsToDouble(j);
	}
	
	/**
	 * byte invert to hex.
	 * @param bs
	 *       byte array.
	 * @param off
	 *       the offset value.
	 * @param len
	 *       the length.
	 * @return
	 *       hex string.
	 */
	public static String bytes2hex(final byte[] bs, final int off, final int len)
	{
		if (off < 0)
			throw new IndexOutOfBoundsException("bytes2hex: offset < 0, offset is " + off);
		if (len < 0)
			throw new IndexOutOfBoundsException("bytes2hex: length < 0, length is " + len);
		if (off + len > bs.length)
			throw new IndexOutOfBoundsException("bytes2hex: offset + length > array length.");
		
		byte b;
		int r = off, w = 0;
		char[] cs = new char[len * 2];
		for (int i = 0; i < len; i++)
		{
			b = bs[r++];
			cs[w++] = BASE16[ (b >> 4) & MASK4 ];
			cs[w++] = BASE16[ b & MASK4 ];
		}
		return new String(cs);
	}
	
	public static String bytes2hex(final byte[] bs) {
		return bytes2hex(bs, 0, bs.length);
	}
	
	public static byte[] hex2bytes(String str)
	{
		return hex2bytes(str, 0, str.length());
	}
	
	/**
	 * hex string to byte array.
	 * 
	 * @param str
	 *        the hex string.
	 * @param off
	 *        the offset value.
	 * @param len
	 *        the length value.
	 * @return
	 *        the byte array.
	 */
	public static byte[] hex2bytes(final String str, final int off, int len)
	{
		if ((len & 0x1) == 1)
			throw new IllegalArgumentException("hex2bytes: (len & 1) == 1 ") ;
		if (off < 0)
			throw new IndexOutOfBoundsException("hex2bytes: offset < 0, offset is " + off);
		if (len < 0)
			throw new IndexOutOfBoundsException("hex2bytes: length < 0, length is " + len);
		if (off + len > str.length())
			throw new IndexOutOfBoundsException("hex2bytes: offset + length > array length.");
		
		int num = len / 2, r = off, w = 0;
		byte[] b = new byte[num];
		for (int i = 0; i < num; i++)
		{
			b[w++] = (byte)(hex(str.charAt(r++)) << 4 | hex(str.charAt(r++)));
		}
		return b;
	}
	
	public static String bytes2base64(byte[] b)
	{
		return bytes2base64(b, 0, b.length, BASE64);
	}
	
	public static String bytes2base64(byte[] b, String code)
	{
		return bytes2base64(b, 0, b.length, code);
	}
	
	public static String bytes2base64(byte[] b, int offset, int length, String code)
	{
		if (code.length() < 64)
			throw new IllegalArgumentException("Base64 code length < 64");
		return bytes2base64(b, offset, length, code.toCharArray());
	}
	
	public static String byte64base64(byte[] b, char[] code)
	{
		return bytes2base64(b, 0, b.length, code);
	}
	
	/**
	 * Base64 algorithm.
	 * byte array to the base64 char.
	 * 
	 * @param bs
	 *       byte array.
	 * @param off
	 *       the offset value.
	 * @param len
	 *       the length value.
	 * @param code
	 *       the base64 invert table.
	 * @return
	 *       the base64 string.
	 */
	public static String bytes2base64(final byte[] bs, final int off, final int len, final char[] code)
	{
		if (off < 0)
			throw new IndexOutOfBoundsException("bytes2base64: offset < 0, offset is " + off);
		if (len < 0)
			throw new IndexOutOfBoundsException("bytes2base64: length < 0, length is " + len);
		if (off + len > bs.length)
			throw new IndexOutOfBoundsException("bytes2base64: offset + length > array length.");
		
		if (code.length < 64)
			throw new IllegalArgumentException("Base64 code length < 64");
		
		boolean pad = code.length > 64; // has pad char. if has pad char '=', the string code length = '65',
		int num = len / 3, rem = len % 3, r = off, w = 0;
		char[] cs = new char[ num * 4 + (rem == 0 ? 0 : pad ? 4 : rem + 1)];
		
		for (int i = 0; i < num; i++)
		{
			int b1 = bs[r++] & MASK8, b2 = bs[r++] & MASK8, b3 = bs[r++] & MASK8;
	//******************** base64 use the byte value for the index of base table.***********************************************************//
			cs[w++] = code[b1 >> 2];// 000000|00  0000|0000  00|000000
			cs[w++] = code[(b1 << 4) & MASK6 | (b2 >> 4)];
			cs[w++] = code[(b2 << 2) & MASK6 | (b3 >> 6)];
			cs[w++] = code[ b3 & MASK6];
		}
		
		if ( rem == 1)
		{
		   int b1 = bs[r++] & MASK8;
		   cs[w++] = code[ b1 >> 2 ];
		   cs[w++] = code[ (b1 << 4) & MASK6];
		   if (pad) 
		   {
			   cs[w++] = code[64];
			   cs[w++] = code[64];
		   }
		} else if ( rem == 2 ) 
		{
			int b1 = bs[r++] & MASK8, b2 = bs[r++] & MASK8;
			cs[w++] = code[ b1 >> 2];
			cs[w++] = code[(b1 << 4) & MASK6 | (b2 >> 4)];
			cs[w++] = code[(b2 << 2) & MASK6];
			if (pad) {
				cs[w++] = code[64];
			} 
		}
		return new String(cs);
	}
	
	public static byte[] base642bytes(String str)
	{
		return base642bytes(str, 0, str.length());
	}
	
	public static byte[] base642bytes(String str, int offset, int length)
	{
		return base642bytes(str, offset, length, C64);
	}
	
	public static byte[] base642bytes(String str, String code)
	{
		return base642bytes(str, 0, str.length(), code);
	}
	
	/**
	 * the reverse base64 algorithm.
	 * 
	 * @param str
	 *        the base64 string.
	 * @param off
	 *        the offset value.
	 * @param len
	 *        the length value.
	 * @param code
	 *        the byte value.
	 * @return
	 */
	public static byte[] base642bytes(final String str, final int off, final int len, final String code)
	{
		if (off < 0)
			throw new IndexOutOfBoundsException("base642bytes: offset < 0, offset is " + off );
		if (len < 0)
			throw new IndexOutOfBoundsException("base642bytes: length < 0, length is " + len);
		if (off + len > str.length()) 
			throw new IndexOutOfBoundsException("base642bytes: offset + length > string length.");
		if (code.length() < 64)
			throw new IllegalArgumentException("Base64 code length < 64.");
		
		int rem = len % 4;// the % case happen when the condition not add pad char.
		                  // the % 3 may happen 0 , 1, 2 ; 1 + 1 = 2, 2 + 1 = 3, so the % 4 == 1 case don't can happen.
		if (rem == 1) 
			throw new IllegalArgumentException("base64bytess: base64 string length % 4 == 1.");
		
		int num = len / 4, size = num * 3;
		if (code.length() > 64) // has pad char.
		{
			if (rem != 0) // has pad char, the length must be 4 times.
				throw new IllegalArgumentException("base642bytes: base64 string length error.");
			
			char pc = code.charAt(64);
			if (str.charAt(off + len - 2) == pc)
			{
				size -= 2; // pad two '='; the byte array size -= 2;
				-- num;    //  loop times - 1; 
				rem = 2;   //  reminder == 2
			} else if (str.charAt(off + len - 1) == pc) 
			{
				size --;   // pad one '='; the byte array size--;
				-- num;    //  loop times - 1;
				rem = 3;   //  reminder == 1
			}
		} else { // has no pad char.
			if (rem == 2)
				size ++;  // the byte array size ++;
			else if (rem == 3)
				size += 2; // the byte array size += 2;
		}
		
		int r = off, w = 0;
		byte[] b = new byte[size], t = decodeTable(code);
		for (int i = 0; i < num; i++)
		{   // 00|000000   00|000000   00|000000   00|000000
			int c1 = t[str.charAt(r++)], c2 = t[str.charAt(r++)];
			int c3 = t[str.charAt(r++)], c4 = t[str.charAt(r++)];
			
			b[w++] = (byte) ((c1 << 2) | (c2 >> 4));
			b[w++] = (byte) ((c2 << 4) | (c3 >> 2));
			b[w++] = (byte) ((c3 << 6) | c4);
		}
		
		if (rem == 2)
		{   // 00|000000  00|000000
			int c1 = t[str.charAt(r++)], c2 = t[str.charAt(r++)];
			b[w++] = (byte) ((c1 << 2) | ( c2 >> 4));
		} else if ( rem == 3) 
		{  // 00|000000    00|000000  00|000000
			int c1 = t[str.charAt(r++)], c2 = t[str.charAt(r++)], c3 = t[str.charAt(r++)];
			
			b[w++] = (byte) ((c1 << 2) | (c2 >> 4));
			b[w++] = (byte) ((c2 << 4) | (c3 >> 2)); 
		}
		return b;
	}
	
	/*public static byte[] base642bytes(String str, char[] code)
	{
		return base642bytes(str, 0, str.length(), code);
	}*/
	
	/**
	 * 
	 * @param str
	 * @param off
	 * @param len
	 * @param code
	 * @return
	 *//*
	public static byte[] base642bytes(final String str, final int off, final int len, final char[] code)
	{
		if (off < 0)
			throw new IndexOutOfBoundsException("base642bytes: offset < 0, offset is " + off);
		if (len < 0)
			throw new IndexOutOfBoundsException("base642bytes: length < 0, length is " + len);
		if (off + len > str.length()) 
			throw new IndexOutOfBoundsException("base642bytes: offset + length > string length.");
		if (code.length < 64)
			throw new IllegalArgumentException("Base64 code length < 64");
		
		int rem = len % 4;
		
		if (rem == 1)
			throw new IllegalArgumentException("base642bytes: base64 string length % 4 == 1");
		
		int num = len / 4, size = num * 3;
		if (code.length > 64) // has pad char '=';
		{
			if (rem != 0)
				throw new IllegalArgumentException("base642bytes: base64 string length error.");
			
			char pc = code[64];
			if (str.charAt(off + len -2) == pc)
				size -= 2;
			else if (str.charAt(off + len -1) == pc)
				size --;
		} else  // has no pad char '=';
		{
			if (rem == 2)
				size ++;
			else if (rem == 3)
				size += 2;
		}
		
		int r = off, w = 0;
		byte[] b = new byte[size];
		for (int i = 0; i < num; i++)
		{    // 00|000000    00|000000   00|000000    00|000000
			int c1 = indexOf(code, str.charAt(r++)), c2 = indexOf(code, str.charAt(r++));
			int c3 = indexOf(code, str.charAt(r++)), c4 = indexOf(code, str.charAt(r++));
			
			b[w++] = (byte) ((c1 << 2) | (c2 >> 4));
			b[w++] = (byte) ((c2 << 4) | (c3 >> 2));
			b[w++] = (byte) ((c3 << 6) | c4);
		}
		
		if (rem == 2)
		{
			int c1 = indexOf(code, str.charAt(r++)), c2 = indexOf(code, str.charAt(r++));
			b[w++] = (byte) ((c1 << 2) | (c2 >> 4));
		} else if (rem == 3) {
			int c1 =  indexOf(code, str.charAt(r++)), c2 = indexOf(code, str.charAt(r++)), c3 = indexOf(code, str.charAt(r++));
			
			b[w++] = (byte) ((c1 << 2) | (c2 >> 4));
			b[w++] = (byte) ((c2 << 4) | (c3 >> 2));
		}
		return b;
 	}*/
	
	/**
	 * @see java.util.zip.DeflaterOutputStream 
	 * @see java.util.zip.DeflaterInputStream
	 * 
	 * @param bytes
	 *      the compress bytes.
	 * @return
	 *      the compressed bytes.
	 * @throws IOException
	 */
	public static byte[] zip(byte[] bytes) throws IOException {
		ByteBufferOutputStream bos = new ByteBufferOutputStream();
		OutputStream os = new DeflaterOutputStream(bos);
		try {
			os.write(bytes);
		} finally {
			os.close();
			bos.close();
		}
		return bos.toByteArray();
	}
	
	/**
	 * above.
	 * uncompress the byte arrays.
	 * @param bytes
	 *        the compressed byte array.
	 * @return
	 *       the uncompress byte array.
	 * @throws IOException
	 */
	public static byte[] unzip(byte[] bytes) throws IOException {
		ByteBufferInputStream bis = new ByteBufferInputStream(bytes);
		ByteBufferOutputStream bos = new ByteBufferOutputStream();
		InputStream is = new InflaterInputStream(bis);
		try {
			IOUtil.write(is, bos);
			return bos.toByteArray();
		} finally {
			is.close();
			bis.close();
			bos.close();
		}
	}
	
	public static byte[] getMD5(String str) {
		return getMD5(str.getBytes());
	}
	
	public static byte[] getMD5(byte[] source)
	{
		MessageDigest md = getMessageDigest();
		return md.digest(source);
	}
	
	public static byte[] getMD5(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		try {
			 return getMD5(is);
		} finally {
			is.close();
		}
	}
	
	public static byte[] getMD5(InputStream is) throws IOException 
	{
		return getMD5(is, 1024 * 8);
	}
	
	/**
	 * hex char to the byte value.
	 * @param c
	 *       the hex char.
	 * @return
	 *       the byte value.
	 */
	private static byte hex(char c) 
	{
		if (c <= '9')  return (byte)(c - '0');
		if (c >= 'a' && c <= 'f') return (byte)(c -'a' + 10);
		if (c >= 'A' && c <= 'F') return (byte) (c -'A' + 10);
		throw new IllegalArgumentException("hex string format error [" + c + "].");
	}
	
	/**
	 * also the helper method for the reverse base64.
	 * @param cs
	 *       the base table char array.
	 * @param c
	 *       the char.
	 * @return
	 *       the index.
	 */
	/*private static int indexOf(char[] cs, char c)
	{
		for (int i = 0, len = cs.length; i < len; i++)
			if (cs[i] == c)
				return i;
		return -1;
	}*/
	
	/**
	 * the helper method of the reverse base64. 
	 * @param code
	 *        the base table.
	 * @return
	 *        the byte array.
	 */
	private static byte[] decodeTable(String code)
	{
		int hash = code.hashCode();
		byte[] ret = DECODE_TABLE_MAP.get(hash);
		if (ret == null) 
		{
			if (code.length() < 64)
				throw new IllegalArgumentException("Base64 code length < 64!");
			//create new decode table.
			ret = new byte[128]; // the ascii code length.
			for (int i = 0; i < 128; i++) // initialize table
				ret[i] = -1;
			for (int i = 0; i < 64; i++)
				ret[code.charAt(i)] = (byte)i;
			DECODE_TABLE_MAP.put(hash, ret);
		}
		return ret;
	}
	
	/**
	 * MD5 encryption.
	 * 
	 * @param is
	 *        the input stream
	 * @param bs
	 *        the buffer size
	 * @return
	 *       the encrypted byte array.
	 * @throws IOException
	 */
	private static byte[] getMD5(InputStream is, int bs) throws IOException 
	{
		MessageDigest md = getMessageDigest();
		byte[] buf = new byte[bs];
		while (is.available() > 0)
		{
			int read, total = 0;
			do {
				if ((read = is.read(buf, total, bs - total)) <= 0)
					break;
				total += read;
			} while (total < bs);
			md.update(buf);
		}
		return md.digest();
	}
	
	/**
	 * The thread local , thread-safe.
	 * @return
	 *       the Message Digest instance.
	 */
	private static MessageDigest getMessageDigest() 
	{
	    MessageDigest ret = MD.get();
	    if (ret == null) 
	    {
	    	try {
	    		ret = MessageDigest.getInstance("MD5");
	    		MD.set(ret);
	    	} catch (NoSuchAlgorithmException e) 
	    	{
	    		throw new RuntimeException(e);
	    	}
	    }
		return ret;
	}
	
	/**
	 * little-endian.
	 * @param n
	 *       the int value.
	 * @return
	 *       byte array.
	 */
	public static byte[] toLH(int n)
	{
		byte[] b = {0, 0, 0, 0};
		b[0] = (byte) (n & 0xFF);
		b[1] = (byte) ((n >> 8) & 0xFF);
		b[2] = (byte) ((n >> 16) & 0xFF);
		b[3] = (byte) ((n >> 24) & 0xFF);
		return b;
	}
	
	/**
	 * big-endian.
	 * the default byte order of java. 
	 * @param n
	 * @return
	 */
	public static byte[] toHH(int n)
	{
		byte[] b = new byte[4];
		b[0] = (byte) ((n >> 24) & 0xFF);
		b[1] = (byte) ((n >> 16) & 0xFF);
		b[2] = (byte) ((n >> 8) & 0xFF);
		b[3] = (byte) (n & 0xFF);
		return b;
	}
	
	public static byte[] toHH(short n)
	{
		byte[] b = new byte[2];
		b[0] = (byte) ((n >> 8) & 0xFF);
		b[1] = (byte) (n & 0xFF);
		return b;
	}
	
	public static byte[] toLH(short n)
	{
		byte[] b = new byte[2];
		b[0] = (byte) (n & 0xFF);
		b[1] = (byte) ((n >> 8) & 0xFF);
		return b;
	}
	
	public static byte[] toLH(float f) 
	{
		byte[] b = new byte[4];
		int i = Float.floatToIntBits(f);
		b[0] = (byte) (i & 0xFF);
		b[1] = (byte) ((i >> 8) & 0xFF);
		b[2] = (byte) ((i >> 16) & 0xFF);
		b[3] = (byte) ((i >> 24) & 0xFF);
		return b;
	}
	
	public static byte[] toHH(float f) 
	{
		byte[] b = new byte[4];
		int i = Float.floatToIntBits(f);
		b[0] = (byte) ((i >> 24) & 0xFF);
		b[1] = (byte) ((i >> 16) & 0xFF);
		b[2] = (byte) ((i >> 8) & 0xFF);
		b[3] = (byte) (i & 0xFF);
		return b;
	}
	
	public static byte[] toLH(long j) 
	{
		byte[] b = new byte[8];
		b[0] = (byte) (j & 0xFF);
		b[1] = (byte) ((j >> 8) & 0xFF);
		b[2] = (byte) ((j >> 16) & 0xFF);
		b[3] = (byte) ((j >> 24) & 0xFF);
		b[4] = (byte) ((j >> 32) & 0xFF);
		b[5] = (byte) ((j >> 40) & 0xFF);
		b[6] = (byte) ((j >> 48) & 0xFF);
		b[7] = (byte) ((j >> 56) & 0xFF);
		return b;
	}
	
	public static byte[] toHH(long j)
	{
		byte[] b = new byte[8];
		b[0] = (byte) ((j >> 56) & 0xFF);
		b[1] = (byte) ((j >> 48) & 0xFF);
		b[2] = (byte) ((j >> 40) & 0xFF);
		b[3] = (byte) ((j >> 32) & 0xFF);
		b[4] = (byte) ((j >> 24) & 0xFF);
		b[5] = (byte) ((j >> 16) & 0xFF);
		b[6] = (byte) ((j >> 8) & 0xFF);
		b[7] = (byte) (j & 0xFF);
		return b;
	}
	
	public static byte[] toLH(double d) 
	{
		byte[] b = new byte[8];
		long j = Double.doubleToLongBits(d);
		b[0] = (byte) (j & 0xFF);
		b[1] = (byte) ((j >> 8) & 0xFF);
		b[2] = (byte) ((j >> 16) & 0xFF);
		b[3] = (byte) ((j >> 24) & 0xFF);
		b[4] = (byte) ((j >> 32) & 0xFF);
		b[5] = (byte) ((j >> 40) & 0xFF);
		b[6] = (byte) ((j >> 48) & 0xFF);
		b[7] = (byte) ((j >> 56) & 0xFF);
		return b;
	}
	
	public static byte[] toHH(double d) 
	{
		byte[] b = new byte[8];
		long j = Double.doubleToLongBits(d);
		b[0] = (byte) ((j >> 56) & 0xFF);
		b[1] = (byte) ((j >> 48) & 0xFF);
		b[2] = (byte) ((j >> 40) & 0xFF);
		b[3] = (byte) ((j >> 32) & 0xFF);
		b[4] = (byte) ((j >> 24) & 0xFF);
		b[5] = (byte) ((j >> 16) & 0xFF);
		b[6] = (byte) ((j >> 8) & 0xFF);
		b[7] = (byte) (j & 0xFF);
		return b;
	}
	
	private ByteHelper() {
		
	}
}
