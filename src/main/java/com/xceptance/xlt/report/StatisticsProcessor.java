package com.xceptance.xlt.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.util.SimpleArrayList;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.ReportProvider;

/**
 * Processes parsed data records. Processing means passing a data record to all configured report providers. Since data
 * processing is not thread-safe (yet), there will be only one data processor.
 */
class StatisticsProcessor implements Runnable
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(StatisticsProcessor.class);

    /**
     * The dispatcher that coordinates result processing.
     */
    private final Dispatcher dispatcher;

    /**
     * Creation time of last data record.
     */
    private long maximumTime;

    /**
     * Creation time of first data record.
     */
    private long minimumTime;

    /**
     * The configured report providers. An array for less overhead.
     */
    private final ReportProvider[] reportProviders;

    /**
     * Mapping of data to provider to avoid unnecessary calls
     */
    private Map<ReportProvider, List<Data>> mapping;
    
    /**
     * A thread limit given from the outside
     */
    private final int threadCount;


    /**
     * Constructor.
     *
     * @param reportProviders
     *            the configured report providers
     * @param dispatcher
     *            the dispatcher that coordinates result processing
     */
    public StatisticsProcessor(final List<ReportProvider> reportProviders, final Dispatcher dispatcher, int threadCount)
    { 
        this.reportProviders = reportProviders.toArray(new ReportProvider[0]);
        this.dispatcher = dispatcher;

        maximumTime = 0;
        minimumTime = Long.MAX_VALUE;
        this.threadCount = threadCount;
    }

    /**
     * Returns the maximum time.
     *
     * @return maximum time
     */
    public final long getMaximumTime()
    {
        return maximumTime;
    }

    /**
     * Returns the minimum time.
     *
     * @return minimum time
     */
    public final long getMinimumTime()
    {
        return (minimumTime == Long.MAX_VALUE) ? 0 : minimumTime;
    }

    /**
     * Setup data and provider mapping for efficiency
     */
    private void setupDataToProviderMapping()
    {
        mapping = new HashMap<>();

        for (ReportProvider p : reportProviders)
        {
            final Data[] dataClasses = p.supportedDataClasses();
            
            if (dataClasses.length > 0)
            {
                for (Data d : dataClasses)
                {
                    mapping.compute(p, (k, v) -> {
                        if (v == null)
                        {
                            v = new ArrayList<>();
                        }
                        v.add(d);

                        return v;    
                    });
                }
            }
            else
            {
                // we don't need a thing
                mapping.put(p, new ArrayList<>(0));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        // setup data classes to be able to selectively send data
        setupDataToProviderMapping();

        // use the recommendation from the outside
        final ForkJoinPool pool = new ForkJoinPool(threadCount);

        while (true)
        {
            try
            {
                // get a chunk of parsed data records
                final SimpleArrayList<Data> dataRecords = dispatcher.getNextParsedDataRecordChunk();
                final List<List<Data>> subDataRecords = dataRecords.partition(20);
                
                // submit this to all report providers and each provider does its own loop
                // we assume that they are independent of each other and hence this is ok
                final ForkJoinTask<?>[] tasks = new ForkJoinTask[reportProviders.length];
                
                for (int d = 0; d < subDataRecords.size(); d++)
                {
                    final List<Data> data = subDataRecords.get(d);
                    
                    for (int i = 0; i < reportProviders.length; i++)
                    {   
                        final ReportProvider reportProvider = reportProviders[i]; 
    
                        // give all data to each process threads for one report provider aka SIMD
                        // single instruction multiple data
                        final ForkJoinTask<?> task = pool.submit(() -> {
                            synchronized(reportProvider)
                            {
                                processDataRecords(reportProvider, data);
                            }
                        });
                        tasks[i] = task;
                    }
                }
                
                maintainStatistics(dataRecords);

                // wait for completion
                for (int i = 0; i < tasks.length; i++)
                {
                    tasks[i].quietlyJoin();
                }

                // one more chunk is complete
                dispatcher.finishedProcessing();
            }
            catch (final InterruptedException e)
            {
                break;
            }
        }

        // clean up
        pool.shutdown();
        try
        {
            // that should not be necessary, but for the argument of it
            pool.awaitTermination(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e1)
        {
        }
    }

    /**
     * Processes the given data records by passing them to a report provider.
     *
     * @param reportProvider
     *            the report provider
     * @param data
     *            the data records
     */
    private void processDataRecords(final ReportProvider reportProvider, final List<Data> data)
    {
        // process the data
        try
        {
            final int size = data.size();
            for (int i = 0; i < size; i++)
            {
                final Data d = data.get(i);
                reportProvider.processDataRecord(d);
            }
        }
        catch (final Throwable t)
        {
            LOG.error("Failed to process data record, discarding a full chunk!", t);
        }
    }


    /**
     * Maintain our statistics
     *
     * @param data
     *            the data records
     */
    private void maintainStatistics(final List<Data> data)
    {
        long min = minimumTime;
        long max = maximumTime;

        // process the data
        final int size = data.size();
        for (int i = 0; i < size; i++)
        {
            // maintain statistics
            final long time = data.get(i).getTime();

            min = Math.min(min, time);
            max = Math.max(max, time);
        }

        minimumTime = Math.min(minimumTime, min);
        maximumTime = Math.max(maximumTime, max);
    }
}
