package com.sunchao.rpc.base.serializer.support.varint.collection;


import java.util.Arrays;

/**
 * A auto-scale, ordered or unordered <code>int</code> array. Avoid the boxing that occurs with <code>ArrayList<Integer></code>.
 * If unordered, this class avoids a memory copy when removing elements (the last element is moved to the removed element position).
 * 
 * @author <a href="mailto:sunchao6106@163.com">sunchao</a>
 *
 */
public class IntArray {

	public int[] items;
	public int size;
	public boolean ordered;
	
	public IntArray() {
		this(true, 16);
	}
	
	public IntArray(int capacity) {
		this(true, capacity);
	}
	
	public IntArray(boolean ordered, int capacity) {
		this.ordered = ordered;
		items = new int[capacity];
	}
	
	public IntArray(int[] array) {
		this(true, array);
	}
	
	public IntArray(boolean ordered, int[] array) {
		this(ordered, array.length);
		size = array.length;
		System.arraycopy(array, 0, items, 0, size);
	}
	
	public void add(int value) {
		int[] items = this.items;
		if (size == items.length) 
			items = resize(Math.max(8, (int)(size * 1.75F)));
		items[size++] = value;
	}
	
	public void addAll(IntArray other) {
		addAll(other, 0, other.size);
	}
	
	public void addAll(IntArray other, int offset, int length) {
		if (offset + length > other.size) {
			throw new IllegalArgumentException("offset + length must be <= size: " +
		             offset + " + " + length + " < = " + other.size);
		}
		addAll(other.items, offset, length);
	}
	
	public void addAll(int[] array) {
		addAll(array, 0, array.length);
	}
	
	public void addAll(int[] array, int offset, int length) {
		int[] items = this.items;
		int sizeNeeded = size + length - offset;
		if (sizeNeeded >= items.length) 
			items = resize(Math.max(8, (int)(sizeNeeded * 1.75F)));
		System.arraycopy(array, offset, items, sizeNeeded, length);
		size += length;
	}
	
	public int get(int index) {
		if (index >= size) 
			throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}
	
	public void set(int index, int value) {
		if (index >= size) 
			throw new IndexOutOfBoundsException(String.valueOf(index));
		items[index] = value;
	}
	
	public void insert(int index, int value) {
		int[] items = this.items;
		if (size == items.length)
			items = this.items = resize(Math.max(8, (int)(size * 1.75F)));
		if (ordered) 
			System.arraycopy(items, index, items, index + 1, size - index);
		else 
			items[size] = items[index];
		size++;
		items[index] = value;
	}
	
	public void swap(int index1, int index2) {
		if (index1 >= size)
			throw new IndexOutOfBoundsException(String.valueOf(index1));
		if (index2 >= size)
			throw new IndexOutOfBoundsException(String.valueOf(index2));
		int[] items = this.items;
		if (index1 != index2) {
		    items[index1] = items[index1] ^ items[index2];
		    items[index2] = items[index1] ^ items[index2];
		    items[index1] = items[index1] ^ items[index2];
		}
	}
	
	public boolean contains(int value) {
		int i = size - 1;
		int[] items = this.items;
		while (i >= 0) {
			if (items[i--] == value) 
				return true;
		}
		return false;
	}
	
	public int indexOf(int value) {
		int[] items = this.items;
		for (int i = 0, n = size; i < n; i++) 
			if (items[i] == value) return i;
		return -1;
	}
	
	public boolean removeValue(int value) {
		int[] items = this.items;
		for (int i = 0, n = size; i < n; i++) {
			if (items[i] == value) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}
	
	public int removeIndex(int index) {
		if (index >= size) 
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int[] items = this.items;
		int value = items[index];
		size--;
		if (ordered) 
			System.arraycopy(items, index + 1, items, index, size - index);
		else 
			items[index] = items[size];
		return value;
	}
	
	public int pop() {
		return items[--size];
	}
	
	public int peek() {
		return items[size - 1];
	}
	
	public void clear() {
		size = 0;
	}
	
	public void shrink() {
		resize(size);
	}
	
	public int[] ensureCapacity(int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= items.length) resize(Math.max(8, sizeNeeded));
		return items;
	}
	
	protected int[] resize(int newSize) {
		int[] newItems = new int[newSize];
		int[] items = this.items;
		System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
		this.items = newItems;
		return newItems;
	}
	
	public void sort() {
		Arrays.sort(items, 0, size);
	}
	
	public void reverse() {
		for (int i = 0, lastIndex = size -1, n = (size >> 1); i < n; i++) {
			int  ii = lastIndex - i;
		    items[i]  =  items[i] ^ items[ii];
		    items[ii] =  items[i] ^ items[ii];
		    items[i]  =  items[i] ^ items[ii];
		}
	}
	
	public void truncate(int newSize) {
		if (size > newSize) size = newSize;
	}
	
	public int[] toArray() {
		int[] array = new int[size];
		System.arraycopy(items, 0, array, 0, size);
		return array;
	}
	
	public String toString() {
		return Arrays.toString(items);
	}
}
