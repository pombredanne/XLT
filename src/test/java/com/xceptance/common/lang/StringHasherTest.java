package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

public class StringHasherTest
{
    @Test
    public void behavesLikeString()
    {
        String s1 = "http://www.com/foobar";
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s1, '#'));
    }

    @Test
    public void ignoresRemaining()
    {
        String s1 = "http://www.com/foobar";
        String s2 = "http://www.com/foobar#nothing";
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s2, '#'));
    }
    
    @Test
    public void handlesEmpty()
    {
        String s1 = "";
        Assert.assertEquals(s1.hashCode(), StringHasher.hashCodeWithLimit(s1, '#'));
    }    

    @Test
    public void handlesOnlyLimited()
    {
        Assert.assertEquals("".hashCode(), StringHasher.hashCodeWithLimit("#", '#'));
    }       
    
    @Test
    public void handlesLimiterAtTheEnd()
    {
        Assert.assertEquals("test".hashCode(), StringHasher.hashCodeWithLimit("test#", '#'));
    }    
}
