package com.xceptance.common.util;

import java.text.ParseException;
import java.util.List;

import com.xceptance.common.lang.OpenStringBuilder;

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
    public static List<XltCharBuffer> parse(final String s)
    {
        return parse(new OpenStringBuilder(s), COMMA);
    }

    private static final int NONE = 0;
    private static final int IN_QUOTES = 1;
    private static final int START_MODE = 2;
    private static final int SEPARATOR_EXPECTED = 4;
    private static final int LAST_WAS_SEPARATOR = 8;
    private static final int JUST_LEFT_QUOTES = 16;    
    private static final int COPY_REST = 32;    
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
    public static List<XltCharBuffer> parse(final OpenStringBuilder _src, final char fieldSeparator)
    {
        final SimpleArrayList<XltCharBuffer> result = new SimpleArrayList<>(30);
        final XltCharBuffer src = new XltCharBuffer(_src.getCharArray(), 0, _src.length());
        
        final int size = src.length();

        int state = START_MODE;
        int pos = 0;
        int start = 0;
        int offset = 0;
        
        Main:
        while (pos < size)
        {
            final char c = src.get(pos);
            
            state = state | COPY_REST;
            state = state & ~LAST_WAS_SEPARATOR;
            
            // do we have a quote?
            if (c == QUOTE_CHAR)
            {
                // our first quote because this all handled in the inner loop
                state = state | IN_QUOTES;
                
                pos++;
                
                // when we just started, we want to move the offset as well to avoid
                // too much copying
                if ((state & START_MODE) == START_MODE)
                {
                    offset++; // we also change the offset to avoid copying too much
                    start = offset;
                }
                
                // now read until we leave this state again or exhaust the buffer
                InQuotes:
                while (pos < size)
                {
                    final char c2 = src.get(pos);
                    
                    if (c2 == QUOTE_CHAR)
                    {
                        // this is either the end quote or a quoted quote
                        
                        // peak
                        final char peakedChar = src.peakAhead(pos + 1);
                        if (peakedChar == 0)
                        {
                            // there is nothing anymore, break here, pos is right because
                            // we just peaked and did not move the cursor
                            state = state | COPY_REST;
                            
                            break Main;
                        }
                        
                        // we have not reached the end, let's see what we got
                        if (peakedChar == QUOTE_CHAR)
                        {
                            // this is a quoted quote, preserve it and jump over
                            // it, but move the offset only by one because now
                            // everything has to be moved until we reach the end OR
                            // worse... more of these
                            src.put(offset, c2);

                            offset++;
                            pos += 2;
                            
                            continue InQuotes;
                        }
                        
                        // we have to jump over the quote, we are at the end
                        pos++;
                        
                        state = state & ~(IN_QUOTES | START_MODE);
                        state = state | JUST_LEFT_QUOTES;
                        
                        continue Main;
                    }
                    
                    // ok, no quote, so this is just text in quotes
                    if (pos != offset)
                    {
                        src.put(offset, c2);
                    }
                    pos++;
                    offset++;

                    state = state & ~START_MODE;
                }
                
                // if we got here, quotes does not close
                // deal with it in a soft way at the moment and copy the rest
                state = state | COPY_REST;
                
                break Main; 
            }
                
            if (c == fieldSeparator)
            {
                // if we just left the quotes, just reset because that is expected
                // or we already skipped a few characters
                state = state & ~(JUST_LEFT_QUOTES | COPY_REST);

                // we saw a separator and we start a new col
                state = state | (LAST_WAS_SEPARATOR | START_MODE);

                // we need the data
                result.add(src.viewFromTo(start, offset));
                
                pos++;
                offset = pos;
                start = pos;
                
                continue Main;
            }

            // this feature is not yet supported because we are in control of our data and don't have 
            // whitespaces around the separator
//            if ((state & JUST_LEFT_QUOTES) == JUST_LEFT_QUOTES)
//            {
//                // we have left quotes, but not seen a separator, so we have garbage or spaces, ignore
//                pos++;
//                
//                continue Main;
//            }
            
            // move the char up if we have to
            if (pos != offset)
            {
                src.put(offset, c);
            }
            
            pos++;
            offset++;
            
            state = state & ~START_MODE;
        }
        
        // There is a rest to copy
        if ((state & COPY_REST) == COPY_REST)
        {
            result.add(src.viewFromTo(start, offset));
        }
        else if ((state & LAST_WAS_SEPARATOR) == LAST_WAS_SEPARATOR)
        {
            // we had a seperator at the end, so we have an empty string to add
            result.add(XltCharBuffer.empty());
        }
        else if (size == 0)
        {
            // the rare case if an empty string
            result.add(XltCharBuffer.empty());
        }
        
        return result;
    }
}
