package com.xceptance.xlt.report;

import java.util.Map;

import com.xceptance.xlt.api.engine.Data;

/**
 * Data classes hold processor for certain data types, such as Request, Transaction, Action, and
 * more. This is indicated in the logs by the first column of the record (a line), such as
 * A, T, R, C, and more. This can be later extended. The column is not limited to a single character
 * and can hold more, in case we run out of options sooner or later.
 */
public class DataRecordFactory
{
    /**
     * The registered data handlers per Data(Record) type. 
     */
    private final Class<? extends Data> classes[];
    private final int offset;

    /**
     * Setup this factory based on the config
     * 
     * @param dataClasses the data classes to support
     */
    public DataRecordFactory(final Map<String, Class<? extends Data>> dataClasses)
    {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            char c = entry.getKey().charAt(0);
            min = Math.min(min, c);
            max = Math.max(max, c);
        }

        offset = min;
        classes = new Class[max - offset + 1];
        
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final int typeCode = entry.getKey().charAt(0);
            final Class<? extends Data> clazz = entry.getValue();
            registerStatisticsClass(clazz, typeCode);
        }
    }
    
    /**
     * Register a new handler under its type code
     * 
     * @param c
     * @param typeCode
     */
    public void registerStatisticsClass(final Class<? extends Data> c, final int typeCode)
    {
        classes[typeCode - offset] = c;
    }
    
    /**
     * Return the 
     * @param s
     * @return
     * @throws Exception
     */
    public Data createStatistics(final String s) throws Exception
    {
        // get the type code
        // get the respective data record class
        final Class<? extends Data> c = classes[s.charAt(0) - offset];

        // create the statistics object
        final Data stats = c.newInstance();
        stats.fromCSV(s);

        return stats;
    }
}
