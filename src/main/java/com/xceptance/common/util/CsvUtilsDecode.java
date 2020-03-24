package com.xceptance.common.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link CsvUtilsDecode} class provides helper methods to encode and decode values to/from the CSV format. Note that we
 * define the "C" in "CSV" to stand for "comma", so other characters are not allowed as field separator.
 */
public final class CsvUtilsDecode
{
    /**
     * Character constant representing a comma.
     */
    private static final char COMMA = ',';

    /**
     * Character constant representing a double quote.
     */
    private static final char QUOTE_CHAR = '"';

    /**
     * Character constant representing a line feed.
     */
    private static final char LF = '\n';

    /**
     * Character constant representing a carriage return.
     */
    private static final char CR = '\r';

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private CsvUtilsDecode()
    {
    }

    /**
     * Decodes the given CSV-encoded data record and returns the plain unquoted fields.
     * 
     * @param s
     *            the CSV-encoded data record
     * @return the plain fields
     */
    public static List<String> parse(final String s)
    {
        return parse(s.toCharArray(), COMMA);
    }

    private static final int NONE = 0;
    private static final int IN_QUOTES = 1;
    private static final int EXPECTED_ESCAPED_QUOTE = 2;
    private static final int SEPARATOR_EXPECTED = 4;
    private static final int LAST_WAS_SEPARATOR = 8;
    
    /**
     * Encodes the given fields to a CSV-encoded data record using the given field separator.
     * 
     * @param s
     *            the plain fields
     * @param fieldSeparator
     *            the field separator to use
     * @return the CSV-encoded data record
     * @throws ParseException 
     */
    public static List<String> parse(final char[] src, final char fieldSeparator)
    {
        final SimpleArrayList<String> result = new SimpleArrayList<>(32);
        
        int state = NONE;
        int pos = 0;
        
        for (int i = 0; i < src.length; i++)
        {
            final char c = src[i];
            
            // do we have a quote?
            if (c == QUOTE_CHAR)
            {
                // reset last state
                state = state ^ LAST_WAS_SEPARATOR;
                
                // start quote
                if ((state & IN_QUOTES) == 0)
                {
                    // we are not in quotes, hence start quote
                    state = state | IN_QUOTES; // set in quotes marker
                    
                    continue;
                }

                // quotes in quotes

                // we have not yet expected one, so that is the start escape
                if ((state & EXPECTED_ESCAPED_QUOTE) == 0)
                {
                    state = state | EXPECTED_ESCAPED_QUOTE;
                    continue;
                }
                else
                {
                    // ok, we have an escaped quote, preserve it 
                    // move the char pos up if we have to
                    if (pos != i)
                    {
                        src[pos] = c;
                    }
                    pos++;

                    // remove state
                    state = state ^ EXPECTED_ESCAPED_QUOTE;

                    continue;
                }
            }

            // we might have though of a quote but have not gotten one, so we did not 
            // see an escaped quote
            if ((state & (EXPECTED_ESCAPED_QUOTE | IN_QUOTES)) == (EXPECTED_ESCAPED_QUOTE | IN_QUOTES))
            {
                // unset quote marker
                state = state ^ (IN_QUOTES | EXPECTED_ESCAPED_QUOTE);
            }
            
            if (c == fieldSeparator)
            {
                if ((state & IN_QUOTES) == 0)
                {
                    // ok, no quotes and seperator, so get us the data
                    // less heavy lifting if we don't have anything
                    result.add(pos == 0 ? "" : String.valueOf(src, 0, pos));
                    
                    // in case we have nothing later, we gotta indicate us an empty string
                    state = LAST_WAS_SEPARATOR;
                    pos = 0;

                    continue;
                }
            }

            // reset last state
            state = state ^ LAST_WAS_SEPARATOR;
            
            // move the char up if we have to
            if (pos != i)
            {
                src[pos] = c;
            }
            pos++;
        }
        
        if (pos > 0)
        {
            result.add(String.valueOf(src, 0, pos));
        }
        else if ((state & LAST_WAS_SEPARATOR) == LAST_WAS_SEPARATOR)
        {
            result.add("");
        }
        
        return result;
    }
}