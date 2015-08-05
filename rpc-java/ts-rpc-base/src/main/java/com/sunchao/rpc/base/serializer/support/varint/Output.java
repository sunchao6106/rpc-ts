package com.sunchao.rpc.base.serializer.support.varint;

import java.io.IOException;
import java.io.OutputStream;

import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * Utility methods are provided for efficiently writing primitive types
 * and strings.
 * 
 * Unsafe.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class Output extends OutputStream {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Output.class);
	
	protected OutputStream output;
	protected int capacity;
	protected char[] charBuf = new char[256];
	protected byte[] buf, tmp = new byte[9];
	protected int position = 0;
	protected long total = 0;
	protected final ClassResolve resolve;
	
	public ClassResolve getResolve() {
		return resolve;
	}

	public Output(OutputStream output, ClassResolve resolve) {
		this(output, 512, resolve);
	}
	
	public Output(OutputStream output, int bufferSize, ClassResolve resolve) {
		if (output == null) throw new IllegalArgumentException("outputStream cannt be null!");
		if (bufferSize <= 0) throw new IllegalArgumentException("Invalid buffer size: " + bufferSize + " , buffer size must be postive!");
		if (resolve == null) throw new IllegalArgumentException("class resolve cannot be null.");
		this.output = output;
		this.capacity = bufferSize;
		this.buf = new byte[bufferSize];
		this.resolve = resolve;
	}
	
	// byte
	public void write(byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
	}
	
	public void write(byte[] bytes, int offset, int len) throws IOException {
		if (bytes == null || len == 0) return;
		int rem = capacity - position;
		if(rem > len) {
			System.arraycopy(bytes, offset, buf, position, len);
			position += len;
		} else { //zero copy, and write the underlying output stream directly.
			System.arraycopy(bytes, offset, buf, position, rem);
			position += rem;
			flush();
			
			offset += rem;
			len -= rem;
			
			while (len > capacity) { // directly
				this.output.write(bytes, offset, capacity);
				offset += capacity;
				len -= capacity;
				total += capacity;
			}
			System.arraycopy(bytes, offset, this.buf, position, len);
			position += len;
			/*IF (CAPACITY > LEN) {
				SYSTEM.ARRAYCOPY(BYTES, OFFSET, THIS.BUF, 0, LEN);
				POSITION = LEN;
			} ELSE {
				THIS.OUTPUT.WRITE(BYTES, OFFSET, LEN);
				TOTAL += LEN;
			}*/
		}
	}
	
	@Override
	public void write(int value) throws IOException {
		if(position == capacity) {flush(); }
		buf[position++] = (byte) value;	
	}
	
	/**
	 * Write a 1 byte boolean.
	 * 
	 * @param value
	 * @throws IOException
	 */
	public void writeBoolean(boolean value) throws IOException {
		if (position == capacity) {flush();}
		buf[position++] = (byte) (value ? 1 : 0);
	}
	
	//short
	public void writeShort(short value) throws IOException {
		writeVarInt(value);
	}
	
	//char
	/**
	 * Write a 2 bytes char. Use LITTER_ENDIAN byte order.
	 * @param value
	 * @throws IOException
	 */
	public void writeChar(char value) throws IOException {
		byte[] charBuf = new byte[2];
		charBuf[0] = (byte) (value & 0xFF);
		charBuf[1] = (byte) (value >>> 8);
		write(charBuf, 0 , 2);
	}

	// int 
	
	/**
	 * Write a 4 bytes int. Use LITTER_ENDIAN byte order.
	 * @param value
	 * @throws IOException
	 */
	public void writeInt(int value) throws IOException {
	   int idx = 0;
	   byte[] buffer = this.tmp;
	   buffer[idx++] = (byte) (value      );
	   buffer[idx++] = (byte) (value >>  8);
	   buffer[idx++] = (byte) (value >> 16);
	   buffer[idx++] = (byte) (value >> 24);
	   write(buffer, 0, idx);
	}
	
	/**
	 * Write a 1-5 byte int. Use the variable length encoding,
	 * and there use ZigZag encode that make sure the small negative
	 * numbers will be efficient, but the positive numbers meanwhile
	 * be 2 * .
	 * 
	 * @param value
	 * @throws IOException 
	 */
	public int writeVarInt(int value) throws IOException {
		int idx = 0;
		byte[] buffer = this.tmp;
		value = (value << 1) ^ (value >> 31); //ZigZag encode.
		if (value >> 7 == 0) { 
			buffer[idx++] = (byte) value;
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 14 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ( value >>  7 )               ); 
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 21 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 14 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 28 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 21 )               );
			write(buffer, 0, idx);
			return idx;
		} else {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 28 )               );
			write(buffer, 0, idx);
			return idx;
		}
	}
	
	/**
	 * Call the method, the caller must ensure the small negative number
	 * could not happen. It's efficient to the positive number.
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public int writeVarIntNonZigZag(int value) throws IOException {
		int idx = 0;
		byte[] buffer = this.tmp;
		//value = (value << 1) ^ (value >> 31); //ZigZag encode.
		if (value >> 7 == 0) { 
			buffer[idx++] = (byte) value;
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 14 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ( value >>  7 )               ); 
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 21 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 14 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 28 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 21 )               );
			write(buffer, 0, idx);
			return idx;
		} else {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 28 )               );
			write(buffer, 0, idx);
			return idx;
		}
	}
	
	//long
	/**
	 * Write a 8 bytes long. Use LETTER_ENDIAN byte order.
	 * @param value
	 * @throws IOException
	 */
	public void writeLong(long value) throws IOException {
		int idx = 0;
		byte[] buffer = this.tmp;
		buffer[idx++] = (byte) (value         & 0xFF);
		buffer[idx++] = (byte) ((value >>  8) & 0xFF);
		buffer[idx++] = (byte) ((value >> 16) & 0xFF);
		buffer[idx++] = (byte) ((value >> 24) & 0xFF);
		buffer[idx++] = (byte) ((value >> 32) & 0xFF);
		buffer[idx++] = (byte) ((value >> 40) & 0xFF);
		buffer[idx++] = (byte) ((value >> 48) & 0xFF);
		buffer[idx++] = (byte) ((value >> 56) & 0xFF);
		write(buffer, 0, idx);
 	}
	
	/**
	 * Write 1 - 9 byte long.
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public int writeVarLongNonZigZag(long value) throws IOException {
		int idx = 0;
		byte[] buffer = this.tmp;
	//	value = (value << 1) ^ (value >> 63); //ZigZag
		if (value >> 7 == 0) {
			buffer[idx++] = (byte) (value);
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 14 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ( value >>  7 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 21 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 14 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 28 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 21 )               );
			write(buffer, 0 , idx);
			return idx;
		} else if (value >> 35 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 28 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 42 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 35 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 49 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 35 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 42 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 56 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 35 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 42 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 49 )               );
			write(buffer, 0, idx);
			return idx;
		} else {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 35 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 42 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 49 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 56 )               );
			write(buffer, 0, idx);
			return idx;
		}
	}
	
	
	public int writeVarLong(long value) throws IOException {
		int idx = 0;
		byte[] buffer = this.tmp;
		value = (value << 1) ^ (value >> 63); //ZigZag
		if (value >> 7 == 0) {
			buffer[idx++] = (byte) (value);
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 14 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ( value >>  7 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 21 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 14 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 28 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 21 )               );
			write(buffer, 0 , idx);
			return idx;
		} else if (value >> 35 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 28 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 42 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 35 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 49 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 35 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 42 )               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >> 56 == 0) {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 35 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 42 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 49 )               );
			write(buffer, 0, idx);
			return idx;
		} else {
			buffer[idx++] = (byte) ( ( value & 0x7F)         | 0x80);
			buffer[idx++] = (byte) ( ((value >>  7 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 14 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 21 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 28 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 35 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 42 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ((value >> 49 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( ( value >> 56 )               );
			write(buffer, 0, idx);
			return idx;
		}
	}
	
	//float
	public void writeFloat(float value) throws IOException {
		writeInt(Float.floatToIntBits(value));
	}
	
	//double 
	public void writeDouble(double value) throws IOException {
		writeLong(Double.doubleToLongBits(value));
	}
	
	//string
	public void writeString(String value) throws IOException {
		if (value == null) {
			write((byte)0x80); // 0 means null, bit 8 means UTF8.
			return;
		}
		
		int count = value.length();
		if (count == 0) {
			write((byte)(1 | 0x80)); //1 means empty string, bit 8 means UTF8;
			return;
		}
		//check whether or not ascii.
		boolean ascii = false;
		if (count > 1 && count < 64) {
			ascii = true;
			for (int i = 0; i < count; i++) {
				int c = value.charAt(i);
				if (c > 0x7F) {
					ascii = false;
					break;
				}
			}
		}
		if (ascii) { // ascii write string
			byte[] charBuf = value.getBytes();
			int rem = capacity - position;
			int offset = 0;
			if (rem < count) {
				System.arraycopy(charBuf, offset, this.buf, position, rem);
				position += rem;
				flush();
				
				offset += rem;
				count -= rem;
				
				if (capacity > count) {
					System.arraycopy(charBuf, offset, this.buf, position, count);
					position += count;
				} else {
					this.output.write(charBuf, offset, count - 1); // there remaining a byte to make tag for tail.
					total += (count - 1);
					this.buf[position++] = charBuf[charBuf.length - 1];
				}
			} else {
				System.arraycopy(charBuf, offset, this.buf, position, count);
				position += count;
			}
			
			this.buf[position - 1] |= 0x80; //ascii stop flag
			
		} else {//uft-8 write string
			writeUTF8Length(count + 1); //the UFT8 write string length = char count + 1(include null);
			int offset = 0, limit = capacity - 3, size;
			char[] buffer = this.charBuf;
			do {
				 size = Math.min(count - offset, buffer.length);
				 value.getChars(offset, offset + size, buffer, 0);
				 
				 for (int i = 0; i < size; i++) {
				     char c = buffer[i];
				     if (position > limit) { //limit = capacity -3 sure write the max length once.
				    	 if (c <= 0x7F) { // ascii
				    		 write((byte) c                          );
				    	 } else if (c > 0x07FF) { //1110 xxxx 10xx xxxx 10xx xxxx 
				    		 write((byte) (0xE0 | ((c >> 12) & 0x0F)));
				    		 write((byte) (0x80 | ((c >>  6) & 0x3F)));
				    		 write((byte) (0x80 |  (c & 0x3F)       ));
				    	 } else { // 110x xxxx  10xx xxxx
				    		 write((byte) (0x80 | ((c >>  6) & 0x1F)));
				    		 write((byte) (0x80 |  (c & 0x3F)       ));
				    	 }
				     } else {
				    	 if (c <= 0x7F) {
				    		 this.buf[position++] = (byte) c;
				    	 } else if (c > 0x07FF) {
				    		 this.buf[position++] = (byte) (0xE0 |((c >> 12) & 0x0F));
				    		 this.buf[position++] = (byte) (0x80 |((c >>  6) & 0x3F));
				    		 this.buf[position++] = (byte) (0x80 | (c & 0x3F)        );
				    	 } else {
				    		 this.buf[position++] = (byte) (0xC0 |((c >>  6) & 0x1F));
				    		 this.buf[position++] = (byte) (0x80 | (c & 0x3F)       );
				    	 }
				     }
				 }
				 offset += size;
			} while (offset < count);
		}
	}
	
	/**
	 * Write a string that is known to only UTF-8.
	 * 
	 * @param value
	 * @throws IOException 
	 */
	public void writeUTF(String value) throws IOException {
		if (value == null) {
			write((byte)0x80);
			return;
		}
		int count = value.length();
		if (count == 0) {
			write((byte)(1 | 0x80));
			return;
		}
		writeUTF8Length(count + 1);
		int idx = 0, limit = capacity - 3, size;
		char[] buffer = this.charBuf;
		do {
			size = Math.min(count - idx, buffer.length);
			value.getChars(idx, idx + size, buffer, 0);
			
			for (int i = 0; i < size; i++) {
				char c = buffer[i];
				if (position > limit) {
					if (c <= 0x7F) {
						write((byte)c);
					} else if (c > 0x07FF) {
						write((byte) (0xE0 | ((c >> 12) & 0x0F)));
						write((byte) (0x80 | ((c >>  6) & 0x3F)));
						write((byte) (0x80 | ( c & 0x3F)       ));
					} else {
						write((byte) (0xC0 | ((c >>  6) & 0x1F)));
						write((byte) (0x80 |  (c & 0x3F)));
					}
				} else {
					if (c <= 0x7F) {
						this.buf[position++] = (byte) c;
					} else if (c > 0x07FF) {
						this.buf[position++] = (byte) (0xE0 |((c >> 12) & 0x0F));
						this.buf[position++] = (byte) (0x80 |((c >>  6) & 0x3F));
						this.buf[position++] = (byte) (0x80 | (c & 0x3F)       );
					} else {
						this.buf[position++] = (byte) (0xC0 |((c >>  6) & 0x1F));
						this.buf[position++] = (byte) (0x80 | (c & 0x3F)       );
					}
				}
			}
			idx += size;
		} while (idx < count);
	}
	
	/**
	 * Write a string that is known to contains only ASCII characters.
	 * @param value
	 * @throws IOException
	 */
	public void writeAscii(String value) throws IOException {
		if (value == null) {
			write((byte)0x80);
			return;
		} 
		int count = value.length();
		switch (count) {
		case 0:
			write((byte)(1 | 0x80)); // write the length + 1, intend to contains the null(0x80 | 0)
			return;
		case 1:              // only one char, write use UTF8;
			write((byte)(2 | 0x80));
			write((byte)value.charAt(0));
			return;
		} 
		int rem = capacity - position, idx = 0;
		byte[] chars = value.getBytes("US-ASCII");
		if (rem < count) {
			System.arraycopy(chars, idx, this.buf, position, rem);
			position += rem;
			flush();
			
			idx += rem;
			count -= rem;
			
			if (capacity > count) {
				System.arraycopy(chars, idx, this.buf, position, count);
				position += count;
			} else {
				this.output.write(chars, idx, count - 1);
				total += (count - 1);
				this.buf[position++] = chars[chars.length- 1]; //remaining the last byte to the the buffered byte array to make tag.
			} 
		} else {
			System.arraycopy(chars, idx, this.buf, position, count);
		    position += count;
		}
		this.buf[position-1] |= 0x80; //ascii string tail tag. 
	}
	
	/**
	 * Write the length of a string, which is a variable length encoded int.
	 *  and the first byte use bit 8 denote the UTF8 and the bit 7 denote whether the next byte is
	 * inline. 
	 * @param value
	 * @throws IOException 
	 */
	private int writeUTF8Length(int value) throws IOException {
		int idx = 0;
		if (value >>> 6 == 0) { //one byte
		    byte[] buffer = this.tmp; 
			buffer[idx++] = (byte) (value | 0x80                  );
			write(buffer, 0, idx);
			return idx;
		} else if (value >>> 13 == 0) { //two bytes.
			byte[] buffer = this.tmp;
			buffer[idx++] = (byte) ( (value & 0x3F) | 0x40  | 0x80);
			buffer[idx++] = (byte) ( (value >>> 6)                );
			write(buffer, 0, idx);
			return idx;
		} else if (value >>> 20 == 0) { //three bytes.
			byte[] buffer = this.tmp;
			buffer[idx++] = (byte) ( (value & 0x3F) | 0x40  | 0x80);
			buffer[idx++] = (byte) (((value >>> 6 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( (value >>> 13)               );
			write(buffer, 0, idx);
			return idx;
		} else if (value >>> 27 == 0) { //four bytes.
			byte[] buffer = this.tmp;
			buffer[idx++] = (byte) ( (value & 0x3F) | 0x40  | 0x80);
			buffer[idx++] = (byte) (((value >>> 6 ) & 0x7F) | 0x80);
			buffer[idx++] = (byte) (((value >>> 13) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( (value >>> 20)               );
			write(buffer, 0 , idx);
			return idx;
		} else { // five bytes.
			byte[] buffer = this.tmp;
			buffer[idx++] = (byte) ( (value & 0x3F) | 0x40  | 0x80);
			buffer[idx++] = (byte) (((value >>>  6) & 0x7F) | 0x80);
			buffer[idx++] = (byte) (((value >>> 13) & 0x7F) | 0x80);
			buffer[idx++] = (byte) (((value >>> 20) & 0x7F) | 0x80);
			buffer[idx++] = (byte) ( (value >>> 27)               );
			write(buffer, 0, idx);
			return idx;
		}
	}
	
	/**
	 * Write Class name or class name id.
	 * 
	 * @param type
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public void writeClass(Class<?> type) throws IOException {
		resolve.writeClass(this, type);
	}
	
	/**
	 * Write Object
	 * @param obj
	 * @throws IOException
	 */
	public void writeObject(Object obj) throws IOException {
		resolve.writeObject(this, obj);
	}
	
	/**
	 * compute the int value encode length.
	 * @param value
	 * @return
	 */
	public static int intSize(int value) {
		if ((value & (0xFFFFFFFF <<  7)) == 0) return 1;
		if ((value & (0xFFFFFFFF << 14)) == 0) return 2;
		if ((value & (0xFFFFFFFF << 21)) == 0) return 3;
		if ((value & (0xFFFFFFFF << 28)) == 0) return 4;
		return 5;
	}
	
	/**
	 * Compute the long value encode length.
	 * @param value
	 * @return
	 */
	public static int longSize(long value) {
		if ((value & (0xFFFFFFFFFFFFFFFFL <<  7)) == 0) return 1;
		if ((value & (0xFFFFFFFFFFFFFFFFL << 14)) == 0) return 2;
		if ((value & (0xFFFFFFFFFFFFFFFFL << 21)) == 0) return 3;
		if ((value & (0xFFFFFFFFFFFFFFFFL << 28)) == 0) return 4;
		if ((value & (0xFFFFFFFFFFFFFFFFL << 35)) == 0) return 5;
		if ((value & (0xFFFFFFFFFFFFFFFFL << 42)) == 0) return 6;
		if ((value & (0xFFFFFFFFFFFFFFFFL << 49)) == 0) return 7;
		if ((value & (0xFFFFFFFFFFFFFFFFL << 56)) == 0) return 8;
		return 9;
	}
	
	/**
	 * flush the buffered data to the underlying output stream
	 * @throws IOException 
	 */
	public void flush() throws IOException {
		if (this.output == null) return;
		this.output.write(buf, 0, position);
		total += position;
		position = 0;
	}
	
	/**
	 * before closing the underlying stream, need to flush the buffered data.
	 */
	public void close() {
		if(this.output != null) {
			try {
				flush();
				this.output.close();
			} catch (IOException e) {
				LOGGER.warn("unexpected error when close the underlying output stream!", e);
			} finally {
				this.resolve.reset();
			}
		}
	}
	
	/**
	 * reset the buffer capacity.
	 */
	public void reset() {
		this.position = 0;
		this.resolve.reset();
	}
	
	public void setPosition(int position) {
		if (position < 0 || position >= capacity) {
			throw new IllegalArgumentException("Invalid position argument: " + position 
					+ " , the capacity: " + capacity);
		}
		this.position = position;
	}
	
	public int position() {
		return this.position;
	}
	
	/**
	 * @return the total written bytes, which include the flushed and buffered bytes 
	 *  yet.
	 */
	public long total() {
		return this.position + this.total;
	}
	
	public OutputStream getOutputStream() {
		return this.output;
	}
	
	
}
