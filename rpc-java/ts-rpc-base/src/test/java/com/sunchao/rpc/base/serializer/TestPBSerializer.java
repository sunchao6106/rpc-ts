package com.sunchao.rpc.base.serializer;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import com.sunchao.rpc.base.exception.RPCException;
import com.sunchao.rpc.base.exception.RPCSerializationException;
import com.sunchao.rpc.base.serializer.PersonProto.Person;
import com.sunchao.rpc.common.ClientConfig;

public class TestPBSerializer {
	private PersonProto.Person person = null;
	private PersonProto.Person.Builder builder = null; 
	private TSerializer serializer = null;
	private Context context = null;
	
	@Before
	public void testPrepareData() {
		 serializer = SerializationFactory.getSerializer(SerializationFactory.SERIALIZER_PROTOBUF);
		 context = new Context();
		 context.setRequest(true);
		 Class<?>[] arguments = new Class<?>[] {PersonProto.Person.class};
		 builder = PersonProto.Person.newBuilder();
	     builder.setEmail("sunchao6106@163.om");
	     builder.setId(1);
	     builder.setName("tomsun");
	     builder.addPhone(PersonProto.Person.PhoneNumber.newBuilder().setNumber("136111111111").
	    		 setType(Person.PhoneType.WORK));
	     builder.addPhone(PersonProto.Person.PhoneNumber.newBuilder().setNumber("0532xxxxx").
	    		 setType(Person.PhoneType.HOME));
	     person = builder.build();
	}
	
	@Test
	public void testSerializer() throws  Exception {
		 ByteBuffer buffer = serializer.serialize(person, context, new ClientConfig(null, 0,null , null));
		 Object obj = serializer.deserialize(buffer, context, new ClientConfig(null, 0,null , null));
		 System.out.println(person);
	}

}
