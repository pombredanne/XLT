package com.xceptance.xlt.mastercontroller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.TestResultAmount;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.report.ReportGenerator;
import com.xceptance.xlt.util.AgentControllerException;
import com.xceptance.xlt.util.ConcurrencyUtils;
import com.xceptance.xlt.util.FailedAgentControllerCollection;
import com.xceptance.xlt.util.FileReplicationIndex;
import com.xceptance.xlt.util.FileReplicationUtils;
import com.xceptance.xlt.util.ProgressBar;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 */
public class MasterController
{
    /**
     * The log facility of this class.
     */
    private static final Log LOG = LogFactory.getLog(MasterController.class);

    /**
     * All agent controllers known to this master controller. The key is the agent controller's name.
     */
    private final Map<String, AgentController> agentControllerMap;

    /**
     * The directory where the agent files are stored in.
     */
    private final File agentFilesDirectory;

    /**
     * The current load profile. Changes only after uploading agent files.
     */
    private TestLoadProfileConfiguration currentLoadProfile;

    /**
     * The name of the test that is currently running. Changes with every started test.
     */
    private String currentTestCaseName;

    /**
     * The current test result directory. Changes when downloading test results.
     */
    private File currentTestResultsDir;

    /**
     * The list with all agent controllers known to this master controller.
     */
    private final TestDeployer testDeployer;

    /**
     * The master controller's temp directory.
     */
    private final File tempDirectory;

    /**
     * The user interface this master controller will interact with.
     */
    private MasterControllerUI userInterface;

    /**
     * Test comment.
     */
    private String testComment;

    /**
     * The name of the file with test-run specific properties.
     */
    private final String propertiesFileName;

    /**
     * Is the agent controller connection relaxed? If yes, broken connections get skipped.
     */
    private final boolean isAgentControllerConnectionRelaxed;

    /**
     * ID of timezone to use for report generation.
     */
    private final String reportTimezoneId;

    private final boolean isEmbedded;

    private final ThreadPoolExecutor commonExecutor;

    private final ThreadPoolExecutor defaultCommunicationExecutor;

    private final ThreadPoolExecutor uploadExecutor;

    private final ThreadPoolExecutor downloadExecutor;

    /**
     * The agent controller's statuses. The key is the agent controller's name.
     */
    private final Map<String, AgentControllerStatus> statuses = new ConcurrentHashMap<String, AgentControllerStatus>();

    /**
     * Running status querying jobs will not proceed if this boolean is set to <code>false</code>.
     */
    private final AtomicBoolean isStatusRequesting = new AtomicBoolean(false);

    /**
     * The root directory where new directories with test results are to be stored in.
     */
    private final File testResultsRootDirectory;

    /**
     * Result output directory override as specified on command line.
     */
    private final File resultOutputDirectory;

    /**
     * Whether or not the test was explicitly stopped by the user.
     */
    private boolean stoppedByUser = false;

    /**
     * Creates a new MasterController object.
     * 
     * @param agentControllerMap
     *            the list of agent controllers
     * @param config
     *            the master controller configuration
     * @param testPropertyFileName
     *            the name of the file with test-run specific properties
     * @param isAgentControllerConnectionRelaxed
     *            is agent controller connection relaxed
     * @param reportTimezoneId
     *            the ID of the timezone to use for report generation (may be <code>null</code>)
     */
    public MasterController(final Map<String, AgentController> agentControllerMap, final MasterControllerConfiguration config,
                            final String testPropertyFileName, final boolean isAgentControllerConnectionRelaxed,
                            final String reportTimezoneId)
    {
        this.agentControllerMap = agentControllerMap;

        testDeployer = new TestDeployer(agentControllerMap);
        agentFilesDirectory = config.getAgentFilesDirectory();
        tempDirectory = config.getTempDirectory();
        propertiesFileName = testPropertyFileName;
        this.isAgentControllerConnectionRelaxed = isAgentControllerConnectionRelaxed;
        this.reportTimezoneId = reportTimezoneId;

        final int parallelCommunicationLimit = config.getParallelCommunicationLimit();
        final int parallelUploadLimit = config.getParallelUploadLimit();
        final int parallelDownloadLimit = config.getParallelDownloadLimit();

        commonExecutor = ConcurrencyUtils.getNewThreadPoolExecutor("MC-pool");
        defaultCommunicationExecutor = ConcurrencyUtils.getNewThreadPoolExecutor("AC-communication-default-pool",
                                                                                 parallelCommunicationLimit);
        uploadExecutor = ConcurrencyUtils.getNewThreadPoolExecutor("AC-communication-upload-pool", parallelUploadLimit);
        downloadExecutor = ConcurrencyUtils.getNewThreadPoolExecutor("AC-communication-download-pool", parallelDownloadLimit);

        testResultsRootDirectory = config.getTestResultsRootDirectory();
        resultOutputDirectory = config.getResultOutputDirectory();

        isEmbedded = config.isEmbedded();

        checkTestPropertiesFileName();
    }

    /**
     * Checks if the passed test properties file name is valid.
     * 
     * @throws IllegalArgumentException
     *             thrown if file path is absolute, does not exist, cannot be read or does not reside in test suite's
     *             configuration directory.
     */
    private void checkTestPropertiesFileName()
    {
        // check if there is something to do
        if (StringUtils.isBlank(propertiesFileName))
        {
            return;
        }

        // test properties files must not be absolute
        if (new File(propertiesFileName).isAbsolute())
        {
            final String msg = "Parameter '%s' is invalid, because its value is not a relative path -> [%s]";
            throw new IllegalArgumentException(String.format(msg, "testPropertiesFile", propertiesFileName));
        }

        final File agentConfDir = new File(agentFilesDirectory, "config");
        final File testPropFile = new File(agentConfDir, propertiesFileName);

        // check if file exists and can be read
        ParameterCheckUtils.isReadableFile(testPropFile, "testPropertiesFile");

        // no check if test suite's configuration directory contains specified properties file
        boolean valid = false;
        try
        {
            valid = FileUtils.directoryContains(agentConfDir, testPropFile);
        }
        catch (IOException ioe)
        {
        }

        if (!valid)
        {
            final String msg = "Parameter '%s' is invalid, because its value does not point to a file inside directory '%s' -> [%s]";
            throw new IllegalArgumentException(String.format(msg, "testPropertiesFile", agentConfDir.getAbsolutePath(),
                                                             propertiesFileName));
        }

    }

    /**
     * Downloads the test results from all configured agent controllers at once.
     * 
     * @param testResultAmount
     *            the amount of test result data to download
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     */
    public boolean downloadTestResults(final TestResultAmount testResultAmount)
    {
        currentTestResultsDir = resultOutputDirectory;
        if (currentTestResultsDir == null)
        {
            currentTestResultsDir = getTestResultsDirectory(testResultsRootDirectory, currentTestCaseName);
        }

        // If the test is still running we will tag the directory as "intermediate results"
        if (!stoppedByUser && isAnyAgentRunning_SAFE())
        {
            String intermediateResultsPath = currentTestResultsDir.getPath() + "-intermediate";
            currentTestResultsDir = new File(intermediateResultsPath);
        }

        final ArrayList<AgentController> agentControllers = new ArrayList<AgentController>(agentControllerMap.values());
        final int agentControllerSize = agentControllers.size();

        // Progress count
        //
        // test config : 3
        // time data : agentControllers.size() + 1
        // archiving : agentControllers.size()
        // archive download : 5 * agentControllers.size()
        // --
        // sum : 7 * agentControllers.size() + 4
        final ProgressBar progress = startNewProgressBar(agentControllerSize > 0 ? 7 * agentControllerSize + 4 : 0);

        // download results
        final ResultDownloader resultDownloader = new ResultDownloader(downloadExecutor, currentTestResultsDir, tempDirectory,
                                                                       agentControllers, progress);
        final boolean downloadSuccess = resultDownloader.download(testResultAmount);

        // inform user
        final FailedAgentControllerCollection failedAgentControllers = resultDownloader.getFailedAgentControllerCollection();
        userInterface.testResultsDownloaded(failedAgentControllers);

        // We have downloaded results successfully
        // AND
        // We have either no failed agentcontroller OR at least 1 agent controller succeeded in case of relaxed
        // connection
        return downloadSuccess && (failedAgentControllers.isEmpty() ||
                                   (isAgentControllerConnectionRelaxed && failedAgentControllers.getMap().size() < agentControllerSize));
    }

    /**
     * Generates the test report from the test results downloaded last.
     * 
     * @param reportCreationType
     *            report creation type
     * @return true if the operation was successful; false otherwise
     */
    public boolean generateReport(final ReportCreationType reportCreationType)
    {
        boolean result = false;

        if (currentTestResultsDir != null)
        {
            final TimeZone systemTZ = TimeZone.getDefault();
            final TimeZone tz = reportTimezoneId != null ? TimeZone.getTimeZone(reportTimezoneId) : systemTZ;
            final boolean overrideTZ = !systemTZ.equals(tz);

            try
            {
                if (overrideTZ)
                {
                    TimeZone.setDefault(tz);
                }

                final FileObject testResultDir = VFS.getManager().resolveFile(currentTestResultsDir.toURI().toString());
                final ReportGenerator reportGenerator = new ReportGenerator(testResultDir, null, false);

                // get limit time range if necessary
                if (reportCreationType.equals(ReportCreationType.ALL))
                {
                    reportGenerator.generateReport(false);
                    result = true;
                }
                else if (reportCreationType.equals(ReportCreationType.NO_RAMPUP))
                {
                    reportGenerator.generateReport(true);
                    result = true;
                }
            }
            catch (final Exception ex)
            {
                LOG.error("Failed to generate report from the results in " + currentTestResultsDir, ex);
            }
            finally
            {
                if (overrideTZ)
                {
                    TimeZone.setDefault(systemTZ);
                }
            }
        }
        else
        {
            LOG.error("There are no downloaded results to generate a report from.");
        }

        return result;
    }

    /**
     * Ping the agent controllers.
     * 
     * @return the ping results keyed by agent controller name
     */
    public Map<String, PingResult> pingAgentControllers()
    {
        final Map<String, PingResult> pingResults = Collections.synchronizedMap(new TreeMap<String, PingResult>());

        // ping agent controllers
        final CountDownLatch latch = new CountDownLatch(agentControllerMap.size());
        for (final AgentController agentcontroller : agentControllerMap.values())
        {
            defaultCommunicationExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    PingResult pingResult;

                    try
                    {
                        TimerUtils.setUseHighPrecisionTimer(true);

                        final long pingStartTime = TimerUtils.getTime();
                        agentcontroller.ping();
                        final long pingTime = TimerUtils.getTime() - pingStartTime;

                        pingResult = new PingResult(pingTime);
                    }
                    catch (final Exception e)
                    {
                        LOG.error("Failed to ping agent controller: " + agentcontroller, e);
                        pingResult = new PingResult(e);
                    }

                    pingResults.put(agentcontroller.toString(), pingResult);

                    latch.countDown();
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for ping results to complete has failed", e);
        }

        return pingResults;
    }

    /**
     * Print the current agent controller information.
     * 
     * @return <code>true</code> if there are agent controller information, <code>false</code> otherwise
     */
    public AgentControllersInformation getAgentControllerInformation()
    {
        return new AgentControllersInformation(agentControllerMap.values(), defaultCommunicationExecutor);
    }

    /**
     * Returns the names of the test cases, which are active in the current load profile.
     * 
     * @return the test case names
     */
    public Set<String> getActiveTestCaseNames()
    {
        if (currentLoadProfile == null)
        {
            return Collections.emptySet();
        }

        return currentLoadProfile.getActiveTestCaseNames();
    }

    /**
     * Start querying agent status list from agent controllers.
     */
    public void startAgentStatusList()
    {
        // inform user
        userInterface.receivingAgentStatus();
        // initialize global requesting status
        isStatusRequesting.set(true);
        // initialize single agent controller's requesting status
        final HashMap<String, AtomicBoolean> acRequestingStatus = getNewAgentControllerRequestingStatusMap();

        commonExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                // as long as requesting is desired
                while (isStatusRequesting.get())
                {
                    // request status from all agent controllers
                    for (final AgentController agentController : agentControllerMap.values())
                    {
                        // query agent controller if it's not still requesting only
                        final AtomicBoolean isStillRequesting = acRequestingStatus.get(agentController.getName());
                        if (!isStillRequesting.get())
                        {
                            // set requesting
                            isStillRequesting.set(true);
                            defaultCommunicationExecutor.execute(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // check whether the executor signaled this worker thread to quit
                                    if (Thread.currentThread().isInterrupted())
                                    {
                                        // yes, quit here
                                        return;
                                    }

                                    AgentControllerStatus agentControllerStatus;

                                    // query AC status
                                    try
                                    {
                                        LOG.debug("Getting agent status from " + agentController);
                                        final Set<AgentStatus> agentStatuses = agentController.getAgentStatus();
                                        agentControllerStatus = new AgentControllerStatus(agentStatuses);
                                        statuses.put(agentController.getName(), agentControllerStatus);
                                    }
                                    catch (final Exception e)
                                    {
                                        LOG.error("Failed getting agent status from " + agentController, e);
                                        agentControllerStatus = statuses.get(agentController.getName());
                                        if (agentControllerStatus == null)
                                        {
                                            statuses.put(agentController.getName(), new AgentControllerStatus(e));
                                            agentControllerStatus = statuses.get(agentController.getName());
                                        }
                                        agentControllerStatus.setException(e);
                                    }

                                    // reset AC requesting status
                                    isStillRequesting.set(false);
                                }
                            });
                        }
                    }

                    // no, wait some time
                    try
                    {
                        Thread.sleep(userInterface.getStatusListUpdateInterval() * 1000);
                    }
                    catch (final InterruptedException e)
                    {
                        // the executor signaled this worker thread to quit
                        return;
                    }
                }
            }
        });
    }

    public TestLoadProfileConfiguration getCurrentLoadProfile()
    {
        return currentLoadProfile;
    }

    private HashMap<String, AtomicBoolean> getNewAgentControllerRequestingStatusMap()
    {
        final HashMap<String, AtomicBoolean> acRequestingStatus = new HashMap<String, AtomicBoolean>();
        for (final AgentController agentController : agentControllerMap.values())
        {
            acRequestingStatus.put(agentController.getName(), new AtomicBoolean(false));
        }
        return acRequestingStatus;
    }

    /**
     * Stop querying agent status list from agent controllers.
     */
    public void stopAgentStatusList()
    {
        isStatusRequesting.set(false);
    }

    /**
     * Returns the status objects of each known agent controller. If there are problems while communicating with an
     * agent controller, the respective agent statuses will not be included in the list.
     * 
     * @return the status list
     */
    public Set<AgentStatus> getAgentStatusList()
    {
        final Queue<AgentStatus> allAgentStatuses = new ConcurrentLinkedQueue<AgentStatus>();

        final FailedAgentControllerCollection failedAgentcontrollers = new FailedAgentControllerCollection();

        userInterface.receivingAgentStatus();

        for (final AgentController agentController : agentControllerMap.values())
        {
            final AgentControllerStatus agentControllerStatus = statuses.get(agentController.getName());

            final Exception e = agentControllerStatus.getException();
            if (e != null)
            {
                failedAgentcontrollers.add(agentController, e);
            }

            final Set<AgentStatus> agentStatuses = agentControllerStatus.getAgentStatuses();
            if (agentStatuses != null)
            {
                for (final AgentStatus stat : agentStatuses)
                {
                    allAgentStatuses.add(stat);
                }
            }
        }

        userInterface.agentStatusReceived(failedAgentcontrollers);

        return new HashSet<AgentStatus>(allAgentStatuses);
    }

    /**
     * Creates a test deployment for the given load profile. If parameter testCaseName is non-null, the load profile is
     * modified such that only the given test is included in the load profile.
     * 
     * @param loadProfile
     *            the load profile
     * @param testCaseName
     *            the test case, or <code>null</code> to to include all tests
     * @return the test deployment
     */
    private TestDeployment getTestDeployment(TestLoadProfileConfiguration loadProfile, final String testCaseName)
    {
        if (testCaseName != null)
        {
            loadProfile = loadProfile.getTestLoadProfileConfiguration(testCaseName);
        }

        final TestDeployment testDeployment = testDeployer.createTestDeployment(loadProfile);
        // log.debug("Test Deployment:\n" + testDeployment);

        return testDeployment;
    }

    /**
     * Creates a sub directory, named after the current date and time as well as the current test case, in the given
     * directory.
     * 
     * @param testResultsRootDir
     *            the root directory
     * @param testCaseName
     *            the name of the active test case
     * @return the new sub directory
     */
    private File getTestResultsDirectory(final File testResultsRootDir, final String testCaseName)
    {
        String dirName = new SimpleDateFormat(XltConstants.DIRECTORY_DATE_FORMAT).format(new Date());

        // append the test case name if we have one
        if (testCaseName != null)
        {
            dirName = dirName + "-" + testCaseName;
        }

        return new File(testResultsRootDir, dirName);
    }

    /**
     * Returns the current user interface.
     * 
     * @return the user interface
     */
    public MasterControllerUI getUserInterface()
    {
        return userInterface;
    }

    /**
     * Checks if the agent controllers do respond.
     * 
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     * @throws IllegalStateException
     *             if unreachable instances are tolerated but no agent controller is connectable
     */
    public void checkAlive() throws AgentControllerException
    {
        LOG.debug("Check if agents are alive");

        final Map<AgentController, Future<Boolean>> agentFutures = getAgentRunningState();

        final FailedAgentControllerCollection failed = new FailedAgentControllerCollection();
        for (final Map.Entry<AgentController, Future<Boolean>> agentFuture : agentFutures.entrySet())
        {
            try
            {
                agentFuture.getValue().get();
            }
            catch (final InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (final Exception e)
            {
                failed.add(agentFuture.getKey(), e);
                LOG.error("Agentcontroller is not alive: " + agentFuture.getKey());
            }
        }

        checkSuccess(failed, true);
    }

    /**
     * Call {@link AgentController#hasRunningAgent()} concurrently for every agent controller. Nevertheless this method
     * is blocking until all agent controllers have sent a response or timed out.
     * 
     * @return query futures; call {@link Future#get()} to receive the running state
     */
    private Map<AgentController, Future<Boolean>> getAgentRunningState()
    {
        final CountDownLatch latch = new CountDownLatch(agentControllerMap.size());
        final Map<AgentController, Future<Boolean>> agentFutures = new HashMap<AgentController, Future<Boolean>>();
        for (final AgentController agentcontroller : agentControllerMap.values())
        {
            agentFutures.put(agentcontroller, defaultCommunicationExecutor.submit(new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {
                    boolean hasRunningAgent;
                    try
                    {
                        hasRunningAgent = agentcontroller.hasRunningAgent();
                    }
                    finally
                    {
                        latch.countDown();
                    }
                    return hasRunningAgent;
                }
            }));
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for agents running check to complete has failed", e);
        }

        return agentFutures;
    }

    /**
     * Checks whether there is at least one responding agent controller with a running agent.
     * 
     * @return <code>true</code> if all agent controllers are responsive and there is at least 1 running agent;
     *         <code>false</code> otherwise
     * @throws AgentControllerException
     *             if not {@link #isAgentControllerConnectionRelaxed} and there is a connection problem with an agent
     *             controller
     */
    public boolean isAnyAgentRunning() throws AgentControllerException
    {
        final FailedAgentControllerCollection failedAgentControllers = new FailedAgentControllerCollection();

        final Map<AgentController, Future<Boolean>> agentFutures = getAgentRunningState();
        final AtomicBoolean result = new AtomicBoolean();
        for (final Map.Entry<AgentController, Future<Boolean>> agentFuture : agentFutures.entrySet())
        {
            try
            {
                if (agentFuture.getValue().get())
                {
                    result.set(true);
                }
            }
            catch (final InterruptedException e)
            {
                // ignore
            }
            catch (final ExecutionException e)
            {
                failedAgentControllers.add(agentFuture.getKey(), e);
            }
        }

        checkSuccess(failedAgentControllers, true);

        return result.get();
    }

    /**
     * Checks whether there is at least one responding agent controller with a running agent.
     * 
     * @return <code>true</code> if there is a running agent; <code>false</code> otherwise
     */
    public boolean isAnyAgentRunning_SAFE()
    {
        final Map<AgentController, Future<Boolean>> agentFutures = getAgentRunningState();

        for (final Map.Entry<AgentController, Future<Boolean>> agentFuture : agentFutures.entrySet())
        {
            try
            {
                if (agentFuture.getValue().get())
                {
                    return true;
                }
            }
            catch (final Exception e)
            {
                // ignore, in this case we don't know the agent state and assume it's not running
            }
        }

        return false;
    }

    /**
     * Checks whether the test suite has been uploaded to all agents.
     * 
     * @return true if all agents are in sync; false otherwise
     */
    public boolean areAgentsInSync()
    {
        return currentLoadProfile != null && currentLoadProfile.getActiveTestCaseNames().size() > 0;
    }

    /**
     * Sets the new user interface.
     * 
     * @param userInterface
     *            the user interface
     */
    public void setUserInterface(final MasterControllerUI userInterface)
    {
        this.userInterface = userInterface;
    }

    /**
     * Starts the agents on all agent controllers at once.
     * 
     * @param testCaseName
     *            the name of the test case to start the agents for, or <code>null</code> if all active test cases
     *            should be started
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     * @throws InterruptedException
     *             if waiting for start jobs fails
     */
    public boolean startAgents(final String testCaseName) throws AgentControllerException
    {
        if (currentLoadProfile == null)
        {
            LOG.error("No files have been uploaded yet.");
            return false;
        }

        resetAgentStatuses();

        currentTestCaseName = testCaseName;

        final TestDeployment testDeployment = getTestDeployment(currentLoadProfile, currentTestCaseName);

        // start the agents
        final FailedAgentControllerCollection failedAgentcontrollers = new FailedAgentControllerCollection();
        final int agentControllerSize = agentControllerMap.size();
        final CountDownLatch latch = new CountDownLatch(agentControllerSize);
        final ProgressBar progress = startNewProgressBar(agentControllerSize);
        for (final AgentController agentController : agentControllerMap.values())
        {
            defaultCommunicationExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final Map<String, List<TestUserConfiguration>> agentControllerLoadProfile = testDeployment.getAgentsUserList(agentController);

                        if (agentControllerLoadProfile == null || agentControllerLoadProfile.isEmpty())
                        {
                            LOG.info("No need to start agents at " + agentController);
                        }
                        else
                        {
                            // tell the agent manager about the reference time
                            agentController.setReferenceTime(System.currentTimeMillis());

                            LOG.info("Starting agents at " + agentController);
                            agentController.startAgents(agentControllerLoadProfile);
                        }
                    }
                    catch (final Exception ex)
                    {
                        failedAgentcontrollers.add(agentController, ex);
                        LOG.error("Failed starting agents at " + agentController, ex);
                    }
                    progress.increaseCount();
                    latch.countDown();
                }
            });
        }

        boolean finished = true;
        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for agent to start failed", e);
            finished = false;
        }

        checkSuccess(failedAgentcontrollers, true);
        userInterface.agentsStarted();

        final boolean operationCompleted = finished && (failedAgentcontrollers.isEmpty() ||
                                                        (isAgentControllerConnectionRelaxed &&
                                                         failedAgentcontrollers.getMap().size() < agentControllerSize));
        if (operationCompleted)
        {
            stoppedByUser = false;
        }
        return operationCompleted;
    }

    /**
     * Stops the agents on all agent controllers at once.
     * 
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    public boolean stopAgents() throws AgentControllerException
    {
        final FailedAgentControllerCollection failedAgentcontrollers = new FailedAgentControllerCollection();
        final int agentControllerSize = agentControllerMap.size();
        final CountDownLatch latch = new CountDownLatch(agentControllerSize);
        final ProgressBar progress = startNewProgressBar(agentControllerSize);
        for (final AgentController agentController : agentControllerMap.values())
        {
            defaultCommunicationExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    LOG.info("Stopping agents at " + agentController);
                    try
                    {
                        agentController.stopAgents();
                    }
                    catch (final Exception ex)
                    {
                        failedAgentcontrollers.add(agentController, ex);
                        LOG.error("Failed stopping agent at " + agentController, ex);
                    }
                    progress.increaseCount();
                    latch.countDown();
                }
            });
        }

        boolean finished = true;

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            finished = false;
        }

        checkSuccess(failedAgentcontrollers, false);

        userInterface.agentsStopped();

        final boolean operationCompleted = finished && (failedAgentcontrollers.isEmpty() ||
                                                        (isAgentControllerConnectionRelaxed &&
                                                         failedAgentcontrollers.getMap().size() < agentControllerSize));
        if (operationCompleted)
        {
            stoppedByUser = true;
        }
        return operationCompleted;
    }

    /**
     * Updates the agent files on all configured agent controllers at once.
     * 
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent controller site</li>
     *             </ul>
     * @throws IOException
     *             if an I/O error ocurres on archiving agent files for update
     * @throws IllegalStateException
     *             if there is no active load test configured
     */
    public void updateAgentFiles() throws AgentControllerException, IOException, IllegalStateException
    {
        System.out.print("    Preparing:");
        final ProgressBar progressPrepare = startNewProgressBar(agentControllerMap.size() + 7);

        /*
         * Cleanup the data left from last upload.
         */
        LOG.info("Cleanup");

        resetAgentStatuses();
        progressPrepare.increaseCount();

        /*
         * Optionally copy and manipulate the test suite.
         */
        LOG.info("Read target test suite");
        // build a file filter that ignores hidden files and the results directory in the agent files directory
        final FileFilter fileFilter = getFileFilter();
        // setup working directory
        final File workDir = setUpWorkDir(fileFilter);
        progressPrepare.increaseCount();

        // Read the configuration files and build load profile.
        final File agentTemplateConfigDir = new File(workDir, XltConstants.CONFIG_DIR_NAME);
        // read load test profile
        currentLoadProfile = getTestProfile(agentTemplateConfigDir);
        progressPrepare.increaseCount();

        progressPrepare.increaseCount();

        if (currentLoadProfile.getActiveTestCaseNames().size() <= 0)
        {
            final String msg = "No test case configured.";
            XltLogger.runTimeLogger.warn(msg);
            throw new IllegalStateException(msg);
        }
        progressPrepare.increaseCount();

        /*
         * Create the file replication index for the local agent files
         */
        LOG.info("Considering files in '" + workDir + "' for upload ...");
        final FileReplicationIndex localIndex = FileReplicationUtils.getIndex(workDir, fileFilter);
        progressPrepare.increaseCount();

        final AgentControllerUpdate updater = new AgentControllerUpdate(agentControllerMap.values(), uploadExecutor, downloadExecutor,
                                                                        tempDirectory);
        updater.prepare(progressPrepare);

        System.out.println("- OK");

        System.out.print("    Uploading:");
        final ProgressBar progressUpload = startNewProgressBar(4 * agentControllerMap.size() + 1);
        updater.update(workDir, localIndex, progressUpload);

        /*
         * Clean up.
         */
        LOG.info("Clean up");
        // delete the copy if we have made one
        if (workDir != agentFilesDirectory)
        {
            org.apache.commons.io.FileUtils.deleteQuietly(workDir);
        }
        progressUpload.increaseCount();

        checkSuccess(updater.getFailedAgentControllers(), true);

        /*
         * User information
         */
        userInterface.agentFilesUploaded();
    }

    /**
     * Set up the working directory.
     * 
     * @param fileFilter
     *            file filter for testsuite
     */
    private File setUpWorkDir(final FileFilter fileFilter)
    {
        // only make a copy if we really have to change project.properties
        final File workDir;
        if (StringUtils.isBlank(propertiesFileName))
        {
            workDir = agentFilesDirectory;
        }
        else
        {
            try
            {
                // create a new sub directory in the temp directory
                workDir = File.createTempFile("xlt-", "", tempDirectory);
                org.apache.commons.io.FileUtils.forceDelete(workDir);
                org.apache.commons.io.FileUtils.forceMkdir(workDir);

                // copy the test suite
                org.apache.commons.io.FileUtils.copyDirectory(agentFilesDirectory, workDir, fileFilter);

                // enter the test properties file into project.properties
                final File projectPropertiesFile = new File(new File(workDir, "config"), "project.properties");
                final PropertiesConfiguration config = new PropertiesConfiguration();
                final FileHandler fileHandler = new FileHandler(config);

                fileHandler.load(projectPropertiesFile);
                config.setProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY, propertiesFileName);
                fileHandler.save(projectPropertiesFile);
            }
            catch (final Exception e)
            {
                throw new RuntimeException("Failed to make a copy of the agent files", e);
            }
        }
        return workDir;
    }

    /**
     * get testsuite file filter
     */
    private FileFilter getFileFilter()
    {
        final IOFileFilter visibleFiles = HiddenFileFilter.VISIBLE;
        final IOFileFilter notResults = FileFilterUtils.notFileFilter(new NameFileFilter(XltConstants.RESULT_ROOT_DIR));
        return FileFilterUtils.and(visibleFiles, notResults);
    }

    /**
     * Get the test profile.
     * 
     * @param agentTemplateConfigDir
     *            agent template config directory
     */
    private TestLoadProfileConfiguration getTestProfile(final File agentTemplateConfigDir) throws IOException
    {
        TestLoadProfileConfiguration testConfig;
        try
        {
            // read the load profile from the configuration
            testConfig = new TestLoadProfileConfiguration(agentTemplateConfigDir.getParentFile(), agentTemplateConfigDir);
            postProcessLoadProfile(testConfig);
        }
        catch (final Throwable ex)
        {
            throw new RuntimeException("Load profile configuration failed using directory: '" + agentTemplateConfigDir + "'. " +
                                       getDetailedMessage(ex), ex);
        }

        // check if test properties file is loaded if configured
        final String testPropertiesFileName = testConfig.getProperties().getProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY);

        if (StringUtils.isNotBlank(testPropertiesFileName))
        {
            final FileObject testPropertiesFile = VFS.getManager().resolveFile(agentTemplateConfigDir, testPropertiesFileName);
            if (!testPropertiesFile.exists() || !testPropertiesFile.isFile() || !testPropertiesFile.isReadable())
            {
                throw new IOException("Unable to load test properties file.");
            }
        }

        return testConfig;
    }

    /**
     * Returns a detailed message for the given throwable object.
     * 
     * @param throwable
     *            the throwable object
     * @return detailed message of given throwable object
     */
    protected static String getDetailedMessage(final Throwable throwable)
    {
        final List<String> messages = new LinkedList<>();

        Throwable t = throwable;
        while (t != null)
        {
            final String msg = t.getMessage();
            if (msg != null)
            {
                messages.add(msg);
            }

            t = t.getCause();
        }

        return StringUtils.join(messages, " :: ");
    }

    /**
     * Sets the test comment.
     * 
     * @param comment
     *            the test comment
     */
    void setTestComment(final String comment)
    {
        if (comment != null)
        {
            testComment = comment.trim();
        }
        else
        {
            testComment = null;
        }
        LOG.debug("Test comment set to: " + StringUtils.defaultString(comment, "<NULL>"));
    }

    File getCurrentTestResultsDirectory()
    {
        return currentTestResultsDir;
    }

    /**
     * Returns the test comment.
     * 
     * @return test comment
     */
    public String getTestComment()
    {
        return testComment;
    }

    /**
     * Sets the comment for the downloaded test results.
     */
    void setTestComment4DownloadedResults()
    {
        if (StringUtils.isBlank(getTestComment()))
        {
            return;
        }

        final File testPropFile = getTestPropertyFile(currentTestResultsDir);

        List<?> lines = null;
        try
        {
            if (testPropFile != null && testPropFile.exists())
            {
                lines = org.apache.commons.io.FileUtils.readLines(testPropFile, StandardCharsets.ISO_8859_1);
            }
        }
        catch (final Exception ex)
        {
            LOG.error("Failed to read content of file '" + testPropFile.getAbsolutePath() + "'.", ex);
        }

        if (lines == null)
        {
            return;
        }

        final ArrayList<String> outLines = new ArrayList<String>();
        for (final Object o : lines)
        {
            final String s = (String) o;
            outLines.add(s);
        }

        outLines.add(XltConstants.EMPTYSTRING);
        outLines.add("# Command line comment (AUTOMATICALLY INSERTED)");
        outLines.add("com.xceptance.xlt.loadtests.comment.commandLine = " + getTestComment());

        try
        {
            org.apache.commons.io.FileUtils.writeLines(testPropFile, outLines);
        }
        catch (final Exception ex)
        {
            LOG.error("Failed to write content to file '" + testPropFile.getAbsolutePath() + "'.", ex);
        }
    }

    /**
     * Returns the value of the test comment property <tt>com.xceptance.xlt.loadtests.comment</tt>.
     * 
     * @return value of test comment property
     */
    public String getTestCommentPropertyValue()
    {
        try
        {
            final FileSystemManager fsMgr = VFS.getManager();
            final XltProperties props = new XltPropertiesImpl(fsMgr.resolveFile(currentTestResultsDir.getAbsolutePath()),
                                                              fsMgr.resolveFile(getConfigDir(currentTestResultsDir).getAbsolutePath()),
                                                              true);

            final String testCommentPropValue = props.getProperty("com.xceptance.xlt.loadtests.comment");

            return testCommentPropValue;
        }
        catch (final FileSystemException fse)
        {
            LOG.error("Failed to read/parse test configuration from '" + currentTestResultsDir + "'");
            return null;
        }
    }

    /**
     * Returns the file containing the test-specific properties.
     * 
     * @param testResultsDir
     *            test result directory
     * @return test-specific properties file
     */
    public static File getTestPropertyFile(final File testResultsDir)
    {
        try
        {
            final FileSystemManager fsMgr = VFS.getManager();

            final File confDir = getConfigDir(testResultsDir);

            final XltProperties props = new XltPropertiesImpl(fsMgr.resolveFile(testResultsDir.getAbsolutePath()),
                                                              fsMgr.resolveFile(confDir.getAbsolutePath()), true);

            final String testPropFileName = props.getProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY, "test.properties");

            return new File(confDir, testPropFileName);
        }
        catch (final FileSystemException fse)
        {
            LOG.error("Failed to read/parse test configuration from '" + testResultsDir + "'");
            return null;
        }
    }

    public void shutdown()
    {
        defaultCommunicationExecutor.shutdownNow();
        uploadExecutor.shutdownNow();
        downloadExecutor.shutdownNow();
    }

    public void init()
    {
        // initialize agent controller statuses
        for (final AgentController agentController : agentControllerMap.values())
        {
            statuses.put(agentController.getName(), new AgentControllerStatus((Exception) null));
        }
    }

    /**
     * Creates a new progress bar and sets the default indentation.
     * 
     * @param total
     *            expected total progress count
     */
    private ProgressBar startNewProgressBar(final int total)
    {
        final ProgressBar progress = new ProgressBar(total);
        System.out.print("    ");
        progress.start();

        return progress;
    }

    /**
     * Reset status of agent controllers (agents)
     * 
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    private void resetAgentStatuses() throws AgentControllerException
    {
        final FailedAgentControllerCollection failedAgentControllers = new FailedAgentControllerCollection();
        final CountDownLatch latch = new CountDownLatch(agentControllerMap.size());
        for (final AgentController agentController : agentControllerMap.values())
        {
            defaultCommunicationExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    Exception ex = null;

                    try
                    {
                        agentController.resetAgentsStatus();
                    }
                    catch (final Exception e)
                    {
                        ex = e;
                        failedAgentControllers.add(agentController, e);
                    }

                    statuses.put(agentController.getName(), new AgentControllerStatus(ex));
                    latch.countDown();
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for resetting agent status to complete has failed");
        }

        checkSuccess(failedAgentControllers, true);
    }

    /**
     * Check if communication with an agent controller failed and react.
     * 
     * @param failedAgentControllers
     * @param keepLivingAgentControllersOnly
     *            if <code>true</code> all unreachable agent controllers get removed from the list of (available) agent
     *            controllers
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    private void checkSuccess(final FailedAgentControllerCollection failedAgentControllers, final boolean keepLivingAgentControllersOnly)
        throws AgentControllerException
    {
        if (failedAgentControllers != null && !failedAgentControllers.isEmpty())
        {
            // tolerate unconnected agent controllers
            if (isAgentControllerConnectionRelaxed)
            {
                // inform user
                userInterface.skipAgentControllerConnections(failedAgentControllers);

                // remove unavailable agent controllers
                if (keepLivingAgentControllersOnly)
                {
                    for (final AgentController ac : failedAgentControllers.getAgentControllers())
                    {
                        agentControllerMap.remove(ac.getName());
                    }
                }

                // check that there is at least 1 agent controller left
                if (agentControllerMap.isEmpty())
                {
                    throw new IllegalStateException("No living AgentController left.");
                }
            }
            else
            {
                throw new AgentControllerException(failedAgentControllers);
            }
        }
    }

    private static File getConfigDir(final File inputDir)
    {
        /*
         * Starting with XLT 4.3.0 the load test configuration resides in a separate sub-directory named "config". We
         * have to fall back to given input directory in case such sub-directory does not exist, is not a directory or
         * is not readable.
         */
        final File configDir = new File(inputDir, XltConstants.RESULT_CONFIG_DIR);
        if (configDir != null && configDir.exists() && configDir.canRead() && configDir.isDirectory())
        {
            // set configuration context
            return configDir;
        }
        else
        {
            return inputDir;
        }

    }

    /**
     * Post-processes the given load profile.
     * 
     * @param loadProfile
     *            the load profile
     */
    private void postProcessLoadProfile(final TestLoadProfileConfiguration loadProfile)
    {
        boolean haveCPTests = false;
        boolean haveLoadTests = false;
        for (final TestCaseLoadProfileConfiguration testConfig : loadProfile.getLoadTestConfiguration())
        {
            haveCPTests = haveCPTests || testConfig.isCPTest();
            haveLoadTests = haveLoadTests || !testConfig.isCPTest();
            if (isEmbedded)
            {
                testConfig.setCPTest(false);
            }
        }

        if (!isEmbedded)
        {
            final int acCount = agentControllerMap.size();
            int nbCPACs = 0;
            for (final AgentController ac : agentControllerMap.values())
            {
                if (ac.runsClientPerformanceTests())
                {
                    ++nbCPACs;
                }
            }

            final int diff = acCount - nbCPACs;
            final boolean haveCPAC = diff != acCount;
            final boolean haveLoadAC = diff > 0;

            if (haveCPTests && !haveCPAC)
            {
                throw new IllegalArgumentException("There is at least one client-performance test configured but no agent controller capable to run client-performance tests could be found.");
            }

            if (haveLoadTests && !haveLoadAC)
            {
                throw new IllegalArgumentException("There is at least one load/performance test configured but no agent controller capable to run load/performance tests could be found.");
            }
        }
    }
}