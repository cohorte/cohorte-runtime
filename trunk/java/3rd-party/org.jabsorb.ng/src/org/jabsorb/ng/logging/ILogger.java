package org.jabsorb.ng.logging;

/**
 * @author ogattaz
 * 
 */
public interface ILogger {

    /**
     * @param aWhat
     * @param aInfos
     */
    public void debug(String aWhat, Object... aInfos);

    /**
     * @param aWhat
     * @param aInfos
     */
    public void error(String aWhat, Object... aInfos);

    /**
     * @return
     */
    public String getName();

    /**
     * @param aWhat
     * @param aInfos
     */
    public void info(String aWhat, Object... aInfos);

    /**
     * @return
     */
    public boolean isDebugEnabled();

    /**
     * @return
     */
    public boolean isErrorEnabled();

    /**
     * @return
     */
    public boolean isInfoEnabled();

    /**
     * @return
     */
    public boolean isTraceEnabled();

    /**
     * @return
     */
    public boolean isWarnEnabled();

    /**
     * @param aWhat
     * @param aInfos
     */
    public void trace(String aWhat, Object... aInfos);

    /**
     * @param aWhat
     * @param aInfos
     */
    public void warn(String aWhat, Object... aInfos);

}
