package org.jabsorb.ng.logging;

/**
 * @author ogattaz
 * 
 */
public class LoggerNull implements ILogger {

    /**
     * 
     */
    public LoggerNull() {

        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#debug(java.lang.String,
     * java.lang.Object[])
     */
    @Override
    public void debug(String aWhat, Object... aInfos) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#error(java.lang.String,
     * java.lang.Object[])
     */
    @Override
    public void error(String aWhat, Object... aInfos) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#getName()
     */
    @Override
    public String getName() {

        return LoggerNull.class.getSimpleName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#info(java.lang.String,
     * java.lang.Object[])
     */
    @Override
    public void info(String aWhat, Object... aInfos) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#trace(java.lang.String,
     * java.lang.Object[])
     */
    @Override
    public void trace(String aWhat, Object... aInfos) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.logging.Logger#warn(java.lang.String,
     * java.lang.Object[])
     */
    @Override
    public void warn(String aWhat, Object... aInfos) {

    }

}
