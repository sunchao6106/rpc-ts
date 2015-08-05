package com.sunchao.rpc.base.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.sunchao.rpc.base.exception.RPCTransportException;
import com.sunchao.rpc.base.metadata.Packet;
import com.sunchao.rpc.base.metadata.PacketHeader;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.SerializationFactory;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.transport.discard.Server;
import com.sunchao.rpc.common.io.ByteBufferInputStream;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * The JDkServer Base Class.
 * Provides common methods used by server 
 * implementations.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public abstract class AbstractServer implements Server {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServer.class);
	/** the flag server state */
    volatile boolean isStop = false;
  
	/**The client socket time out*/
	final int clientTimeOut;
	
	protected ServerSocketChannel serverSocketChannel = null;
	
	protected ServerSocket serverSocket = null;
	
	/**
	 * The maximum amount of memory allocate to client IO buffer at a time.
	 * Without the limit, the server will into an OOM.
	 */
	final long CLIENT_IO_LIMIT;
	
	final AtomicLong readBufferAllocated = new AtomicLong(0);
	
	public static abstract class AbstractServerArgs<T extends AbstractServerArgs<T>> {
		public int backlog;
		public InetSocketAddress bindAddress;
		public int clientTimeOut;
		public long CLIENT_IO_LIMIT;
		
		public AbstractServerArgs() {}
		
		public AbstractServerArgs(int backlog, InetSocketAddress bindAddress, int clientTimeOut
				, long CLIENT_IO_LIMIT) {
			this.backlog = backlog;
			this.bindAddress = bindAddress;
			this.clientTimeOut = clientTimeOut;
			this.CLIENT_IO_LIMIT = CLIENT_IO_LIMIT;
		}
		
		@SuppressWarnings("unchecked")
		public T backlog(int backlog) {
			this.backlog = backlog;
			return (T) this;
		}
		
		@SuppressWarnings("unchecked")
		public T port(int port) {
			this.bindAddress = new InetSocketAddress(port);
			return (T) this;
		}
		
		@SuppressWarnings("unchecked")
		public T clientTimeOut(int timeOut) {
			this.clientTimeOut = timeOut;
			return (T) this;
		}
		
		@SuppressWarnings("unchecked")
		public T clientIOLimit(long clientIOLimit) {
			this.CLIENT_IO_LIMIT = clientIOLimit;
			return (T) this;
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public AbstractServer(AbstractServerArgs args) {
		this.CLIENT_IO_LIMIT = args.CLIENT_IO_LIMIT;
		this.clientTimeOut = args.clientTimeOut;
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false); //non-blocking model.
			
			serverSocket = serverSocketChannel.socket();
			serverSocket.setReuseAddress(true);//the TCP 2MSL delay problem on server restart.
			serverSocket.bind(args.bindAddress, args.backlog);
		} catch (IOException e) {
			serverSocket = null;
			throw new RPCTransportException(
					RPCTransportException.UNKNOWN, 
					"Could not create ServerSocket on address: " + args.bindAddress.toString()
					, e);
		}
	}
	
	public void registerSelector(Selector selector) {
		try {
			this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			LOGGER.warn("WARN: server socket has been stoped !," + e.getMessage());
		}
	}
	
	public void serve() {
		//start IO threads.
		if (!startThreads()) { return;}
		//start listen, operation-placeholder
		if (!listen()) {return;}
		//block while server start.
		waitForShutdown();
		
		setStopFlag();
		//clean up work.
		close();
	}
	
	protected void close() {
	   if (serverSocket != null) {
		   LOGGER.info("stopping " + serverSocket.getInetAddress());
		   try {
			serverSocket.close();
		   } catch (IOException e) {
			  LOGGER.warn("Could not close server socket: " + e.getMessage());
		   }
		   serverSocket = null;
	   }
	}
	
	public int getPort() {
		if (serverSocket != null) 
			return -1;
		return serverSocket.getLocalPort();
	}
	
	protected boolean listen() {
		if (serverSocket != null) {
			LOGGER.info("starting " + serverSocket.getInetAddress());
			try {
				serverSocket.setSoTimeout(0); //<code>ServerSocket.accept() blocked until connection coming</code>
				return true;
			} catch (SocketException sx) {
				LOGGER.warn("Could not start listening the server socket!", sx);
				return false;
			}
		}
		throw new RPCTransportException(RPCTransportException.NOT_OPEN, "the server socket not start yet!");
	}
	
	public final Transceiver accept() throws RPCTransportException {
		if (serverSocket == null) {
			throw new RPCTransportException(
					RPCTransportException.NOT_OPEN, 
					"No underlying server socket open!");
		}
		//LOGGER.info("opening " + serverSocket.getInetAddress());
		try {
			SocketChannel socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false); //Non-blocking.
			Socket socket = socketChannel.socket();
			socket.setSoLinger(false, 0);  //nagle
			socket.setTcpNoDelay(true); //nagle
			socket.setKeepAlive(true); //tcp keep alive
			socket.setSoTimeout(this.clientTimeOut);
			return new PeerTransceiver(socketChannel);
		} catch (IOException e) {
			LOGGER.warn("unexpected error", e);
			throw new RPCTransportException(e);
		}
	}
	
	/**
	 * Starts any threads required for server.
	 * @return 
	 *        true if everything is OK.
	 *        false if threads could not be started.
	 */
	protected abstract boolean startThreads();
	
	
	/**
	 * The method will block until when threads handling the serving have been
	 * shut down.
	 */
	protected abstract void waitForShutdown();
	
	
	public boolean isStop() {
		return this.isStop;
	}
	
	public void setStopFlag() throws RPCTransportException {
		this.isStop = true;
	}
	
	/**
	 * 
	 * 
	 * @param buffer
	 * @return
	 */
	protected abstract boolean requestInvoke(TransportBuffer buffer);
	
	/**
	 * An thread that handles selecting on a set of connection.
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 */
	protected abstract class AbstractSelectThread extends Thread {
		
		protected final Selector selector;
		
		public AbstractSelectThread() throws Exception {
			this.selector = SelectorProvider.provider().openSelector();
		}
		
		/**
		 * If the selector blocked, wake up.
		 */
		public void wakeupSelector() {
			this.selector.wakeup();
		}
		
		protected void handleRead(SelectionKey key) {
			
		}
	}
	
	/**
	 * A series of states for the TransportBuffer state machine.
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 */
	private enum TransportBufferState {
		//in the process of reading the transport size off the wire.
		READING_BUFFER_SIZE,
		// reading the actual transport data now, but not done yet.
		READING_BUFFER,
		// completely read the transport buffer, so an invocation can be happen.
		READ_BUFFER_COMPLETE,
		// waiting to get switched to listening for writing events.
		AWAITING_REGISTER_WRITE,
		//starting writing response data, not fully complete yet.
		WRITING,
		// another thread wants this transport buffer to go back to reading.
		AWAITING_REGISTER_READ,
		//want to the selection key and the socket channel invalid(close) in the selector thread.
		AWAITING_CLOSE;	
	}

	/**
	 * The class implements a sort of state machine around the interaction with
	 * a client and an processor. It manages reading the buffer size and the buffer
	 * data, getting it handed off as transports, and then the writing of response
	 * data back to the client.
	 * 
	 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
	 *
	 */
	public class TransportBuffer {
		
		private final Logger LOGGER = LoggerFactory.getLogger(TransportBuffer.class);
		
		//the underly socket channel to client.
		protected final Transceiver transceiver;
		
		//the socket channel's selection key.
		protected final SelectionKey selectionKey;
		
		//the SelectThread that owns the registration of the socket channel.
		protected final AbstractSelectThread selectThread;
		//the initial state of the process.
		protected TransportBufferState state = TransportBufferState.READING_BUFFER_SIZE;
		//the delimited capacity of read/write packet header.
		protected final ByteBuffer header = ByteBuffer.allocate(PacketHeader.PACK_HEADER_SIZE);
		//the buffer which used to read/write, depending in the {@link TransportBufferState}
		protected List<ByteBuffer> messages;
		//the serializer/deserializer.
		protected Serializer serializer;
		//the rpc context for serializer/deserializer.
		protected final Context context;
		
		public TransportBuffer(final PeerTransceiver transceiver,
				final SelectionKey selectionKey,
				final AbstractSelectThread selectThread,
				final Context context) {
			this.transceiver = transceiver;
			this.selectionKey = selectionKey;
			this.selectThread = selectThread;
			this.context = context;
		}
		
		public boolean read() {
			if (state == TransportBufferState.READING_BUFFER_SIZE) {
				//try to read the packet header into the header buffer completely.
				if (!channelRead()) {
					return false;
				}
				//re-check again, ensure the header buffer is full;
				if (header.remaining() == 0) {
					//decode the packet content.
					Packet packet = Packet.decode(new DataInputStream(
							new ByteBufferInputStream(header)));
					context.set
				}	
					
				
				
			}
		}
		
		/**
		 * Read the packet header into the header buffer.
		 * 
		 * @return true if the read succeeded, false if there was an error
		 * or the read length lower the packet header length.
		 */
		private boolean channelRead() {
			try {
				if ((transceiver.readBuffer(header)) 
						 < PacketHeader.PACK_HEADER_SIZE) {
					return false;
				}
				return true;
				
				do {
					transceiver.readBuffer(header);
				} 
			} catch (IOException e) {
				LOGGER.warn("get an IOException in channelRead!", e);
				return false;
			}
		}
		
	}
}
