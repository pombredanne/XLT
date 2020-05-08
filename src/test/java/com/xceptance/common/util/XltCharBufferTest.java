package com.xceptance.common.util;

import org.junit.Assert;
import org.junit.Test;

public class XltCharBufferTest
{
    @Test
    public void create()
    {
        final char[] src = "Test".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);
        Assert.assertEquals(4, b.length());
        Assert.assertEquals(String.valueOf(src), b.toString());
    }

    @Test
    public void string()
    {
        {
            final char[] src = "Test".toCharArray();
            final XltCharBuffer b = new XltCharBuffer(src);
            Assert.assertEquals(String.valueOf(src), b.toString());
        }

        {
            final char[] src = "TestFoobarRally".toCharArray();
            final XltCharBuffer b = new XltCharBuffer(src);
            Assert.assertEquals(String.valueOf(src), b.toString());
            
            Assert.assertEquals("Test", b.viewByLength(0, 4).toString());
            Assert.assertEquals("Foobar", b.viewByLength(4, 6).toString());
            Assert.assertEquals("Rally", b.viewByLength(10, 5).toString());
        }
    }

    @Test
    public void put()
    {
        final char[] src = "Test".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);
        
        b.put(1, 'ä'); Assert.assertEquals("Täst", b.toString());
        b.put(0, '1'); Assert.assertEquals("1äst", b.toString());
        b.put(3, '3'); Assert.assertEquals("1äs3", b.toString());
        b.put(2, '2'); Assert.assertEquals("1ä23", b.toString());
    }

    @Test
    public void putWithView()
    {
        final char[] src = "TestFoo".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);
        final XltCharBuffer b1 = b.viewByLength(0, 4);
        final XltCharBuffer b2 = b.viewByLength(4, 3);
        
        b1.put(1, 'ä');
        Assert.assertEquals("Täst", b1.toString());
        Assert.assertEquals("Foo", b2.toString());

        b2.put(1, '1');
        Assert.assertEquals("Täst", b1.toString());
        Assert.assertEquals("F1o", b2.toString());

        Assert.assertEquals("TästF1o", b.toString());
    }

    @Test
    public void get()
    {
        final char[] src = "TestFo2".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);
        final XltCharBuffer b1 = b.viewByLength(0, 4);
        final XltCharBuffer b2 = b.viewByLength(4, 3);
        
        Assert.assertEquals('T', b.get(0));
        Assert.assertEquals('o', b.get(5));
        Assert.assertEquals('2', b.get(6));
        
        Assert.assertEquals('T', b1.get(0));
        Assert.assertEquals('e', b1.get(1));
        Assert.assertEquals('s', b1.get(2));
        Assert.assertEquals('t', b1.get(3));

        Assert.assertEquals('F', b2.get(0));
        Assert.assertEquals('o', b2.get(1));
        Assert.assertEquals('2', b2.get(2));
    }

    @Test
    public void viewByLength()
    {
        final char[] src = "TestFo2".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);
        
        Assert.assertEquals("", b.viewByLength(0, 0).toString());
        Assert.assertEquals("T", b.viewByLength(0, 1).toString());
        Assert.assertEquals("TestFo2", b.viewByLength(0, 7).toString());
        Assert.assertEquals("2", b.viewByLength(6, 1).toString());
        Assert.assertEquals("", b.viewByLength(6, 0).toString());
    }

    @Test
    public void viewFromTo()
    {
        final char[] src = "TestFo2".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);
        
        // Assert.assertEquals("", b.viewFromTo(0, 0).toString());
        Assert.assertEquals("T", b.viewFromTo(0, 1).toString());
        Assert.assertEquals("TestFo2", b.viewFromTo(0, 7).toString());
        Assert.assertEquals("2", b.viewFromTo(6, 7).toString());
        // Assert.assertEquals("", b.viewFromTo(6, 6).toString());

        Assert.assertEquals("est", b.viewFromTo(1, 4).toString());
        Assert.assertEquals("Fo2", b.viewFromTo(4, 7).toString());

    }
    
    @Test
    public void peakAhead()
    {
        final char[] src = "TestFo2".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);

        Assert.assertEquals('T', b.peakAhead(0));
        Assert.assertEquals('e', b.peakAhead(1));
        Assert.assertEquals('s', b.peakAhead(2));
        Assert.assertEquals('t', b.peakAhead(3));
        Assert.assertEquals('F', b.peakAhead(4));
        Assert.assertEquals('o', b.peakAhead(5));
        Assert.assertEquals('2', b.peakAhead(6));
        Assert.assertEquals(0, b.peakAhead(7));
        Assert.assertEquals(0, b.peakAhead(8));
    }

//    @Test
//    public void viewOfViews()
//    {
//        final char[] src = "TestFo2".toCharArray();
//        final XltCharBuffer b = new XltCharBuffer(src);
//        
//        Assert.assertEquals("", b.viewFromTo(0, 0).toString());
//
//    }
//    
    @Test
    public void length()
    {
        Assert.assertEquals(0, new XltCharBuffer("".toCharArray()).length());
        Assert.assertEquals(1, new XltCharBuffer("T".toCharArray()).length());
        Assert.assertEquals(2, new XltCharBuffer("TA".toCharArray()).length());

        Assert.assertEquals(0, new XltCharBuffer("TA".toCharArray()).viewByLength(0, 0).length());
        Assert.assertEquals(1, new XltCharBuffer("TA".toCharArray()).viewByLength(0, 1).length());
        Assert.assertEquals(1, new XltCharBuffer("TA".toCharArray()).viewByLength(1, 1).length());
        Assert.assertEquals(2, new XltCharBuffer("TA".toCharArray()).viewByLength(0, 2).length());
    }

    @Test
    public void empty()
    {
        final char[] src = "TestFo2".toCharArray();
        final XltCharBuffer b = new XltCharBuffer(src);
        
        Assert.assertEquals(0, b.viewByLength(0, 0).length());
        Assert.assertEquals("", b.viewByLength(0, 0).toString());
        
        Assert.assertEquals(0, XltCharBuffer.empty().length());
        Assert.assertEquals("", XltCharBuffer.empty().toString());
    }
}
