package com.sunchao.rpc.common.serializable.support.java;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * compacted java object output stream.
 * which extends the class {@link java.io.ObjectOutputStream}.
 * @author sunchao
 *
 *@see java.io.ObjectStreamField.
 *@see java.io.ObjectStreamClass.
 */
public class CompactedObjectOutputStream extends ObjectOutputStream {

	public CompactedObjectOutputStream(OutputStream os) throws IOException {
		super(os);
	}
	
	
	/**
	 * @see java.io.ObjectStreamField;
	 * @see java.io.ObjectStreamClass;
	 * <p>
	 * The class includes the class name and the serialVersionUID 
	 * and the instance member that used in serialization.
	 * </p> 
	 * <p>
	 * The<code>ObjectOutputStream</code> default write the whole
	 * class name and serialVersionUID, ObjectStreamField array
	 * which contains the length of the array, every member 
	 * 's type code (ZBCSIFDJL) of array, and if the member
	 * is not primitive type, meanwhile it will write the type
	 * string. And class only write the whole class name, if the
	 * member is object type.
	 * </p>
	 * @see java.io.ObjectStreamClass #getSerialVersionUID();
	 * @see java.io.ObjectStreamClass #getName();
	 * @see java.io.ObjectStreamClass #getClass();
	 * @see java.io.ObjectStreamClass #getFields();
	 * @see java.io.ObjectStreamField #getName();
	 * @see java.io.ObjectStreamField #getTypeCode();
	 * @see java.io.ObjectStreamField #getTypeString();
	 * 
	 */
	@Override
	protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException
	{
		Class<?> clazz = desc.forClass(); // get the serialized the class instance.
		if (clazz.isPrimitive() || clazz.isArray()) // if the class is primitive type and array type.
		{
			write(0); // distinguish the primitive type, array type and the object type,
			super.writeClassDescriptor(desc); // if the class is primitive type or the array type, directly call the super
		} // class to handle.
		else 
		{
			write(1);  // if the class is object type, and write the whole class name.
			writeUTF(desc.getName());
		}
	}

}
