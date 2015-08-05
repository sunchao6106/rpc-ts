package com.sunchao.rpc.base.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.avro.Protocol;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

import com.sunchao.rpc.base.exception.RPCApplicationException;
import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.common.utils.HolderUtil;

/**
 * Avro Serilaier Helper.
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class AvroSerializerHelper {
	
	/**
	 * The hash map use to store the avro rpc protocol.
	 */
	private static final ConcurrentMap<String, HolderUtil<Protocol>> PROTOCOL_STORE =
			new ConcurrentHashMap<String, HolderUtil<Protocol>>();

	private static final int ED_BUFFER_SIZE = 4096;
	private static final EncoderFactory ENCODE_FACTORY;
	private static final DecoderFactory DECODE_FACTORY;
	
    static {
    	ENCODE_FACTORY = new EncoderFactory().configureBufferSize(ED_BUFFER_SIZE);
    	DECODE_FACTORY = new DecoderFactory().configureDecoderBufferSize(ED_BUFFER_SIZE);
    }
    
    /**
     * use the <code>ThreadLocal</code> to reuse the encoder/decoder. 
     */
    private static final ThreadLocal<Encoder> ENCODER_CACHE = new ThreadLocal<Encoder>();
    private static final ThreadLocal<Decoder> DECODER_CACHE = new ThreadLocal<Decoder>();
    
    
    /**
     * Reuse the encoder with instead the <code>OutputStream</code>
     * 
     * @param output
     *            the encode output stream.
     * @return
     *        the new encoder when <code>null</code>, or the reused encoder. 
     */
    public static Encoder getEncoder(OutputStream output) {
    	Encoder encoder = ENCODER_CACHE.get();
    	if (encoder == null) {
    		encoder = ENCODE_FACTORY.binaryEncoder(output, null);
    	} else {
    		encoder = ENCODE_FACTORY.binaryEncoder(output, (BinaryEncoder)encoder);
    	}
    	ENCODER_CACHE.set(encoder);
    	return encoder;
    }
    
    /**
     * 
     * @param data
     *         the input byte array.
     * @return
     *         the new decoder when <code>null</code>, or the reused decoder. 
     */
    public static Decoder getDecoder(byte[] data) {
    	Decoder decoder = DECODER_CACHE.get();
    	if (decoder == null) {
    		decoder = DECODE_FACTORY.binaryDecoder(data, null);
    	} else {
    		decoder = DECODE_FACTORY.binaryDecoder(data, (BinaryDecoder)decoder);
    	}
    	DECODER_CACHE.set(decoder);
    	return decoder;
    }
    
    
	/**
	 * Client call to register the protocol and store it, when to be reused next time.
	 * Because create the protocol is very expensive. And the avro rpc need to exchange the 
	 * schema in the handshake.
	 * 
	 * @param protocolName
	 *                   The register protocol name.
	 * @param filepath
	 *                   The protocol file(.avpr) storage.
	 * @return
	 * @throws IOException
	 * @throws RPCException 
	 */
	public static Protocol registerProtocol(final String protocolName, final String filepath) 
			throws IOException, RPCException {
		if (protocolName == null || protocolName.length() == 0 || filepath == null || filepath.length() == 0) {
			throw new RPCException( "Error when register Protocol with the avroSerializer, because the protocolName or"
					     + " the .avpr file path is null or length is invalid!");
		}
			
		ClassLoader loader = AvroSerializerHelper.class.getClassLoader();
		if (loader == null)
			loader = Thread.currentThread().getContextClassLoader();
		HolderUtil<Protocol> holder = PROTOCOL_STORE.get(protocolName);
		if (holder == null) {
			 holder = new HolderUtil<Protocol>();
			 HolderUtil<Protocol> oldHolder = PROTOCOL_STORE.putIfAbsent(protocolName, holder);
			 if (oldHolder != null) {
				 holder = oldHolder;
			 }
		} 
		Protocol protocol = holder.get();
		if (protocol == null) {
			synchronized (holder) {
				protocol = holder.get();
				if (protocol == null) {
					protocol = Protocol.parse(loader.getResourceAsStream(filepath));
					holder.set(protocol);
				}
			}
		}
		return protocol;
	}
	
	
	/**
	 * 
	 * @param protocolName
	 * @return
	 */
   public static synchronized Protocol getProtocol(final String protocolName) {
	   return PROTOCOL_STORE.get(protocolName).get();
   }
   
   private AvroSerializerHelper() {}
}
