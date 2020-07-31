package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * Provides basic content type statistics.
 */
public class ContentTypesReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from content types to their corresponding {@link ContentTypeReport} objects.
     */
    private final Map<String, ContentTypeReport> contentTypeReports = new HashMap<String, ContentTypeReport>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ContentTypesReport report = new ContentTypesReport();

        report.contentTypes = new ArrayList<ContentTypeReport>(contentTypeReports.values());

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

            final String contentType = reqStats.getContentType();
            
            // the content type is never null, it might be just "" and if this is " " or similar
            // we don't care and keep the speed, (none is set where it is produced)
//            if (contentType.length() == 0)
//            {
//                contentType = "(none)";
//            }

            ContentTypeReport contentTypeReport = contentTypeReports.get(contentType);
            if (contentTypeReport == null)
            {
                contentTypeReport = new ContentTypeReport();
                contentTypeReport.contentType = contentType;

                contentTypeReports.put(contentType, contentTypeReport);
            }

            contentTypeReport.count++;
        }
    }
}
