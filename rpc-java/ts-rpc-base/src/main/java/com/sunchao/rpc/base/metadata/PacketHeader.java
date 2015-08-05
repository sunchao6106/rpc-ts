package com.sunchao.rpc.base.metadata;

import java.util.Arrays;

import com.sunchao.rpc.base.Config;
import com.sunchao.rpc.base.MessageType;
import com.sunchao.rpc.base.serializer.SerializationFactory;

/**
 * /**
 * </p>The message packet include the message header(use extends) and the message body</p>
 * 
 * </p>the message header 12 bytes,as following : </p> </p>
 * 
 * </p>Offset: 0             2           4       5       6       7        8          15   </p> 
 * </p>      : +-------------+-----------+-------+-------+-------+--------+--------------+</p>
 *             |             |           |       |       |       |        |              |
 * </p>      : +--mn---------+----vn-----+--mt---+---mt--+---st--+--st----+------pl------+</p>
 *             |             |           |       |       |       |        |              |   
 * </p>      : +-------------+-----------+-------+-------+-------+--------+--------------+ 
 * 
 * <pre>
 * |<-                             message header                                            ->|<- message body->|
 * +----------------+-----------------+---------------+-------------+--------------+-----------+-----------------+ 
 * | magic(3 bytes) | version(2 bytes)|serializer type|message type | service type |packet len |                 |                                                    |                    |
 * +----------------+-----------------+---------------+-------------+--------------+-----------+-----------------+
 * |<-                                             message size                                                ->|
 * </pre>
 * 
 * 
 * </p>a message packet may has more body chunks, as following:</p> 
 * 
 * </p> Offset: 0        3             (Length + 4)</p> 
 * </p>         +--------+------------------------+</p> 
 * </p> Fields: | Length | Actual message content |</p>
 * </p>         +--------+------------------------+</p>
 * 
 * 
 * The message header, which has the delimited length of 12.
 * More detail, please @see {@link Packet}
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class PacketHeader {
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PacketHeader [messageType=" + messageType + ", serializerType="
				+ serializerType + ", serviceType=" + serviceType
				+ ", version=" + version + ", magic=" + Arrays.toString(magic)
				+ ", packetLen=" + packetLen + "]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(magic);
		result = prime * result + messageType;
		result = prime * result + (int) (packetLen ^ (packetLen >>> 32));
		result = prime * result + serializerType;
		result = prime * result + serviceType;
		result = prime * result + version;
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PacketHeader other = (PacketHeader) obj;
		if (!Arrays.equals(magic, other.magic))
			return false;
		if (messageType != other.messageType)
			return false;
		if (packetLen != other.packetLen)
			return false;
		if (serializerType != other.serializerType)
			return false;
		if (serviceType != other.serviceType)
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	/**
	 * @return the version
	 */
	public short getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(short version) {
		this.version = version;
	}

	/**
	 * @return the magic
	 */
	public byte[] getMagic() {
		return magic;
	}

	/**
	 * @param magic the magic to set
	 */
	public void setMagic(byte[] magic) {
		this.magic = magic;
	}

	public void setServiceType(byte serviceType) {
	    this.serviceType = serviceType;	
	}
	
	public byte getServiceType() {
		return this.serviceType;
	}
	
	
	public int getPacketLen() {
		return this.packetLen;
	}
	

	public byte getMessageType() {
		return messageType;
	}

	public void setMessageType(byte messageType) {
		this.messageType = messageType;
	}

	public byte getSerializerType() {
		return serializerType;
	}

	public void setSerializerType(byte serializerType) {
		this.serializerType = serializerType;
	}

     
	/**
	 * The delimited length packet header.
	 */
	public static final int PACK_HEADER_SIZE = 12;
	
	/**the protocol magic-number*/
	public static final byte[] MAGIC_NUMBER = Config.RPC_MAGIC; 
	
	/** the rpc protocol version  */
	public static final short VERSION = Config.RPC_VERSION;
	
	/**
	 * the message type, which has four status. And there use one
	 * byte to contain the message type
	 * default use the {@link MessageType #REQUEST}
	 * {@link MessageType #ONEWAY}
	 * {@link MessageType #REPLY}
	 * {@link MessageType #EXCEPTION}
	 * {@link MessageType #REQUEST}
	 * 
	 */
	protected byte messageType = MessageType.REQUEST;
	
	/**
	 * the serializer type,
	 * default use the {@link SerializationFactory #SERIALIZER_HESSION}
	 */
	protected byte serializerType = SerializationFactory.SERIALIZER_VARINT;
	
	/**
	 * The serviceType (byte) 0x00 the common communication data.
	 * The serviceType (byte) 0x01 the heart beat packet.
	 */
	
	public static final byte HEART_BEAT = 0x01;
	
	public static final byte MESSAGE = 0x00;
    
	/**the service process type. */
	protected byte serviceType = (byte) 0x00;
	
	protected short version;
	
	protected byte[] magic;

	/** the message chunk size */
	protected int packetLen;

	/**
	 * @param packetLen the packetLen to set
	 */
	public void setPacketLen(int packetLen) {
		this.packetLen = packetLen;
	}
}
