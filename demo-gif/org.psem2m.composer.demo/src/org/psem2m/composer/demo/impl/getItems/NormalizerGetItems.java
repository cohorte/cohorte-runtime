/**
 * File:   ErpCaller.java
 * Author: Thomas Calmant
 * Date:   14 nov. 2011
 */
package org.psem2m.composer.demo.impl.getItems;

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
import org.psem2m.composer.demo.CComponentPojo;
import org.psem2m.composer.demo.CComponentsConstants;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
import org.psem2m.composer.demo.IQualityLevels;
import org.psem2m.composer.demo.QualityUtilities;
import org.psem2m.isolates.base.IIsolateLoggerSvc;

/**
 * getItems result normalizer
 * 
 * @author Thomas Calmant
 */
@Component(name = CComponentsConstants.COMPONENT_NORMALIZER_GETITEMS)
@Provides(specifications = IComponent.class)
public class NormalizerGetItems extends CComponentPojo implements IComponent {

    /** The key used in the result map to store the cache age */
    @Property(name = "cacheAgeEntry", value = "__cache_age")
    private String pCacheAgeEntry;

    /** Result keys translation map */
    private final Map<String, String> pKeyTranslationMap = new HashMap<String, String>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The instance name */
    @Property(name = CComponentsConstants.PROPERTY_INSTANCE_NAME)
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

        final List<Map<String, Object>> newResults = new ArrayList<Map<String, Object>>(
                aContext.getResults().size());

        for (final Map<String, Object> itemBean : aContext.getResults()) {

            /*
             * Do this before the copy loop, to avoid keeping an internal value
             * in the result
             */
            final int qualityLevel = extractCacheQualityLevel(itemBean);

            final Map<String, Object> newItem = new HashMap<String, Object>();
            for (final Entry<String, Object> entry : itemBean.entrySet()) {

                final String key = entry.getKey();
                final Object entryValue = entry.getValue();

                // Translate the key, if needed
                final String translatedKey = pKeyTranslationMap.get(key);
                if (translatedKey == null) {
                    // No translation for this key
                    newItem.put(key, entryValue);

                } else if (!translatedKey.isEmpty()) {
                    // Not empty new key name : store it with a new name
                    newItem.put(translatedKey, entryValue);
                }
                // Empty translation = ignore the key
            }

            // Set the quality level at last, to avoid to be erased by the map
            // content
            newItem.put("qualityLevel", qualityLevel);

            newResults.add(newItem);
        }

        aContext.getResults().clear();
        aContext.getResults().addAll(newResults);

        // Unknown result type, return the whole data
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
     * @see org.psem2m.composer.demo.impl.CComposable#getName()
     */
    @Override
    public String getName() {

        return pName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pKeyTranslationMap.clear();

        pLogger.logInfo(this, "invalidatePojo", "cpnt=[%25s] Gone",
                getShortName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Keys translation ERP -> WebStore
        pKeyTranslationMap.put("lib", "name");
        pKeyTranslationMap.put("text", "description");

        pLogger.logInfo(this, "validatePojo", "cpnt=[%25s] Ready",
                getShortName());
    }
}
