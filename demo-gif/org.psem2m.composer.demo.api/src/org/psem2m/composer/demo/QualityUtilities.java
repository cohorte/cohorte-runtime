/**
 * File:   QualityUtilities.java
 * Author: Thomas Calmant
 * Date:   3 oct. 2011
 */
package org.psem2m.composer.demo;

/**
 * Cache quality methods
 * 
 * @author Thomas Calmant
 */
public class QualityUtilities {

    /** 1 minute in milliseconds */
    public static final long A_MINUTE = 60 * 1000;

    /**
     * Computes the information quality of the given age
     * 
     * @param aCacheAge
     *            A cached value last update time
     * @return The cached information quality
     */
    public static int computeCacheQuality(final long aCacheAge) {

        // Compute the quality
        final long currentTime = System.currentTimeMillis();

        if (aCacheAge > currentTime) {
            // Item in the future ? We're in sync'
            return IQualityLevels.CACHE_LEVEL_SYNC;

        } else {

            if (aCacheAge + A_MINUTE >= currentTime) {
                // Information was stored less than 1 minute ago
                return IQualityLevels.CACHE_LEVEL_FRESH;

            } else if (aCacheAge + 5 * A_MINUTE >= currentTime) {
                // Information was stored less than 5 minutes ago
                return IQualityLevels.CACHE_LEVEL_ACCEPTABLE;

            } else if (aCacheAge + 15 * A_MINUTE >= currentTime) {
                // Information was stored less than 15 minutes ago
                return IQualityLevels.CACHE_LEVEL_WARNING;

            } else {
                // Information was stored even earlier
                return IQualityLevels.CACHE_LEVEL_CRITICAL;
            }
        }
    }

    /**
     * Returns the String representation of the given quality level (UNKNOWN if
     * invalid)
     * 
     * @param aQualityLevel
     *            Quality level to transform into a String
     * @return The String form of the quality level
     */
    public static String qualityToString(final int aQualityLevel) {

        switch (aQualityLevel) {

        case IQualityLevels.CACHE_LEVEL_SYNC:
            return "SYNC";

        case IQualityLevels.CACHE_LEVEL_FRESH:
            return "FRESH";

        case IQualityLevels.CACHE_LEVEL_WARNING:
            return "WARNING";

        case IQualityLevels.CACHE_LEVEL_CRITICAL:
            return "CRITICAL";

        default:
            return "UNKNOWN";
        }
    }
}
