/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * Tests the implementation of {@link DataManagerImpl}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DataManagerImplTest
{
    private static final String PROP_RESULT_DIR = "com.xceptance.xlt.result-dir";

    private String resultDir;

    @Before
    public void setup()
    {
        resultDir = XltProperties.getInstance().getProperty(PROP_RESULT_DIR);
    }

    @After
    public void tearDown()
    {
        if(resultDir != null)
        {
            XltProperties.getInstance().setProperty(PROP_RESULT_DIR, resultDir);
        }
    }

    @Test
    public void testGetTimerFile_ResultDirIsEmpty() throws Exception
    {
        XltProperties.getInstance().setProperty(PROP_RESULT_DIR, "");
        Session.getCurrent().clear();

        final Method m = Whitebox.getMethod(DataManagerImpl.class, "getTimerFile");
        m.setAccessible(true);

        final MyAppender appender = new MyAppender();
        XltLogger.runTimeLogger.addAppender(appender);

        try
        {
            final File timerFile = (File) m.invoke(new DataManagerImpl((SessionImpl) Session.getCurrent()));

            Assert.assertNotNull("No timer file", timerFile);
            Assert.assertNull("Nothing should come up", appender.t);
            timerFile.equals(new File(new File(XltConstants.RESULT_ROOT_DIR), XltConstants.TIMER_FILENAME));
        }
        finally
        {
            XltLogger.runTimeLogger.removeAppender(appender);
        }

    }

    @Test
    public void testGetTimerFile_ResultDirIsNotEmpty() throws Exception
    {
        final String dir = ".";
        XltProperties.getInstance().setProperty(PROP_RESULT_DIR, dir);
        Session.getCurrent().clear();

        final Method m = Whitebox.getMethod(DataManagerImpl.class, "getTimerFile");
        m.setAccessible(true);

        final MyAppender appender = new MyAppender();
        XltLogger.runTimeLogger.addAppender(appender);

        try
        {
            final File timerFile = (File) m.invoke(new DataManagerImpl((SessionImpl) Session.getCurrent()));

            Assert.assertNotNull("No timer file", timerFile);
            Assert.assertNull("Nothing should come up", appender.t);
            timerFile.equals(new File(new File(dir), XltConstants.TIMER_FILENAME));
        }
        finally
        {
            XltLogger.runTimeLogger.removeAppender(appender);
        }

    }

    private static class MyAppender implements Appender
    {
        private Throwable t;

        /**
         * {@inheritDoc}
         */
        @Override
        public void addFilter(Filter newFilter)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Filter getFilter()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clearFilters()
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close()
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doAppend(LoggingEvent event)
        {
            if (event != null && Level.FATAL.equals(event.getLevel()))
            {
                final ThrowableInformation tinf = event.getThrowableInformation();
                if (tinf != null)
                {
                    t = tinf.getThrowable();
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setErrorHandler(ErrorHandler errorHandler)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ErrorHandler getErrorHandler()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setLayout(Layout layout)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Layout getLayout()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setName(String name)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean requiresLayout()
        {
            return false;
        }

    }
}
