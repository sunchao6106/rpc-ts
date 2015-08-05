package com.sunchao.rpc.common.io;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Non-ThreadSafe
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ByteBufferInputStream extends InputStream {
	private List<ByteBuffer>  buffers;
	private int current;
	
   
    public ByteBufferInputStream(ByteBuffer buffer) {
    	List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
    	buffers.add(buffer);
    	this.buffers = buffers;
    }
    
    public ByteBufferInputStream(List<ByteBuffer> buffers) {
		this.buffers = buffers;
	}
    
    public ByteBufferInputStream(byte[] buffer) {
    	this(buffer, 0, buffer.length);
    }
    
    public ByteBufferInputStream(byte[] buffer, int offset) {
    	this(buffer, offset, buffer.length - offset);
    }
    
    public ByteBufferInputStream(byte[] buffer, int offset, int length) {
    	this(ByteBuffer.wrap(buffer, offset, length));
    }
    
    
    public int position() throws IOException {
    	return getBuffer().position();
    }
    
    public void position(int newPosition) throws IOException {
    	getBuffer().position(newPosition);
    }
	
	@Override
	public int available() throws IOException {
		return getBuffer().remaining();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public synchronized void mark(int arg0) {
		try {
			getBuffer().mark();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public int read(byte[] b, int offset, int len) throws IOException {
		if (b == null) 
			throw new NullPointerException();
		if (offset < 0 || len < 0 || len > b.length - offset)
			throw new IndexOutOfBoundsException();
		if (len == 0)
			return 0;
		ByteBuffer buffer = getBuffer();
		int remaining = buffer.remaining();
		if (len > remaining) {
			buffer.get(b, offset, remaining);
			return remaining;
		} else {
			buffer.get(b, offset, len);
			return len;
		}
	}
	
	public ByteBuffer readBuffer(int len) throws IOException {
		if (len <= 0) return ByteBuffer.allocate(0);
		ByteBuffer buffer = getBuffer();
		if (buffer.remaining() == len) {
			current++;
			return buffer;
		}
		ByteBuffer result = ByteBuffer.allocate(len);
		int begin = 0;
		while (begin < len) 
			begin += read(result.array(), begin, len - begin);
		return result;
	}

	@Override
	public synchronized void reset() throws IOException {
		getBuffer().reset();
	}

	@Override
	public long skip(long arg0) throws IOException {
		return 0;
	}

	@Override
	public int read() throws IOException {
		return getBuffer().get() & 0xFF;
	}
	
	private ByteBuffer getBuffer() throws IOException {
		while (current < buffers.size()) {
			ByteBuffer buffer = buffers.get(current);
			if (buffer.hasRemaining()) {
				return buffer;
			}
			current++;
		}
			throw new EOFException();
	}
}
