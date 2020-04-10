package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Test;

public class MinMaxValueTest
{
    @Test
    public final void testMinMaxValue()
    {
        final IntMinMaxValue set = new IntMinMaxValue(0);
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
        final IntMinMaxValue set = new IntMinMaxValue(0);
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
        final IntMinMaxValue set = new IntMinMaxValue(1);
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
        final IntMinMaxValue set = new IntMinMaxValue(-2);
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
        final IntMinMaxValue set = new IntMinMaxValue(0);
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
        final IntMinMaxValue set1 = new IntMinMaxValue(10);
        final IntMinMaxValue set2 = new IntMinMaxValue(20);
        final IntMinMaxValue set = new IntMinMaxValue(0);

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
        final IntMinMaxValue set = new IntMinMaxValue(88);
        Assert.assertEquals("88/88/88/88/1", set.toString());

        set.updateValue(12);
        Assert.assertEquals("50/100/12/88/2", set.toString());
    }

    @Test
    public final void testEquals()
    {
        final IntMinMaxValue set = new IntMinMaxValue(76210);
        Assert.assertFalse(set.equals(null));
        Assert.assertFalse(set.equals("Foo"));
        Assert.assertTrue(set.equals(set));
        Assert.assertTrue(set.equals(new IntMinMaxValue(76210)));
        Assert.assertFalse(set.equals(new IntMinMaxValue(6210)));
    }

}
