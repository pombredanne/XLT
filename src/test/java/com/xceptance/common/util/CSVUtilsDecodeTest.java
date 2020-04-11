package com.xceptance.common.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CSVUtilsDecodeTest
{
    private void test(String s, String... expected) 
    {
        List<char[]> cExpected = new ArrayList<>();
        for (String st : expected)
        {
            cExpected.add(st.toCharArray());
        }

        Assert.assertArrayEquals(cExpected.toArray(), CsvUtilsDecode.parse(s).toArray());
    }

    @Test
    public void col1() 
    {
        test("a", "a");
    }
    @Test
    public void col1_long() 
    {
        test("foobar", "foobar");
    }
    @Test
    public void col1Quoted() 
    {
        test("\"a\"", "a");
    }
    @Test
    public void col1Quoted_long() 
    {
        test("\"foobar\"", "foobar");
    }
    @Test
    public void col2() 
    {
        test("a,b", "a", "b");
        test("aa,bb", "aa", "bb");
    }
    @Test
    public void col2Quoted() 
    {
        test("\"a\",\"b\"", "a", "b");
        test("\"aa\",\"bb\"", "aa", "bb");
    }
    @Test
    public void quotedQuotes() 
    {
        test("\"\"\"\"", "\"");
        test("\"a\"\"a\"", "a\"a");
    }

    @Test
    public void emptyCols() 
    {
        test("a,,,b,", "a", "", "", "b", "");
        test("a,,b", "a", "", "b");
        test("a,,,b", "a", "", "", "b");
        test(",", "", "");
    }

}
