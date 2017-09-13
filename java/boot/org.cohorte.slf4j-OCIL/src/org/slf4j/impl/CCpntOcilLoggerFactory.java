/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.slf4j.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.isolates.slf4j_ocil.IOcilManager;
import org.psem2m.utilities.logging.CActivityLoggerBasicConsole;
import org.psem2m.utilities.logging.CXJulUtils;
import org.psem2m.utilities.logging.IActivityLoggerJul;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * JDK14LoggerFactory is an implementation of {@link ILoggerFactory} returning
 * the appropriately named {@link COcilLoggerAdapter} instance.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author ogattaz
 *
 */
@Component(name = "cohorte-isolate-slf4j-ocil-factory", factoryMethod = "getSingleton")
@Instantiate(name = "cohorte-isolate-slf4j-ocil")
@Provides(specifications = { IOcilManager.class })
public class CCpntOcilLoggerFactory implements ILoggerFactory, IOcilManager {

	private static CCpntOcilLoggerFactory sMe = new CCpntOcilLoggerFactory();

	/**
	 * @return
	 */
	public static CCpntOcilLoggerFactory getSingleton() {
		return sMe;
	}

	/**
	 * The logger used to log the activity of this component.
	 *
	 * This injected IActivityLoggerJul service is registered by the
	 * CIsolateBaseActivator
	 *
	 * ATTENTION The name of the property and the value are defined in the
	 * bundle "org.psem2m.isolates.base"
	 *
	 * @see org.psem2m.isolates.base.IIsolateLoggerSvc
	 * @see org.psem2m.isolates.base.internal.CIsolateBaseActivator
	 */
	@Requires(filter = "(julname=org.chohorte.isolate.logger.svc)")
	private IActivityLoggerJul pActivityLogger = null;

	// key: name (String), value: a JDK14LoggerAdapter;
	private final ConcurrentMap<String, COcilLoggerAdapter> pOcilLoggerMap = new ConcurrentHashMap<String, COcilLoggerAdapter>();

	private java.util.logging.Logger pParentJulLogger;

	/**
	 *
	 */
	private CCpntOcilLoggerFactory() {
		super();
		// ensure jul initialization. see SLF4J-359
		// note that call to java.util.logging.LogManager.getLogManager() fails
		// on the Google App Engine platform. See SLF4J-363
		pParentJulLogger = java.util.logging.Logger.getLogger("");

		pActivityLogger = CActivityLoggerBasicConsole.getInstance();

		pActivityLogger.logInfo(this, "<init>",
				"CCpntOcilLoggerFactory instanciated");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.cohorte.isolates.slf4j_ocil.IOcilManager#addDumpCurrentLoggersInSB
	 * (java.lang.StringBuilder)
	 */
	@Override
	public StringBuilder addDumpCurrentLoggersInSB(final StringBuilder aSB) {

		try {

			final Map<String, COcilLoggerAdapter> treeMap = new TreeMap<String, COcilLoggerAdapter>(
					pOcilLoggerMap);

			int wIdx = 0;
			for (final Entry<String, COcilLoggerAdapter> wLoggerEntry : treeMap
					.entrySet()) {

				aSB.append(String.format("\n(%3d) %s ", wIdx,
						formatOneLogger(wLoggerEntry.getValue())));
				wIdx++;
			}
		} catch (final Exception e) {

			final StringWriter wStackTraceWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(wStackTraceWriter));
			final String wStackTrace = wStackTraceWriter.toString();

			aSB.append(String.format("\nERROR: [%s] [%s] \n%s", e.getClass()
					.getSimpleName(), e.getMessage(), wStackTrace));
		}
		return aSB;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.cohorte.isolates.slf4j_ocil.IOcilManager#dumpCurrentLoggers()
	 */
	@Override
	public String dumpCurrentLoggers() {
		return addDumpCurrentLoggersInSB(new StringBuilder()).toString();
	}

	/**
	 * map that associate filter to loggerJul in order to automatically assign 
	 * the right logger while a new slf4j logger is created
	 */
	private Map<String,java.util.logging.Logger> pMapFilterParentLogger = new ConcurrentHashMap<String, java.util.logging.Logger>();
	
	/**
	 * @param aLogger
	 * @param aFilter
	 * @return
	 */
	private boolean filterLogger(final Logger aLogger, final String aFilter) {
		if (aFilter == null || aFilter.isEmpty()) {
			return true;
		}
		if (aFilter.endsWith("*")) {
			final String wFilterPrefix = aFilter.substring(0,
					aFilter.length() - 2);
			return aLogger.getName().startsWith(wFilterPrefix);
		}
		return aLogger.getName().equals(aFilter);
	}

	/**
	 * @param aOcilLoggerAdapter
	 * @return
	 */
	private String formatOneLogger(final COcilLoggerAdapter aOcilLoggerAdapter) {
		return String.format("logger=[%-70s]: [%-20s] [%s] %s",
				aOcilLoggerAdapter.getName(), aOcilLoggerAdapter.getClass()
						.getSimpleName(), getLevelOfLogger(aOcilLoggerAdapter),
				CXJulUtils.toString(aOcilLoggerAdapter.getJulLogger()));
	}

	/**
	 * @param aLogger
	 * @return
	 */
	private String getLevelOfLogger(final Logger aLogger) {

		if (aLogger.isDebugEnabled()) {
			return "DEBUG";
		}
		if (aLogger.isErrorEnabled()) {
			return "ERROR";
		}
		if (aLogger.isInfoEnabled()) {
			return "INFO";
		}
		if (aLogger.isTraceEnabled()) {
			return "TRACE";
		}
		if (aLogger.isWarnEnabled()) {
			return "WARNING";
		}
		return "OFF";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 */
	@Override
	public Logger getLogger(String name) {
		final String wLogName = name;

		// the root logger is called "" in JUL
		if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
			name = "";
		}

		COcilLoggerAdapter wSlf4jLogger = pOcilLoggerMap.get(name);
		if (wSlf4jLogger != null) {
			return wSlf4jLogger;
		} else {
			final java.util.logging.Logger wJulLogger = java.util.logging.Logger
					.getLogger(name);

			// if the filter is already defined and assign by a switch we assign the correct parent julLogger 
			Optional<String> wOptKey = pMapFilterParentLogger.keySet().stream().filter(wKey->{
				if( wKey.endsWith("*") ){
					return wLogName.startsWith(wKey.substring(0,wKey.length()-1));
				}else{
					return wLogName!= null && wLogName.equals(wKey);
				}
			}).findFirst();
			
			if( wOptKey.get() != null ){
				wJulLogger.setParent(pMapFilterParentLogger.get(wOptKey.get()));
			}else{
				wJulLogger.setParent(pParentJulLogger);
			}

			final COcilLoggerAdapter newInstance = new COcilLoggerAdapter(name,
					wJulLogger);
			final COcilLoggerAdapter oldInstance = pOcilLoggerMap.putIfAbsent(
					name, newInstance);

			wSlf4jLogger = (oldInstance == null) ? newInstance : oldInstance;

			pActivityLogger.logInfo(this, "getLogger", "Create %s %s",
					formatOneLogger(wSlf4jLogger),
					CXJulUtils.toString(wSlf4jLogger.getJulLogger()));

			return wSlf4jLogger;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.cohorte.isolates.slf4j_ocil.IOcilManager#getOcilLoggerMap()
	 */
	@Override
	public Map<String, COcilLoggerAdapter> getOcilLoggerMap() {
		return pOcilLoggerMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cohorte.isolates.slf4j_ocil.IOcilManager#getParentJulLogger()
	 */
	@Override
	public java.util.logging.Logger getParentJulLogger() {
		return pParentJulLogger;
	}

	@Invalidate
	void invalidate() {
		pActivityLogger.logInfo(this, "invalidate", "invalidating...");
		try {

		} catch (Exception | Error e) {
			pActivityLogger.logSevere(this, "invalidate", "ERROR: %s", e);

		}
		pActivityLogger.logInfo(this, "invalidate", "invalidated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cohorte.isolates.slf4j_ocil.IOcilManager#setLevel(java.util.logging
	 * .Level)
	 */
	@Override
	public int setLevel(final java.util.logging.Level aJulLevel) {
		return setLevel(aJulLevel, NO_LOGGER_NAME_FILTER);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cohorte.isolates.slf4j_ocil.IOcilManager#setLevel(java.util.logging
	 * .Level, java.lang.String)
	 */
	@Override
	public int setLevel(final java.util.logging.Level aJulLevel,
			final String aLoggerNameFilter) {

		int wNbSet = 0;

		for (final Entry<String, COcilLoggerAdapter> wEntry : pOcilLoggerMap
				.entrySet()) {

			if (filterLogger(wEntry.getValue(), aLoggerNameFilter)) {
				final COcilLoggerAdapter wOcilLogger = wEntry.getValue();

				wOcilLogger.setLevel(aJulLevel);
				wNbSet++;
			}
		}
		return wNbSet;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.cohorte.isolates.slf4j_ocil.IOcilManager#setParentJulLogger(java.
	 * util.logging.Logger)
	 */
	@Override
	public void setParentJulLogger(final java.util.logging.Logger aJulLogger) {
		pParentJulLogger = aJulLogger;

		switchParentJulLogger(pParentJulLogger, NO_LOGGER_NAME_FILTER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cohorte.isolates.slf4j_ocil.IOcilManager#switchParentJulLogger()
	 */
	@Override
	public int switchParentJulLogger() {
		return switchParentJulLogger(getParentJulLogger());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cohorte.isolates.slf4j_ocil.IOcilManager#switchParentJulLogger(java
	 * .util.logging.Logger)
	 */
	@Override
	public int switchParentJulLogger(
			final java.util.logging.Logger aParentJulLogger) {
		return switchParentJulLogger(aParentJulLogger, NO_LOGGER_NAME_FILTER);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.cohorte.isolates.slf4j_ocil.IOcilManager#switchParentJulLogger(java
	 * .util.logging.Logger, java.lang.String)
	 */
	@Override
	public int switchParentJulLogger(
			final java.util.logging.Logger aParentJulLogger,
			final String aLoggerNameFilter) {

		int wNbSwitch = 0;
		
		pMapFilterParentLogger.put(aLoggerNameFilter, aParentJulLogger);

		for (final Entry<String, COcilLoggerAdapter> wEntry : pOcilLoggerMap
				.entrySet()) {
			if (filterLogger(wEntry.getValue(), aLoggerNameFilter)) {
				final COcilLoggerAdapter wOcilLogger = wEntry.getValue();
				pActivityLogger.logInfo(this, "switchParentJulLogger", "switch COcilLoggerAdapter %s to loggerJul %s",wOcilLogger,aParentJulLogger);
				wOcilLogger.setParentJulLogger(aParentJulLogger);
				wNbSwitch++;
			}
		}
		return wNbSwitch;
	}

	@Validate
	void validate() {
		pActivityLogger.logInfo(this, "validate", "validating...");
		try {

			// set the current parent Jul Logger
			setParentJulLogger(pActivityLogger.getJulLogger());
			pActivityLogger.logInfo(this, "validate",
					"Current parent jul logger=[%s]", getParentJulLogger()
							.getName());

			// set the parent jul logger of all the currents OcilLoggers with
			// the current parrent jul logger
			final int wNbSwitch = switchParentJulLogger();
			pActivityLogger.logInfo(this, "validate", "NbSwitch=[%d]",
					wNbSwitch);

			pActivityLogger.logInfo(this, "validate",
					"The Current loggers :\n%s ", dumpCurrentLoggers());

		} catch (Exception | Error e) {
			pActivityLogger.logSevere(this, "validate", "ERROR: %s", e);

		}
		pActivityLogger.logInfo(this, "validate", "validated");
	}

}
