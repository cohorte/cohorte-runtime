/**
 * File:   HelloTest.java
 * Author: Thomas Calmant
 * Date:   27 oct. 2011
 */
package org.psem2m.composer.core.test;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleException;
import org.psem2m.composer.test.api.IHelloSvc;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;

/**
 * Simple "Hello World" service for tests
 * 
 * @author Thomas Calmant
 */
@Component(name = "hello-test")
@Provides(specifications = IHelloSvc.class)
public class HelloTest extends CPojoBase implements IHelloSvc {

    /** The logger */
    @Requires(id = "logger")
    private IIsolateLoggerSvc pLogger;

    /**
     * Default constructor
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

        pLogger.logInfo(this, "invalidatePojo", "Composer.Test.Hello Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.core.test.IHelloSvc#logHello(java.lang.String)
     */
    @Override
    public void logHello(final String aName) {

        pLogger.logInfo(this, "logHello", sayHello(aName));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.core.test.IHelloSvc#sayHello(java.lang.String)
     */
    @Override
    public String sayHello(final String aName) {

        return "Hello, " + aName + " !";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Composer.Test.Hello Ready");
    }
}
