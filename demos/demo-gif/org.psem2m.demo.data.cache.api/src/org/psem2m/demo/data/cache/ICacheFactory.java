/**
 * File:   ICacheFactory.java
 * Author: Thomas Calmant
 * Date:   11 oct. 2011
 */
package org.psem2m.demo.data.cache;

import java.io.Serializable;

/**
 * Defines a cache channel factory
 * 
 * @author Thomas Calmant
 */
public interface ICacheFactory extends Serializable {

    /**
     * Closes the channel with the given name
     * 
     * @param aName
     *            Name of the channel
     */
    void clearChannel(String aName);

    /**
     * Flushes the cache.
     */
    void flush();

    /**
     * Tests if the given channel is already opened
     * 
     * @param aName
     *            The channel name
     * @return True if the channel is opened
     */
    boolean isChannelOpened(String aName);

    /**
     * Tests if the given dequeue channel is already opened
     * 
     * @param aName
     *            The channel name
     * @return True if the channel is opened
     */
    boolean isDequeueChannelOpened(String aName);

    /**
     * Opens a standard cache channel. Creates a new one if needed.
     * 
     * Standard and Dequeue are indexed in different maps. Therefore, it is
     * possible to have a standard and a dequeue channel with the same name.
     * 
     * @param aName
     *            Name of the channel
     * @return The cache channel
     */
    <K extends Serializable, V extends Serializable> ICacheChannel<K, V> openChannel(
            String aName);

    /**
     * Opens a dequeue cache channel. Creates a new one if needed.
     * 
     * Standard and Dequeue are indexed in different maps. Therefore, it is
     * possible to have a standard and a dequeue channel with the same name.
     * 
     * @param aName
     *            Name of the channel
     * @return The cache channel
     */
    <K extends Serializable, V extends Serializable> ICacheDequeueChannel<K, V> openDequeueChannel(
            String aName);

    /**
     * Reloads the cache from the flush target
     */
    void reload();
}
