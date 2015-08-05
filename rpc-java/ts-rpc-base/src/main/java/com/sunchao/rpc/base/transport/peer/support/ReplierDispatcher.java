package com.sunchao.rpc.base.transport.peer.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.transport.peer.PeerChannel;
import com.sunchao.rpc.base.transport.peer.Replier;

/**
 * The request handler's dispatcher.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class ReplierDispatcher implements Replier<Object> {

	/**
	 * The default replier, while not found the suit replier
	 * (maybe not registered) ,use the default replier to
	 * deal with the request.
	 */
	private final Replier<?> defaultReliper;
	
	/**
	 * The register replier by the specified class type.
	 */
	private final Map<Class<?>, Replier<?>> repliers = new ConcurrentHashMap<Class<?>, Replier<?>>();
	
	public ReplierDispatcher() {
		this(null, null);
	}
	
	public ReplierDispatcher(Replier<?> defaultReplier) {
		this(defaultReplier, null);
	}
	
	public ReplierDispatcher(Replier<?> defaultReplier,Map<Class<?>, Replier<?>> repliers) {
		this.defaultReliper = defaultReplier;
		if (repliers != null && repliers.size() > 0)
			this.repliers.putAll(repliers);
	}
	
	public <T> ReplierDispatcher addReplier(Class<T> type, Replier<T> replier) {
		repliers.put(type, replier);
		return this;
	}
	
	public <T> ReplierDispatcher removeReplier(Class<T> type) {
		repliers.remove(type);
		return this;
	}
	
	/**
	 * Get the replier by the specified class type.
	 * 
	 * @param type the specified request class type.
	 * @return
	 */
	private Replier<?> getReplier(Class<?> type) {
		for (Map.Entry<Class<?>, Replier<?>> entry : repliers.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				return entry.getValue();
			}
		}
		if (defaultReliper != null)
			return defaultReliper;
		throw new IllegalStateException("Replier not found, Unsupported message object: " + type);
	}
	
	/**
	 * invoke the replier {@link Replier#reply(PeerChannel, Object)} method
	 * to deal with the request and generate the result.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object reply(PeerChannel channel, Object request)
			throws RPCException {
		return ((Replier)getReplier(request.getClass())).reply(channel, request);
	}

}
