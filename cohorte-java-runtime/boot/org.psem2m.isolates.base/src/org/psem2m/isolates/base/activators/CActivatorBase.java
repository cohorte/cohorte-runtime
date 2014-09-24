package org.psem2m.isolates.base.activators;

import java.util.logging.Level;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;

/**
 * This abstract class is the base of all the PSEM2M bundles activator.
 * 
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public abstract class CActivatorBase extends CXObjectBase implements
        IActivatorBase, BundleActivator {

    /**
     * The listener used to wait for the IIsolateLoggerSvc OSGI service.
     * 
     * @author isandlatech (www.isandlatech.com) - ogattaz
     * 
     */
    private class CIsolateLoggerListener implements ServiceListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework
         * .ServiceEvent)
         */
        @Override
        public void serviceChanged(final ServiceEvent event) {

            switch (event.getType()) {
            case ServiceEvent.MODIFIED:
            case ServiceEvent.REGISTERED:
                if (hasIsolateLoggerSvc()) {
                    // Nothing to do
                    break;
                }

                // Bind the logger
                bindIsolateLogerSvc(event.getServiceReference());
                break;

            case ServiceEvent.MODIFIED_ENDMATCH:
            case ServiceEvent.UNREGISTERING:
                if (!hasIsolateLoggerSvc()) {
                    // Nothing to do
                    break;
                }

                // Unbind the logger
                unbindIsolateLoggerSvc();

                // Search for a new one
                findLoggerSvc();
                break;

            default:
                break;
            }
        }

    }

    /** Bundle ID key name */
    private static final String LIB_BNDL_ID = "BundleId";

    /**
     * the BundleContext given by the framework during the call of the method
     * start
     **/
    private BundleContext pContext = null;

    /** the reference to the isolate logger service **/
    private IIsolateLoggerSvc pLogger = null;

    /** The logger service reference */
    private ServiceReference<?> pLoggerReference = null;

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
     * Binds the log service
     * 
     * @param aServiceReference
     *            A reference to an isolate logger service
     */
    private boolean bindIsolateLogerSvc(
            final ServiceReference<?> aServiceReference) {

        if (aServiceReference == null) {
            // Invalid reference
            return false;
        }

        pLogger = (IIsolateLoggerSvc) pContext.getService(aServiceReference);
        if (pLogger == null) {
            // Service not found
            return false;
        }

        pLoggerReference = aServiceReference;
        return true;
    }

    /**
     * Looks for an {@link IIsolateLoggerSvc} service
     * 
     * @return True if a logger has been bound, else false
     */
    private boolean findLoggerSvc() {

        // find and bind the LogService
        final ServiceReference<IIsolateLoggerSvc> wIsolateLoggerServiceRef = pContext
                .getServiceReference(IIsolateLoggerSvc.class);

        if (wIsolateLoggerServiceRef != null) {
            return bindIsolateLogerSvc(wIsolateLoggerServiceRef);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.activators.IActivatorBase#getAllServiceReferences
     * ()
     */
    @Override
    public ServiceReference<?>[] getAllServiceReferences() {

        try {
            final ServiceReference<?>[] wSRs = pContext
                    .getAllServiceReferences(null, null);

            log(Level.INFO, "getAllServiceReferences", "NbServices=[%d]",
                    wSRs.length);

            return wSRs;

        } catch (final InvalidSyntaxException e) {
            // can't throw an InvalidSyntaxException, the method
            // getAllServiceReferences uses no filter !
            log(Level.SEVERE, "getAllServiceReferences", e);
            return new ServiceReference[0];
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.IActivatorBase#getBundleId()
     */
    @Override
    public String getBundleId() {

        return getClass().getPackage().getName();
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

    /**
     * Retrieves the currently bound log service (can be null)
     * 
     * @return The currently bound log service
     */
    protected IIsolateLoggerSvc getIsolateLoggerSvc() {

        return pLogger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.activators.IActivatorBase#getServiceReference
     * (java.lang.Long)
     */
    @Override
    public ServiceReference<?> getServiceReference(final Long aServiceId)
            throws Exception {

        final String wFilter = String.format("(%s=%s)",
                org.osgi.framework.Constants.SERVICE_ID, aServiceId.toString());
        final ServiceReference<?>[] wServiceReferences = pContext
                .getServiceReferences((String) null, wFilter);
        if (wServiceReferences != null && wServiceReferences.length > 0) {
            return wServiceReferences[0];
        }
        return null;
    }

    /**
     * Tests if a log service is currently bound
     * 
     * @return True if a log service is bound
     */
    protected boolean hasIsolateLoggerSvc() {

        return pLogger != null;
    }

    /**
     * Logs the given event, if a logger is bound
     * 
     * @param aLevel
     *            Log level
     * @param aWho
     *            Current object
     * @param aWhat
     *            Current action
     * @param aInfos
     *            Log message
     */
    private void log(final Level aLevel, final CharSequence aWhat,
            final Object... aInfos) {

        if (hasIsolateLoggerSvc()) {
            pLogger.log(aLevel, this, aWhat, aInfos);
        }
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

        // Try to bind to a logger
        findLoggerSvc();

        // Register a service listener
        final StringBuilder filterBuilder = new StringBuilder();
        filterBuilder.append("(objectclass=");
        filterBuilder.append(IIsolateLoggerSvc.class.getName());
        filterBuilder.append(")");

        pContext.addServiceListener(new CIsolateLoggerListener(),
                filterBuilder.toString());

        log(Level.INFO, "start", "START", toDescription());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext bundleContext) throws Exception {

        log(Level.INFO, "stop", "STOP", toDescription());

        unbindIsolateLoggerSvc();
        pContext = null;
    }

    /**
     * Logs the event then un-binds the logger
     */
    public void unbindIsolateLoggerSvc() {

        if (hasIsolateLoggerSvc()) {
            pLogger.logInfo(this, "unbindLogService");
            pContext.ungetService(pLoggerReference);
        }

        pLoggerReference = null;
        pLogger = null;
    }

}
