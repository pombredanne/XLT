package com.xceptance.common.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SimpleCacheAwareArrayList<T> implements List<T>
{
    private static final int SHIFT = 4;
    public static final int SUBARRAYSIZE = 2 << SHIFT;
    
    Object[] base;
    Object[] currentSlot;
    
    int size;

    int slot;
    int slotPos;
    
    SimpleCacheAwareArrayList(final SimpleCacheAwareArrayList<T> list)
    {
        base = list.base;
        size = list.size;
        slot = list.slot;
        slotPos = list.slotPos;
    }
    
    public SimpleCacheAwareArrayList(int capacity)
    {
        base = new Object[Math.max(1,  capacity >> SHIFT)];
        base[0] = new Object[SUBARRAYSIZE];
        currentSlot = (Object[]) base[0];
    }
    
    @Override
    public boolean add(T o)
    {
        if (slotPos == SUBARRAYSIZE)
        {
            slotPos = 0;
            slot++; 

            // worst case, we are totally full
            if (slot == base.length)
            {
                final int length = base.length;

                final Object[] newData = new Object[length << 1];
                System.arraycopy(base, 0, newData, 0, length);
                
                base = newData;
            }
            
            base[slot] = new Object[SUBARRAYSIZE];
            currentSlot = (Object[]) base[slot];
        }

        currentSlot[slotPos] = o;
        
        slotPos++;
        
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        int slotNo = index >> (SHIFT + 1);
        int slotPos = index & (SUBARRAYSIZE - 1);
        
        final Object[] slot = (Object[]) base[slotNo];
        return (T) slot[slotPos];
    }

    @Override
    public int size()
    {
        return (this.slot << (this.SHIFT + 1)) + slotPos;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T[] toArray() 
    {
        return null;
//        return (T[]) Arrays.copyOf(data, size);
    }
    
    /**
     * Returns view partitions on the underlying list. If the count is larger than size
     * you get back the maximum possible list number with one element each. If count 
     * is 0 or smaller, we correct it to 1.
     * 
     * @param count how many list do we want
     * @return a list of lists
     */
    public List<List<T>> partition(int count)
    {
        final int _count;
        if (count > size)
        {
            _count = size;
        }
        else
        {
            _count = count <= 0 ? 1 : count;
        }
        
        final SimpleCacheAwareArrayList<List<T>> result = new SimpleCacheAwareArrayList<>(count);
        
        final int newSize = (int) Math.ceil((double) size / (double) _count); 
        for (int i = 0; i < _count; i++)
        {
            int from = i * newSize;
            int to = from + newSize - 1;
            if (to >= size)
            {
                to = size - 1; 
            }
            result.add(new Partition<>(this, from, to));
        }
        
        return result;
    }
    
    class Partition<K> extends SimpleCacheAwareArrayList<K>
    {
        private final int from;
        private final int size;
        
        public Partition(final SimpleCacheAwareArrayList<K> list, final int from, final int to)
        {
            super(list);
            
            this.from = from;
            this.size = to - from + 1;
        }
     
        @Override
        public boolean add(K o)
        {
            throw new RuntimeException("Cannot modify the partition");
        }
        
        @Override
        public K get(int index)
        {
            return super.get(index + from);
        }

        @Override
        public int size()
        {
            return size;
        }
        
        @Override
        public K[] toArray() 
        {
            throw new RuntimeException("Cannot modify the partition");
        }
        
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
