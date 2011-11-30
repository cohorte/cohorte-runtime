/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.demo.impl;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.demo.ComponentContextDumper;
import org.psem2m.composer.demo.IComponent;
import org.psem2m.composer.demo.IComponentContext;
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

    /**
     * The next component (can fall) => nullable = false => no mock, pNext could
     * be null !
     */
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
    public IComponentContext computeResult(final IComponentContext aContext)
            throws Exception {

        // Prepare the result context reference
        IComponentContext result = aContext;

        // Flag to use fall back component
        boolean useFallback = true;

        if (pNext != null) {

            pLogger.logInfo(this, "fallback.computeResult",
                    "component=[%s]  First choice seems present", pName);

            try {
                result = pNext.computeResult(aContext);

                // Use fall back component if there is no result
                // and if an error occurred
                useFallback = result == null || !result.hasResult()
                        && result.hasError();
            } catch (RuntimeException wErr) {
                // An error occurred
                pLogger.logWarn(this, "fallback.computeResult",
                        "component=[%s]  ERROR calling next component  : %s ",
                        pName, wErr.getMessage());
            } catch (final Throwable error) {
                // An error occurred
                pLogger.logWarn(this, "fallback.computeResult",
                        "component=[%s] ERROR calling next component : ",
                        pName, error);
            }
        }

        if (useFallback) {

            // Reset context if needed
            if (result != null) {
                if (result.hasError()) {
                    result.getErrors().clear();
                }
                if (result.hasResult()) {
                    result.getResults().clear();
                }
            }
            pLogger.logWarn(this, "fallback.computeResult", "Using fall-back");

            // Something went wrong, call the other guy
            result = pFallback.computeResult(aContext);
        }

        pLogger.logDebug(this, "fallback.computeResult",
                "component=[%s] RESULT: %s", pName,
                result != null ? ComponentContextDumper.dump(result)
                        : "result is null");

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

    @Bind(id = NEXT_FIELD_ID)
    public void nextBind(final IComponent aNext) {

        pLogger.logInfo(this, "nextBind", "component=[%s] next=[%s]", pName,
                String.valueOf(aNext));
    }

    @Unbind(id = NEXT_FIELD_ID)
    public void nextUnbind(final IComponent aNext) {

        pLogger.logInfo(this, "nextUnbind", "component=[%s] next=[%s]", pName,
                String.valueOf(aNext));
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
