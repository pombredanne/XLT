package com.xceptance.xlt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.threadly.concurrent.wrapper.KeyDistributedExecutor;

import com.xceptance.common.util.SimpleArrayList;
import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.ReportProvider;

/**
 * Processes parsed data records. Processing means passing a data record to all configured report providers. Since data
 * processing is not thread-safe (yet), there will be only one data processor.
 */
class StatisticsProcessor
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(StatisticsProcessor.class);

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
    private final List<ReportProvider> reportProviders;
    //    private final List<ExecutorService> reportProvidersExecutors;
    private final KeyDistributedExecutor statisticsMaintenanceExecutor;

    /**
     * Constructor.
     *
     * @param reportProviders
     *            the configured report providers
     * @param dispatcher
     *            the dispatcher that coordinates result processing
     */
    public StatisticsProcessor(final List<ReportProvider> reportProviders, final int threadCount)
    {
        this.reportProviders = reportProviders;
        //        this.reportProvidersExecutors = new ArrayList<>();
        //
        //        for (int i = 0; i < reportProviders.size(); i++)
        //        {
        //            final ReportProvider r = reportProviders.get(i);
        //            reportProvidersExecutors.add(Executors.newSingleThreadExecutor(new DaemonThreadFactory(c -> r.getClass().getSimpleName() + "-" + c)));
        //        }

        statisticsMaintenanceExecutor = new KeyDistributedExecutor(Executors.newFixedThreadPool(threadCount, new DaemonThreadFactory(c -> "Providers-" + c)));

        maximumTime = 0;
        minimumTime = Long.MAX_VALUE;
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

    public void run(final SimpleArrayList<Data> data)
    {
        final List<Future<?>> tasks = new ArrayList<>();

        tasks.add(statisticsMaintenanceExecutor.submit(data, () -> 
        {
            maintainStatistics(data);
        }));

        for (int i = 0; i < reportProviders.size(); i++)
        {
            final ReportProvider reportProvider = reportProviders.get(i);

            tasks.add(statisticsMaintenanceExecutor.submit(reportProvider, () ->
            {
                final int size = data.size();
                for (int d = 0; d < size; d++)
                {
                    final Data record = data.get(d);

                    try
                    {
                        reportProvider.processDataRecord(record);
                    }
                    catch (final Throwable t)
                    {
                        LOG.error("Failed to process data record", t);
                    }
                }
            }));
        }

        for (int i = 0; i < tasks.size(); i++)
        {
            try
            {
                tasks.get(i).get();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
        final int size = data.size();
        for (int i = 0; i < size; i++)
        {
            try
            {
                reportProvider.processDataRecord(data.get(i));
            }
            catch (final Throwable t)
            {
                LOG.error("Failed to process data record", t);
            }
        }
    }

    /**
     * Maintain our statistics
     *
     * @param data
     *            the data records
     */
    private synchronized void maintainStatistics(final List<Data> data)
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

        minimumTime = min;
        maximumTime = max;
    }
}
