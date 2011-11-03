package org.jabsorb.ng.logging;

/**
 * @author ogattaz
 * 
 */
public class LoggerFactory {

    private static ILoggerProvider sLoggerProvider = null;

    /**
     * @param aClass
     * @return
     */
    public static ILogger getLogger(Class<?> aClass) {

        return (hasLoggerProvider()) ? getLoggerProvider().getLogger(aClass)
                : new LoggerNull();
    }

    /**
     * @return
     */
    public static ILoggerProvider getLoggerProvider() {

        return sLoggerProvider;
    }

    /**
     * @return
     */
    public static boolean hasLoggerProvider() {

        return sLoggerProvider != null;
    }

    /**
     * 
     */
    public static void removeLoggerProvider() {

        sLoggerProvider = null;
    }

    /**
     * @param aLoggerProvider
     */
    public static void setLoggerProvider(ILoggerProvider aLoggerProvider) {

        sLoggerProvider = aLoggerProvider;
    }
}
