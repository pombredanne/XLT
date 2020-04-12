package com.xceptance.xlt.report;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.util.SimpleArrayList;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule;
import com.xceptance.xlt.report.mergerules.RequestProcessingRuleResult;

/**
 * Parses lines to data records and performs any data record preprocessing that can be done in parallel. Preprocessing
 * also includes executing request merge rules.
 */
class DataPostprocessingThread implements Runnable
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(DataPostprocessingThread.class);

    /**
     * The dispatcher that coordinates result processing.
     */
    private final Dispatcher dispatcher;

    /**
     * The request processing rules.
     */
    private final List<RequestProcessingRule> requestProcessingRules;

    /**
     * Whether to automatically remove any indexes from the request name (i.e. "HomePage.1.27" -> "HomePage").
     */
    private final boolean removeIndexesFromRequestNames;

    /**
     * Constructor.
     *
     * @param dataRecordFactory
     *            the data record factory
     * @param fromTime
     *            the start time
     * @param toTime
     *            the end time
     * @param requestProcessingRules
     *            the request processing rules
     * @param dispatcher
     *            the dispatcher that coordinates result processing
     * @param removeIndexesFromRequestNames
     *            whether to automatically remove any indexes from request names
     */
    public DataPostprocessingThread(final Dispatcher dispatcher, final List<RequestProcessingRule> requestProcessingRules, boolean removeIndexesFromRequestNames)
    {
        this.requestProcessingRules = requestProcessingRules;
        this.dispatcher = dispatcher;
        this.removeIndexesFromRequestNames = removeIndexesFromRequestNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                // parse the chunk of lines and preprocess the results
                final SimpleArrayList<Data> records = dispatcher.retrieveParsedData();
                final int size = records.size();

                final SimpleArrayList<Data> result = new SimpleArrayList<>(size);

                for (int i = 0; i < size; i++)
                {
                    final Data preprocessedData = preprocessDataRecord(records.get(i));
                    if (preprocessedData != null)
                    {
                        result.add(preprocessedData);
                    }
                }

                // deliver the chunk of parsed data records
                dispatcher.addPostprocessedData(result);
            }
            catch (final InterruptedException e)
            {
                break;
            }
        }
    }

    /**
     * Preprocesses the passed data record.
     *
     * @param data
     *            the data record
     * @return the preprocessed data record, or <code>null</code> if the data record is to be discarded
     */
    private Data preprocessDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            return processRequestData((RequestData) data);
        }
        else
        {
            return data;
        }
    }

    /**
     * Processes a request according to the configured request processing rules. Currently, this means renaming or
     * discarding requests.
     *
     * @param requestData
     *            the request data record
     * @return the processed request data record, or <code>null</code> if the data record is to be discarded
     */
    private RequestData processRequestData(RequestData requestData)
    {
        // fix up the name first (Product.1.2 -> Product) if so configured
        if (removeIndexesFromRequestNames)
        {
            // @TODO Chance for more performance here
            String requestName = requestData.getName();

            final int firstDotPos = requestName.indexOf(".");
            if (firstDotPos > 0)
            {
                requestName = requestName.substring(0, firstDotPos);
                requestData.setName(requestName);
            }
        }

        // remember the original name so we can restore it in case request processing fails
        final String originalName = requestData.getName();

        // execute all processing rules one after the other until processing is complete
        final int size = requestProcessingRules.size();
        for (int i = 0; i < size; i++)
        {
            final RequestProcessingRule requestProcessingRule = requestProcessingRules.get(i);

            try
            {
                final RequestProcessingRuleResult result = requestProcessingRule.process(requestData);

                requestData = result.requestData;

                if (result.stopRequestProcessing)
                {
                    break;
                }
            }
            catch (final Throwable t)
            {
                final String msg = String.format("Failed to apply request merge rule: %s\n%s", requestProcessingRule, t);
                LOG.error(msg);

                // restore the request's original name
                requestData.setName(originalName);

                break;
            }
        }

        return requestData;
    }
}
