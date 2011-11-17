/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl.getItemsStock;

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
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.composer.test.api.IComponentContext;
import org.psem2m.demo.erp.api.beans.IQualityLevels;
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

    /** The key used in the result map to store the cache age */
    @Property(name = "cacheAgeEntry")
    private String pCacheAgeEntry = "__cache_age";

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

        // Result is a List of Maps containing id, stock and __cache_age keys
        for (final Map<String, Object> itemStockMap : aContext.getResults()) {

            // Get the quality level
            final int qualityLevel = extractCacheQualityLevel(itemStockMap);

            // No key translation needed.

            // Add it to the map
            itemStockMap.put("qualityLevel", qualityLevel);
        }

        return aContext;
    }

    /**
     * Extracts and removes the cache age entry in the given map and returns the
     * corresponding quality level
     * 
     * @param aMap
     *            Map to use
     * @return The data quality level, {@link IQualityLevels#CACHE_LEVEL_SYNC}
     *         by default
     */
    protected int extractCacheQualityLevel(final Map<String, Object> aMap) {

        if (!aMap.containsKey(pCacheAgeEntry)) {
            // Key missing : SYNC
            return IQualityLevels.CACHE_LEVEL_SYNC;
        }

        // Get the cache age, if any ...
        final Number cacheAge = (Number) aMap.get(pCacheAgeEntry);
        final int qualityLevel;
        if (cacheAge == null || cacheAge.longValue() <= 0) {

            // Quality level 0, negative = SYNC
            qualityLevel = IQualityLevels.CACHE_LEVEL_SYNC;

        } else {

            // Compute the quality according to the indicated age
            qualityLevel = QualityUtilities.computeCacheQuality(cacheAge
                    .longValue());
        }

        // ... then remove it
        aMap.remove(pCacheAgeEntry);

        return qualityLevel;
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
