package com.sunchao.rpc.base.serializer.support.varint;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Utility methods are provided for efficiently to read primitive type
 * and string.
 * 
 * Unsafe
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class Input extends InputStream {

	private static final Logger LOGGER = LoggerFactory.getLogger(Input.class);
	protected byte[] buf;
	protected int position = 0;
	protected int limit = 0;
	protected int capacity;
	protected int totalRetired = 0;
	protected InputStream input;
	protected char[] chars = new char[64];
	protected final ClassResolve resolve;
	
    public ClassResolve getResolve() {
		return resolve;
	}

	public Input(InputStream input, ClassResolve resolve) {
    	this(input, 4096, resolve);
    }
    
    public Input(byte[] buffer, ClassResolve resolve) {
    	this(buffer, 0 , buffer.length, resolve);
    }
    
    public Input(byte[] buffer, int offset, int length, ClassResolve resolve) {
    	if (buffer == null) throw new IllegalArgumentException("bytes data cannot be null");
    	if (resolve == null) throw new IllegalArgumentException("class resolve cannot be null.");
    	this.buf = buffer;
    	this.position = offset;
    	this.limit = offset + length;
    	this.input = null;
    	this.capacity = length;
    	this.totalRetired = - offset;
    	this.resolve = resolve;
    }
    
    public Input(InputStream input, int bufferSize, ClassResolve resolve) {
    	if (input == null) throw new IllegalArgumentException("input stream cannot be null!");
    	if (bufferSize <= 0) throw new IllegalArgumentException("Invalid buffer size: " + bufferSize + " ,  buffer size must be positive!");
    	if (resolve == null) throw new IllegalArgumentException("class resolve cannot be null.");
    	this.input = input;
    	this.limit = 0;
    	this.buf = new byte[bufferSize];
    	this.capacity = bufferSize;
    	this.resolve = resolve;
    }
	
    /**
     * read one byte from the input.
     */
	@Override
	public int read() throws IOException {
		if (position == limit) 
			ensureAvailable(1);
		return this.buf[position++] & 0xFF;
	}
	
	/**
	 * read byte array from the input.
	 */
	public int read(byte[] bytes) throws IOException {
		return read(bytes, 0 , bytes.length);
	}
	
	/**
	 * read byte array with the specified offset and len.
	 */
	public int read(byte[] bytes, int offset, int length) throws IOException {
		if (bytes == null) throw new IllegalArgumentException("bytes cannot be null.");
		final int pos = position;
		if (length <= (limit - pos) && length > 0) {
			System.arraycopy(this.buf, position, bytes, offset, length);
			position = pos + length;
			return length;
		} else {
			return readBytesSlowPath(bytes, offset, length);
		}
	}
	
	/**
	 * Read the bytes when the remaining buffer bytes lower than the length specified.
	 * so need to do some efficiently thing to for large bytes read.
	 * 
	 * @param bytes
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	protected int readBytesSlowPath(byte[] bytes, int offset, int length) throws IOException {
		if (length <= 0) return 0;
		// read more bytes in the buffer.
		if (length < capacity) {
			//copy the remaining buffer bytes.
			int pos = limit - position;
			System.arraycopy(this.buf, position, bytes, offset, pos);
			position = limit;
			
			//fill the buffer and then copy from the buffer into the byte array rather read
			//directly into the byte array because the input may be unbuffered.
			ensureAvailable(length - pos);
			System.arraycopy(this.buf, 0, bytes, offset + pos, length - pos);
			position = length - pos;
	
		} else {
			//the size is very large, so directly read from the underlying input stream
			System.arraycopy(this.buf, position, bytes, offset, limit - position);
			offset += (limit - position);
			int sizeLeft = length - (limit - position);
			//mark the read consumed.
			totalRetired += limit;
			position = 0;
			limit = 0;
			
			int idx = offset;
			while (sizeLeft > 0) {
				int size = Math.min(sizeLeft, capacity);
				int pos = 0;
				while (pos < size) {
					final int n = (this.input == null) ? -1 :
						input.read(bytes, idx, size - pos);
					if (n == -1) {
						throw new RuntimeException(
								"While parsing a message, the input ended unexpectedly " +
								"in the middle of a field. This could mean either that the " +
								"input has been truncated or the message misreported its own length");
					}
					totalRetired += n;
					pos += n;
					idx += n;
				}
				sizeLeft -= size;
			}	
		}
		return length;
	}
	
	//byte
	/**
	 * Read only a single byte
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte readByte() throws IOException {
		if (position == limit) 
			ensureAvailable(1);
		return this.buf[position++];
	}
	
	/**
	 * Read a byte as an int from 0 to 255.
	 * @return
	 * @throws IOException
	 */
	public int readByteUnsigned() throws IOException {
		if (position == limit)
			ensureAvailable(1);
		return this.buf[position++] & 0xFF;
	}
	
	public byte[] readBytes(int length) throws IOException {
		byte[] bytes = new byte[length];
		read(bytes);
		return bytes;
	}
	
	public void readBytes(byte[] bytes) throws IOException {
		read(bytes);
	}
	
	public void readBytes(byte[] bytes, int offset, int length) throws IOException {
		if (bytes == null) throw new IllegalArgumentException("bytes cannot be null.");
		if (offset < 0 || offset >= bytes.length || length > bytes.length || length <= 0) { 
			throw new IllegalArgumentException("Invalid byte array argument. the offset: " + 
		        offset + ", length: " + length  + ", the byte array' length: " + bytes.length);
		}
		read(bytes, offset, length);
	}
	
	//int
	/**
	 * Read a 32 bit LITTER-ENDIAN int from the stream.
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readInt() throws IOException {
		int pos = position;
		if (limit - pos < 4) {
			ensureAvailable(4);
			pos = position;
		}
		final byte[] buffer = this.buf;
		position = pos + 4;
		return (((buffer[pos]     & 0xFF))       |
				((buffer[pos + 1] & 0xFF) << 8)  |
				((buffer[pos + 2] & 0xFF) << 16) |
				((buffer[pos + 3] & 0xFF) << 24));
	}
	
	/**
	 * Read a 1-5 byte int. 
	 * @return
	 * @throws IOException 
	 */
	public int readVarInt() throws IOException {
		if (limit - position < 5) return readVarInt_slow();
		int b = this.buf[position++]; //1
		int result = b & 0x7F;
		if ((b & 0x80) != 0) {
			byte[] buffer = this.buf;
			b = buffer[position++];  //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = buffer[position++]; //3
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					b = buffer[position++]; // 4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = buffer[position++]; // 5
						result |= (b & 0x7F) << 28;
					}
				}
			}
		}
		return ((result >>> 1) ^ - (result & 1));
	}
	
	public int readVarIntNonZigZag() throws IOException {
		if (limit - position < 5) return readVarIntNonZigZag_slow();
		int b = this.buf[position++]; //1
		int result = b & 0x7F;
		if ((b & 0x80) != 0) {
			byte[] buffer = this.buf;
			b = buffer[position++];  //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = buffer[position++]; //3
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					b = buffer[position++]; // 4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = buffer[position++]; // 5
						result |= (b & 0x7F) << 28;
					}
				}
			}
		}
		return result;
	}
	
	protected int readVarIntNonZigZag_slow() throws IOException {
		if (limit == position) 
			ensureAvailable(1);
		int b = this.buf[position++]; //1
		int result = b & 0x7F;
		if ((b & 0x80) != 0) {
			ensureAvailable(1);
			byte[] buffer = this.buf;
			b = buffer[position++];  //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				ensureAvailable(1);
				b = buffer[position++]; //3
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					ensureAvailable(1);
					b = buffer[position++];//4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						ensureAvailable(1);
						b = buffer[position++];//5
						result |= (b & 0x7F) << 28;
					}
				}
			}
		}
		return result;
	}
	
	protected int readVarInt_slow() throws IOException {
		if (limit == position) 
			ensureAvailable(1);
		int b = this.buf[position++]; //1
		int result = b & 0x7F;
		if ((b & 0x80) != 0) {
			ensureAvailable(1);
			byte[] buffer = this.buf;
			b = buffer[position++];  //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				ensureAvailable(1);
				b = buffer[position++]; //3
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					ensureAvailable(1);
					b = buffer[position++];//4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						ensureAvailable(1);
						b = buffer[position++];//5
						result |= (b & 0x7F) << 28;
					}
				}
			}
		}
		return ((result >>> 1) ^ -(result & 1));
	}
	
	//long
	
	/**
	 * Read a 64 bit LITTER-ENDIAN long from the stream.
	 * 
	 * @return
	 * @throws IOException
	 */
	public long readLong() throws IOException {
		int pos = position;
		if (limit - pos < 8) {
			ensureAvailable(8);
			pos = position;
		}
		final byte[] buffer = this.buf;
		position = pos + 8;
		return ((((long) buffer[pos]     & 0xFFL))       |
				(((long) buffer[pos + 1] & 0xFFL) <<  8) |
				(((long) buffer[pos + 2] & 0xFFL) << 16) |
				(((long) buffer[pos + 3] & 0xFFL) << 24) |
				(((long) buffer[pos + 4] & 0xFFL) << 32) |
				(((long) buffer[pos + 5] & 0xFFL) << 40) |
				(((long) buffer[pos + 6] & 0xFFL) << 48) |
				(((long) buffer[pos + 7] & 0xFFL) << 56));
	}
	
	/**
	 * Read a 1-9 byte long. 
	 * 
	 * @return
	 * @throws IOException
	 */
	public long readVarLong() throws IOException {
		if (limit - position < 9)  return readVarLong_slow();
		int b = this.buf[position++]; //1
		long result = b & 0x7F;
		if ((b & 0x80) != 0) {
			byte[] buffer = this.buf;
			b = buffer[position++]; //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = buffer[position++];//3
				result |= (b & 0x7F) << 14; 
				if ((b & 0x80) != 0) {
					b = buffer[position++];//4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = buffer[position++]; //5
						result |= (long)(b & 0x7F) << 28;
						if ((b & 0x80) != 0) {
							b = buffer[position++]; //6
							result |= (long)(b & 0x7F) << 35;
							if ((b & 0x80) != 0) {
								b = buffer[position++];//7
								result |= (long)(b & 0x7F) << 42;
								if ((b & 0x80) != 0) {
									b = buffer[position++];//8
									result |= (long)(b & 0x7F) << 49;
									if ((b & 0x80) != 0) {
										b = buffer[position++]; //9
										result |= (long)(b & 0x7F) << 56;
									}
								}
							}
						}
					}
				}
			}
		}
		return (long)((result >>> 1) ^ -(result & 1));
	}
	
	public long readVarLongNonZigZag() throws IOException {
		if (limit - position < 9)  return readVarLongNonZigZag_slow();
		int b = this.buf[position++]; //1
		long result = b & 0x7F;
		if ((b & 0x80) != 0) {
			byte[] buffer = this.buf;
			b = buffer[position++]; //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = buffer[position++];//3
				result |= (b & 0x7F) << 14; 
				if ((b & 0x80) != 0) {
					b = buffer[position++];//4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = buffer[position++]; //5
						result |= (long)(b & 0x7F) << 28;
						if ((b & 0x80) != 0) {
							b = buffer[position++]; //6
							result |= (long)(b & 0x7F) << 35;
							if ((b & 0x80) != 0) {
								b = buffer[position++];//7
								result |= (long)(b & 0x7F) << 42;
								if ((b & 0x80) != 0) {
									b = buffer[position++];//8
									result |= (long)(b & 0x7F) << 49;
									if ((b & 0x80) != 0) {
										b = buffer[position++]; //9
										result |= (long)(b & 0x7F) << 56;
									}
								}
							}
						}
					}
				}
			}
		}
		return (long)result;
	}
	
	protected long readVarLongNonZigZag_slow() throws IOException {
		if (limit == position) 
			ensureAvailable(1);
		int b = this.buf[position++]; //1
		long result = b & 0x7F;
		if ((b & 0x80) != 0) {
			ensureAvailable(1);
			byte[] buffer = this.buf;
			b = buffer[position++]; //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				ensureAvailable(1);
				b = buffer[position++]; //3
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					ensureAvailable(1);
					b = buffer[position++]; //4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						ensureAvailable(1);
						b = buffer[position++]; //5
						result |= (long) (b & 0x7F) << 28;
						if ((b & 0x80) != 0) {
							ensureAvailable(1);
							b = buffer[position++]; //6
							result |= (long)(b & 0x7F) << 35;
							if ((b & 0x80) != 0) {
								ensureAvailable(1);
								b = buffer[position++]; //7
								result |= (long) (b & 0x7F) << 42;
								if ((b & 0x80) != 0) {
									ensureAvailable(1);
									b = buffer[position++]; //8
									result |= (long) (b & 0x7F) << 49;
									if ((b & 0x80) != 0) {
										ensureAvailable(1);
										b = buffer[position++]; //9
										result |= (long) (b & 0x7F) << 56;	
									}
								}
							}
						}
					}
				}
			}
		}
		return (long) result;
	}
	
	protected long readVarLong_slow() throws IOException {
		if (limit == position) 
			ensureAvailable(1);
		int b = this.buf[position++]; //1
		long result = b & 0x7F;
		if ((b & 0x80) != 0) {
			ensureAvailable(1);
			byte[] buffer = this.buf;
			b = buffer[position++]; //2
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				ensureAvailable(1);
				b = buffer[position++]; //3
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					ensureAvailable(1);
					b = buffer[position++]; //4
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						ensureAvailable(1);
						b = buffer[position++]; //5
						result |= (long) (b & 0x7F) << 28;
						if ((b & 0x80) != 0) {
							ensureAvailable(1);
							b = buffer[position++]; //6
							result |= (long)(b & 0x7F) << 35;
							if ((b & 0x80) != 0) {
								ensureAvailable(1);
								b = buffer[position++]; //7
								result |= (long) (b & 0x7F) << 42;
								if ((b & 0x80) != 0) {
									ensureAvailable(1);
									b = buffer[position++]; //8
									result |= (long) (b & 0x7F) << 49;
									if ((b & 0x80) != 0) {
										ensureAvailable(1);
										b = buffer[position++]; //9
										result |= (long) (b & 0x7F) << 56;	
									}
								}
							}
						}
					}
				}
			}
		}
		return (long) ((result >>> 1) ^ -(result & 1));
	}
	
	
	
	/**
	 * Read a 2 bytes short.
	 * @return
	 * @throws IOException 
	 */
	public short readShort() throws IOException {
		return (short) readVarInt();
	}
	
	/**
	 * Read 2 bytes char in the stream. Use the LETTER-ENDIAN.
	 * 
	 * @return
	 * @throws IOException
	 */
	public char readChar() throws IOException {
		if (limit - position < 2) 
			ensureAvailable(2);
		return (char) ((this.buf[position++] & 0xFF)  | ((this.buf[position++] & 0xFF) << 8));
	}
	
	/**
	 * Read boolean.
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean readBoolean() throws IOException {
		if (position == limit) {
			ensureAvailable(1);
		}
		return this.buf[position++] == 1;
	}
	
	/**
	 * Read Float.
	 * 
	 * @return
	 * @throws IOException
	 */
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}
	
	/**
	 * Read Double.
	 * 
	 * @return
	 * @throws IOException
	 */
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}
	
	//string
	public String readString() throws IOException {
		if (limit == position) 
			ensureAvailable(1);
		int b = this.buf[position++];
		if ((b & 0x80) == 0) return readAscii0(); //ascii.
		//null, empty, utf8.
		int charCount = (limit - position >= 5) ? readUTF8Length(b) : readUTF8Length_slow(b);
		switch ( charCount ) {
		case 0:
			return null;
		case 1:
			return "";
		}
		charCount--; //the actual length == charCount - 1, because for intention to include the null, the null == 0 | 0x80. empty == 1 | 0x80
		if (chars.length < charCount) chars = new char[charCount];
		readUTF(charCount);
		return new String(chars, 0, charCount);
	}
	
	public String readUTF() throws IOException {
		ensureAvailable(1);
		int b = this.buf[position++];
		if ((b & 0x80) == 0) return readAscii0(); // ASCII.
		// Null, empty, or UTF8.
		int charCount = (limit - position >= 5) ? readUTF8Length(b) : readUTF8Length_slow(b);
		switch (charCount) {
		case 0:
			return null;
		case 1:
			return "";
		}
		charCount--;
		if (chars.length < charCount) chars = new char[charCount];
		readUTF(charCount);
		StringBuilder builder = new StringBuilder(charCount);
		builder.append(chars, 0, charCount);
		return builder.toString();
		
	}
	
	public void readUTF(int charCount) throws IOException {
		byte[] buffer = this.buf;
		char[] chars0 = this.chars;
		int idx = 0, b;
		ensureAvailable(1);//make sure the buffer has byte to read at least one.
		int pos = this.position;
		int count = Math.min(limit - pos, charCount);
		while (idx < count) {
			b = buffer[pos++];
			if (b < 0) { //non-ascii, the two bytes or three bytes.
				pos--;   // 110x xxxx 10xx xxxx, 1110 xxxx 10xx xxxx 10xx xxxx.
				break;
			}
			chars0[idx++] = (char)b;
		}
		this.position = pos;
		if (idx < charCount) 
			readUTF_slow(idx, charCount); //two situation, 1: the buffer has not all the string bytes
	}                                     //2: encounter the non-ascii byte, the utf-8 two bytes or three bytes.
	
	protected void readUTF_slow(int index, int charCount) throws IOException {
		char[] chars0 = this.chars;
		byte[] buffer = this.buf;
		while (index < charCount) {
			if (position == limit) //the first situation , has no bytes in the buffer. 
				ensureAvailable(1);
			int b = buffer[position++] & 0xFF; //byte => positive int value
			switch ( b >> 4 ) { //      >> 4
			case 0 :            // 1 byte   :                     0xxx xxxx.
			case 1 :            // 2 bytes  :           110x xxxx 10xx xxxx
			case 2 :            // 3 bytes  : 1110 xxxx 10xx xxxx 10xx xxxx
			case 3 :            // 1 byte   :     0-----0              0xxx
			case 4 :            // 2 bytes  :     0-----0         110x 10xx   
			case 5 :            // 3 bytes  :0---0 1110 xxxx 10xx xxxx 10xx           
			case 6 :            //
			case 7 :
				chars0[index] = (char)b;
				break;
			case 12:
			case 13: {
				if (position == limit) {
					ensureAvailable(1);
				}
				chars0[index] = (char)((b & 0x1F) << 6 | buffer[position++] & 0x3F);
				break;
			}
			case 14:{
				ensureAvailable(2);
				chars0[index] = (char) ((b & 0x0F) << 12 | (buffer[position++] & 0x3F) << 6 | buffer[position++] & 0x3F);
				break;
			}
			}
			index++;
		}
	}
	
	public String readAscii() throws IOException {
		byte[] buffer = this.buf;
		int end = this.position;
		int start = end; 
		int limit0 = this.limit;
		int b;
		do {
			if (end == limit0) return readAscii_slow();
			b = buffer[end++];
		} while((b & 0x80) == 0);
		buffer[end - 1] &= 0x7F; //remove the tag of the last ascii value. 
		String value = new String(buffer, start, end -start, "US-ASCII");
		buffer[end - 1] |= 0x80; //restore the state.
		position = end;
		return value;
	}
	
	protected String readAscii_slow() throws IOException {
		int charCount = limit - position; //copy chars currently in buffer.
		if (charCount > chars.length) chars = new char[charCount * 2];
		char[] chars0 = this.chars;
		byte[] buffer = this.buf;
		for (int i = position, j = 0, n = limit; i < n; i++, j++)
			chars0[j] = (char)buffer[i];
		position = limit;
		
		while (true) {
			ensureAvailable(1);
			int b = buffer[position++];
			if (charCount == chars0.length) {
				char[] newChars = new char[charCount * 2];
				System.arraycopy(chars0, 0, newChars, 0, charCount);
				chars0 = newChars;
				this.chars = newChars;
			}
			if ((b & 0x80) == 0x80) {//the tail ascii value.
				chars0[charCount++] = (char) (b & 0x7F);
				break;
			}
			chars0[charCount++] = (char)b;
		}
		return new String(chars0, 0 ,charCount);
	}
	


	protected String readAscii0() throws IOException {
		byte[] buffer = this.buf;
		int end = this.position;
		int start = end - 1; //because the first byte has been read, so need decrement 1 the position.
		int limit0 = this.limit;
		int b;
		do {
			if (end == limit0) return readAscii_slow0();
			b = buffer[end++];
		} while((b & 0x80) == 0);
		buffer[end - 1] &= 0x7F; //remove the tag of the last ascii value. 
		String value = new String(buffer, start, end -start, "US-ASCII");
		buffer[end - 1] |= 0x80; //restore the state.
		position = end;
		return value;
	}

	protected String readAscii_slow0() throws IOException {
		position--; //re-read the first byte.
		int charCount = limit - position; //copy chars currently in buffer.
		if (charCount > chars.length) chars = new char[charCount * 2];
		char[] chars0 = this.chars;
		byte[] buffer = this.buf;
		for (int i = position, j = 0, n = limit; i < n; i++, j++)
			chars0[j] = (char)buffer[i];
		position = limit;
		
		while (true) {
			ensureAvailable(1);
			int b = buffer[position++];
			if (charCount == chars0.length) {
				char[] newChars = new char[charCount * 2];
				System.arraycopy(chars0, 0, newChars, 0, charCount);
				chars0 = newChars;
				this.chars = newChars;
			}
			if ((b & 0x80) == 0x80) {//the tail ascii value.
				chars0[charCount++] = (char) (b & 0x7F);
				break;
			}
			chars0[charCount++] = (char)b;
		}
		return new String(chars0, 0 ,charCount);
	}

	/**
	 * Compute the string varint length (1-5 bytes).
	 * the first byte bit 8 means utf8, bit 7 means the next byte also is the length' content,
	 * and the next byte sequence the bit 8 means the next byte also the length' content.
	 * 
	 * @param b
	 * @return
	 */
	public int readUTF8Length(int b) {
		int result = b & 0x3F;  // the first byte low 6 bits denote the length value.
		if ((b & 0x40) != 0) { //the first byte bit 7 denote the next byte also is the content of length value. bit 8 denote utf8.
			byte[] buffer = this.buf;
			b = buffer[position++]; //2
			result |= (b & 0x7F) << 6;
			if ((b & 0x80) != 0) {
				b = buffer[position++];//3
				result |= (b & 0x7F) << 13;
				if ((b & 0x80) != 0) {
					b = buffer[position++]; //4
					result |= (b & 0x7F) << 20;
					if ((b & 0x80) != 0) {
						b = buffer[position++];//5
						result |= (b & 0x7F) << 27;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Read the Class name or class name id.
	 * 
	 * @return
	 * @throws IOException
	 */
	public Class<?> readClass() throws IOException {
		return this.resolve.readClass(this);
	}
	
	/**
	 * Read Object.
	 * @return
	 * @throws IOException
	 */
	public Object readObject() throws IOException {
		return this.resolve.readObject(this);
	}
	
	/**
	 * explain above. 
	 * 
	 * @param b
	 * @return
	 * @throws IOException 
	 */
	protected int readUTF8Length_slow(int b) throws IOException {
		int result = b & 0x3F; //1
		if ((b & 0x40) != 0) {
			ensureAvailable(1);
			byte[] buffer = this.buf;
			b = buffer[position++]; //2
			result |= (b & 0x7F) << 6;
			if ((b & 0x80) != 0) {
				ensureAvailable(1);
				b = buffer[position++];//3
				result |= (b & 0x7F) << 13;
				if ((b & 0x80) != 0) {
					ensureAvailable(1);
					b = buffer[position++]; //4
					result |= (b & 0x7F) << 20;
					if ((b & 0x80) != 0) {
						ensureAvailable(1);
						b = buffer[position++];
						result |= (b & 0x7F) << 27;
					}
				}
			}
		}
		return result;
	}
	
	public InputStream getInputStream() {
		return this.input;
	}
	
	/**
	 * Get the position on the buffer now.
	 * @return
	 */
	public int position() {
		return this.position;
	}
	
	/**
	 * Get the number of bytes read include the buffered bytes.
	 * @return
	 */
	public int total() {
		return this.position + this.totalRetired;
	}
	
	/*public void skip(int len) {
		int
	}*/

	public void reset() {
		this.position = 0;
		this.totalRetired = 0;
		this.resolve.reset();
	}
	
	public void setPosition(int position) {
		if (position < 0 || position >= capacity) {
			throw new IllegalArgumentException("Invalid position argument:" + position +
					", and the buffer capacity: " + capacity);
		}
		this.position = position;
	}
	
	/**
	 * the available bytes, different from the byte array and the underlying stream.
	 */
	public int available() throws IOException {
		return limit - position + ((input != null) ? input.available() : 0);
	}
	
	/**
	 * read and discard {@code size} bytes.
	 * 
	 * @param size the need skipped bytes number.
	 * @throws IOException  the end of the stream.
	 */
	public void skipBytes(final int size) throws IOException {
		if (size <= (limit - position) && size >= 0) {
			position += size;
		} else {
			skipBytesSlowPath(size);	
		}
	}
	
	/**
	 * call the method must need check the fast path
	 * : (size <= (limit - position) && size >= 0)
	 * @param size  the need skipped bytes
	 * @throws IOException 
	 */
	protected void skipBytesSlowPath(final int size) throws IOException {
		if (size < 0) {
			throw new RuntimeException("The skip size cannot be negative: " + size);
		}
		//Skipping more bytes than are in the buffer, firstly skip what have already.
		int pos = limit - position;
		position = limit;
		
		//keep fill buffer until get to the point wanted to skip to.
		//This must ensure the limits are updated correctly.
		ensureAvailable(1);
		
		while (size - pos > limit) {
			pos += limit;
			position = limit;
			ensureAvailable(1);
		}
		
		position = size - pos;
	}
	
	
	/**
	 * Fill the buffer with more bytes, if caller call the method and common return ,
	 * means the required bytes available, if the required bytes bigger that the 
	 * buffer capacity or the underly stream to the end(EOF), it will throw exception.
	 * 
	 * @param n
	 * @throws IOException
	 */
	protected void ensureAvailable(int n) throws IOException {
		int remaining = limit - position;
		if (remaining >= n) return;
		if (n > capacity) {
			throw new RuntimeException("Buffer too small: capacity: " + capacity + ", required: " + n);
		}
		fillBuffer(n);
	}
	
	protected void fillBuffer(int n) throws IOException {
		if (!tryFillBuffer(n)) {
			throw new RuntimeException("Invalid underlying message format, be truncated!");
		}
	}
	
	/**
	 * Try to read more bytes from the input, making at least {@code n}
	 * bytes available in the buffer. Caller must ensure that the requested
	 * space is not yet available, and that the requested space is less that
	 * {@code capacity}.
	 * 
	 * @param n  the requested space.
	 * @return   {@code true} if the bytes could be made available; {@code false}
	 * if the end of the stream.
	 * @throws IOException
	 */
	protected boolean tryFillBuffer(int n) throws IOException {
		if (position + n <= limit) {
			throw new IllegalStateException(
					"fillBuffer() called when " + n + 
					"bytes were already available in buffer");
		}
		if (this.input != null) {
			int pos = position;
			if (pos > 0) { //Non- initial read processor.
				if (limit > pos) { //Non-reach the limit tail.
					System.arraycopy(this.buf, pos, this.buf, 0, limit - pos); //discard the bytes read before the pos. 
				}
				totalRetired += pos;
				limit -= pos;
				position = 0;
			}
			int bytesRead = this.input.read(this.buf, limit, capacity - limit);
			if (bytesRead == 0 || bytesRead  < -1 || bytesRead > capacity) {
				throw new IllegalStateException(
						"InputStream#read(byte[]) returned invalid result: " + bytesRead +
						"\nThe InputStream implementation is buggy.");
			}
			if (bytesRead > 0) {
				limit += bytesRead;
			}
			return (limit >= n) ? true : tryFillBuffer(n); // if read bytes less that n, loop retry again until succeed, 
		}
		return false;
	}
	
	public void close() {
		if (this.input != null) {
			try {
				this.input.close();
			} catch (IOException e) {
				LOGGER.warn("ERROR when close the underlying input stream!", e);
			} finally {
				this.resolve.reset();
			}
		}
	}
	
}
