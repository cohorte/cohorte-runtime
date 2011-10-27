/**
 * File:   HelloTest.java
 * Author: "Thomas Calmant"
 * Date:   27 oct. 2011
 */
package org.psem2m.composer.core.test;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleException;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * @author "Thomas Calmant"
 * 
 */
@Component(name = "hello-test")
public class HelloTest extends CPojoBase {

    @Requires(id = "logger")
    private IIsolateLoggerSvc pLogger;

    /**
     * 
     */
    public HelloTest() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    public void invalidatePojo() throws BundleException {

        System.out.println("Hello test stopped");
        pLogger.logInfo(this, "INVALIDATE", "HELLO TEST GONE");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

        System.out.println("Hello test started");
        pLogger.logInfo(this, "VALIDATE", "HELLO TEST READY");
    }
}
