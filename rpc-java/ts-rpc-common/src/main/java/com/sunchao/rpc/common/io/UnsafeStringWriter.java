package com.sunchao.rpc.common.io;

import java.io.IOException;
import java.io.Writer;

/**
 * (Non-Thread-Safe).
 * 
 * @author sunchao
 *
 *@see <code>java.io.Writer.</code><code>lock</code>
 * to synchronize the write and not use the <code>this</code>
 * or <i>synchronized method</i> to synchronized.
 *
 *
 */
public class UnsafeStringWriter extends Writer {

	private StringBuilder sb;
	
	public UnsafeStringWriter() {
		  lock = sb = new StringBuilder();
	}
	
	public UnsafeStringWriter(int size)
	{
		if (size < 0)
			throw new IllegalArgumentException("Negative buffer size!");
		
		lock = sb = new StringBuilder();
	}
	
	
	@Override
	public void write(int c) 
	{
		sb.append((char)c);
	}
	
	
	@Override
	public void write(char[] cs) throws IOException {
		sb.append(cs, 0 ,cs.length);
	}
	
	
	
	@Override
	public void close() throws IOException {
		
		
	}

	@Override
	public void flush() throws IOException {
		
		
	}

	@Override
	public void write(char[] cs, int off, int len) throws IOException {
	
		if ((off < 0) || (off > cs.length) || (len < 0) ||
				((off + len) > cs.length) || ((off + len) < 0))
			throw new IndexOutOfBoundsException();
		if (len > 0)
			sb.append(cs, off, len);
	}

	@Override
	public void write(String str)
	{
		sb.append(str);
	}
	
	@Override
	public void write(String str, int off, int len)
	{
		sb.append(str.substring(off, off + len));
	}
	
	@Override
	public Writer append(CharSequence csq, int start, int end)
	{
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}
	
	@Override
	public Writer append(CharSequence csq)
	{
		if (csq == null)
			write("null");
		else 
			write(csq.toString());
		return this;
	}
	
	@Override
	public Writer append(char c)
	{
		sb.append(c);
		return this;
	}
	
	@Override
	public String toString()
	{
		return sb.toString();
	}
}
