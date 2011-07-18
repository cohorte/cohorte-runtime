package org.psem2m.isolates.base;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CActivityFormaterBasic;
import org.psem2m.utilities.logging.IActivityFormater;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * 
 * 
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public abstract class CActivatorBase extends CXObjectBase implements
        IActivatorBase, IActivityLoggerBase, BundleActivator {
    /**
     * @author isandlatech (www.isandlatech.com) - ogattaz
     * 
     */
    class CLogReaderServiceListner implements ServiceListener {

        /**
         * Explicit default constructor
         */
        CLogReaderServiceListner() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework
         * .ServiceEvent)
         */
        @Override
        public void serviceChanged(final ServiceEvent event) {
            if (event.getType() == ServiceEvent.REGISTERED) {
                ServiceReference wLogReaderServiceRef = event
                        .getServiceReference();
                LogReaderService wLogReaderService = (LogReaderService) getContext()
                        .getService(wLogReaderServiceRef);
                bindLogReaderService(wLogReaderService);

            } else if (event.getType() == ServiceEvent.UNREGISTERING) {
                getContext().ungetService(event.getServiceReference());
                unbindLogReaderService();
            }
        }

    }

    /**
     * @author isandlatech (www.isandlatech.com) - ogattaz
     * 
     */
    class CLogServiceListner implements ServiceListener {

        /**
         * Explicit default constructor
         */
        CLogServiceListner() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework
         * .ServiceEvent)
         */
        @Override
        public void serviceChanged(final ServiceEvent event) {
            if (event.getType() == ServiceEvent.REGISTERED) {
                ServiceReference wConfigRef = event.getServiceReference();
                LogService wLogService = (LogService) getContext().getService(
                        wConfigRef);
                bindLogService(wLogService);

            } else if (event.getType() == ServiceEvent.UNREGISTERING) {
                getContext().ungetService(event.getServiceReference());
                unbindLogService();
            }
        }

    }

    public static String LIB_BNDL_ID = "BundleId";

    public static String LIB_POJO_ID = "PojoId";

    /**
     * The formater which format the log line
     * 
     * <pre>
     * TimeStamp     TimeStamp      TimeStamp  TimeStamp    Level    Thread name       Instance id          Method               LogLine
     * (millis)      (nano)         (date)     (hhmmss.sss) 
     * 1309180295049;00000065317000;2011/06/27;15:11:35:049;INFO   ;   FelixStartLevel;CIsolateLogger_2236 ;__validatePojo      ;EnvContext:
     * </pre>
     **/
    private final IActivityFormater pCActivityFormaterBasic = CActivityFormaterBasic
            .getInstance();

    private BundleContext pContext = null;

    /** **/
    private final CLogIsolatesRedirector pLogIsolatesRedirector = CLogIsolatesRedirector
            .getInstance();

    /**
     * the flag to indicates if that the LogListener was put in place by this
     * Activator
     **/
    private boolean pLogListenerSet = false;

    /** the reference to the LogReader service **/
    private LogReaderService pLogReaderService = null;

    /** the reference to the Log service **/
    private LogService pLogService = null;

    /**
     * Explicit default constructor
     */
    public CActivatorBase() {
        super();
        pCActivityFormaterBasic.acceptMultiline(true);
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
        CXStringUtils.appendKeyValInBuff(aBuffer, LIB_BNDL_ID, getBundleId());
        return aBuffer;
    }

    /**
     * @param aLogReaderService
     */
    public void bindLogReaderService(final LogReaderService aLogReaderService) {
        logInfo(this, "bindLogReaderService",
                " LogListener already in place=[%b]",
                CLogServiceRedirector.hasListener());

        pLogReaderService = aLogReaderService;

        pLogListenerSet = CLogServiceRedirector
                .putInPlaceLogListener(pLogReaderService);
        if (pLogListenerSet) {

            logInfo(this, "bindLogReaderService",
                    "------------ LogService redirector set !  re-log stored LogEntries begin.");
            int wNb = CLogServiceRedirector.relogLogEntries();
            logInfo(this,
                    "bindLogReaderService",
                    "------------ re-log stored LogEntries end. NbEntries=[%d]",
                    wNb);
        }
    }

    /**
     * @param aLogService
     */
    public void bindLogService(final LogService aLogService) {
        pLogService = aLogService;

        logInfo(this, "bindLogService");
    }

    /**
     * @param aLevel
     * @return
     */
    private int convertToLogServiceLevel(final Level aLevel) {
        if (aLevel.equals(Level.INFO)) {
            return LogService.LOG_INFO;
        }
        if (aLevel.equals(Level.FINE)) {
            return LogService.LOG_DEBUG;
        }
        if (aLevel.equals(Level.SEVERE)) {
            return LogService.LOG_ERROR;
        }
        if (aLevel.equals(Level.WARNING)) {
            return LogService.LOG_WARNING;
        }

        return LogService.LOG_INFO;
    }

    /**
     * @return the id of the bundle
     */
    @Override
    public abstract String getBundleId();

    /**
     * @return
     */
    protected IActivityFormater getCActivityFormater() {
        return pCActivityFormaterBasic;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.IActivatorBase#getContext()
     */
    @Override
    public BundleContext getContext() {
        return pContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLogger#isLogDebugOn()
     */
    @Override
    public boolean isLogDebugOn() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLogger#isLoggable(java.util.logging
     * .Level)
     */
    @Override
    public boolean isLoggable(final Level aLevel) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLogger#isLogInfoOn()
     */
    @Override
    public boolean isLogInfoOn() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLogger#isLogSevereOn()
     */
    @Override
    public boolean isLogSevereOn() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLogger#log(java.util.logging.Level,
     * java.lang.Object, java.lang.CharSequence, java.lang.Object[])
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.logging.IActivityLogger#isLogWarningOn()
     */
    @Override
    public boolean isLogWarningOn() {
        return true;
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

        CharSequence wWhat = (aWhat != null) ? aWhat : CXJavaRunContext
                .getPreCallingMethod();
        pLogIsolatesRedirector.log(aLevel, aWho, wWhat, aInfos);
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
        String wLine = pCActivityFormaterBasic.format(record);
        if (pLogService != null) {
            pLogService.log(convertToLogServiceLevel(record.getLevel()), wLine);
        } else {
            System.out.println(wLine);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLogger#logDebug(java.lang.Object,
     * java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logDebug(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {
        this.log(Level.FINE, aWho, aWhat, aInfos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLogger#logInfo(java.lang.Object,
     * java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logInfo(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {
        this.log(Level.INFO, aWho, aWhat, aInfos);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLogger#logSevere(java.lang.Object,
     * java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logSevere(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {
        this.log(Level.SEVERE, aWho, aWhat, aInfos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.logging.IActivityLogger#logWarn(java.lang.Object,
     * java.lang.CharSequence, java.lang.Object[])
     */
    @Override
    public void logWarn(final Object aWho, final CharSequence aWhat,
            final Object... aInfos) {
        this.log(Level.WARNING, aWho, aWhat, aInfos);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        pContext = bundleContext;

        // find and bind the LogService
        ServiceReference scrServiceRef = bundleContext
                .getServiceReference(LogService.class.getName());

        LogService wLogService = null;
        if (scrServiceRef != null) {
            wLogService = (LogService) bundleContext.getService(scrServiceRef);
        }

        if (wLogService != null) {
            bindLogService(wLogService);
        } else {
            String wFilter = "(objectclass=" + LogService.class.getName() + ")";
            getContext().addServiceListener(new CLogServiceListner(), wFilter);
        }

        // find and bind the LogReaderService
        ServiceReference wLogReaderServiceRef = bundleContext
                .getServiceReference(LogReaderService.class.getName());

        LogReaderService wLogReaderService = null;
        if (wLogReaderServiceRef != null) {
            wLogReaderService = (LogReaderService) bundleContext
                    .getService(wLogReaderServiceRef);
        }

        if (wLogReaderService != null) {
            bindLogReaderService(wLogReaderService);
        } else {
            String wFilter = "(objectclass=" + LogReaderService.class.getName()
                    + ")";
            getContext().addServiceListener(new CLogReaderServiceListner(),
                    wFilter);
        }

        logInfo(this, "start", "START", toDescription());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
        logInfo(this, "stop", "STOP", toDescription());
        pContext = null;
    }

    /**
     * unbind the logReadService and remove the LogListener if this activator
     * put it in place during the binding of the logReadService.
     */
    public void unbindLogReaderService() {
        logInfo(this, "unbindLogReaderService");

        if (pLogListenerSet) {
            logInfo(this, "unbindLogReaderService",
                    "remove the LogListener put in place by this activator.");
            CLogServiceRedirector.removeLogListener();
        }
        pLogReaderService = null;

    }

    /**
	 * 
	 */
    public void unbindLogService() {
        logInfo(this, "unbindLogService");
        pLogService = null;
    }

}
