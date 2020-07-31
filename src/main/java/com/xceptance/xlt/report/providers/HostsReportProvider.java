package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;

/**
 * Provides basic statistics for the hosts visited during the test.
 */
public class HostsReportProvider extends AbstractReportProvider
{
    /**
     * A mapping from host names to their corresponding {@link HostReport} objects.
     */
    private final Map<String, HostReport> hostReports = new HashMap<String, HostReport>(11);

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

            final String hostName = reqData.getHost();
            
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
