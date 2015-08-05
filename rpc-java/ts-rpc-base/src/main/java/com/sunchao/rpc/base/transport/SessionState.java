package com.sunchao.rpc.base.transport;

/**
 * Record the client send and receive state for statistics and session manage.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public abstract class SessionState {

	//private static final Logger LOGGER = LoggerFactory.getLogger(SessionState.class);
	
	protected boolean initialized;
	
	protected long sendCount = 0;
	protected long recvCount = 0;
	protected long lastPing;
	protected long lastSend;
	protected long now;
	
	void updateNow() {
		now = System.currentTimeMillis();
	}
	
	int getIdleRecv() {
		return (int) (now - lastSend); 
	}
	
	long getSendCount() {
		return sendCount;
	}
	
	long getRecvCount() {
		return recvCount;
	}
	
	void updateLastSend() {
		this.lastSend = now;
	}
	
	void updateLastPing() {
		this.lastPing = now;
	}
	
	void updateLastSendAndPing() {
		this.lastPing = now;
		this.lastSend = now;
	}
}
