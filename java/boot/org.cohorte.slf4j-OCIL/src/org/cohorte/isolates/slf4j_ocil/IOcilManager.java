package org.cohorte.isolates.slf4j_ocil;

import java.util.Map;

import org.slf4j.Logger;

/**
 * @author ogattaz
 *
 */
public interface IOcilManager {

	String NO_LOGGER_NAME_FILTER = null;

	/**
	 * @param aSB
	 * @return
	 */
	StringBuilder addDumpCurrentLoggersInSB(final StringBuilder aSB);

	/**
	 * @return
	 */
	String dumpCurrentLoggers();

	/**
	 * @return
	 */
	Map<String, ? extends Logger> getOcilLoggerMap();

	/**
	 * @return the current parent jul Logger
	 */
	java.util.logging.Logger getParentJulLogger();

	/**
	 * @param aJulLevel
	 * @return
	 */
	int setLevel(final java.util.logging.Level aJulLevel);

	/**
	 * @param aJulLevel
	 * @param aAdapterNameFilter
	 * @return
	 */
	int setLevel(final java.util.logging.Level aJulLevel,
			final String aAdapterNameFilter);

	/**
	 * Set the current parent jul logger.
	 *
	 * It is used to set the parent of all the new Adapter
	 *
	 * @param aJulLogger
	 */
	void setParentJulLogger(final java.util.logging.Logger aJulLogger);

	/**
	 * reset the parent jul logger of all the existing Adapters using the
	 * current parent
	 *
	 * @param aParentJulLogger
	 * @return
	 */
	int switchParentJulLogger();

	/**
	 * Switch the parent jul logger of all the existing Adapters
	 *
	 * @param aParentJulLogger
	 * @return
	 */
	int switchParentJulLogger(final java.util.logging.Logger aParentJulLogger);

	/**
	 * Switch the parent of the Adapters matching the filter (one or more)
	 *
	 * @param aParentJulLogger
	 * @param aAdapterNameFilter
	 *            The filter to apply. Test the string equality by default. If
	 *            the last char is a star "*", the filter is used as prefix.
	 * @return
	 */
	int switchParentJulLogger(final java.util.logging.Logger aParentJulLogger,
			final String aAdapterNameFilter);

}
