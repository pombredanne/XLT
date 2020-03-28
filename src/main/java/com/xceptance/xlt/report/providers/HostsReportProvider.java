package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.xceptance.common.collection.LRUHashMap;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.engine.util.UrlUtils;

/**
 * Provides basic statistics for the hosts visited during the test.
 */
public class HostsReportProvider extends AbstractReportProvider
{
    /**
     * The value to show if the host could not be determined from a URL.
     */
    private static final String UNKNOWN_HOST = "(unknown)";

    /**
     * local cache to speed up processing because host retrieval is expensive
     */
    private static final LRUHashMap<String, String> cache = new LRUHashMap<>(7001);
    
    /**
     * A mapping from host names to their corresponding {@link HostReport} objects.
     */
    private final Map<String, HostReport> hostReports = new HashMap<String, HostReport>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final HostsReport report = new HostsReport();

        report.hosts = new ArrayList<HostReport>(hostReports.values());

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            final RequestData reqData = (RequestData) data;

            // determine the host name
            
            final String url = reqData.getUrl();
            
            final String hostName;
            if (url == null || url.length() == 0)
            {
                hostName = UNKNOWN_HOST;
            }
            else
            {
                final String result = cache.get(url);
                
                if (result == null)
                {
                    hostName = UrlUtils.retrieveHostFromUrl(url);
                    cache.put(url, hostName);
                }
                else
                {
                    hostName = result;
                }
            }

            // get/create the respective host report
            HostReport hostReport = hostReports.get(hostName);
            if (hostReport == null)
            {
                hostReport = new HostReport();
                hostReport.name = hostName;

                hostReports.put(hostName, hostReport);
            }

            // update the statistics
            hostReport.count++;
        }
    }
}
