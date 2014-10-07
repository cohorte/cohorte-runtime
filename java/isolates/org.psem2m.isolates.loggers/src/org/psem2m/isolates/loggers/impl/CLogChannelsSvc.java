/**
 * Copyright 2014 isandlaTech
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

package org.psem2m.isolates.loggers.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.CActivityLoggerBasicConsole;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 *
 * This service is a Service factory.
 *
 *
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public class CLogChannelsSvc extends CPojoBase implements ILogChannelsSvc {

    /**
     * @author ogattaz
     *
     */
    class CActivityLoggerConsole extends CActivityLoggerBasicConsole implements
            ILogChannelSvc {

        /**
         * @param aLoggerName
         */
        CActivityLoggerConsole(final String aLoggerName) {

            super();
            // open();
        }

    }

    /**
     * @author isandlatech (www.isandlatech.com) - ogattaz
     *
     */
    class CActivityLoggerPsem2m extends CActivityLoggerBasic implements
            ILogChannelSvc {

        /**
         * @param aLoggerName
         * @param aFilePathPattern
         * @param aLevel
         * @param aFileLimit
         * @param aFileCount
         * @throws Exception
         */
        CActivityLoggerPsem2m(final String aLoggerName,
                final String aFilePathPattern, final String aLevel,
                final int aFileLimit, final int aFileCount) throws Exception {

            super(aLoggerName, aFilePathPattern, aLevel, aFileLimit, aFileCount);
            initFileHandler();
            open();
        }

    }

    /**
     * Service reference managed by iPojo (see metadata.xml)
     *
     * This service is the logger of the current bundle
     **/
    private IIsolateLoggerSvc pIsolateLoggerSvc;

    /** the "repository" of the opened logging channels **/
    TreeMap<String, ILogChannelSvc> pLoggers = new TreeMap<String, ILogChannelSvc>();

    /** Service reference managed by iPojo (see metadata.xml) **/
    private IPlatformDirsSvc pPlatformDirsSvc;

    /**
     * Explicit default constructor
     */
    public CLogChannelsSvc() {

        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.utilities.CXObjectBase#destroy()
     */
    @Override
    public void destroy() {

        for (final Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers
                .entrySet()) {
            wLoggerEntry.getValue().close();
        }
        pLoggers.clear();
    }

    /**
     * @return the list of available channels
     */
    @Override
    public List<ILogChannelSvc> getChannels() {

        final List<ILogChannelSvc> wLoggers = new ArrayList<ILogChannelSvc>();
        for (final Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers
                .entrySet()) {
            wLoggers.add(wLoggerEntry.getValue());
        }

        return wLoggers;
    }

    /**
     * @return the list of the ids of the available channels
     */
    @Override
    public List<String> getChannelsIds() {

        final List<String> wIds = new ArrayList<String>();
        for (final Entry<String, ILogChannelSvc> wLoggerEntry : pLoggers
                .entrySet()) {
            wIds.add(wLoggerEntry.getKey());
        }
        return wIds;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.loggers.ILogChannelsSvc#getLogChannel(java.lang.String
     * )
     */
    @Override
    public ILogChannelSvc getLogChannel(final String aChannelId)
            throws Exception {

        final ILogChannelSvc wLogger = pLoggers.get(aChannelId);
        if (wLogger != null) {
            return wLogger;
        }

        return newLogChannel(aChannelId);
    }

    /**
     * @param aChannelId
     * @param aLogDir
     * @return
     * @throws Exception
     */
    private ILogChannelSvc instanciateLogChannel(final String aChannelId,
            final File aLogDir) throws Exception {

        final File wLogFile = new File(aLogDir, aChannelId + "_%g.log");
        return new CActivityLoggerPsem2m(aChannelId,
                wLogFile.getAbsolutePath(), IActivityLoggerBase.ALL,
                1024 * 1024 * 100, 5);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.base.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() {

        // logs in the bundle output
        pIsolateLoggerSvc.logInfo(this, "invalidatePojo", "INVALIDATE",
                toDescription());
        destroy();
    }

    /**
     * @param aChannelId
     * @return
     */
    public ILogChannelSvc newLogChannel(final String aChannelId)
            throws Exception {

        final ILogChannelSvc wLogger = instanciateLogChannel(aChannelId,
                pPlatformDirsSvc.getIsolateLogDir());

        pLoggers.put(aChannelId, wLogger);

        return wLogger;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.base.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() {

        // logs in the isolate logger
        pIsolateLoggerSvc.logInfo(this, "validatePojo", "VALIDATE",
                toDescription());

        try {

            // ...

        } catch (final Exception e) {
            pIsolateLoggerSvc.logSevere(this, "validatePojo", e);
        }
    }
}
