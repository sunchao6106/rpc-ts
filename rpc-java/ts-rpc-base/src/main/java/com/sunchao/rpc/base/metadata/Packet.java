package com.sunchao.rpc.base.metadata;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.common.logger.Logger;
import com.sunchao.rpc.common.logger.LoggerFactory;

/**
 * </p>The message packet include the message header(use extends) and the message body</p>
 * 
 * </p>the message header 12 bytes,as following : </p> </p>
 * 
 * </p>Offset: 0         2            6      7      8      9             13</p> 
 * </p>      : +---------+------------+------+------+------+--------------+</p>
 *             |         |            |      |      |      |              |
 * </p>      : +--mn-----+-----msn----+--st--+--mt--+--st1-+--------bs----+</p>
 *             |         |            |      |      |      |              |   
 * </p>      : +---------+------------+------+------+------+--------------+ 
 * 
 * <pre>
 * |<-                             message header                                                 ->|<- message body->|
 * +----------------+----------------------+---------------+-------------+--------------+-----------+-----------------+ 
 * | magic(3 bytes) |meg serial id(4 bytes)|serializer type|message type | service type |body count |                 |                                                    |                    |
 * +----------------+----------------------+---------------+-------------+--------------+-----------+-----------------+
 * |<-                                             message size                                                     ->|
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
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
@Deprecated
public class Packet extends PacketHeader {
	
}
