/**
 * File:   IsolateComposer.java
 * Author: Thomas Calmant
 * Date:   18 oct. 2013
 */
package org.cohorte.composer.isolate;

import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.cohorte.composer.api.IAgent;
import org.cohorte.composer.api.IIsolateComposer;
import org.cohorte.composer.api.Isolate;
import org.cohorte.composer.api.RawComponent;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.constants.IPlatformProperties;

/**
 * The Isolate Composer entry point
 * 
 * @author Thomas Calmant
 */
@Component
@Provides(specifications = IIsolateComposer.class,
        properties = {
                @StaticServiceProperty(name = "service.exported.interfaces",
                        type = "String",
                        value = "org.cohorte.composer.api.IIsolateComposer"),
                @StaticServiceProperty(name = "cohorte.remote.synonyms",
                        type = "String[]",
                        value = "{python:/cohorte.composer.isolate}") })
@Instantiate(name = "cohorte-composer-isolate")
public class IsolateComposer implements IIsolateComposer {

    /** The composer agent */
    private IAgent pAgent;

    /** Bundle context */
    private final BundleContext pContext;

    /** The logger */
    @Requires
    private LogService pLogger;

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

        // TODO: Get the components

        // Return the bean
        return new Isolate(name, "java", new HashSet<RawComponent>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IIsolateComposer#instantiate(java.util.Set)
     */
    @Override
    public void instantiate(final Set<RawComponent> aComponents) {

        // Instantiate the components
        pAgent.handle(aComponents);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.composer.api.IIsolateComposer#kill(java.util.Set)
     */
    @Override
    public void kill(final Set<String> aNames) {

        // Kill the components
        for (final String name : aNames) {
            pAgent.kill(name);
        }
    }
}
