package com.xceptance.xlt.report.providers;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * The {@link AbstractDataProcessorBasedReportProvider} class provides common functionality of a typical report
 * provider, which internally uses {@link AbstractDataProcessor} instances to calculate statistics.
 */
public abstract class AbstractDataProcessorBasedReportProvider<T extends AbstractDataProcessor> extends AbstractReportProvider
{
    /**
     * The data processor class.
     */
    private final Class<T> implClass;

    /**
     * A mapping from timer names to data processor instances.
     */
    private final Map<String, T> processors = new HashMap<String, T>();

    /**
     * Creates a new {@link AbstractDataProcessorBasedReportProvider} instance.
     * 
     * @param c
     *            the data processor implementation class
     */
    protected AbstractDataProcessorBasedReportProvider(final Class<T> c)
    {
        this.implClass = c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        final T processor = getProcessor(stat.getName());
        processor.processDataRecord(stat);
    }

    /**
     * Returns the data processor responsible for timers with the given name.
     * 
     * @param name
     *            the timer name
     * @return the data processor
     */
    protected T getProcessor(final String name)
    {
        T processor = processors.get(name);

        if (processor == null)
        {
            // lazily create a processor for that timer name
            try
            {
                final Constructor<T> constructor = implClass.getConstructor(String.class, AbstractReportProvider.class);

                processor = constructor.newInstance(name, this);
            }
            catch (final Exception ex)
            {
                throw new RuntimeException("", ex);
            }

            processors.put(name, processor);
        }

        return processor;
    }

    /**
     * Returns the collection of data processor instances used by this report provider.
     * 
     * @return the data processors
     */
    protected Collection<T> getProcessors()
    {
        // return the processors sorted by timer name
        return Collections.unmodifiableCollection(new TreeMap<String, T>(processors).values());
    }
}