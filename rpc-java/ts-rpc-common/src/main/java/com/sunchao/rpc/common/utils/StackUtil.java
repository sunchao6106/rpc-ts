package com.sunchao.rpc.common.utils;

import java.lang.reflect.Array;

import com.sunchao.rpc.common.annotation.Utility;

/**
 * Stack Utility.
 * 
 * @author sunchao
 *
 */

@Utility
public class StackUtil<T> {

	private static final int DEFAULT_INIT_CAPACITY = 20;
	
	private int top = -1;
	private Class<T> cls;
	private T[] vector;
	
	
	public StackUtil(Class<T> cls)
	{
		this(cls, DEFAULT_INIT_CAPACITY);
	}
	
	@SuppressWarnings("unchecked")
	public StackUtil(Class<T> cls, int capacity)
	{
		if (capacity <= 0)
		{
			throw new IllegalArgumentException("invalid argument: \"capacity\" ");
		}
		this.cls = cls;
		this.vector = (T[]) Array.newInstance(cls, capacity);
	}
	
	@SuppressWarnings("unchecked")
	private void growSelf()
	{
		T[] newVector = (T[]) Array.newInstance(cls, vector.length << 1);
		System.arraycopy(vector, 0, newVector, 0, vector.length);
		this.vector = newVector;
	}
	
	public void push(T element)
	{
		if (this.vector.length == top + 1)
		{
			growSelf();
		}
		this.vector[++top] = element;
	}
	
	public T pop()
	{
		if (top < 0)
		{
			throw new IllegalStateException("the stack is empty!");
		}
		return vector[this.top--];
	}
	
	public T peek()
	{
		if (top < 0)
		{
			throw new IllegalStateException("the stack is empty!");
		}
		return vector[this.top];
		
	}
	
	public int size()
	{
		return this.top + 1;
	}
	
	public void clear()
	{
		this.top = -1;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("The stack <" + cls.getSimpleName() + ">[");
		for (int i = 0; i < vector.length; i++)
		{
			if (i != 0)
			{
				sb.append(", ");
			}
			if (i == this.top)
			{
				sb.append("<<");
			}
			sb.append(vector[i]);
			if (i == this.top)
			{
				sb.append(">>");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
