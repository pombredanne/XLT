package com.xceptance.xlt.report;

import java.util.HashMap;
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
    private final Map<String, Class<? extends Data>> classes = new HashMap<String, Class<? extends Data>>(11);

    /**
     * Setup this factory based on the config
     * 
     * @param dataClasses the data classes to support
     */
    public DataRecordFactory(final Map<String, Class<? extends Data>> dataClasses)
    {
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final String typeCode = entry.getKey();
            final Class<? extends Data> c = entry.getValue();
            registerStatisticsClass(c, typeCode);
        }
    }
    
    /**
     * Register a new handler under its type code
     * 
     * @param c
     * @param typeCode
     */
    public void registerStatisticsClass(final Class<? extends Data> c, final String typeCode)
    {
        classes.put(typeCode, c);
    }
    
    /**
     * Remove a data processor/handler
     * 
     * @param typeCode
     */
    public void unregisterStatisticsClass(final String typeCode)
    {
        classes.remove(typeCode);
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
        int i = s.indexOf(Data.DELIMITER);
        if (i == -1)
        {
            i = s.length();
        }

        final String typeCode = s.substring(0, i);

        // get the respective data record class
        final Class<? extends Data> c = classes.get(typeCode);
        if (c == null)
        {
            throw new RuntimeException("No class found for type code: " + typeCode);
        }

        // create the statistics object
        final Data stats = c.newInstance();
        stats.fromCSV(s);

        return stats;
    }
}
