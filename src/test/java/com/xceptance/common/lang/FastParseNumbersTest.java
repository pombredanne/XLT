package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for parsing longs and ints.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class FastParseNumbersTest
{
    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseLong(java.lang.String)}.
     */
    @Test
    public final void testParseLong()
    {
        {
            final String s = "1670036109465868";
            Assert.assertEquals((long) Long.valueOf(s), FastParseNumbers.fastParseLong(s.toCharArray()));
        }
        {
            final String s = "0";
            Assert.assertEquals((long) Long.valueOf(s), FastParseNumbers.fastParseLong(s.toCharArray()));
        }
        {
            final String s = "1670036";
            Assert.assertEquals((long) Long.valueOf(s), FastParseNumbers.fastParseLong(s.toCharArray()));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test
    public final void testParseInt()
    {
        {
            final String s = "1670036108";
            Assert.assertEquals((int) Integer.valueOf(s), FastParseNumbers.fastParseInt(s.toCharArray()));
        }
        {
            final String s = "0";
            Assert.assertEquals((int) Integer.valueOf(s), FastParseNumbers.fastParseInt(s.toCharArray()));
        }
        {
            final String s = "1670036";
            Assert.assertEquals((int) Integer.valueOf(s), FastParseNumbers.fastParseInt(s.toCharArray()));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Empty()
    {
        final String s = "";
        FastParseNumbers.fastParseInt(s.toCharArray());
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test
    public final void testNumberFormatExceptionInt_Space()
    {
        final String s = " ";
        FastParseNumbers.fastParseInt(s.toCharArray());
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test
    public final void testNumberFormatExceptionInt_WrongCharacter()
    {
        final String s = "aaa";
        FastParseNumbers.fastParseInt(s.toCharArray());
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Null()
    {
        FastParseNumbers.fastParseInt(null);
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Empty()
    {
        final String s = "";
        FastParseNumbers.fastParseLong(s.toCharArray());
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test
    public final void testNumberFormatExceptionLong_Space()
    {
        final String s = " ";
        FastParseNumbers.fastParseLong(s.toCharArray());
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test
    public final void testNumberFormatExceptionLong_WrongCharacter()
    {
        final String s = "aaa";
        FastParseNumbers.fastParseLong(s.toCharArray());
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Null()
    {
        FastParseNumbers.fastParseLong(null);
    }
}
