/**
 * File:   ExceptionCatcher.java
 * Author: Thomas Calmant
 * Date:   4 nov. 2011
 */
package org.psem2m.composer.core.test.chain;

import java.util.HashMap;
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
 * A standard component that catches exceptions and stores them in the "error"
 * data key.
 * 
 * @author Thomas Calmant
 */
@Component(name = "test-entry")
@Provides(specifications = IComponent.class)
public class TestEntry extends CPojoBase implements IComponent {

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The component instance name */
    @Property(name = PROPERTY_INSTANCE_NAME)
    private String pName;

    @Property(name = "nbIterations", value = "10")
    private int pNbIterations;

    /** The next component */
    @Requires(id = NEXT_FIELD_ID)
    private IComponent pNext;

    /**
     * Default constructor
     */
    public TestEntry() {

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

        for (int i = 0; i < Math.max(pNbIterations, 1); i++) {

            try {
                aData.clear();

                // Do the job
                final Map<String, Object> result = pNext.computeResult(aData);

                // Log the result !
                pLogger.logInfo(this, "KING KING KING", i, "Result =",
                        result.get(KEY_RESULT));

                pLogger.logInfo(this, "KING KING KING", i, "Error  =",
                        result.get(KEY_ERROR));

            } catch (final Exception e) {
                pLogger.logSevere(this, "", "Something wrong happened :", e);
            }

            // Wait 500 ms
            Thread.sleep(500);
        }

        return aData;
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

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    pLogger.logInfo(this, "KING KING", "Ready for",
                            pNbIterations, "iterations...");
                    computeResult(new HashMap<String, Object>());

                } catch (final Exception e) {
                    pLogger.logSevere(this, "validatePojo", "Error in chain :",
                            e);
                }
            }
        }).start();
    }
}
