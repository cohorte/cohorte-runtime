/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl.applyCart;

import java.util.HashMap;
import java.util.Map;

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
 * applyCart result normalizer
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_NORMALIZER_APPLYCART)
@Provides(specifications = IComponent.class)
public class NormalizerApplyCart extends CPojoBase implements IComponent {

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        if (!aContext.hasResult()) {
            // No error and no result...
            aContext.addError(pName, "No result found...");
        }

        if (aContext.hasError()) {
            // Prepare a new map, with 'message' 'reason' and 'code'
            final Map<String, Object> resultMap = new HashMap<String, Object>();

            /*
             * 
             * array ( 'status' => 500, 'message' =>
             * 'DataServerApplication.CartsApplier2.safeErpCaller : Exception
             * caught class=[org.jabsorb.client.ErrorResponse] mess(1)=[JSONRPC
             * error code -32603: | ErrorResponse |
             * org.jabsorb.client.Client(processException:227)]
             * stack=[org.jabsorb.client.Client(processException:227)
             * org.jabsorb.client.Client(invoke:174)
             * org.jabsorb.client.Client(invoke:135) $Proxy1(applyCart:-1)
             * org.psem2m.composer.demo.erpproxy.ErpProxy(__M_applyCart:92)
             * org.psem2m.composer.demo.erpproxy.ErpProxy(applyCart:-1)
             * org.psem2m.composer.demo.erpproxy.ErpProxy(__M_computeResult:119)
             * org.psem2m.composer.demo.erpproxy.ErpProxy(computeResult:-1)
             */
            String wMessage = "no message !";
            String wReason = "no reason !";
            if (aContext.getErrors().size() > 0) {
                final String wInfos = aContext.getErrors().get(0);
                final String[] wParts = wInfos.split("\\|");
                if (wParts != null && wParts.length >= 2) {
                    wReason = wParts[0];
                    wMessage = wParts[1];
                }
            }
            resultMap.put("message", wMessage);
            resultMap.put("reason", wReason);
            resultMap.put("code", 500);

            aContext.setResult(resultMap);
            return aContext;
        }

        return aContext;
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
