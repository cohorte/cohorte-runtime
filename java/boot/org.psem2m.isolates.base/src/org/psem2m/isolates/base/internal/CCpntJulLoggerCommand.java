package org.psem2m.isolates.base.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.CAbstractGoGoCommand;
import org.psem2m.isolates.base.IGoGoCommand;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CXJulUtils;

/**
 * MOD_OG_1.0.17 create component
 * 
 * 
 * 
 * @author ogattaz
 *
 */
@Component(name = "cohorte-isolate-base-jullogger-gogocommand-factory")
@Instantiate(name = "cohorte-isolate-base-jullogger-gogocommand")
@Provides(specifications = { IGoGoCommand.class })
public class CCpntJulLoggerCommand extends CAbstractGoGoCommand implements IGoGoCommand {

	private final BundleContext pBundleContext;

	/**
	 * The Gogo commands name. ATTENTION : look at the name of the methods and the
	 * declaration
	 */
	@ServiceProperty(name = "osgi.command.function", value = "{julinfos,juldump,julsetLevel,julsetLevelAll}")
	private String[] pCommands;

	/**
	 * Cohorte isolate logger service
	 */
	@Requires
	private IIsolateLoggerSvc pLogger;

	/**
	 * The Gogo commands scope
	 */
	@ServiceProperty(name = "osgi.command.scope", value = "jullogger")
	private String pScope;

	/**
	 * 
	 */
	public CCpntJulLoggerCommand(final BundleContext aBundleContext) {
		super();
		pBundleContext = aBundleContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.IGogoCommand#getCommands()
	 */
	@Override
	public String[] getCommands() {
		return pCommands;
	}

	private Logger getJul(final String aJulName) {

		// returns Logger if exists else returns null;
		return LogManager.getLogManager().getLogger(aJulName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.CAbstractGoGoCommand#getLogger()
	 */
	@Override
	public IIsolateLoggerSvc getLogger() {
		return pLogger;
	}

	/**
	 *
	 */
	@Invalidate
	public void invalidate() {

		pLogger.logInfo(this, "invalidate", "invalidated");
	}

	/**
	 * Gogo command "juldump"
	 */
	@Descriptor("Dump jul loggers state")
	public void juldump() {
		juldump(null);
	}

	/**
	 * Gogo command "juldump"
	 */
	@Descriptor("Dump jul loggers state")
	public void juldump(@Descriptor("the filter 'xx.xx*' ") final String aJulFilter) {

		logTwiceInfo("juldump", "exec  juldump(%s)", aJulFilter);

		logTwiceInfo("juldump", CXJulUtils.addDumpCurrentLoggersInSB(new StringBuilder(), aJulFilter).toString());

	}

	/**
	 * Gogo command "infos"
	 */
	@Descriptor("Dump jul loggers infos")
	public void julinfos(final String... aArgs) {
		logTwiceInfo("julinfos", "dump jul logger infos");

		List<String> wJulNames = Collections.list(LogManager.getLogManager().getLoggerNames());
		wJulNames.sort(Comparator.naturalOrder());
		int wIdx = 0;
		for (String wJulName : wJulNames) {
			Logger wLogger = getJul(wJulName);
			Level wLevel = wLogger.getLevel();
			logTwiceInfo("julinfos", "- %3d [%-80s] [%s]", wIdx, wJulName,
					(wLevel != null) ? wLogger.getName() : "-\\-");
			wIdx++;
		}
	}

	/**
	 * Gogo command "setLevel"
	 * 
	 * @see http://enroute.osgi.org/appnotes/gogo-cmd.html #Formatting of Output
	 */
	@Descriptor("Set the log level of the isolatelogger")
	public void julsetLevel(@Descriptor("the index of the jul to set") final int aJulIndex,
			@Descriptor("the name of the level to set") final String aLevelName) {

		List<String> wJulNames = Collections.list(LogManager.getLogManager().getLoggerNames());
		wJulNames.sort(Comparator.naturalOrder());
		julsetLevel(wJulNames.get(aJulIndex), aLevelName);
	}

	/**
	 * Gogo command "setLevel"
	 * 
	 * @see http://enroute.osgi.org/appnotes/gogo-cmd.html #Formatting of Output
	 */
	@Descriptor("Set the log level of the isolatelogger")
	public void julsetLevel(@Descriptor("the name of the jul to set") final String aJulName,
			@Descriptor("the name of the level to set") final String aLevelName) {

		Level wOldLevel = null;
		try {

			Logger wLogger = getJul(aJulName);

			if (wLogger == null) {
				logTwiceWarn("julsetLevel", "The jul [%s] doesn't exist", aJulName);

			} else {
				wOldLevel = wLogger.getLevel();
				logTwiceInfo("julsetLevel", "set the level of jul [%s] to [%s], old level [%s]", aJulName, aLevelName,
						wOldLevel);

				Level wLevelBefore = pLogger.getLevel();

				pLogger.setLevel(aLevelName);

				Level wLevelAfter = pLogger.getLevel();

				logTwiceInfo("julsetLevel", "jul level set to=[%s] (before=[%s])", wLevelAfter.getName(),
						wLevelBefore.getName());
			}

		} catch (Exception | Error e) {
			logTwiceSevere("julsetLevel", e);
		}
	}

	/**
	 * Gogo command "setLevelAll"
	 */
	@Descriptor("Set the log level of a jul logger in all the isolates of the group 'all'")
	public void julsetLevelAll(@Descriptor("the index of the jul to set") final int aJulIndex,
			@Descriptor("the name of the level to set") final String aLevelName) {
		List<String> wJulNames = Collections.list(LogManager.getLogManager().getLoggerNames());
		wJulNames.sort(Comparator.naturalOrder());
		julsetLevelAll(wJulNames.get(aJulIndex), aLevelName);
	}

	/**
	 * Gogo command "setLevelAll"
	 */
	@Descriptor("Set the log level of a jul logger in all the isolates of the group 'all'")
	public void julsetLevelAll(@Descriptor("the name of the jul to set") final String aJulName,
			@Descriptor("the name of the level to set") final String aLevelName) {

		// todo
		logTwiceWarn("julsetLevelAll", "To be implemented...");
	}

	/**
	 *
	 */
	@Validate
	public void validate() {

		pLogger.logInfo(this, "validate", "validated Commands=[%s]", CXStringUtils.stringTableToString(pCommands));
	}

}
