/**
 * File:   CacheFlushJunitTest.java
 * Author: Thomas Calmant
 * Date:   17 oct. 2011
 */
package org.psem2m.demo.data.cache.test;

import org.psem2m.utilities.teststools.CQualityTestBase;

/**
 * Tests / stresses the flush capacity of the data.cache
 * 
 * @author Thomas Calmant
 */
public class CacheFlushJunitTest extends CQualityTestBase {

    /** Cache tester console */
    private CacheFlushTester pCacheTester;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {

        pCacheTester = new CacheFlushTester(START_ARGS);
        logInfo(this, "startTests", "CacheFlushJunitTest App=[%s]",
                pCacheTester.getIdentifier());
    }

    /**
     * Flush / Load test
     * 
     * @throws Exception
     */
    public void testFlushLoad() throws Exception {

        logInfo(this, "testFlushLoad", "cmde=[%s]",
                CacheFlushTester.FLUSH_LOAD_COMMAND);

        pCacheTester.execLine(CacheFlushTester.FLUSH_LOAD_COMMAND);
    }
}
