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

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public class CIsolateLoggerSvc extends CXObjectBase implements
        IIsolateLoggerSvc {

    /**
     * The logger presence key name
     */
    private static final String LIB_HAS_AL = "hasActivityLogger";

    /**
     * reference to the LoggerBase
     */
    private IActivityLoggerBase pActivityLoggerBase;

    /**
     * Constructor
     *
     * @param aActivityLoggerBase
     *            Reference to the activator
     */
    public CIsolateLoggerSvc(final IActivityLoggerBase aActivityLoggerBase) {

        super();
        pActivityLoggerBase = aActivityLoggerBase;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
     * )
     */
    @Override
    public Appendable addDescriptionInBuffer(final Appendable aBuffer) {

        super.addDescriptionInBuffer(aBuffer);
        CXStringUtils.appendKeyValInBuff(aBuffer, LIB_HAS_AL,
                hasActivityLogger());
        return aBuffer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#getLevel()
     */
    @Override
    public Level getLevel() {

        return Level.ALL;
    }

    /**
     * Tests if the reference to activity logger is valid
     *
     * @return True if the reference is valid
     */
    public boolean hasActivityLogger() {

        return pActivityLoggerBase != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogDebugOn()
     */
    @Override
    public boolean isLogDebugOn() {

        return pActivityLoggerBase != null ? pActivityLoggerBase.isLogDebugOn()
                : false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#isLoggable(java.util
     * .logging.Level)
     */
    @Override
    public boolean isLoggable(final Level aLevel) {

        return pActivityLoggerBase != null ? pActivityLoggerBase
                .isLoggable(aLevel) : false;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogInfoOn()
     */
    @Override
    public boolean isLogInfoOn() {

        return pActivityLoggerBase != null ? pActivityLoggerBase.isLogInfoOn()
                : false;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogSevereOn()
     */
    @Override
    public boolean isLogSevereOn() {

        return pActivityLoggerBase != null ? pActivityLoggerBase
                .isLogSevereOn() : false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogWarningOn()
     */
    @Override
    public boolean isLogWarningOn() {

        return pActivityLoggerBase != null ? pActivityLoggerBase
                .isLogWarningOn() : false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
     * .Level, java.lang.Object, java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void log(final Level aLevel, final Object aWho,
            final CharSequence aWhat, final Object... aInfos) {

        if (pActivityLoggerBase != null) {
            pActivityLoggerBase.log(aLevel, aWho, aWhat, aInfos);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
     * .LogRecord)
     */
    @Override
    public void log(final LogRecord record) {

        if (pActivityLoggerBase != null) {
            pActivityLoggerBase.log(record);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#logDebug(java.lang.Object
     * , java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logDebug(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {

        if (pActivityLoggerBase != null) {
            pActivityLoggerBase.logDebug(aWho, aWhat, aInfos);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#logInfo(java.lang.Object
     * , java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logInfo(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {

        if (pActivityLoggerBase != null) {
            pActivityLoggerBase.logInfo(aWho, aWhat, aInfos);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#logSevere(java.lang.
     * Object, java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logSevere(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {

        if (pActivityLoggerBase != null) {
            pActivityLoggerBase.logSevere(aWho, aWhat, aInfos);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.utilities.logging.IActivityLoggerBase#logWarn(java.lang.Object
     * , java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logWarn(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {

        if (pActivityLoggerBase != null) {
            pActivityLoggerBase.logWarn(aWho, aWhat, aInfos);
        }
    }

    /**
     * @param aActivityLoggerBase
     */
    protected void setActivityLoggerBase(
            final IActivityLoggerBase aActivityLoggerBase) {

        pActivityLoggerBase = aActivityLoggerBase;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.utilities.IXDescriber#toDescription()
     */
    @Override
    public String toDescription() {

        return addDescriptionInBuffer(new StringBuilder(128)).toString();
    }
}
