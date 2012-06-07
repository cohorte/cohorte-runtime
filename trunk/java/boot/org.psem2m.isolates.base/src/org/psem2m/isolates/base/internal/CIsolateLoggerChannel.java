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

        CActivityFileHandler wFileHandler = new CActivityFileHandler(
                getFilePathPattern(), getFileLimit(), getFileCount());
        wFileHandler.setFormatter((Formatter) CActivityFormaterHuman
                .getInstance());
        super.setFileHandler(wFileHandler);
    }

}
