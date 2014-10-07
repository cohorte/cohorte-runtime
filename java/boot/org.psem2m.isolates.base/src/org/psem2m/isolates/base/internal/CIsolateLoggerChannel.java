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

package org.psem2m.isolates.base.internal;

import java.util.logging.Formatter;

import org.psem2m.utilities.logging.CActivityFileHandler;
import org.psem2m.utilities.logging.CActivityFormaterHuman;
import org.psem2m.utilities.logging.CActivityLoggerBasic;

public class CIsolateLoggerChannel extends CActivityLoggerBasic {

    /**
     * @param aLoggerName
     * @param aFilePathPattern
     * @param aLevel
     * @param aFileLimit
     * @param aFileCount
     * @throws Exception
     */
    CIsolateLoggerChannel(final String aLoggerName,
            final String aFilePathPattern, final String aLevel,
            final int aFileLimit, final int aFileCount) throws Exception {

        super(aLoggerName, aFilePathPattern, aLevel, aFileLimit, aFileCount);
        initFileHandler();
        open();
    }

    @Override
    protected void initFileHandler() throws Exception {

        final CActivityFileHandler wFileHandler = new CActivityFileHandler(
                getFilePathPattern(), getFileLimit(), getFileCount());
        wFileHandler.setFormatter((Formatter) CActivityFormaterHuman
                .getInstance());
        super.setFileHandler(wFileHandler);
    }
}
