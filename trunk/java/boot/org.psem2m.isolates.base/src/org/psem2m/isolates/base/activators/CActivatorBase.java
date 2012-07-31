package org.psem2m.isolates.base.activators;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
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
    class CIsolateLoggerListner implements ServiceListener {

        /**
         * Explicit default constructor
         */
        CIsolateLoggerListner() {

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
                final ServiceReference wConfigRef = event.getServiceReference();
                final IIsolateLoggerSvc wLogService = (IIsolateLoggerSvc) getContext()
                        .getService(wConfigRef);
                bindIsolateLogerSvc(wLogService);

            } else if (event.getType() == ServiceEvent.UNREGISTERING) {
                getContext().ungetService(event.getServiceReference());
                unbindIsolateLogerSvc();
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

    /** the reference to the isolateLogger service **/
    private IIsolateLoggerSvc pLogger;

    /**
     * Explicit default constructor
     */
    public CActivatorBase() {

        super();
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
     * Binds the log service
     * 
     * @param aIsolateLogerSvc
     *            An isolate logger service
     */
    private void bindIsolateLogerSvc(final IIsolateLoggerSvc aIsolateLogerSvc) {

        pLogger = aIsolateLogerSvc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.base.activators.IActivatorBase#getAllServiceReferences
     * ()
     */
    @Override
    public ServiceReference[] getAllServiceReferences() {

        try {
            final ServiceReference[] wSRs = getContext()
                    .getAllServiceReferences(null, null);

            pLogger.logInfo(this, "getAllServiceReferences", "NbServices=[%d]",
                    wSRs.length);
            return wSRs;

        } catch (final InvalidSyntaxException e) {
            // can't throw an InvalidSyntaxException, the method
            // getAllServiceReferences uses no filter !
            pLogger.logSevere(this, "getAllServiceReferences", e);
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
    public ServiceReference getServiceReference(final Long aServiceId)
            throws Exception {

        final String wFilter = String.format("(%s=%s)",
                org.osgi.framework.Constants.SERVICE_ID, aServiceId.toString());
        final ServiceReference[] wServiceReferences = getContext()
                .getServiceReferences(null, wFilter);
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

        return getIsolateLoggerSvc() != null;
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
        final ServiceReference wIsolateLoggerServiceRef = bundleContext
                .getServiceReference(IIsolateLoggerSvc.class.getName());

        IIsolateLoggerSvc wIsolateLogerSvc = null;
        if (wIsolateLoggerServiceRef != null) {
            wIsolateLogerSvc = (IIsolateLoggerSvc) bundleContext
                    .getService(wIsolateLoggerServiceRef);
        }

        if (wIsolateLogerSvc != null) {
            bindIsolateLogerSvc(wIsolateLogerSvc);
        } else {
            final String wFilter = "(objectclass=" + LogService.class.getName()
                    + ")";
            getContext().addServiceListener(new CIsolateLoggerListner(),
                    wFilter);
        }

        if (hasIsolateLoggerSvc()) {
            pLogger.logInfo(this, "start", "START", toDescription());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext bundleContext) throws Exception {

        if (hasIsolateLoggerSvc()) {
            pLogger.logInfo(this, "stop", "STOP", toDescription());
        }
        pContext = null;
    }

    /**
     * Logs the event then un-binds the logger
     */
    public void unbindIsolateLogerSvc() {

        if (hasIsolateLoggerSvc()) {
            pLogger.logInfo(this, "unbindLogService");
        }
        pLogger = null;
    }

}
