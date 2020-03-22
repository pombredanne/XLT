package com.xceptance.common.lang;

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
    private static final int BASE = 10;
    
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
        
        try
        {
            long value = 0;
            for (int i = 0; i < length; i++)
            {
                final int digit = s.charAt(i);
                
                if (digit >= '0' && digit <= '9')
                {
                    value = ((value << 3) + (value << 1)) + (digit - DIGITOFFSET);
                }
                else
                {
                    throw new NumberFormatException();
                }
            }

            return value;
        }
        catch (final Exception e)
        {
            return Long.parseLong(s);
        }
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
    public static int parseInt(final String s)
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
        
        try
        {
            int value = 0;
            for (int i = 0; i < length; i++)
            {
                final int digit = s.charAt(i);
                
                if (digit >= '0' && digit <= '9')
                {
                    value = ((value << 3) + (value << 1)) + (digit - DIGITOFFSET);
                }
                else
                {
                    throw new NumberFormatException();
                }
            }

            return value;
        }
        catch (final Exception e)
        {
            return Integer.parseInt(s);
        }
    }
}
