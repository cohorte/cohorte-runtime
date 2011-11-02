/**
 * File:   HelloTest.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.psem2m.composer.core.test;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.test.api.IHelloSvc;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Simple "Hello World" service for tests
 * 
 * @author Thomas Calmant
 */
@Component(name = "hello-consumer-test")
public class HelloConsumerTest extends CPojoBase {

    /** The logger */
    @Requires(id = "logger")
    private IIsolateLoggerSvc pLogger;

    /** The test service */
    @Requires(id = "hello-svc")
    private IHelloSvc pService;

    /**
     * Default constructor
     */
    public HelloConsumerTest() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Composer.Test.Hello Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Composer.Test.Hello Ready");

        pLogger.logInfo(this, "validatePojo", "Say hello = ",
                pService.sayHello("ConsumerTester"));

        pService.logHello("from ConsumerTester");
    }
}
