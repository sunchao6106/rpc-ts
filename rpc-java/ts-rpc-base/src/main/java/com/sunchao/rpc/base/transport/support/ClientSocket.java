package com.sunchao.rpc.base.transport.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.sunchao.rpc.base.exception.RPCTransportException;
import com.sunchao.rpc.base.transport.ClientTransceiver;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

public class ClientSocket {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientSocket.class);

	/**
	 * next step in the call, initialized by {@code #start()}
	 */
	private State state = null;
	
	private ClientTransceiver transceiver;
	private final boolean isOneway;
//	private long sequenceId;
	private final long timeout;
	private long startTime;
	
	private ByteBuffer headerBuffer;
	//private final byte[] sizeBufferArray = new byte[4];
	
	private ByteBuffer bodyBuffer;
	
	
	public ClientSocket(final boolean isOneway, final long timeout,
			long sequenceId, final InetSocketAddress remoteAddress) throws RPCTransportException {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Start an new remote rpc call, and the sequenceID: " + sequenceId);
		}
		this.isOneway = isOneway;
	//	this.sequenceId = sequenceId;
		this.timeout = timeout;
		this.startTime = System.currentTimeMillis();
		try {
			this.transceiver = new ClientTransceiver(remoteAddress);
		} catch (IOException e) {
			throw new RPCTransportException(RPCTransportException.UNKNOWN,
					"Error when initialize the connect to server.", e);
		}
	}
	
	/**
	 * Get the state now.
	 * 
	 * @return
	 */
	protected State getState() {
		return state;
	}
	
	/**
	 * Get the async rpc start time. Use for the time out check.
	 * 
	 * @return
	 */
	protected long getStartTime() {
		return startTime;
	}
	
	public long getTimeoutstamp() {
		return startTime + timeout;
	}
	
	public boolean hasTimeout() {
		return timeout > 0;
	}
	
	public void start(Selector selector) throws IOException {
		SelectionKey key;
		if (this.transceiver.isOpen()) {
			state = State.WRITING_REQUEST_SIZE;
			key = this.transceiver.registerSelector(selector, SelectionKey.OP_WRITE);
		} else {
			state = State.CONNECTION;
			key = this.transceiver.registerSelector(selector, SelectionKey.OP_CONNECT);
			//non-blocking connect complete immediately.
			//so do not expect the OP_CONNECT.
			if (this.transceiver.startConnect()) {
				registerForWrite(key);
			}
		}
		key.attach(this);
	}
	
	/**
	 * Switch the state for Write.
	 * 
	 * @param key
	 */
	protected void registerForWrite(SelectionKey key) {
		state = State.WRITING_REQUEST_SIZE;
		key.interestOps(SelectionKey.OP_WRITE);
	}

	/**
	 * The state machine transition to next state, doing whatever work is specified.
	 * Since this method is only called by the selector thread, we can make changes to
	 * our select interests without worry about concurrency.
	 * 
	 * @param key the selection key.
	 */
	protected void transition(SelectionKey key) {
		if (!key.isValid()) {
			key.cancel();
			Exception e = new RPCTransportException("The underlying transport selection key invalid.");
			emmitError(e);
			return;
		}
		
		//transition state.
		try {
			switch (state) {
			case CONNECTION:
				doConnection(key);
				break;
			case WRITING_REQUEST_SIZE:
				doWritingRequestSize(key);
				break;
			case WRITING_REQUEST_BODY:
				doWritingRequestBody(key);
			    break;
			case READING_RESPONSE_SIZE:
				doReadingResponseSize();
				break;
			case READING_RESPONSE_BODY:
				doReadingResponseBody(key);
				break;
			default://RESPONSE_READ, ERROR, or bug
				throw new IllegalStateException("Message call in state: " + state
						+ " but selector called transition method. Seems likes a bug...");
			}
		} catch (Exception e) {
			key.cancel();
			state = State.ERROR;
		}
	}
	
	private void doConnection(SelectionKey key) throws IOException {
		if (!key.isConnectable() || ! this.transceiver.finishConnect()) {
			throw new IOException("not connectable or finishConnect return false after get the OP_CONNECT");
		}
		registerForWrite(key);
	}
	
	private void doWritingRequestSize(SelectionKey key) throws IOException, RPCTransportException {
		if (this.transceiver.writeBuffer(headerBuffer) < 0) 
			throw new IOException("write the packet size fail.");
		if (headerBuffer.remaining() == 0)
			state = State.WRITING_REQUEST_BODY;
	}
	
	private void doWritingRequestBody(SelectionKey key) throws IOException, RPCTransportException {
		if (this.transceiver.writeBuffer(bodyBuffer) < 0)
			throw new IOException("Write the body buffer failed.");
		if (bodyBuffer.remaining() == 0) {
			if (isOneway) {
				cleanUpAndEmitCallback(key);
			} else {
				state = State.READING_RESPONSE_SIZE;
				headerBuffer.rewind(); //prepare to read incoming packet size;
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	
	private void cleanUpAndEmitCallback(SelectionKey key) {
		state = State.RESPONSE_READ; //over.
		key.interestOps(0);
		key.attach(null);
		
	}
	
	private void doReadingResponseSize() throws IOException, RPCTransportException {
		if (this.transceiver.readBuffer(headerBuffer) < 0) 
			throw new IOException("Read call frame size failed.");
		if (headerBuffer.remaining() == 0) {
			state = State.READING_RESPONSE_BODY;
			
		}	
	}
	

	private void doReadingResponseBody(SelectionKey key) throws IOException {
		
	}
	
	protected void emmitError(Exception e) {
		
	}
	
	/**
	 * 
	 * The state machine of asynchronous client operation.
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 */
	public static enum State {
		CONNECTION,
		WRITING_REQUEST_SIZE,
		WRITING_REQUEST_BODY,
		READING_RESPONSE_SIZE,
		READING_RESPONSE_BODY,
		RESPONSE_READ,
		ERROR;
	}
}
