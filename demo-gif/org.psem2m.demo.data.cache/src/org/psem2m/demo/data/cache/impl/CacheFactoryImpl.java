/**
 * File:   CacheFactoryImpl.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.demo.data.cache.ICacheChannel;
import org.psem2m.demo.data.cache.ICacheDequeueChannel;
import org.psem2m.demo.data.cache.ICacheFactory;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * Implementation of a cache channel factory
 * 
 * @author Thomas Calmant
 */
@Component(name = "cache-channel-factory-factory", publicFactory = false)
@Provides(specifications = ICacheFactory.class)
@Instantiate(name = "cache-channel-factory")
public class CacheFactoryImpl extends CPojoBase implements ICacheFactory {

    /** Cache file name */
    public static final String CACHE_FILE_NAME = "cache.data";

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** Opened standard channels */
    private final Map<String, ICacheChannel<?, ?>> pChannels = new HashMap<String, ICacheChannel<?, ?>>();

    /** File access thread */
    private ExecutorService pExecutor;

    /** Log service */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** Platform directories service */
    @Requires
    private IPlatformDirsSvc pPlatformDirsSvc;

    /** Opened queued channels */
    private final Map<String, ICacheDequeueChannel<?, ?>> pQueueChannels = new HashMap<String, ICacheDequeueChannel<?, ?>>();

    /** Runnable that load the cache to a file */
    private Runnable pRunLoadFile = new Runnable() {

        @Override
        public void run() {

            loadFromFile();
        }
    };

    /** Runnable that writes the cache to a file */
    private Runnable pRunWriteFile = new Runnable() {

        @Override
        public void run() {

            storeToFile();
        }
    };

    /**
     * Default constructor
     */
    public CacheFactoryImpl() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.ICacheFactory#closeChannel(java.lang.String)
     */
    @Override
    public void closeChannel(final String aName) {

        ICacheChannel<?, ?> channel = pChannels.get(aName);
        if (channel != null) {
            pChannels.remove(channel);
            channel.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.data.cache.ICacheFactory#flush()
     */
    @Override
    public void flush() {

        // Flush from the executor
        pExecutor.execute(pRunWriteFile);
    }

    /**
     * Writes the channels maps in the stream
     * 
     * @param aObjectOutputStream
     *            The object output stream
     * @throws IOException
     *             An error occurred while writing objects
     */
    private void internalWriteObject(
            final ObjectOutputStream aObjectOutputStream) throws IOException {

        // The standard channels
        aObjectOutputStream.writeObject(pChannels);

        // The queued channels
        aObjectOutputStream.writeObject(pQueueChannels);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        // Save the cache state
        flush();

        // Shutdown the thread "pool"
        pExecutor.shutdown();

        // Wait for a second, to let all flushes to be done
        try {
            if (pExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                // All tasks are terminated, close all channels
                for (ICacheChannel<?, ?> channel : pChannels.values()) {
                    channel.close();
                }
                pChannels.clear();
            }

        } catch (InterruptedException e) {
            // Ignore
        }

        pLogger.logInfo(this, "invalidatePojo", "Cache channel factory Gone");
    }

    /**
     * Loads the cache file from a thread
     */
    protected void load() {

        // Read from the executor
        pExecutor.execute(pRunLoadFile);
    }

    /**
     * Loads the isolate cache file content
     */
    private synchronized void loadFromFile() {

        // Get the isolate working directory
        final File workingDir = pPlatformDirsSvc
                .getIsolateStorageDir(pPlatformDirsSvc.getIsolateId());

        // Open the cache file
        final File cacheFile = new File(workingDir, CACHE_FILE_NAME);
        if (!cacheFile.exists()) {
            // No cache file
            pLogger.logInfo(this, "loadFromFile", "No previous cache found");
            return;
        }

        ObjectInputStream objectStream = null;
        try {
            // Read it
            objectStream = new ObjectInputStream(new FileInputStream(cacheFile));
            readObject(objectStream);

        } catch (Exception e) {
            pLogger.logWarn(this, "loadFromFile",
                    "Error loading cache from file", cacheFile, " :", e);

        } finally {
            if (objectStream != null) {
                try {
                    objectStream.close();
                } catch (IOException e) {
                    // Ignore errors
                }
            }
        }

        pLogger.logInfo(this, "loadFromFile", "Cache read - channels =",
                pChannels.size(), "- queued =", pQueueChannels.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.ICacheFactory#openChannel(java.lang.String)
     */
    @Override
    public <K extends Serializable, V extends Serializable> ICacheChannel<K, V> openChannel(
            final String aName) {

        // Get the existing one
        @SuppressWarnings("unchecked")
        ICacheChannel<K, V> channel = (ICacheChannel<K, V>) pChannels
                .get(aName);

        if (channel == null) {
            // Create a new one if needed
            channel = new CacheChannelImpl<K, V>();

            // Store it
            pChannels.put(aName, channel);
        }

        return channel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.demo.data.cache.ICacheFactory#openDequeueChannel(java.lang
     * .String)
     */
    @Override
    public <K extends Serializable, V extends Serializable> ICacheDequeueChannel<K, V> openDequeueChannel(
            final String aName) {

        // Get the existing one
        @SuppressWarnings("unchecked")
        ICacheDequeueChannel<K, V> channel = (ICacheDequeueChannel<K, V>) pQueueChannels
                .get(aName);

        if (channel == null) {
            // Create a new one if needed
            channel = new CacheDequeueChannelImpl<K, V>();

            // Store it
            pQueueChannels.put(aName, channel);
        }

        return channel;
    }

    /**
     * Custom de-serialization
     * 
     * @param aObjectInputStream
     *            An object input stream
     * @throws IOException
     *             An error occurred while reading the object
     * @throws ClassNotFoundException
     *             A required class wasn't found
     */
    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream aObjectInputStream)
            throws IOException, ClassNotFoundException {

        // The standard channels
        final Map<String, ICacheChannel<?, ?>> readStdChannels = (Map<String, ICacheChannel<?, ?>>) aObjectInputStream
                .readObject();
        if (readStdChannels != null) {
            pChannels.putAll(readStdChannels);
        }

        // The queued channels
        Map<String, ICacheDequeueChannel<?, ?>> readQueueChannels = (Map<String, ICacheDequeueChannel<?, ?>>) aObjectInputStream
                .readObject();
        if (readQueueChannels != null) {
            pQueueChannels.putAll(readQueueChannels);
        }
    }

    /**
     * Stores the cache to a file
     */
    private synchronized void storeToFile() {

        // Get the isolate working directory
        final File workingDir = pPlatformDirsSvc
                .getIsolateStorageDir(pPlatformDirsSvc.getIsolateId());

        // Open the cache file
        final File cacheFile = new File(workingDir, CACHE_FILE_NAME);
        if (!cacheFile.exists()) {
            // Create a new cache file
            try {
                cacheFile.createNewFile();

            } catch (IOException e) {
                pLogger.logSevere(this, "storeToFile",
                        "Can't create the cache file :", cacheFile);
                return;
            }
        }

        ObjectOutputStream objectStream = null;
        try {
            pLogger.logInfo(this, "flush", "FLUSH CACHE...");

            // Write it
            objectStream = new ObjectOutputStream(new FileOutputStream(
                    cacheFile));
            internalWriteObject(objectStream);

            pLogger.logInfo(this, "flush", "FLUSH CACHE DONE");

        } catch (Exception e) {
            pLogger.logWarn(this, "storeToFile", "Error writing cache to file",
                    cacheFile, " :", e);

        } finally {
            if (objectStream != null) {
                try {
                    objectStream.close();
                } catch (IOException e) {
                    // Ignore...
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        // Prepare a rendez-vous thread
        pExecutor = Executors.newFixedThreadPool(1);

        // Load the previous cache
        load();

        pLogger.logInfo(this, "validatePojo", "Cache channel factory Ready");
    }

    /**
     * Custom serialization
     * 
     * @param aObjectOutputStream
     *            The object output stream
     * @throws IOException
     *             An error occurred while writing objects
     */
    private void writeObject(final ObjectOutputStream aObjectOutputStream)
            throws IOException {

        internalWriteObject(aObjectOutputStream);
    }
}
