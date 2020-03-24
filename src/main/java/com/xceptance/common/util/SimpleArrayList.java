package com.xceptance.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SimpleArrayList<T> implements List<T>
{
    private Object[] data;
    private int size;
    
    public SimpleArrayList(int capacity)
    {
        data = new Object[capacity];
    }
    
    public boolean add(T o)
    {
        final int length = data.length;
        if (size == length)
        {
            final Object[] newData = new Object[data.length << 1];
            System.arraycopy(data, 0, newData, 0, length);
            data = newData;
        }

        data[size] = o;
        size++;
        
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        return (T) data[index];
    }
    
    public int size()
    {
        return size;
    }
    
    @SuppressWarnings("unchecked")
    public T[] toArray() 
    {
        return (T[]) Arrays.copyOf(data, size);
    }

    @Override
    public boolean isEmpty()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(Object o)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<T> iterator()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(Object o)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public T set(int index, T element)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void add(int index, T element)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public T remove(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int indexOf(Object o)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int lastIndexOf(Object o)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ListIterator<T> listIterator()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
