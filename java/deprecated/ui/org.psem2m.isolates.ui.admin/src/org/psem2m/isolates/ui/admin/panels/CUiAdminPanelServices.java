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

package org.psem2m.isolates.ui.admin.panels;

import javax.swing.SwingUtilities;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.ui.admin.CBundleUiActivator;
import org.psem2m.isolates.ui.admin.api.EUiAdminFont;
import org.psem2m.isolates.ui.admin.api.EUiAdminPanelLocation;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanel;
import org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler;
import org.psem2m.isolates.ui.admin.api.IUiAdminSvc;

/**
 * @author ogattaz
 *
 */
@Component(name = "psem2m-ui-admin-panel-services-factory",
        publicFactory = false)
@Instantiate(name = "psem2m-ui-admin-panel-services")
public class CUiAdminPanelServices extends CPojoBase implements
        IUiAdminPanelControler {

    /**
     * @author ogattaz
     *
     */
    private class CServiceListener implements ServiceListener {

        /*
         * (non-Javadoc)
         *
         * @see
         * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework
         * .ServiceEvent)
         */
        @Override
        public void serviceChanged(final ServiceEvent aServiceEvent) {

            final int eventType = aServiceEvent.getType();
            if (eventType != ServiceEvent.MODIFIED) {
                // Do not log service modifications
                pLogger.logInfo(this, "serviceChanged",
                        "ServiceEvent=[%s] service=[%s]",
                        serviceEventToString(eventType), aServiceEvent
                                .getServiceReference().toString());
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    if (pJPanel != null) {
                        switch (eventType) {
                        case ServiceEvent.MODIFIED:
                        case ServiceEvent.REGISTERED:
                            pJPanel.setRow(aServiceEvent.getServiceReference());
                            break;

                        case ServiceEvent.UNREGISTERING:
                            pJPanel.removeRow(aServiceEvent
                                    .getServiceReference());
                            break;
                        }
                    }
                }
            });

        }

        private String serviceEventToString(final int aType) {

            switch (aType) {
            case ServiceEvent.REGISTERED:
                return String.format("%d REGISTERED", aType);

            case ServiceEvent.UNREGISTERING:
                return String.format("%d UNREGISTERING", aType);

            case ServiceEvent.MODIFIED:
                return String.format("%d MODIFIED", aType);

            case ServiceEvent.MODIFIED_ENDMATCH:
                return String.format("%d MODIFIED_ENDMATCH", aType);

            default:
                return String.format("%d ???", aType);
            }
        }
    }

    /** the JPanel **/
    private CJPanelTableServices pJPanel = null;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    private CServiceListener pServiceListener = null;

    /** the UiAdminPanel returned by the IUiAdminScv **/
    private IUiAdminPanel pUiAdminPanel = null;

    /** the IUiAdminScv **/
    @Requires
    private IUiAdminSvc pUiAdminSvc;

    /**
     * Explicit constructor
     */
    public CUiAdminPanelServices() {

        super();
    }

    /**
     *
     */
    private void initContent() {

        final Runnable wRunnable = new Runnable() {

            @Override
            public void run() {

                pJPanel = new CJPanelTableServices(pLogger,
                        pUiAdminPanel.getPanel());

                pLogger.logInfo(this, "initContent");
                // put in place the list of all registered services.
                pJPanel.setRows(CBundleUiActivator.getInstance()
                        .getAllServiceReferences());

                pUiAdminPanel.pack();

                pServiceListener = new CServiceListener();
                CBundleUiActivator.getInstance().getContext()
                        .addServiceListener(pServiceListener);
            }
        };
        try {
            // gives the runnable to the UIExecutor
            SwingUtilities.invokeLater(wRunnable);

        } catch (final Exception e) {
            pLogger.logSevere(this, "init", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // logs in the bundle output
        pLogger.logInfo(this, "invalidatePojo", "INVALIDATE", toDescription());
        try {
            pUiAdminSvc.removeUiAdminPanel(pUiAdminPanel);

            if (pServiceListener != null) {
                CBundleUiActivator.getInstance().getContext()
                        .removeServiceListener(pServiceListener);
                pServiceListener = null;
            }

            if (pJPanel != null) {
                pJPanel.destroy();
                pJPanel = null;
            }

        } catch (final Exception e) {
            pLogger.logSevere(this, "invalidatePojo", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.psem2m.isolates.ui.admin.api.IUiAdminPanelControler#setUiAdminFont
     * (org.psem2m.isolates.ui.admin.api.EUiAdminFont)
     */
    @Override
    public void setUiAdminFont(final EUiAdminFont aUiAdminFont) {

        if (pJPanel != null) {
            pJPanel.setTableFont(aUiAdminFont);
            pJPanel.setTextFont(aUiAdminFont);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // logs in the bundle output
        pLogger.logInfo(this, "validatePojo", "VALIDATE", toDescription());

        try {
            pUiAdminPanel = pUiAdminSvc.newUiAdminPanel("Services",
                    "Services list and managment.", null, this,
                    EUiAdminPanelLocation.FIRST);

            initContent();

        } catch (final Exception e) {
            pLogger.logSevere(this, "validatePojo", e);
        }
    }
}
