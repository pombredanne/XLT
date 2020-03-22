package com.xceptance.common.lang;

public class StringHasher
{
    /**
     * Hashes a string up to a terminal character excluding it. Will 
     * return the regular hash code if the terminal character is not included.
     * 
     * @param s the string to return the hash code for
     * @param limitingChar the limiting character when to stop 
     */
    public static int hashCodeWithLimit(final String s, final char limitingChar)
    {
        final int pos = s.indexOf(limitingChar);
        if (pos >= 0)
        {
            return _hashCodeWithLimit(s, limitingChar);
        }
        else
        {
            return s.hashCode();
        }
    }    
    
    /**
     * Hashes the string up to the terminal character. This always hashes the string
     * without falling back to the original hash code, hence the code above checks first.
     * This speeds up the code.
     */
    private static int _hashCodeWithLimit(final String s, final char limitingChar)
    {
        int hash = 0;
        
        for (int i = 0; i < s.length(); i++) 
        {
            final char c = s.charAt(i);
            
            if (c != limitingChar)
            {
                hash = 31 * hash + c;
            }
            else
            {
                // early end reached
                break;
            }
        }
        
        return hash;
    }    

}
