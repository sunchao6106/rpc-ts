package com.sunchao.rpc.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.sunchao.rpc.common.annotation.Utility;

/**
 *  IOUtility. 
 * @author sunchao
 *
 */

@Utility("IOUtil")
public class IOUtil {

	private static final int BUFFER_SIZE = 1024 * 8;
	
	private IOUtil() {}
	
	public static long write(InputStream is, OutputStream os) throws IOException
	{
		return write(is, os, BUFFER_SIZE);
	}
	
	
	/**
	 * read from the input stream into the buffer, and write to the output stream.
	 * 
	 * @param is
	 *        input stream.
	 * @param os
	 *        output stream.
	 * @param bufferSize
	 *        the buffer size.
	 * @return
	 *        the total write byte number.
	 * @throws IOException
	 */
	public static long write(InputStream is, OutputStream os, int bufferSize) throws IOException 
	{
		int read;
		long total = 0;
		byte[] buffer = new byte[bufferSize]; // the buffer is byte array.
		while (is.available() > 0)
		{
			read = is.read(buffer, 0, buffer.length);//repeat read, if the actual number less that the length, the number of read will return the
			if (read > 0) // actual read number,
			{
				os.write(buffer, 0, read);
				total += read;
			}
		}
		return total;
	}
	
	public static String read(Reader reader) throws IOException {
		StringWriter sw = new StringWriter();
		try {
			write(reader, sw);
			return sw.getBuffer().toString();
		} finally {
			sw.close();
		}
	}
	
	public static long write(Writer writer, String string) throws IOException
	{
		Reader reader = new StringReader(string);
		try {
			return write(reader, writer);
		} finally {
			reader.close();
		} 
	}
	
	public static long write(Reader reader, Writer writer) throws IOException {
		return write(reader, writer, BUFFER_SIZE);
	}
	
	/**
	 * read the string and write it to the writer.
	 * 
	 * @param reader
	 *         @see java.io.Reader.
	 * @param writer
	 *         @see java.io.Writer.
	 * @param bufferSize
	 *         the buffer size.
	 * @return
	 *        the number write char number.
	 * @throws IOException
	 */
	public static long write(Reader reader, Writer writer, int bufferSize) throws IOException {
		int read;
		long total = 0;
		char[] buffer = new char[BUFFER_SIZE];
		while ((read = reader.read(buffer)) != -1) // the buffer is char array.
		{
			writer.write(buffer, 0, read);
			total += read;
		}
		return total;
	}
	
	
	public static String[] readLines(File file) throws IOException {
		if (file == null || ! file.exists() || !file.canRead()) //robust the condition 1: file  == null, ! file.exists(), !file.canRead(). 
			return new String[0];
		return readLines(new FileInputStream(file));
	}
	
	/**
	 * read line  use the <code>InputStreamReader</code>
	 * and the <code>BufferedReader</code>
	 * @param is
	 *        input stream.
	 * @return
	 *        the string line array.
	 * @throws IOException
	 */
	public static String[] readLines(InputStream is) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is)); // the stream and reader adapter.
		try {
			String line;
			while ((line = reader.readLine()) != null) 
				lines.add(line);
			return lines.toArray(new String[0]);
		} finally {
			reader.close();
		}
	}
	
	/**
	 * write lines use the <code>PrintWriter</code> and the
	 * <code>OutputStreamWriter</code>
	 * @param os
	 * @param lines
	 */
	public static void writeLines(OutputStream os, String[] lines) 
	{
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
		try {
			for (String line : lines)
				pw.println(line);
			pw.flush();
		} finally {
			pw.close();
		}
	}
	
	public static void writeLines(File file, String[] lines) throws IOException {
		if (file == null)
			throw new IOException("File is null.");
		writeLines(new FileOutputStream(file), lines);
	}
	
	/**
	 * @see java.io.FileOutputStream #FileOutputStream(String, boolean);
	 * @see java.io.FileOutputStream #FileOutputStream(File, boolean) append pattern.
	 * @param file
	 * @param lines
	 * @throws IOException
	 */
	public static void appendLines(File file, String[] lines) throws IOException {
		if (file == null)
			throw new IOException("File is null.");
		writeLines(new FileOutputStream(file, true), lines);
	}
}
