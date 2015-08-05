package com.sunchao.rpc.base.transport.peer.support.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.MessageType;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.metadata.PacketHeader;
import com.sunchao.rpc.base.metadata.Request;
import com.sunchao.rpc.base.metadata.Response;
import com.sunchao.rpc.base.serializer.Context;
import com.sunchao.rpc.base.serializer.SerializationFactory;
import com.sunchao.rpc.base.serializer.Serializer;
import com.sunchao.rpc.base.serializer.TSerializer;
import com.sunchao.rpc.base.transport.Channel;
import com.sunchao.rpc.base.transport.Codec;
import com.sunchao.rpc.base.transport.mock.MockChannel;
import com.sunchao.rpc.common.ClientConfig;
import com.sunchao.rpc.common.extension.HotSwapLoader;
import com.sunchao.rpc.common.io.ByteBufferOutputStream;
import com.sunchao.rpc.common.io.ByteHelper;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * encode/decode
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class PeerCodec implements Codec {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerCodec.class);
	
	protected static final int PACKET_HEADER_LENGTH = 19; // packet-header-length
	//3
	protected static final byte[]      MAGIC               =      new byte[]{0x72, 0x70, 0x60}; //"rpc"
	//2
	protected static final short      VERSION              =      (short)1;
	
	/****************************************************************************/
	protected static final int        ST_SHIFT             =           7;
	
	protected static final int        MT_SHIFT             =           5;
	
	protected static final byte       SE_MASK              =         (byte) 0x1F;
	
	protected static final byte       MT_MASK              =         (byte) 0x60;
	
	protected static final byte       ST_MASK              =         (byte) 0x80;
	
	protected static final byte       HB_SIGN              =         (byte) 0x80;
	
	/*****************************************************************************/
	
	protected static final byte       REQUEST              =       MessageType.REQUEST;
	
	protected static final byte        ONEWAY              =       MessageType.ONEWAY;
	
	protected static final byte         REPLY              =       MessageType.REPLY;
	
	/**
	 * set the common message or heart beat flag at the
	 * packet header, so that can remove the packet body part
	 * when the message is heart beat.
	 */
	
	
	protected static final byte        MESSAGE              =             0x00;
	
	protected static final byte      HEARTBEAT              =             0x01;
	
	/**
	 * used in the client-side.
	 */
	protected static final byte      STATUS_CODE_NULL       =             0x11;
	
	protected static final byte          ZERO_PADDING       =             0x00;
	/**
	 * serialization type.
	 * why set the serialization type in the packet header.
	 * it's used to support the more serialization scheme.
	 * and give the receive-side a change to know the send-side's
	 * Serialization type, and can dynamic select the same type
	 * to deserialize the message, but in generic the serialization
	 * schema is set before the system running in the configuration.
	 * so there exists only has reminder function. 
	 * 
	 */
	
	protected static final byte           EMPTY              =          0x08;
	
	protected static final byte            AVRO              = SerializationFactory.SERIALIZER_AVRO;
	
	protected static final byte          THRIFT              = SerializationFactory.SERIALIZER_THRIFT;
	
	protected static final byte         HESSIAN              = SerializationFactory.SERIALIZER_HESSION;
	
	protected static final byte     JAVA_NATIVE              = SerializationFactory.SERIALIZER_JAVA;
	
	protected static final byte        PROTOBUF              = SerializationFactory.SERIALIZER_PROTOBUF;
	
	protected static final byte            KRYO              = SerializationFactory.SERIALIZER_KRYO;
	
	protected static final byte            JSON              = SerializationFactory.SERIALIZER_JSON;
	
	protected static final byte          VARINT              = SerializationFactory.SERIALIZER_VARINT;
	
	/*************************************************************************************************/
	protected static final int PACKET_LENGTH_INDEX =  15;
	
	protected byte[] packetCheckBuf = new byte[19];
	
	protected Serializer serializer;
	
	protected Object result;
	
	/**
	 * @return the result
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}

	/**
	 * @return the request
	 */
	public boolean isRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(boolean request) {
		this.request = request;
	}

	protected boolean request;
	
	//encode.
	public void encode(Channel channel, ByteBuffer buffer, Object msg, Context context) throws Exception {
	    if (msg instanceof Request) {
			encodeRequest(channel, buffer, (Request) msg, context);
		} else if (msg instanceof Response) {
			encodeResponse(channel, buffer, (Response) msg, context);
		} else {
			throw new RPCException("the message format non supported.");
		}
	}
	
	/**
	 * when the message is an heart beat packet, we just send a packet header, and the
	 * {@code #HEARTBEAT} flag with be set, and receive-side can pick it up, also the slot
	 * that should write the packet length now use to write the message id, intend to just send
	 * a packet header, we take the packet length(original 4 bytes) in the packet header,
	 * now expand to 8 bytes(because the request id need 8 bytes), and take the unique message
	 * id, system can unique recognize the heart beat.
	 * 
	 * @param channel
	 * @param buffer
	 * @param header
	 * @throws IOException
	 */
	@Deprecated
	protected void encodePacketHeader(Channel channel, ByteBuffer buffer, PacketHeader header) throws IOException {
		byte[] packetHeader = new byte[PACKET_HEADER_LENGTH];
		//magic number
		for (int i = 0; i < MAGIC.length; i++) 
			packetHeader[i] = MAGIC[i]; //3 bytes
		packetHeader[MAGIC.length] = (byte)(VERSION >> 8);
		packetHeader[MAGIC.length + 1] = (byte) VERSION;
		//because just only header, so the serialization type flag EMPTY.
		packetHeader[MAGIC.length + 2] = (byte) (EMPTY | (REQUEST << MT_SHIFT)
				                                       |  HB_SIGN);
		long messageId = header.getPacketLen(); //here the packet length just load the message unique long id == Request.nextLongId();
		for (int i = 0, index = MAGIC.length + 3; i < 8; i++, index++) {
			packetHeader[index] = (byte) (messageId >>> ((7 - i) * 8));
		}
		buffer.put(packetHeader);
	}
	
	/**
	 * When the message is an request packet, we need send the extra actual data for the message.
	 * {@see com.sunchao.rpc.base.metadata.Request}.
	 * 
	 * @param channel the channel.
	 * @param buffer  the byte buffer.
	 * @param msg     the request message.
	 * @param context  the serialization context.
	 * @throws Exception 
	 * @throws RPCSerializationException 
	 */
   
	protected void encodeRequest(Channel channel, ByteBuffer buffer, Request msg, Context context) throws Exception {
		
    	byte[] packetHeader = new byte[PACKET_HEADER_LENGTH];
    	for (int i = 0; i < MAGIC.length; i++) {
			packetHeader[i] = MAGIC[i]; //3 bytes
    	}
    	//version
		packetHeader[MAGIC.length] = (byte)(VERSION >> 8);
		packetHeader[MAGIC.length + 1] = (byte) VERSION;
		//service type | message type | serialization type.
        if (msg.isHeartBeatFlag()) {
        	packetHeader[MAGIC.length + 2] = (byte) (EMPTY | (REQUEST << MT_SHIFT)
        			                                       |  HB_SIGN);
        } else { // common request message.
        	if (serializer == null) {
        	    serializer = getSerializer(channel);
        	}
        	if (msg.isOneway()) { //one way.
        		packetHeader[MAGIC.length + 2] = (byte) (serializer.getIdentifyId() | (ONEWAY << MT_SHIFT)
        				                                                            |  MESSAGE);
        	} else { //two way.
        		packetHeader[MAGIC.length + 2] = (byte) (serializer.getIdentifyId() | (REQUEST << MT_SHIFT)
        				                                                            |  MESSAGE);
        	}	       
        }
        //status code.
        packetHeader[MAGIC.length + 3] = STATUS_CODE_NULL;
        //message id.
        long messageId = msg.getId();
        for (int i = 0, index = MAGIC.length + 4; i < 8; i++, index++) {
			packetHeader[index] = (byte) (messageId >>> ((7 - i) * 8));
		}
        //packet body size; placeholder.
        for (int i = PACKET_LENGTH_INDEX; i < PACKET_HEADER_LENGTH; i++) {
        	packetHeader[i] = ZERO_PADDING;
        }
       // buffer.put(packetHeader);
        //common request message
        //int dataLen = 0;
        ByteBuffer buf = null;
        if (! msg.isHeartBeatFlag()) {
            buf = serializer.serialize(msg.getData(), context, channel.getConfig());
        	buf.flip();
        	int dataLen = buf.limit();
        	System.out.println(dataLen);
        	for (int i = PACKET_LENGTH_INDEX, j = 0; i < PACKET_HEADER_LENGTH; i++, j++) {
        		packetHeader[i] = (byte) (dataLen >> ((3 - j) * 8));
        	}
        }
       buffer.clear();
       buffer.put(packetHeader);
       if (buf != null) 
           buffer.put(buf);
       buffer.flip();
        
	/*	TSerializer serializer = getSerializer(channel);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream();
		byte[] packetHeader = new byte[PACKET_HEADER_LENGTH];
		for (int i = 0; i < MAGIC.length; i++)
			packetHeader[i] = MAGIC[i];//3 bytes.
		packetHeader[MAGIC.length] = (byte) (VERSION >> 8); //version 2 bytes
		packetHeader[MAGIC.length + 1] = (byte) VERSION; 
		packetHeader[MAGIC.length + 2] = (byte) (serializer.getIdentifyId() | (REQUEST << MT_SHIFT) //serialization type, message type, service type.
				                                                            |  MESSAGE);
		//packet length.
		long max = Long.MAX_VALUE;
		//placeholder, there the packet length are not sure, so just make room for here,
		//and later write the real value again.
		for (int i = 0, index = MAGIC.length + 3; i < 8; i++, index++) {
			packetHeader[index] = (byte) (max >> ((7 - i) * 8));
		}
		bbos.write(packetHeader);
		ByteBuffer buf = serializer.serialize(msg, context);
		buf.flip();
		bbos.write(buf.array(), buf.arrayOffset() + buf.position(), buf.limit());
		int packetLen = bbos.size();
		bbos.setWireTag(PACKET_LENGTH_INDEX);
		for (int i = 0; i < 8; i++) {
			bbos.write((byte)(((long) packetLen) >> ((7 - i) *  8)));
		}
		bbos.resetWriteTag();
		buffer.wrap(bbos.toByteArray());*/
	}
    
    /**
     * When the message is an response, we need send the extra actual data for the message.
     * {@see com.sunchao.rpc.base.metadata.Response}.
     * 
     * @param channel
     * @param buffer
     * @param msg
     * @param context
     * @throws IOException 
     */
	protected void encodeResponse(Channel channel, ByteBuffer buffer, Response msg, Context context) throws Exception {
		try {
			
			byte[] packetHeader = new byte[PACKET_HEADER_LENGTH];
			for (int i = 0; i < MAGIC.length; i++) {
				packetHeader[i] = MAGIC[i]; //3 bytes
			}
	    	//version
			packetHeader[MAGIC.length] = (byte)(VERSION >> 8);
			packetHeader[MAGIC.length + 1] = (byte) VERSION;
			//service type | message type | serialization type.
			if (msg.isHeartBeatFlag()) {
				packetHeader[MAGIC.length + 2] = (byte) (EMPTY | (REPLY << MT_SHIFT)
						                                       |  HB_SIGN);
			} else {
				if (serializer == null) {
				    serializer = getSerializer(channel);
				}
				packetHeader[MAGIC.length + 2] = (byte) (serializer.getIdentifyId() | (REPLY << MT_SHIFT)
						                                                            |  MESSAGE);
			}
			byte status_code = msg.getStatus_code();
			//status code.
			packetHeader[MAGIC.length + 3] = status_code;
			  //message id. there is equals request id.
	        long messageId = msg.getrId();
	        for (int i = 0, index = MAGIC.length + 4; i < 8; i++, index++) {
				packetHeader[index] = (byte) (messageId >>> ((7 - i) * 8));
			}
	        //packet body size; placeholder.
	        for (int i = PACKET_LENGTH_INDEX; i < PACKET_HEADER_LENGTH; i++) {
	        	packetHeader[i] = ZERO_PADDING;
	        }
	        ByteBuffer buf = null;
	        if (status_code == Response.OK) { //status oK ,only the response need return the invocation result, heart beat just return 
	        	if (! msg.isHeartBeatFlag()) { //the status_code ok,
	        		if (serializer == null) {
	        		    serializer = getSerializer(channel);
	        		}
	        		buf = serializer.serialize(msg.getResult(), context, channel.getConfig());
	        		buf.flip();
	        		int dataLen = buf.limit();
	            	for (int i = PACKET_LENGTH_INDEX, j = 0; i < PACKET_HEADER_LENGTH; i++, j++) {
	            		packetHeader[i] = (byte) (dataLen >> ((3 - j) * 8));
	            	}
	        	}
	        } else { //has error, both response and the heart beat return the error_message_information(string).
	        	if (serializer == null) {
	            	serializer = getSerializer(channel);
	        	}
	        	buf = serializer.serialize(msg.getErrorMsg(), context, channel.getConfig());
	        	buf.flip();
	        	int dataLen = buf.limit();
            	for (int i = PACKET_LENGTH_INDEX, j = 0; i < PACKET_HEADER_LENGTH; i++, j++) {
            		packetHeader[i] = (byte) (dataLen >> ((3 - j) * 8));
            	}
	        }
	        buffer.clear();
	        buffer.put(packetHeader);
	        buffer.put(buf);
	        buffer.flip();
			
		} catch (Throwable t) {
			if (! msg.isHeartBeatFlag() && msg.getStatus_code() != Response.BAD_RESPONSE) {
				try {
					LOGGER.warn("Fail to encode repsonse: " + msg + ", send bad_ response info instead, cause: " + t.getMessage(), t);
					
					Response r = new Response(msg.getrId());
					r.setStatus_code(Response.BAD_RESPONSE);
					r.setErrorMsg("Failed to send response : " + msg + ", cause: " + t.getMessage());
					channel.send(r);
					return;
				} catch (RPCException e) {
					LOGGER.warn("Failed to send bad_response info back: " + msg + ", cause: " + e.getMessage(), e);
				}
			}
			
			if (t instanceof IOException) {
				throw (IOException) t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else if (t instanceof Error) {
				throw (Error) t;
			} else {
				throw new RuntimeException(t.getMessage(), t);
			}
		}
	}
	
	/**
	 * Decode the byte buffer of the packet header.
	 * 
	 * @param channel the underlying channel.
	 * @param buffer  the data buffer which need decoded.
	 * @return the packet body length.
	 * @throws IOException the exception who deal with?
	 */
	public void decodePacketHeader(Channel channel, ByteBuffer buffer) throws IOException {
		if (buffer == null) 
			throw new IOException("the packet header buffer cannot be null.");
		buffer.flip();
		
		//packet header length check.
		if (buffer.limit() != PACKET_HEADER_LENGTH)
			throw new IOException("the data length of buffer lower than the packet heander length, the data length : " + buffer.limit());
		buffer.get(packetCheckBuf);
		
		//magic check.
		if (packetCheckBuf[0] != MAGIC[0] || packetCheckBuf[1] != MAGIC[1]
				|| packetCheckBuf[2] != MAGIC[2])
			throw new IOException("Illegal packet header magic: " + new String(packetCheckBuf,0,3));
		
		//version check.
		short version = (short) (packetCheckBuf[3] << 8 | packetCheckBuf[4]);
		if (version != VERSION)
			throw new IOException("Illegal version: " + version + ", and the correct VERSION: " + VERSION);
		
		//status code.
		byte status_code = packetCheckBuf[6];
		
		//message id
		long messageId = 
		       ((((long) packetCheckBuf[7]     & 0xFFL) << 56) |
				(((long) packetCheckBuf[8]     & 0xFFL) << 48) |
				(((long) packetCheckBuf[9]     & 0xFFL) << 40) |
				(((long) packetCheckBuf[10]    & 0xFFL) << 32) |
				(((long) packetCheckBuf[11]    & 0xFFL) << 24) |
				(((long) packetCheckBuf[12]    & 0xFFL) << 16) |
				(((long) packetCheckBuf[13]    & 0xFFL) << 8)  |
				(((long) packetCheckBuf[14]    & 0xFFL) ));
		
		//packet body size
		int packetBodyLen = (((packetCheckBuf[PACKET_LENGTH_INDEX]     & 0xFF) << 24) |
				             ((packetCheckBuf[PACKET_LENGTH_INDEX + 1] & 0xFF) << 16) |
				             ((packetCheckBuf[PACKET_LENGTH_INDEX + 2] & 0xFF) <<  8) |
				             ((packetCheckBuf[PACKET_LENGTH_INDEX + 3] & 0xFF)     ));
		
		//service type
		byte serviceType = (byte) (((byte) ( packetCheckBuf[5] & ST_MASK )) >>> ST_SHIFT);
		
		//message type
		byte messageType = (byte) (((byte) ( packetCheckBuf[5] & MT_MASK )) >>> MT_SHIFT);
		
		//serialization type.
		byte serialiType = (byte) (packetCheckBuf[5] & SE_MASK);
		
		if ( messageType == REPLY ) { //reply
			request = false;
			Response response = new Response(messageId);
			response.setStatus_code(status_code);
			if (serviceType == HEARTBEAT) { //heart beat response.
				response.setHeartBeatFlag(true);	
			} else { //common reply.
				response.setHeartBeatFlag(false);
			}
			response.setPacketLen(packetBodyLen); //record the packet body size 
			response.setSerializerType(serialiType);
			result = response;
		} else { //request.
			request = true;
			Request request = new Request(messageId);
			if (messageType == ONEWAY) { //one way request.
				request.setHeartBeatFlag(false);
				request.setOneway(true);
			} else {
				request.setOneway(false);
				if (serviceType == HEARTBEAT) { // heart beat request.
					request.setHeartBeatFlag(true);
				} else {
					request.setHeartBeatFlag(false); //request
				}
			}
			request.setSerializerType(serialiType);
			request.setPacketLen(packetBodyLen);
			result = request;
		}
	}
	
	/**
	 * 
	 * decode the packet body with the specified data buffer.
	 * Note: the heart beat request/response already directly deal
	 * . So there are the body size not 0 .
	 * 
	 * @param channel the underlying channel.
	 * @param buffer the data buffer.
	 * @return  the decoded object.
	 * @throws IOException
	 * @throws RPCException 
	 * @throws RPCSerializationException 
	 */
	public Object decodePacketBody(Channel channel, ByteBuffer buffer, Context context) throws Exception {
		if (this.result == null)
			throw new IOException("The logic error. when the packet header decoded ahead.");
		buffer.flip();
		if (serializer == null)
			serializer = getSerializer(channel);
		if (request) {
			Request req = (Request) result;
			if (req.getPacketLen() != buffer.limit())
				throw new IOException("The packet body size not equals the packet header specified value.");
			Object obj = serializer.deserialize(buffer, context, channel.getConfig());
			req.setData(obj);
			return req;
		} else {
			Response resp = (Response) result;
			if (resp.getPacketLen() != buffer.limit())
				throw new IOException("The packet body size not equals the packet header specified value.");
            if (resp.getStatus_code() == Response.OK) {
            	Object obj = serializer.deserialize(buffer, context, channel.getConfig());
            	resp.setResult(obj);
            } else {
            	String errorMsg = serializer.deserialize(buffer, context, channel.getConfig());
            	resp.setErrorMsg(errorMsg);
            }
            return resp;
		}
	}
	
	
	public static Serializer getSerializer(Channel channel) {
		if (channel == null)
			throw new IllegalArgumentException("the channel cannot be null.");
		return getSerializer(channel.getConfig());
	}
	
	private static Serializer getSerializer(ClientConfig config) {
		return HotSwapLoader.getExtensionLoader(Serializer.class).getExtension(config.getParameterOrDefault("serializer", "varint"));
	}

	public static Serializer getSerializer() {
		 return HotSwapLoader.getExtensionLoader(Serializer.class).getAdaptiveExtension();
	}
	
	public static void main(String args[]) throws Exception {
	  /* // Serializer se = getSerializer();
		Request re1 = new Request();
		String data = "nihao";
	    Request re = new Request();
	  //  re.setHeartBeatFlag(true);
	    re.setOneway(true);
	    re.setData(re1);
	    PeerCodec codec = new PeerCodec();
	    ByteBuffer buffer = ByteBuffer.allocate(512);
	    codec.encode(new MockChannel(), buffer, re, new Context());
	   // byte[] buf = buffer.array();
	    //System.out.println(buffer.limit());
	    byte[] buf = new byte[buffer.limit()];
	    buffer.get(buf);*/
		Request re = new Request();
		Serializer  se = getSerializer();
		ByteBuffer buf = se.serialize(re, null, null);
		buf.flip();
	    System.out.println(buf.limit() + " : " + ByteHelper.bytes2hex(buf.array(), buf.arrayOffset() + buf.position() , buf.limit()));
	}
	
}
