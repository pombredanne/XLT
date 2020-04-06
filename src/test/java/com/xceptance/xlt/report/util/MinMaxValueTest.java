package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Test;

public class MinMaxValueTest
{
    @Test
    public final void testMinMaxValue()
    {
        final MinMaxValue set = new MinMaxValue(0);
        Assert.assertEquals(0, set.getAccumulatedValue());
        Assert.assertEquals(0, set.getAverageValue());
        Assert.assertEquals(0, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(0, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_0()
    {
        final MinMaxValue set = new MinMaxValue(0);
        Assert.assertEquals(0, set.getAccumulatedValue());
        Assert.assertEquals(0, set.getAverageValue());
        Assert.assertEquals(0, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(0, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_1()
    {
        final MinMaxValue set = new MinMaxValue(1);
        Assert.assertEquals(1, set.getAccumulatedValue());
        Assert.assertEquals(1, set.getAverageValue());
        Assert.assertEquals(1, set.getMaximumValue());
        Assert.assertEquals(1, set.getMinimumValue());
        Assert.assertEquals(1, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_neg2()
    {
        final MinMaxValue set = new MinMaxValue(-2);
        Assert.assertEquals(-2, set.getAccumulatedValue());
        Assert.assertEquals(-2, set.getAverageValue());
        Assert.assertEquals(-2, set.getMaximumValue());
        Assert.assertEquals(-2, set.getMinimumValue());
        Assert.assertEquals(-2, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testUpdateValue()
    {
        final MinMaxValue set = new MinMaxValue(0);
        set.updateValue(2);

        Assert.assertEquals(2, set.getAccumulatedValue());
        Assert.assertEquals(1, set.getAverageValue());
        Assert.assertEquals(2, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(1, set.getValue());
        Assert.assertEquals(2, set.getValueCount());

        // next max
        set.updateValue(10);

        Assert.assertEquals(12, set.getAccumulatedValue());
        Assert.assertEquals(4, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(4, set.getValue());
        Assert.assertEquals(3, set.getValueCount());

        // next min
        set.updateValue(-2);

        Assert.assertEquals(10, set.getAccumulatedValue());
        Assert.assertEquals(2, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(-2, set.getMinimumValue());
        Assert.assertEquals(2, set.getValue());
        Assert.assertEquals(4, set.getValueCount());

        // next middle value
        set.updateValue(2);
        Assert.assertEquals(12, set.getAccumulatedValue());
        Assert.assertEquals(2, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(-2, set.getMinimumValue());
        Assert.assertEquals(2, set.getValue());
        Assert.assertEquals(5, set.getValueCount());
    }

    @Test
    public final void testMerge()
    {
        final MinMaxValue set1 = new MinMaxValue(10);
        final MinMaxValue set2 = new MinMaxValue(20);
        final MinMaxValue set = new MinMaxValue(0);

        set.merge(set1);
        Assert.assertEquals(10, set.getAccumulatedValue());
        Assert.assertEquals(5, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(5, set.getValue());
        Assert.assertEquals(2, set.getValueCount());

        set.merge(set2);
        Assert.assertEquals(30, set.getAccumulatedValue());
        Assert.assertEquals(10, set.getAverageValue());
        Assert.assertEquals(20, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(10, set.getValue());
        Assert.assertEquals(3, set.getValueCount());
    }

    @Test
    public final void testToString()
    {
        final MinMaxValue set = new MinMaxValue(88);
        Assert.assertEquals("88/88/88/88/1", set.toString());

        set.updateValue(12);
        Assert.assertEquals("50/100/12/88/2", set.toString());
    }

    @Test
    public final void testEquals()
    {
        final MinMaxValue set = new MinMaxValue(76210);
        Assert.assertFalse(set.equals(null));
        Assert.assertFalse(set.equals("Foo"));
        Assert.assertTrue(set.equals(set));
        Assert.assertTrue(set.equals(new MinMaxValue(76210)));
        Assert.assertFalse(set.equals(new MinMaxValue(6210)));
    }

}
