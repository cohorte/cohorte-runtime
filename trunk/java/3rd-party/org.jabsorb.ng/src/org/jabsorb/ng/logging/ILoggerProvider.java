package org.jabsorb.ng.logging;

/**
 * @author ogattaz
 * 
 */
public interface ILoggerProvider {

    /**
     * @param aClass
     * @return
     */
    public ILogger getLogger(Class<?> aClass);

}
