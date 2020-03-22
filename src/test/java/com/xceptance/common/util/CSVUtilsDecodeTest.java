package com.xceptance.common.util;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

public class CSVUtilsDecodeTest
{
    private void test(String s, String... expected) throws ParseException
    {
        Assert.assertArrayEquals(expected, CsvUtilsDecode.parse(s).toArray());
    }
    
    @Test
    public void col1() throws ParseException
    {
        test("a", "a");
    }
    @Test
    public void col1_long() throws ParseException
    {
        test("foobar", "foobar");
    }
    @Test
    public void col1Quoted() throws ParseException
    {
        test("\"a\"", "a");
    }
    @Test
    public void col1Quoted_long() throws ParseException
    {
        test("\"foobar\"", "foobar");
    }
    @Test
    public void col2() throws ParseException
    {
        test("a,b", "a", "b");
        test("aa,bb", "aa", "bb");
    }
    @Test
    public void col2Quoted() throws ParseException
    {
        test("\"a\",\"b\"", "a", "b");
        test("\"aa\",\"bb\"", "aa", "bb");
    }
    @Test
    public void quotedQuotes() throws ParseException
    {
        test("\"\"\"\"", "\"");
        test("\"a\"\"a\"", "a\"a");
    }

    @Test
    public void emptyCols() throws ParseException
    {
        test("a,,,b,", "a", "", "", "b", "");
        test("a,,b", "a", "", "b");
        test("a,,,b", "a", "", "", "b");
        test(",", "", "");
    }
}
