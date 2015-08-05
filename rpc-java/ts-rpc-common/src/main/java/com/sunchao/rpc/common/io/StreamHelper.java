package com.sunchao.rpc.common.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Stream helper class.
 * 
 * @author  <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class StreamHelper {
     /**
      * decorate pattern.
      * @param is
      *         the underlying stream.
      * @param limit
      *         the limit number.
      * @return
      *        the number of bytes read.
      * @throws IOException
      */
	public static InputStream limitedInputStream(final InputStream is, final int limit) // final desc.Anonymous class which in the method, when the anonymous class 
			throws IOException { // use the outer arguments, the outer arguments need add the description of 'final';
		return new InputStream() {
			private int mPostition = 0, mMark = 0, mLimit = Math.min(limit, is.available());
			
			@Override
			public int read() throws IOException { // one byte by one byte.
			     if (mPostition < mLimit) 
			     {
			    	 mPostition++;
			    	 return is.read();
			     }
			     return -1;
			}
			
			@Override
			public int read(byte b[], int off, int len) throws IOException {
				if (b == null) {
					throw new  NullPointerException();
				}
				if (off < 0 || len < 0 || len > b.length - off) 
					throw new IndexOutOfBoundsException();
				
				if (mPostition >= mLimit) 
					return -1;
				
				if (mPostition + len > mLimit)
					len = mLimit - mPostition;
				
				if (len == 0) 
					return 0;
				
				is.read(b, off, len);
				mPostition += len;
				return len;
			}

			@Override
			public long skip(long len) throws IOException {
				if (mPostition + len > mLimit) {
					len = mLimit - mPostition;
				}
				if (len <= 0) {
					return 0;
				}
				is.skip(len);
				mPostition += len;
				return len;
				}
			
			@Override
			public int available() {
				return mLimit - mPostition;
			}
			
			@Override
			public boolean markSupported() {
				return is.markSupported();
			}
			
			@Override
			public void mark(int readlimit) {
				is.mark(readlimit);
				this.mMark = mPostition;
			}
			
			@Override
			public void reset() throws IOException {
				is.reset();
				mPostition = mMark;
			}
			
			public void close() throws IOException {
				
			}
		};
	}
	
	public static InputStream markSupportedInputStream(final InputStream is, final int markBufferSize) {
		if (is.markSupported()) {
			return is;
		}
		
		return new InputStream() {

			byte[] mMarkBuffer;	
			boolean mInMarked = false;
			boolean mInReset = false;
			private int mPosition = 0;
			private int mCount = 0;
			
			boolean mDry = false; // End Of File (EOF)
			
			@Override
			public int read() throws IOException {
				if (!mInMarked) {
					return is.read();
				} else {
					if (mPosition < mCount) {
						byte b = mMarkBuffer[mPosition++];
						return b & 0xFF;
					}
					
					if (!mInReset) {
						if (mDry) return -1;
						
						if (null == mMarkBuffer) {
							mMarkBuffer = new byte[markBufferSize];
						}
						if (mPosition >= markBufferSize) {
							throw new IOException("Mark Buffer is full !");
						}
						
						int read = is.read();
						if (-1 == read) {
							mDry = true;
							return -1; // return -1, and the caller will deal with the condition.
						}
						
						mMarkBuffer[mPosition++] = (byte) read;
						mCount++;
						return read;
					} else {
						//mark buffer is used, exit mark status!
						mInMarked = false;
						mInReset = false;
						mPosition = 0;
						mCount = 0;
						return is.read();
					}
				}
			}

			@Override
			public int available() throws IOException {
				int available = is.available();
				if (mInMarked && mInReset) available += mCount - mPosition;
				return available;
			}


			/**
			 * NOTE: the<code>read limit</code> argument for this class
			 * has no meaning.
			 */
			@Override
			public synchronized void mark(int readlimit) {
				mInMarked = true;
				mInReset = false;
				//mark buffer is not empty
				int count = mCount - mPosition;
				if (count > 0) {
					System.arraycopy(mMarkBuffer, mPosition, mMarkBuffer, 0, count);
					mCount = count;
					mPosition = 0;
				}
			}

			@Override
			public boolean markSupported() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public synchronized void reset() throws IOException {
				if (!mInMarked) {
					throw new IOException("should mark before reset!");
				}
				mInReset = true;
				mPosition = 0;
			}
			
		};
	}
	
	public static InputStream markSupportedInputStream(final InputStream is) {
		return markSupportedInputStream(is, 1024);
	}
	
	public static void skipUnusedStream(InputStream is) throws IOException {
		if (is.available() > 0) {
			is.skip(is.available());
		}
	}
 }
