package com.xceptance.common.util;

/**
 * This class does not implement the CharBuffer of the JDK, but uses the idea of a shares
 * character array with views. This is also a very unsafe implementation with as little
 * as possible bound checks to achieve the maximum speed possible.
 * 
 * @author rschwietzke
 *
 */
public class XltCharBuffer
{
    private static final XltCharBuffer EMPTY = new XltCharBuffer(null);
    private static final String EMPTY_STRING = "";
    
    private final char[] src;
    private final int from;
    private final int length;
    
    public XltCharBuffer(final char[] src)
    {
        this.src = src;
        this.from = 0;
        this.length = src != null ? src.length : 0;
    }
    
    public XltCharBuffer(final char[] src, final int from, final int length)
    {
        this.src = src;
        this.from = from;
        this.length = length;
    }
    
    public char get(final int pos)
    {
        return src[from + pos];
    }

    public void put(final int pos, final char c)
    {
        src[from + pos] = c;
    }
    
    /**
     * Looks ahead, otherwise returns 0. Only safety bound against ahead misses, not 
     * any behind misses
     * 
     * @param pos the position to look at
     * @return the content of the peaked pos or 0 if this position does not exist
     */
    public char peakAhead(final int pos)
    {
        return from + pos < length ? get(pos) : 0;
    }
    
    public XltCharBuffer viewByLength(final int from, final int length)
    {
        return new XltCharBuffer(this.src, from, length);
    }

    public XltCharBuffer viewFromTo(final int from, final int to)
    {
        return new XltCharBuffer(this.src, from, to - from);
    }
    
    public static XltCharBuffer empty()
    {
        return EMPTY;
    }
    
    @Override
    public String toString()
    {
        return src == null ? EMPTY_STRING : String.valueOf(src, from, length);
    }
    
    public int length()
    {
        return length;
    }
}
