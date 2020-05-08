package com.xceptance.common.lang;

import com.xceptance.common.util.XltCharBuffer;

/**
 * This is a small helper class for parsing strings and converting them into longs. This implementation is optimized for
 * speed not for functionality. It is only able to parse plain numbers with base 10, e.g. 100828171 (see
 * String.parseLong())
 * 
 * @author René Schwietzke
 */
public final class ParseNumbers
{
    private static final int DIGITOFFSET = 48;

    /**
     * Parses the string and returns the result as int. Raises a NumberFormatException in case of an non-convertable
     * string. Due to conversion limitations, the content of s might be larger than an int, precision might be
     * inaccurate.
     * 
     * @param s
     *            the string to parse
     * @return the converted string as int
     * @throws java.lang.NumberFormatException
     */
    public static long parseLong(final String s)
    {
        // no string
        if (s == null)
        {
            throw new NumberFormatException("null");
        }

        // determine length
        final int length = s.length();
        
        if (length == 0)
        {
            throw new NumberFormatException("length = 0");
        }
        
        // that is safe, we already know that we are > 0
        final int digit = s.charAt(0);
        
        // turn the compare around to allow the compiler and cpu
        // to run the next code most of the time
        if (digit < '0' || digit > '9')
        {
            return Long.parseLong(s);
        }
        
        long value = digit - DIGITOFFSET;
        
        for (int i = 1; i < length; i++)
        {
            final int d = s.charAt(i);
            if (d < '0' || d > '9')
            {
                return Long.parseLong(s);
            }

            value = ((value << 3) + (value << 1));
            value += (d - DIGITOFFSET);
        }

        return value;
    }
    
    /**
     * Parses the string and returns the result as int. Raises a NumberFormatException in case of an non-convertable
     * string. Due to conversion limitations, the content of s might be larger than an int, precision might be
     * inaccurate.
     * 
     * @param s
     *            the string to parse
     * @return the converted string as int
     * @throws java.lang.NumberFormatException
     */
    public static long parseLong(final XltCharBuffer s)
    {
        // no string
        if (s == null)
        {
            throw new NumberFormatException("null");
        }

        // determine length
        final int length = s.length();
        
        if (length == 0)
        {
            throw new NumberFormatException("length = 0");
        }
        
        // that is safe, we already know that we are > 0
        final int digit = s.get(0);
        
        // turn the compare around to allow the compiler and cpu
        // to run the next code most of the time
        if (digit < '0' || digit > '9')
        {
            return Long.parseLong(s.toString());
        }
        
        long value = digit - DIGITOFFSET;
        
        for (int i = 1; i < length; i++)
        {
            final int d = s.get(i);
            if (d < '0' || d > '9')
            {
                return Long.parseLong(s.toString());
            }

            value = ((value << 3) + (value << 1));
            value += (d - DIGITOFFSET);
        }

        return value;
    }

    /**
     * Parses the string and returns the result as int. Raises a NumberFormatException in case of an non-convertable
     * string. Due to conversion limitations, the content of s might be larger than an int, precision might be
     * inaccurate.
     * 
     * @param s
     *            the string to parse
     * @return the converted string as int
     * @throws java.lang.NumberFormatException
     */
    public static int parseInt(final XltCharBuffer s)
    {
        // no string
        if (s == null)
        {
            throw new NumberFormatException("null");
        }

        // determine length
        final int length = s.length();
        
        if (length == 0)
        {
            throw new NumberFormatException("length = 0");
        }
        
        // that is safe, we already know that we are > 0
        final int digit = s.get(0);
        
        // turn the compare around to allow the compiler and cpu
        // to run the next code most of the time
        if (digit < '0' || digit > '9')
        {
            return Integer.parseInt(s.toString());
        }
        
        int value = digit - DIGITOFFSET;
        
        for (int i = 1; i < length; i++)
        {
            final int d = s.get(i);
            if (d < '0' || d > '9')
            {
                return Integer.parseInt(s.toString());
            }

            value = ((value << 3) + (value << 1));
            value += (d - DIGITOFFSET);
        }

        return value;
    }
}
