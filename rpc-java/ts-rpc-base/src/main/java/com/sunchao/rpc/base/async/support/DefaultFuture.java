package com.sunchao.rpc.base.async.support;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.async.AsyncCallback;
import com.sunchao.rpc.base.async.ClientManager;
import com.sunchao.rpc.base.async.Future;
import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;
/**
 * The Default Future.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class DefaultFuture implements Future {

	/**
	 * Check send.
	 * @return the send
	 */
	public boolean hasSend() {
		return send > 0;
	}
	
	public long getSend() {
		return send;
	}

	/**
	 * @param send the send to set
	 */
	public void setSend() {
	    this.send = System.currentTimeMillis();
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(Response response) {
		if (isDone()) {
			return;
		} else {
			stateLock.lock();
			try {
				if (isDone()) {
					return;
				} else {
					this.response = response;
					DONE.signal();
					if (callback != null)
						emitCallback(callback);
				}
			} finally {
				stateLock.unlock();
			}
		}
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}
	
	public long getTimeoutTimeStamp() {
		return start + timeout;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DefaultFuture [id=" + id + ", channel=" + channel
				+ ", request=" + request + ", timeout=" + timeout + ", start="
				+ start + ", send=" + send + "]";
	}

	private final long id;
	
	private final Channel channel;
	
	private final Request request;
	
	private final int timeout;
	
	private final long start = System.currentTimeMillis();
	
	private final Lock stateLock = new ReentrantLock();
	
	/**
	 * the done condition.
	 */
	private final Condition DONE = stateLock.newCondition();
	
	
	
	//private final Lock sendLock = new ReentrantLock();
	
	/**
	 * The below three variables with the modifier of
	 * {@code volatile}, because the three variables need to be 
	 * exchanged with the client thread and the send/receive thread.
	 * 
	 */
	private volatile long send;
	
	private volatile Response response;
	
	private volatile AsyncCallback callback;
	
//	private volatile boolean cancelled;
	
	/**
	 * NOTE: If the client do not denote the time out.
	 * the default time out will be used.
	 * {@see com.sunchao.rpc.base.Config}
	 * 
	 * @param request the request for rpc invocation.
	 * @param channel the underlying transport channel.
	 */
	public DefaultFuture(Request request, Channel channel) {
		this(request, channel, Config.RESPONSE_TIMEOUT);
	}
	

	/**
	 * TDDO Is there exist initialization escape ?
	 * 
	 * @param request the remote request.
	 * @param channel the transport channel.
	 * @param timeout the invocation timeout.
	 */
	public DefaultFuture(Request request, Channel channel, int timeout) {
		if (request == null)
			throw new IllegalArgumentException("the request cannot be null.");
		if (channel == null)
			throw new IllegalArgumentException("the underlying transport channel not initialization, and cannot be null.");
		if (timeout <= 0)
			timeout = Config.RESPONSE_TIMEOUT;
		this.request = request;
		this.id = request.getId();
		this.channel = channel;
	    this.timeout = timeout;
	    ClientManager.registerPendingRequest(this); 
	}
	
	/**
	 * No specified timeout, using the default value.
	 */
	public Object get() throws RPCException {
		return get(Config.RESPONSE_TIMEOUT);
	}

	/**
	 * Using the specified timeout value.
	 */
	public Object get(int timeout) throws RPCException {
		if (timeout <= 0) 
			timeout = Config.RESPONSE_TIMEOUT;
		if (!isDone()) {
			long now = System.currentTimeMillis();
			stateLock.lock();
			try {
				while (!isDone()) {//Spurious waking of threads is possible
					DONE.await(timeout, TimeUnit.MILLISECONDS);
					if (isDone() || System.currentTimeMillis() - now > timeout) {
						break;
					}
				}
			} catch (InterruptedException e) {
				throw new RPCException(e);
			} finally {
				stateLock.unlock();
			}
		}
		if (!isDone()) {
			throw new RPCException(RPCException.TIMEOUT, 
					"the wait time exceed the specified timeout: " + timeout);
		}
		return extractResult();
	}

	public void setCallback(AsyncCallback callback) {
	    if (isDone()) {
	    	emitCallback(callback);
	    } else {
	    	stateLock.lock();
	    	try {
	    		if (isDone()) {
	    			setCallback(callback); //invoke self, come into the first condition 'if'.
	    		} else {
	    			this.callback = callback;
	    		}
	    	} finally {
	    		stateLock.unlock();
	    	}
	    }
	}
	
	/**
	 * Invoke the call back
	 * @param callback
	 */
	private void emitCallback(AsyncCallback callback) {
		AsyncCallback back = callback;
    	if (back == null) 
    		throw new NullPointerException("the async call back cannot be null.");
    	callback = null;
    	Response resp = this.response;
    	if (resp == null)
    		throw new IllegalStateException("Internal error, when the repsonse shouldn't be null.");
    	if (resp.getStatus_code() == Response.OK) {
    		try {
    			back.handleResult(resp.getResult());
    		} catch (Exception e) {
    			LOGGER.error("callback invoke Error. result: " + resp.getResult() + ", URI: " + channel.getRemoteAddress().toString());
    		}
    	} else if (resp.getStatus_code() == Response.CLIENT_TIMEOUT || resp.getStatus_code() == Response.SERVER_TIMEOUT) {
    		try {
    	  	     RPCException exception  = new RPCException(RPCException.TIMEOUT, 
    			        	"the result is time-out-exception, maybe the slow network pass on or the length server-side servicce  execute.");
    		     back.handleError(exception);
    		} catch (Exception e) {
    			LOGGER.error("callback invoke Error, result: " + channel.getRemoteAddress().toString());
    		}
    	} else {
    		try {
    			RPCException exception  = new RPCException(RPCException.UNKNOWN, resp.getErrorMsg());
    			back.handleError(exception);
    		} catch (Exception e) {
    			LOGGER.error("callback invoke error: " + e.getMessage());
    		}
    	}
	}
	
	private Object extractResult() throws RPCException {
		Response result = this.response;
		if (result == null) {
			throw new RPCException( "Inner error, the invocation has over in the specifed time, but response == null");
		}
		if (result.getStatus_code() == Response.OK) {
			return result.getResult();
		}
		
		if (result.getStatus_code() == Response.CLIENT_TIMEOUT || result.getStatus_code() == Response.SERVER_TIMEOUT) {
			throw new RPCException(RPCException.TIMEOUT, "the execution time exceed, may be the network pass on or the server execute time too long. ");
		}
		throw new RPCException(RPCException.UNKNOWN, result.getErrorMsg());
	}
	

	public boolean isDone() {
		return this.response != null;
	}

	/**
	 * the method just remove the record the asynchronous remote invocation,
	 * and not cancel the invocation actually, just ignore and discard the response when 
	 * the result coming from network, and not notice the client.
	 * When the response has been set. 
	 */
	public void cancel() {
		Response resp = new Response(id);
		resp.setErrorMsg("request future has been canceled.");
		response = resp;
		ClientManager.cancelPending(this);	
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("the request future has been canceled by the client, and the request: " + request);
		}
	}
	
	public static String getTimeoutMessage(boolean scan, DefaultFuture future) {
		long nowTimestamp = System.currentTimeMillis();
		return (future.hasSend() ? "Waiting server-side repsonse timeout" : "Sending request timeout in client-side")
				+ (scan ? " by scan time" : "") + ". start time: " 
				+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(future.getStart())) + ", end time: "
				+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())) + ","
				+ (future.hasSend() ? " client elapsed: " + (future.getSend() - future.getStart())
				+ " ms, server elapse: " + (nowTimestamp - future.getSend()) : " elapse: " + (nowTimestamp - future.getStart())
				+ " ms, timeout: "  + future.getTimeout() + " ms, request: " + future.getRequest() + ", channel: " +
				future.getChannel().getLocalAddress() + " -> " + future.getChannel().getRemoteAddress()));
	}
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFuture.class);

}
