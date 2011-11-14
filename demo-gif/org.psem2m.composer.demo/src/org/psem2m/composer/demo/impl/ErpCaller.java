/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl;

import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * getItem treatment chain entry point
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_ERP_CALLER)
@Provides(specifications = IComponent.class)
public class ErpCaller extends CPojoBase implements IComponent {

    /** The ERP host name */
    @Property(name = "host")
    private String pErpHost;

    /** The ERP method to call */
    @Property(name = "method")
    private String pErpMethod;

    /** The ERP host port */
    @Property(name = "port")
    private int pErpPort;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pInstanceName;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public Map<String, Object> computeResult(final Map<String, Object> aData)
            throws Exception {

        // TODO Auto-generated method stub
        pLogger.logInfo(this, "computeResult", pInstanceName, "called...");

        aData.put(IComponent.KEY_RESULT,
                "ERP Called at " + System.currentTimeMillis() + " - "
                        + pErpHost + ":" + pErpPort + "/" + pErpMethod);
        return aData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Component", pInstanceName,
                "Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Component", pInstanceName,
                "Ready");
    }
}
