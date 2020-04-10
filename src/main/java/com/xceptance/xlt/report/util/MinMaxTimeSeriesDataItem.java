package com.xceptance.xlt.report.util;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * A time series data item that wraps a {@link IntMinMaxValue}.
 */
public class MinMaxTimeSeriesDataItem extends TimeSeriesDataItem
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6836336147826544850L;

    /**
     * The wrapped min/max value.
     */
    private final IntMinMaxValue minMaxValue;

    /**
     * Constructor. Sets the average from the passed min/max value as the super class's value.
     * 
     * @param period
     *            the time period
     * @param minMaxValue
     *            the min/max value
     */
    public MinMaxTimeSeriesDataItem(final RegularTimePeriod period, final IntMinMaxValue minMaxValue)
    {
        super(period, (double) minMaxValue.getAccumulatedValue() / (double) minMaxValue.getValueCount());

        this.minMaxValue = minMaxValue;
    }

    /**
     * Returns the wrapped min/max value.
     * 
     * @return the min/max value
     */
    public IntMinMaxValue getMinMaxValue()
    {
        return minMaxValue;
    }
}
