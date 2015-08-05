package com.sunchao.rpc.base.serializer;

import java.util.List;

import org.junit.Test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sunchao.rpc.base.serializer.PersonProto.Person;
import com.sunchao.rpc.base.serializer.PersonProto.Person.PhoneNumber;

public class TestPersonProto {
		
	    @Test
		public void test() {
		     PersonProto.Person.Builder builder = PersonProto.Person.newBuilder();
		     builder.setEmail("sunchao6106@163.om");
		     builder.setId(1);
		     builder.setName("tomsun");
		     builder.addPhone(PersonProto.Person.PhoneNumber.newBuilder().setNumber("136111111111").
		    		 setType(Person.PhoneType.WORK));
		     builder.addPhone(PersonProto.Person.PhoneNumber.newBuilder().setNumber("0532xxxxx").
		    		 setType(Person.PhoneType.HOME));
		     Person person = builder.build();
		     byte[] buf = person.toByteArray();
		     
		     try {
				Person person1 = PersonProto.Person.parseFrom(buf);
				System.out.println("person email: " + person1.getEmail());
				System.out.println("person id: " + person1.getId());
				System.out.println("person name: " + person1.getName());
				System.out.println("<****************************************************************************>");
				List<PhoneNumber> list  = person1.getPhoneList();
				for (PhoneNumber pm : list)
				{
					System.out.print("Phone Type: " + pm.getType() + " , ");
					System.out.print("Phone Number: " + pm.getNumber());
					System.out.println();
				}
				System.out.println(person1 == person);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
