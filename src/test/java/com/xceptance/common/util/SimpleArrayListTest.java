package com.xceptance.common.util;

import org.junit.Assert;
import org.junit.Test;

public class SimpleArrayListTest
{
    @Test
    public void create()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(5);
        Assert.assertEquals(0, l.size());
    }

    @Test
    public void fill()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(5);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        Assert.assertEquals(5, l.size());

        Assert.assertEquals("a", l.get(0));
        Assert.assertEquals("b", l.get(1));
        Assert.assertEquals("c", l.get(2));
        Assert.assertEquals("d", l.get(3));
        Assert.assertEquals("e", l.get(4));

        // no growth
        try
        {
            l.get(5);
            Assert.fail();
        }
        catch(IndexOutOfBoundsException e)
        {
            // yeah... expected
        }
    }
    
    @Test
    public void fillAndGrow()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(2);
        l.add("a");
        l.add("b");
        Assert.assertEquals(2, l.size());
        l.add("d");
        l.add("e");
        Assert.assertEquals("d", l.get(2));
        Assert.assertEquals("e", l.get(3));
        Assert.assertEquals(4, l.size());

        // limited growth
        try
        {
            l.get(5);
            Assert.fail();
        }
        catch(IndexOutOfBoundsException e)
        {
            // yeah... expected
        }
    }
}
