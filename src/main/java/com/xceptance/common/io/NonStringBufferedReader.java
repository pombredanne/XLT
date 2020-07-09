package com.xceptance.common.io;

import java.io.IOException;
import java.io.Reader;

import com.xceptance.common.lang.OpenStringBuilder;

/**
 * Trying to be a very fast buffered reader with minimal functionality
 */
public class NonStringBufferedReader extends Reader 
{
    private final Reader in;

    private static int DEFAULTCHARBUFFERSIZE = 8192;
    private static int DEFAULTEXPECTEDLINELENGTH = 80;

    // this will be set true when we saw a \r before to avoid splitting up
    // \r\n combinations (greetings from the non-unix world)
    private boolean skipLF = false;

    // our buffer and this 
    private char[] buffer;

    // the size of the current buffer, because it might not have been
    // filled fully last time
    private int lastSize = -1;

    // where are we in the buffer
    private int currentPos = 0;

    // reached end of file
    private boolean eof = false;
    
    // buffer size
    private final int size;
    
    /**
     * Creates a buffering character-input stream that uses an input buffer of
     * the specified size.
     *
     * @param  in   A Reader
     * @param  sz   Input-buffer size
     *
     * @exception  IllegalArgumentException  If sz is <= 0
     */
    public NonStringBufferedReader(final Reader in, int size) 
    {
        super(in);

        this.in = in;
        this.size = size;
        
        this.buffer = new char[size];
    }
    /**
     * Creates a buffering character-input stream that uses a default-sized
     * input buffer.
     *
     * @param  in   A Reader
     */
    public NonStringBufferedReader(Reader in) 
    {
        this(in, DEFAULTCHARBUFFERSIZE);
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return     A String containing the contents of the line, not including
     *             any line-termination characters, or null if the end of the
     *             stream has been reached
     *
     * @see        java.io.LineNumberReader#readLine()
     *
     * @exception  IOException  If an I/O error occurs
     */
    public OpenStringBuilder readLine() throws IOException 
    {
        if (eof)
        {
            return null;
        }
        
        OpenStringBuilder sb = null;
        int _currentPos = currentPos; // enforce register use
        int startPos = _currentPos;
        
        while (!eof)
        {
            // do we have anything?
            if (_currentPos < this.lastSize)
            {
                inner:
                while (_currentPos < lastSize)
                {
                    final char c = buffer[_currentPos];

                    if (c > '\r')
                    {
                        skipLF = false;
                        _currentPos++;
                        
                        continue inner;
                    }
                    else
                    {
                        if (c == '\n')
                        {
                            if (skipLF)
                            {
                                skipLF = false;
                                _currentPos++;
                                startPos++;
                                continue inner;
                            }
                            
                            if (sb == null)
                            {
                                final int length = _currentPos - startPos;
                                sb = new OpenStringBuilder(length);
                                sb.append(buffer, startPos, length);
                            }
                            else
                            {
                                sb.append(buffer, startPos, _currentPos - startPos);
                            }
                            _currentPos++;
    
                            currentPos = _currentPos;
                            return sb;
                        }
                        else if (c == '\r')
                        {
                            // jump next LF
                            skipLF = true;
                            
                            if (sb == null)
                            {
                                final int length = _currentPos - startPos;
                                sb = new OpenStringBuilder(length);
                                sb.append(buffer, startPos, length);
                            }
                            else
                            {
                                sb.append(buffer, startPos, _currentPos - startPos);
                            }                            
                            _currentPos++;
                            
                            // finish line
                            currentPos = _currentPos;
                            return sb;
                        }
                        else // all remaining characters
                        {
                            skipLF = false;
                            _currentPos++;
                        }
                    }
                }
            }
            else
            {
                // safe the rest before filling the buffer again
                final int length = _currentPos - startPos;
                if (length > 0 )
                {
                    if (sb == null) sb = new OpenStringBuilder(DEFAULTEXPECTEDLINELENGTH);
                    sb.append(buffer, startPos, length);
                }
                
                // fill it
                fill();
                _currentPos = 0;
                
                startPos = 0;
            }
            
        }
    
        final int length = _currentPos - startPos;
        if (length > 0 )
        {
            if (sb == null) sb = new OpenStringBuilder(length);

            sb.append(buffer, startPos, length);
        }    
        
        currentPos = _currentPos;
        return sb;
    }

    /**
     * Read more into the buffer, return false if not eof, true otherwise
     * @return
     * @throws IOException 
     */
    private void fill() throws IOException
    {
        // read as much as possible
        lastSize = read(buffer, 0, size);
        
        eof = lastSize == -1;
    }
    
    @Override
    public void close() throws IOException 
    {
        in.close();
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        return in.read(cbuf, off, len);
    }
}