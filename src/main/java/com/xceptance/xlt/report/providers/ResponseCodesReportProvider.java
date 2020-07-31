package com.xceptance.xlt.report.providers;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.http.impl.EnglishReasonPhraseCatalog;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * 
 */
public class ResponseCodesReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from response codes to their corresponding {@link ResponseCodeReport} objects.
     */
    private final ResponseCodeReport[] responseCodeReports = new ResponseCodeReport[1000];

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ResponseCodesReport report = new ResponseCodesReport();

        report.responseCodes = Arrays.asList(responseCodeReports).stream().filter(e -> e != null).collect(Collectors.toList());

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        if (stat instanceof RequestData)
        {
            final RequestData reqStats = (RequestData) stat;

            final int code = reqStats.getResponseCode();

            ResponseCodeReport responseCodeReport = responseCodeReports[code];
            if (responseCodeReport == null)
            {
                responseCodeReport = new ResponseCodeReport();
                responseCodeReport.code = code;
                responseCodeReport.statusText = getStatusText(code);

                responseCodeReports[code] = responseCodeReport;
            }

            responseCodeReport.count++;
        }
    }

    /**
     * Returns the corresponding status text for the given HTTP status code.
     * 
     * @param statusCode
     *            the status code
     * @return the status text
     */
    private String getStatusText(final int statusCode)
    {
        String statusText;

        if (statusCode == 0)
        {
            // our status code for network-related problems
            statusText = "No Response Available";
        }
        else
        {
            try
            {
                statusText = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null);
            }
            catch (final IllegalArgumentException e)
            {
                // status code was other than 1xx...5xx
                statusText = null;
            }

            statusText = (statusText == null) ? "???" : statusText;
        }

        return statusText;
    }
}
