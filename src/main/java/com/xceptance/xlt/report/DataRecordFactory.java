package com.xceptance.xlt.report;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;

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
    private final Constructor<? extends Data> ctrs[];
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
        ctrs = new Constructor[max - offset + 1];
        
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final int typeCode = entry.getKey().charAt(0);
            Constructor<? extends Data> clazz;
            try
            {
                clazz = entry.getValue().getConstructor();
                registerStatisticsClass(clazz, typeCode);
            }
            catch (NoSuchMethodException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (SecurityException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Register a new handler under its type code
     * 
     * @param c
     * @param typeCode
     */
    private void registerStatisticsClass(final Constructor<? extends Data> c, final int typeCode)
    {
        ctrs[typeCode - offset] = c;
    }
    
    /**
     * Return the 
     * @param s
     * @return
     * @throws Exception
     */
    public Data createStatistics(final String s) throws Exception
    {
        // create the statistics object
        final Data data;
        
        switch (s.charAt(0))
        {
            case 'R': data = new RequestData(); break;
            case 'A': data = new ActionData(); break;
            case 'T': data = new TransactionData(); break;
            case 'C': data = new CustomData(); break;
            case 'E': data = new EventData(); break;
            case 'J': data = new JvmResourceUsageData(); break;
            case 'V': data = new CustomValue(); break;
            case 'P': data = new PageLoadTimingData(); break;
            default: 
                // we failed... try the expensive way
                final Constructor<? extends Data> c = ctrs[s.charAt(0) - offset];
                data = c.newInstance();
        }
        
        data.fromCSV(s);

        return data;
    }
}
