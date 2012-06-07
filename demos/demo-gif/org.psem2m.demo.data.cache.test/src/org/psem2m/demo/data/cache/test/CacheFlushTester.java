/**
 * File:   CacheFlushTester.java
 * Author: Thomas Calmant
 * Date:   17 oct. 2011
 */
package org.psem2m.demo.data.cache.test;

import java.util.StringTokenizer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.demo.data.cache.ICachedObject;
import org.psem2m.utilities.logging.CLogToolsException;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * The cache flush tester
 * 
 * @author Thomas Calmant
 */
public class CacheFlushTester extends CConsoleTester {

    /** FlushLoad test command */
    public static final String FLUSH_LOAD_COMMAND = "flush-load";

    /**
     * @param args
     */
    public static void main(final String[] args) {

        try {
            CConsoleTester.main(new CacheFlushTester(args));

        } catch (Throwable e) {
            System.out.println("main(): EXCEPTION\n");
            System.out.println(CLogToolsException.getInstance().eInString(e));
            System.exit(1);
        }
    }

    /**
     * Standard constructor
     * 
     * @param aArgs
     * @throws Exception
     */
    public CacheFlushTester(final String[] aArgs) throws Exception {

        super(aArgs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.utilities.teststools.CConsoleTester#buildHelp(java.lang.
     * StringBuilder)
     */
    @Override
    protected void buildHelp(final StringBuilder aHelp) {

        addHelpTitle(aHelp, "Cache Tester");

        addHelpLine(aHelp, FLUSH_LOAD_COMMAND);
        addHelpSubLine(aHelp, "flushes some data then reloads them.");
    }

    /**
     * Simple test : insert some data in the cache, then flush'em to the disk
     * and reload the file.
     * 
     * @return True on success, false on error
     * @throws Exception
     */
    protected boolean doTestFlushLoad() throws Exception {

        // Get the service
        final ICacheFactory factory = getCacheFactory();
        if (factory == null) {
            logInfo("No cache factory.");
            return false;
        }

        // Open channels
        ICacheChannel<Integer, String> channel1 = factory
                .openChannel("channel-1");
        if (channel1 == null) {
            throw new Exception("Error openning channel 1");
        }

        ICacheDequeueChannel<Integer, String> channel2 = factory
                .openDequeueChannel("channel-2");
        if (channel2 == null) {
            throw new Exception("Error openning channel 2");
        }

        // Insert some data
        for (int i = 0; i < 10; i++) {
            // In channel 1...
            channel1.put(Integer.valueOf(i), Integer.toString(i));

            // In channel 2...
            channel2.add(Integer.toString(i));
        }

        // Flush the cache
        factory.flush();

        // Reload the cache file
        factory.reload();

        // Reopen the channels
        channel1 = factory.openChannel("channel-1");
        if (channel1 == null) {
            throw new Exception("Error re-openning channel 1");
        }

        channel2 = factory.openDequeueChannel("channel-2");
        if (channel2 == null) {
            throw new Exception("Error re-openning channel 2");
        }

        // Compare data with expected one
        for (int i = 0; i < 10; i++) {

            // Channel 1
            final ICachedObject<String> channel1Str = channel1.get(Integer
                    .valueOf(i));
            if (channel1Str == null
                    || !channel1Str.getObject().equals(Integer.toString(i))) {
                throw new Exception("Channel1 - Invalid string '" + channel1Str
                        + "' for int " + i);
            }

            // Channel 2
            final ICachedObject<String> channel2Str = channel2.poll();
            if (channel2Str == null || !channel2Str.equals(Integer.toString(i))) {
                throw new Exception("Channel2 - Invalid string '" + channel2Str
                        + "' for int " + i);
            }
        }

        logInfo("doTestFlushLoad done");

        return true;
    }

    /**
     * Retrieves the cache factory service, null if not found
     * 
     * @return The cache factory service or null
     */
    protected ICacheFactory getCacheFactory() {

        final BundleContext context = TestActivator.getInstance().getContext();
        try {
            final ServiceReference[] refs = context.getServiceReferences(
                    ICacheFactory.class.getName(), null);

            if (refs != null && refs.length != 0) {
                return (ICacheFactory) context.getService(refs[0]);
            }

        } catch (InvalidSyntaxException e) {
            logInfo("Error searching for a forker : ");
            logInfo(e);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.teststools.CConsoleTester#monitorCommand(java.lang
     * .String, java.lang.String, java.util.StringTokenizer)
     */
    @Override
    public boolean monitorCommand(final String aCommand, final String aLine,
            final StringTokenizer aST) throws Exception {

        if (aCommand.equals(FLUSH_LOAD_COMMAND)) {
            return doTestFlushLoad();
        }

        return false;
    }
}
