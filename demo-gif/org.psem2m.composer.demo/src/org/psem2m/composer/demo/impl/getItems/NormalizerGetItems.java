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
import org.psem2m.composer.demo.DemoComponentsConstants;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.composer.test.api.IComponentContext;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.demo.erp.api.beans.QualityUtilities;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * getItems result normalizer
 * 
 * @author Thomas Calmant
 */
@Component(name = DemoComponentsConstants.COMPONENT_NORMALIZER_GETITEMS)
@Provides(specifications = IComponent.class)
public class NormalizerGetItems extends CPojoBase implements IComponent {

    /** Result keys translation map */
    private final Map<String, String> pKeyTranslationMap = new HashMap<String, String>();

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

        final List<Map<String, Object>> newResults = new ArrayList<Map<String, Object>>(
                aContext.getResults().size());

        for (final Map<String, Object> itemBean : aContext.getResults()) {

            final Map<String, Object> newItem = new HashMap<String, Object>();
            long minAge = Integer.MAX_VALUE;

            for (final Entry<String, Object> entry : itemBean.entrySet()) {

                final String key = entry.getKey();
                final Object entryValue = entry.getValue();
                final Object storedObject;

                if (entryValue instanceof ICachedObject) {

                    final ICachedObject<?> cachedObject = (ICachedObject<?>) entryValue;

                    // Compute minimum age
                    if (minAge > cachedObject.getCacheAge()) {
                        minAge = cachedObject.getCacheAge();
                    }

                    storedObject = cachedObject.getObject();

                } else {
                    // Raw storage...
                    storedObject = entryValue;
                }

                // Translate the key, if needed
                final String translatedKey = pKeyTranslationMap.get(key);
                if (translatedKey == null) {
                    // No translation for this key
                    newItem.put(key, storedObject);

                } else if (!translatedKey.isEmpty()) {
                    // Not empty new key name : store it with a new name
                    newItem.put(translatedKey, storedObject);
                }
                // Empty translation = ignore the key
            }

            // Get the worst quality level of the overall values
            newItem.put("qualityLevel",
                    QualityUtilities.computeCacheQuality(minAge));

            newResults.add(newItem);
        }

        aContext.getResults().clear();
        aContext.getResults().addAll(newResults);

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

        pKeyTranslationMap.clear();

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

        // Keys translation ERP -> WebStore
        pKeyTranslationMap.put("lib", "name");
        pKeyTranslationMap.put("text", "description");

        pLogger.logInfo(this, "validatePojo", "Component", pName, "Ready");
    }
}
