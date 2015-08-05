package com.sunchao.rpc.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Non-Thread.Safe
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ByteBufferOutputStream extends OutputStream {
    private byte[] buffer;
    private int position;
    private int mark = -1;
	
    /**
     * Because every serialization, the serilizer{@link com.sunchao.rpc.protocol.TSerilizer #serializer(Object)}
     * will create an new buffer, so we set {@link #DEFAULT_BUFFER_SIZE} 64, if not big enough, it will ensure double
     * size atomically. 
     */
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	
    public ByteBufferOutputStream() {
		this(DEFAULT_BUFFER_SIZE);
	}
    
    public ByteBufferOutputStream(int size) {
    	if (size < 0)
    		throw new IllegalArgumentException("The buffer size can't be negative :" + size);
    	this.buffer= new byte[size];
    	this.position = 0;
    }
    
    public int size() {
    	return position;
    }
 
	@Override
	public void write(int b) throws IOException {
		int newposition = position + 1;
		if (newposition > this.buffer.length) { 
			this.buffer = ByteHelper.copyOf(this.buffer, this.buffer.length << 1);
		}
		buffer[position] = (byte)b;
		position = newposition;
	}
	
	public void write(byte[] buf, int offset, int length) {
		if ((offset < 0) || (offset > this.buffer.length) || (length < 0) || 
    			((offset + length) > this.buffer.length) || (offset + length) < 0)
    		throw new IndexOutOfBoundsException();
    	if (length == 0)
    		return;
    	int newPosition = position + length;
    	if (newPosition > this.buffer.length) {
    		this.buffer = ByteHelper.copyOf(this.buffer, Math.max(this.buffer.length << 1, newPosition));
    		
    	}
    	System.arraycopy(buf, offset, buffer, position, length);
    	this.position = newPosition;
	}
	
	public void reset() {
		this.position = 0;
	}
	
	public void setWireTag(int index) {
		if (this.mark != -1) 
			throw new MarkRepeatException("The mark pointer has already used, and not reset, the mark: " + mark + ", reset and used." );
		if (index > position || index < 0)
			throw new IllegalArgumentException("Invalid mark pointer, pointer: " + index + ", the current position: " + position);
		this.mark = position;
		this.position = index;
	}
	
	public void resetWriteTag() {
		if (this.mark == -1)
			throw new IllegalStateException("No setWriteTag operation, so the operation invalid");
		if (position > mark)
			throw new IllegalStateException("the mark back write operation has exceed the original position, the data already invalid all.");
		this.position = mark;
		this.mark = -1;
	}
	
	public void close() {}
	
	public byte[] toByteArray() {
		return ByteHelper.copyOf(buffer, position);
	}
	
	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(buffer, 0, position);
	}
	
	public void writeTo(OutputStream out) throws IOException {
		out.write(buffer, 0, position );
	}
	
	
	public static class MarkRepeatException extends RuntimeException {
		
		private static final long serialVersionUID = 4089917998607877849L;

		public MarkRepeatException(String msg) {
			super(msg);
		}
		
		public MarkRepeatException(Throwable t) {
			super(t);
		}
		
		public MarkRepeatException(String msg, Throwable t) {
			super(msg, t);
		}
	}
	
}
