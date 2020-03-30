package com.xceptance.xlt.report;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.xceptance.common.util.SimpleArrayList;
import com.xceptance.common.util.SynchronizingCounter;
import com.xceptance.xlt.api.engine.Data;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * The {@link Dispatcher} is responsible to coordinate the various reader/parser/processor threads involved when
 * processing test results. It does not only pass the results from one thread to another, but makes sure as well that no
 * more than X threads are active at the same time.
 *
 * @see DataRecordReader
 * @see DataRecordParser
 * @see StatisticsProcessor
 */
public class Dispatcher
{
    /**
     * The maximum number of lines in a chunk.
     */
    public static final int DEFAULT_QUEUE_CHUNK_SIZE = 1000;

    /**
     * The maximum number of lines in a chunk.
     */
    public static final int DEFAULT_QUEUE_LENGTH = 50;
    
    /**
     * The number of directories that still need to be processed.
     */
    private final SynchronizingCounter remainingDirectories = new SynchronizingCounter();

    /**
     * Total number of directories to be or already have been processed
     */
    private final SynchronizingCounter totalDirectories = new SynchronizingCounter();
    
    /**
     * The number of chunks that still need to be processed.
     */
    private final SynchronizingCounter chunksToBeProcessed = new SynchronizingCounter();

    /**
     * The line chunks waiting to be parsed.
     */
    private final BlockingQueue<LineChunk> lineChunkQueue;

    /**
     * The data record chunks waiting to be send to the statistics providers.
     */
    private final BlockingQueue<SimpleArrayList<Data>> dataRecordChunkQueue;

    /**
     * Size of the chunks in the queues
     */
    public final int chunkSize;
    
    /**
     * Our progress bar
     */
    private final ProgressBar progressBar = new ProgressBar("Reading", 100, ProgressBarStyle.ASCII);
   
    /**
     * Creates a new {@link Dispatcher} object with the given thread limit.
     *
     * @param directoriesToBeProcessed
     *            the number of directories that need to be processed
     * @param maxActiveThreads
     *            the maximum number of active threads
     */
    public Dispatcher(final ReportGeneratorConfiguration config)
    {
        lineChunkQueue = new LinkedBlockingQueue<>(config.threadQueueLength);
        dataRecordChunkQueue = new LinkedBlockingQueue<>(config.threadQueueLength);
        chunkSize = config.threadQueueBucketSize;
    }

    /**
     * Count the directories to be processed up by one
     */
    public void incremementDirectoryCount()
    {
        totalDirectories.increment();
        remainingDirectories.increment();
    }
    
    /**
     * Indicates that a reader thread is about to begin reading. Called by a reader thread.
     */
    public void beginReading() throws InterruptedException
    {
        progressBar.maxHint(totalDirectories.get());
        progressBar.step();
    }

    /**
     * Adds a new chunk of lines for further processing. Called by a reader thread.
     *
     * @param lineChunk
     *            the line chunk
     */
    public void addNewLineChunk(final LineChunk lineChunk) throws InterruptedException
    {
        chunksToBeProcessed.increment();
        lineChunkQueue.put(lineChunk);
    }

    /**
     * Indicates that a reader thread has finished reading. Called by a reader thread.
     */
    public void finishedReading()
    {
        remainingDirectories.decrement();
        progressBar.maxHint(totalDirectories.get());
    }

    /**
     * Returns a chunk of lines for further processing. Called by a parser thread.
     *
     * @return the line chunk
     */
    public LineChunk getNextLineChunk() throws InterruptedException
    {
        final LineChunk c = lineChunkQueue.take();
        return c;
    }

    /**
     * Adds a new chunk of parsed data records for further processing. Called by a parser thread.
     *
     * @param dataRecordChunk
     *            the data record chunk
     */
    public void addNewParsedDataRecordChunk(final SimpleArrayList<Data> dataRecordChunk) throws InterruptedException
    {
        dataRecordChunkQueue.put(dataRecordChunk);
    }

    /**
     * Returns a chunk of parsed data records for further processing. Called by a processor thread.
     *
     * @return the data record chunk
     */
    public SimpleArrayList<Data> getNextParsedDataRecordChunk() throws InterruptedException
    {
        final SimpleArrayList<Data> data = dataRecordChunkQueue.take();
        return data;
    }

    /**
     * Indicates that a processor thread has finished processing. Called by a processor thread.
     */
    public void finishedProcessing()
    {
        chunksToBeProcessed.decrement();
    }

    /**
     * Waits until data record processing is complete. Called by the main thread.
     *
     * @throws InterruptedException
     */
    public void waitForDataRecordProcessingToComplete() throws InterruptedException
    {
        // wait for the readers to complete
        remainingDirectories.awaitZero();

        // wait for the data processor thread to finish data record chunks
        chunksToBeProcessed.awaitZero();
        
        // stop progress
        progressBar.close();
    }
    
    /**
     * Return the number of remaining directories
     * 
     * @return remaining directory to be processed
     */
    public int getRemainingDirectoryCount()
    {
        return remainingDirectories.get();
    }
    
    /**
     * Return the number of remaining or processed directory
     * 
     * @return total number of processed or to be processed directory
     */
    public int getTotalDirectoryCount()
    {
        return totalDirectories.get();
    }
}
