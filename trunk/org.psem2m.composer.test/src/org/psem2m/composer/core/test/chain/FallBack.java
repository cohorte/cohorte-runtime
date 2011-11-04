/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.test.api.IComponent;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * A component that use a preferred component if possible, but which can use a
 * second component on failure
 * 
 * @author Thomas Calmant
 */
@Component(name = "fall-back")
@Provides(specifications = IComponent.class)
public class FallBack extends CPojoBase implements IComponent {

    /** The component to use if the first one is down */
    @Requires(id = "second")
    private IComponent pFallback;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The component instance name */
    @Property(name = PROPERTY_INSTANCE_NAME)
    private String pName;

    /** The next component (can fall) */
    @Requires(id = NEXT_FIELD_ID, nullable = false, optional = true)
    private IComponent pNext;

    /**
     * Default constructor
     */
    public FallBack() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponent#computeResult(java.util.Map)
     */
    @Override
    public Map<String, Object> computeResult(final Map<String, Object> aData)
            throws Exception {

        // Prepare the result map reference
        Map<String, Object> result = aData;

        // Flag to use fall back component
        boolean useFallback = true;

        if (pNext != null) {

            pLogger.logInfo(this, "fallback.computeResult",
                    "First choice seems present");

            try {
                result = pNext.computeResult(aData);

                // Use fall back component if there is no result
                // and if an error occurred
                useFallback = (result.get(KEY_RESULT) == null)
                        && (result.get(KEY_ERROR) != null);

                pLogger.logInfo(this, "fallback.computeResult",
                        "Post Compute result =", useFallback, "- result =",
                        result.get(KEY_RESULT), "- error =",
                        result.get(KEY_ERROR));

            } catch (final Throwable error) {
                // An error occurred
                pLogger.logWarn(this, "fallback.computeResult",
                        "Error calling next component : ", error);
            }
        }

        if (useFallback) {
            // Something went wrong, call the other guy
            pFallback.computeResult(aData);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Component '" + pName
                + "' Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Component '" + pName + "' Ready");
    }
}
