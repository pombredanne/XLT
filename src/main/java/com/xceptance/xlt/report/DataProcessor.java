package com.xceptance.xlt.report;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;

import com.xceptance.common.util.StringMatcher;
import com.xceptance.common.util.SynchronizingCounter;
import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.agent.CustomSamplersRunner;
import com.xceptance.xlt.agent.JvmResourceUsageDataGenerator;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule;

/**
 * Processor for the chain file to log line to parsed log line via 
 * log line filter and transformation to finally the statistics part
 * where the report provider will collect there data
 * 
 * DataProcessor
 * +- file reading pool
 * +- line processing pool
 * -> StatisicsProcessor (single thread)
 *
 * @see DataRecordReader
 * @see DataRecordParser
 * @see StatisticsProcessor
 */
public class DataProcessor
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(DataProcessor.class);

    /**
     * The executor dealing with the data record parser threads.
     */
    private final ExecutorService dataRecordParserExecutor;

    /**
     * The one and only data record processor.
     */
    private final StatisticsProcessor dataRecordProcessor;

    /**
     * The executor dealing with the data record reader threads.
     */
    private final ExecutorService dataRecordReaderExecutor;

    /**
     * The dispatcher that coordinates all the reader/parser/processor threads.
     */
    private final Dispatcher dispatcher;

    /**
     * Input directory.
     */
    private final FileObject inputDir;

    /**
     * The total number of lines/data records read.
     */
    private final AtomicInteger totalLinesCounter = new AtomicInteger();

    /**
     * The filter to skip the results of certain test cases when reading.
     */
    private final StringMatcher testCaseFilter;

    /**
     * Only include the results of those agents that match this filter.
     */
    private final StringMatcher agentFilter;

    /**
     * Constructor.
     *
     * @param inputDir
     *            input directory
     * @param dataRecordFactory
     *            data record factory
     * @param fromTime
     *            time when report starts
     * @param toTime
     *            time when report ends
     * @param reportProviders
     *            report providers
     * @param requestMergeRules
     *            the request merge rules
     * @param maxThreadCount
     *            the number of parallel threads allowed
     * @param testCaseIncludePatternList
     *            a comma-separated list of reg-ex patterns that match the test cases to read
     * @param testCaseExcludePatternList
     *            a comma-separated list of reg-ex patterns that match the test cases not to read
     * @param agentIncludePatternList
     *            a comma-separated list of reg-ex patterns that match the result directories of those agents to read
     * @param agentExcludePatternList
     *            a comma-separated list of reg-ex patterns that match the result directories of those agents to skip
     * @param removeIndexesFromRequestNames
     *            whether to automatically remove any indexes from request names
     */
    public DataProcessor(
                     final ReportGeneratorConfiguration config,
                     final FileObject inputDir, final DataRecordFactory dataRecordFactory, final long fromTime, final long toTime,
                     final List<ReportProvider> reportProviders, 
                     final String testCaseIncludePatternList, final String testCaseExcludePatternList,
                     final String agentIncludePatternList, final String agentExcludePatternList)
    {
        this.inputDir = inputDir;
        
        testCaseFilter = new StringMatcher(testCaseIncludePatternList, testCaseExcludePatternList, true);
        agentFilter = new StringMatcher(agentIncludePatternList, agentExcludePatternList, true);

        // the one and only data record processor
        dataRecordProcessor = new StatisticsProcessor(reportProviders, config.statisticsThreadCount);

        // create the dispatcher
        dispatcher = new Dispatcher(config, dataRecordProcessor);

        // create the reader executor
        dataRecordReaderExecutor = Executors.newFixedThreadPool(config.readerThreadCount, new DaemonThreadFactory(i -> "DataRecordReader-" + i, Thread.MAX_PRIORITY));

        // create the data record parser threads
        dataRecordParserExecutor = Executors.newFixedThreadPool(config.parserThreadCount, new DaemonThreadFactory(i -> "DataRecordParser-" + i));
        
        // start the threads
        for (int i = 0; i < config.parserThreadCount; i++)
        {
            dataRecordParserExecutor.execute(new DataRecordParser(dataRecordFactory, 
                                                                  fromTime, toTime, 
                                                                  config.getRequestProcessingRules(), dispatcher,
                                                                  config.getRemoveIndexesFromRequestNames()));
        }

        LOG.info(String.format("Reading files from input directory '%s' ...\n", inputDir));
    }

    /**
     * Returns the maximum time.
     *
     * @return maximum time
     */
    public final long getMaximumTime()
    {
        return dataRecordProcessor.getMaximumTime();
    }

    /**
     * Returns the minimum time.
     *
     * @return minimum time
     */
    public final long getMinimumTime()
    {
        return dataRecordProcessor.getMinimumTime();
    }

    /**
     * Reads all the data records from the configured input directory.
     */
    public void readDataRecords()
    {
        try
        {
            final long start = TimerUtils.getTime();

            for (final FileObject file : inputDir.getChildren())
            {
                if (file.getType() == FileType.FOLDER)
                {
                    // Check if we need to process the current agent directory
                    final String directoryName = file.getName().getBaseName();
                    if (agentFilter.isAccepted(directoryName))
                    {
                        readDataRecordsFromAgentDir(file);
                    }
                }
            }

            // wait for the data processing to finish
            dispatcher.waitForDataRecordProcessingToComplete();

            final long duration = TimerUtils.getTime() - start;
            
            final int totalLines = totalLinesCounter.get();
            final long linesPerSecond = (totalLinesCounter.get() * 1000l) / Math.max(1, (int) duration);
            
            LOG.info(String.format("Data records read: %,d (%,d ms) - (%,d lines/s)\n", 
                                   totalLines, duration, linesPerSecond));
        }
        catch (final Exception e)
        {
            LOG.error("Failed to read data records", e);
        }
        finally
        {
            // stop background threads
            dataRecordParserExecutor.shutdownNow();
            dataRecordReaderExecutor.shutdownNow();
        }
    }

    /**
     * Reads the timer files from the given agent directory.
     *
     * @param agentDir
     *            agent directory
     * @throws IOException
     *             thrown on I/O-Error
     */
    private void readDataRecordsFromAgentDir(final FileObject agentDir) throws Exception
    {
        for (final FileObject file : agentDir.getChildren())
        {
            if (file.getType() == FileType.FOLDER)
            {
                // filter out certain directories if so configured
                final String directoryName = file.getName().getBaseName();
                if (isSpecialDirectory(directoryName) || testCaseFilter.isAccepted(directoryName))
                {
                    readDataRecordsFromTestCaseDir(file, agentDir.getName().getBaseName());
                }
            }
        }
    }

    /**
     * Reads the timer files from the given test case directory.
     *
     * @param testCaseDir
     *            test case directory
     * @param agentName
     *            the associated agent
     * @throws IOException
     *             thrown on I/O-Error
     */
    private void readDataRecordsFromTestCaseDir(final FileObject testCaseDir, final String agentName) throws Exception
    {
        final String testCaseName = testCaseDir.getName().getBaseName();

        for (final FileObject file : testCaseDir.getChildren())
        {
            if (file.getType() == FileType.FOLDER)
            {
                readDataRecordsFromTestUserDir(file, agentName, testCaseName);
            }
        }
    }

    /**
     * Reads the timer files from the given test user directory.
     *
     * @param testUserDir
     *            test user directory
     * @param agentName
     *            the associated agent
     * @param testCaseName
     *            the associated test case
     * @throws IOException
     *             thrown on I/O-Error
     */
    private void readDataRecordsFromTestUserDir(final FileObject testUserDir, final String agentName, final String testCaseName)
        throws Exception
    {
        dispatcher.incremementDirectoryCount();
        
        // create a new reader for each user directory and enqueue it for execution
        final String userNumber = testUserDir.getName().getBaseName();
        final DataRecordReader reader = new DataRecordReader(testUserDir, agentName, testCaseName, userNumber, totalLinesCounter,
                                                             dispatcher);
        dataRecordReaderExecutor.execute(reader);
    }

    /**
     * Determines whether the given directory name denotes a special directory. Special directories are
     * "Agent-JVM-Monitor" and "CustomSampler".
     *
     * @param directoryName
     *            the directory to check
     * @return whether the directory is special
     */
    private boolean isSpecialDirectory(String directoryName)
    {
        return CustomSamplersRunner.RESULT_DIRECTORY_NAME.equals(directoryName) ||
               JvmResourceUsageDataGenerator.RESULT_DIRECTORY_NAME.equals(directoryName);
    }
}
