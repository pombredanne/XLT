package com.xceptance.xlt.engine.util;

/**
 * This is a wrapper around string to cache the hashcode and 
 * change the equals comparison. This is a perfect candidate for Valhalla's
 * inline classes
 *  
 * @author rschwietzke
 *
 */
public class FastString
{
    private final int hashCode;
    private final String data;
    
    public FastString(final String data, int hashCode)
    {
        this.data = data;
        this.hashCode = hashCode;
    }
    public FastString(final String data)
    {
        this.data = data;
        this.hashCode = data.hashCode();
    }
    
    @Override
    public String toString()
    {
        return data;
    }
    
    @Override
    public int hashCode()
    {
        return hashCode;
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }

        FastString other = (FastString) obj;
        if (hashCode != obj.hashCode())
        {
            return false;
        }
        if (data.length() == other.data.length())
        {
            // compare content, don't use string, because we don't know if it
            // will pull a hashcode
            int n = data.length();
            int i = 0;
            while (n-- != 0) 
            {
                if (data.charAt(i) != other.data.charAt(i))
                {
                    return false;
                }
                i++;
            }

        }
        
        return true;
    }
    
    
}
