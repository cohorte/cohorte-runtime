/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.dataserver;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * getItems treatment chain entry point
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_ENDPOINT_GETITEMS)
@Provides(specifications = IComponent.class)
public class EndpointGetItems extends CPojoBase implements IComponent {

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The next component of the chain */
    @Requires(id = DemoComponentsConstants.WIRE_NEXT)
    private IComponent pNext;

    /** The result normalizer */
    @Requires(id = DemoComponentsConstants.WIRE_NORMALIZER)
    private IComponent pNormalizer;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        return pNormalizer.computeResult(pNext.computeResult(aContext));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Component", pName, "Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Component", pName, "Ready");
    }
}
