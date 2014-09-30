/**
 * File:   IsolateComposer.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2013
 */
package org.cohorte.composer.isolate;

import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.composer.api.ComposerConstants;
import org.cohorte.composer.api.IAgent;
import org.cohorte.composer.api.IIsolateComposer;
import org.cohorte.composer.api.Isolate;
import org.cohorte.composer.api.RawComponent;
import org.cohorte.remote.IRemoteServicesConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * The Isolate Composer entry point
 *
 * @author Thomas Calmant
 */
@Component
@Provides(specifications = IIsolateComposer.class, properties = {
        @StaticServiceProperty(name = Constants.SERVICE_EXPORTED_INTERFACES,
                type = "String",
                value = "org.cohorte.composer.api.IIsolateComposer"),
        @StaticServiceProperty(name = IRemoteServicesConstants.PROP_SYNONYMS,
                type = "String",
                value = ComposerConstants.SYNONYM_ISOLATE_COMPOSER) })
@Instantiate(name = "cohorte-composer-isolate")
public class IsolateComposer implements IIsolateComposer {

    /** The composer agent */
    @Requires
    private IAgent pAgent;

    /** Bundle context */
    private final BundleContext pContext;

    /** Host isolate name */
    @ServiceProperty(name = ComposerConstants.PROP_ISOLATE_NAME)
    private String pIsolateName;

    /** The logger */
    @Requires
    private LogService pLogger;

    /** Host node name */
    @ServiceProperty(name = ComposerConstants.PROP_NODE_NAME)
    private String pNodeName;

    /** Host node UID */
    @ServiceProperty(name = ComposerConstants.PROP_NODE_UID)
    private String pNodeUid;

    /** Isolate composer status */
    private LocalStatus pStatus;

    /**
     * Component creation
     *
     * @param aContext
     *            The bundle context
     */
    public IsolateComposer(final BundleContext aContext) {

        pContext = aContext;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.cohorte.composer.api.IIsolateComposer#get_isolate_info()
     */
    @Override
    public Isolate get_isolate_info() {

        // Get the isolate name
        final String name = pContext
                .getProperty(IPlatformProperties.PROP_ISOLATE_NAME);

        // Return the bean
        return new Isolate(name, "java", pStatus.getComponents());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IIsolateComposer#get_isolate_uid()
     */
    @Override
    public String get_isolate_uid() {

        return pContext.getProperty(IPlatformProperties.PROP_ISOLATE_UID);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.cohorte.composer.api.IIsolateComposer#instantiate(java.util.Set)
     */
    @Override
    public void instantiate(final Set<RawComponent> aComponents) {

        // Store the new components
        pStatus.store(aComponents);

        // Instantiate the components
        pAgent.handle(aComponents);
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Clean up values
        pIsolateName = null;
        pNodeName = null;
        pNodeUid = null;
        pStatus = null;

        pLogger.log(LogService.LOG_INFO, "Isolate composer invalidated");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IIsolateComposer#kill(java.util.Set)
     */
    @Override
    public void kill(final Set<String> aNames) {

        // Update the status storage
        pStatus.remove(aNames);

        // Kill the components
        for (final String name : aNames) {
            pAgent.kill(name);
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Prepare the status storage
        pStatus = new LocalStatus(pLogger);

        // Store isolate information
        pIsolateName = pContext
                .getProperty(IPlatformProperties.PROP_ISOLATE_NAME);
        pNodeName = pContext.getProperty(IPlatformProperties.PROP_NODE_NAME);
        pNodeUid = pContext.getProperty(IPlatformProperties.PROP_NODE_UID);

        pLogger.log(LogService.LOG_INFO, "Isolate composer validated");
    }
}
