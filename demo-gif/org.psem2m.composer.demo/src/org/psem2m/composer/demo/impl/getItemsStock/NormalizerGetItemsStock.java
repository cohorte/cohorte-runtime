/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl.getItemsStock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.composer.test.api.IComponentContext;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.demo.erp.api.beans.QualityUtilities;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * getItemsStock result normalizer
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_NORMALIZER_GETITEMSSTOCK)
@Provides(specifications = IComponent.class)
public class NormalizerGetItemsStock extends CPojoBase implements IComponent {

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = DemoComponentsConstants.PROPERTY_INSTANCE_NAME)
    private String pName;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.test.api.IComponent#computeResult(org.psem2m.composer
     * .test.api.IComponentContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        if (aContext.hasError()) {
            // Prepare a new map, with both result and error
            final Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put(KEY_ERROR, aContext.getErrors().toArray());
            resultMap.put(KEY_RESULT, aContext.getResults().toArray());

            aContext.setResult(resultMap);
            return aContext;
        }

        if (!aContext.hasResult()) {
            // No error and no result...
            aContext.addError(pName, "No result found...");
            return aContext;
        }

        pLogger.logInfo(this, "...Normalizer GetStocks.result",
                aContext.getResults());

        // Result is a Map, containing ICachedObject<Integer> instances
        final Map<String, Object> result = aContext.getResults().get(0);
        final List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        pLogger.logInfo(this, "...Normalizer GetStocks...", result);

        for (final Entry<String, Object> entry : result.entrySet()) {

            final Object entryValue = entry.getValue();

            if (entryValue instanceof ICachedObject) {

                final ICachedObject<Integer> cachedObject = (ICachedObject<Integer>) entryValue;
                final int qualityLevel = QualityUtilities
                        .computeCacheQuality(cachedObject.getCacheAge());

                final Map<String, Object> itemMap = new HashMap<String, Object>();
                itemMap.put("id", entry.getKey());
                itemMap.put("stock", cachedObject.getObject());
                itemMap.put("qualityLevel", qualityLevel);

                resultList.add(itemMap);

            } else {
                aContext.addError(pName, "Don't know how to handle : "
                        + entryValue);
            }
        }

        final Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put(KEY_RESULT, resultList.toArray());
        aContext.setResult(newMap);

        // Unknown result type, return the whole data
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
