package com.xceptance.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.OpenStringBuilder;


public class NonStringBufferedReader_Test
{
    private String compose(String[] s, String sep)
    {
        return Arrays.stream(s).collect(Collectors.joining(sep));
    }
    
    private String compose(String[] s)
    {
        return Arrays.stream(s).collect(Collectors.joining());
    }
    
    private void test(String[] s, String sep, int bufferSize)
    {
        test(compose(s, sep), bufferSize);
    }
    
    private void test(String[] s, String sep)
    {
        test(compose(s, sep), 8192);
    }
    
    private void test(String[] s)
    {
        test(compose(s), 8192);
    }
    
    private void test(String src, int bufferSize)
    {
        final List<String> newBR = new ArrayList<>();
        try (final NonStringBufferedReader r = new NonStringBufferedReader(new StringReader(src), bufferSize))
        {
            OpenStringBuilder osb = null;
            while ((osb = r.readLine()) != null)
            {
                newBR.add(osb.toString());
            }
        }
        catch (IOException e)
        {
            Assert.fail();
        }

        final List<String> original = new ArrayList<>();
        try (final BufferedReader r = new BufferedReader(new StringReader(src), bufferSize))
        {
            String osb = null;
            while ((osb = r.readLine()) != null)
            {
                original.add(osb.toString());
            }
        }
        catch (IOException e)
        {
            Assert.fail();
        }
        
        // verify
        Assert.assertArrayEquals(newBR.toArray(), original.toArray());
    }
    
    @Test
    public void happyPath()
    {
        final String[] data = new String[] {
                "T"
        };
       
        test(data, "\r");
        test(data, "\r\n");
        test(data, "\n");    }
    
    @Test
    public void happyPathEmptyLineLast()
    {
        final String[] data = new String[] {
                "T",
                ""
        };
       
        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }
    
    @Test
    public void happyPathEmptyLineMIddle()
    {
        final String[] data = new String[] {
                "T",
                "",
                "A"
        };
       
        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }
    
    @Test
    public void happyPathEmpty()
    {
        final String[] data = new String[] {
                ""
        };
       
        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }
    
    @Test
    public void happyPathEmptyOnly()
    {
        final String[] data = new String[] {
                "", ""
        };
       
        test(data, "\r");
        test(data, "\n");    
        test(data, "\r\n");
    }

    @Test
    public void happyPathEmptyOnly3Full()
    {
        final String[] data = new String[] {
                "T", "a", "B"
        };
       
        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }
    
    @Test
    public void bufferSmallerThanLines()
    {
        final String[] data = new String[] {
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", // 100
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" 
        };
       
        test(data, "\r", 250);
        test(data, "\n", 250);
        test(data, "\r\n", 250);
    }

    @Test
    public void bufferSmallerThanLine()
    {
        final String[] data = new String[] {
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" 
        };
       
        test(data, "\r", 12);
        test(data, "\n", 6);
        test(data, "\r\n", 45);
    }
    
    @Test
    public void lastLineEmpty()
    {
        final String[] data = new String[] {
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\n", 
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\n", 
                "" 
        };
       
        test(data);
    }
    
//    public List<OpenStringBuilder> readViaNonStringBufferedReader(InputStream in)
//    {
//        final List<OpenStringBuilder> result = new ArrayList<>();
//        try (final NonStringBufferedReader r = new NonStringBufferedReader(new InputStreamReader(in)))
//        {
//            OpenStringBuilder osb = null;
//            while ((osb = r.readLine()) != null)
//            {
//                result.add(osb);
//            }
//        }
//        catch (IOException e)
//        {
//            Assert.fail();
//        }
//        
//        return result;
//    }
//    
//    public List<String> readViaBufferedReader(InputStream in)
//    {
//        final List<String> result = new ArrayList<>();
//        try (final BufferedReader r = new BufferedReader(new InputStreamReader(in)))
//        {
//            String osb = null;
//            while ((osb = r.readLine()) != null)
//            {
//                result.add(osb);
//            }
//        }
//        catch (IOException e)
//        {
//            Assert.fail();
//        }
//
//        return result;
//    }
//    
//    @Test
//    public void compare() throws FileNotFoundException
//    {
//        final String file = "/tmp/ramdisk/20191022-230434-torrid-small/ac005_us-west2-a_00/TBrowse/164/timers.csv";
//        
//        final List<String> regular = readViaBufferedReader(new FileInputStream(new File(file)));
//        final List<OpenStringBuilder> fancy = readViaNonStringBufferedReader(new FileInputStream(new File(file)));
//        
//        Assert.assertEquals(regular.size(), fancy.size());
//        
//        for (int i = 0; i < regular.size(); i++)
//        {
//            String r = regular.get(i);
//            OpenStringBuilder f = fancy.get(i);
//            
//            Arrays.equals(r.toCharArray(), f.getCharArray());
//        }
//    }
}
