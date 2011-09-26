package org.psem2m.isolates.base.activators;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
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
                ServiceReference wConfigRef = event.getServiceReference();
                IIsolateLoggerSvc wLogService = (IIsolateLoggerSvc) getContext()
                        .getService(wConfigRef);
                bindIsolateLogerSvc(wLogService);

            } else if (event.getType() == ServiceEvent.UNREGISTERING) {
                getContext().ungetService(event.getServiceReference());
                unbindIsolateLogerSvc();
            }
        }

    }

    public static String LIB_BNDL_ID = "BundleId";

    public static String LIB_POJO_ID = "PojoId";

    /**
     * the BundleContext given by the framework during the call of the method
     * start
     **/
    private BundleContext pContext = null;

    /** the reference to the isolateLogger service **/
    private IIsolateLoggerSvc pIsolateLoggerSvc;

    /**
     * the flag to indicates if that the LogListener was put in place by this
     * Activator
     **/
    private boolean pLogListenerSet = false;

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
     * @param aIsolateLogerSvc
     */
    private void bindIsolateLogerSvc(final IIsolateLoggerSvc aIsolateLogerSvc) {

        pIsolateLoggerSvc = aIsolateLogerSvc;
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
     * @return
     */
    private boolean hasIsolateLoggerSvc() {

        return pIsolateLoggerSvc != null;
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
        ServiceReference wIsolateLoggerServiceRef = bundleContext
                .getServiceReference(IIsolateLoggerSvc.class.getName());

        IIsolateLoggerSvc wIsolateLogerSvc = null;
        if (wIsolateLoggerServiceRef != null) {
            wIsolateLogerSvc = (IIsolateLoggerSvc) bundleContext
                    .getService(wIsolateLoggerServiceRef);
        }

        if (wIsolateLogerSvc != null) {
            bindIsolateLogerSvc(wIsolateLogerSvc);
        } else {
            String wFilter = "(objectclass=" + LogService.class.getName() + ")";
            getContext().addServiceListener(new CIsolateLoggerListner(),
                    wFilter);
        }

        if (hasIsolateLoggerSvc()) {
            pIsolateLoggerSvc.logInfo(this, "start", "START", toDescription());
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
            pIsolateLoggerSvc.logInfo(this, "stop", "STOP", toDescription());
        }
        pContext = null;
    }

    /**
	 *
	 */
    public void unbindIsolateLogerSvc() {

        if (hasIsolateLoggerSvc()) {
            pIsolateLoggerSvc.logInfo(this, "unbindLogService");
        }
        pIsolateLoggerSvc = null;
    }

}
