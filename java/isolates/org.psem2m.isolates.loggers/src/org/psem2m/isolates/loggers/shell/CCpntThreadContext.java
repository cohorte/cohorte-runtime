package org.psem2m.isolates.loggers.shell;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;

/**
 * MOD_OG_CASE_0000211 Associates a LogChannel to a thread to trace explicitly a
 * request
 *
 * Internal Thread context compomnent design to manage the ThreadLocal intances.
 *
 * @author ogattaz
 *
 */
@Component(name = "psem2m-loggers-channels-commands-threadcontext-Factory")
@Instantiate(name = "psem2m-loggers-channels-commands-threadcontext")
@Provides(specifications = { IThreadContext.class })
public class CCpntThreadContext implements IThreadContext {

	private static final ThreadLocal<ILogChannelSvc> pDebugChannel = new ThreadLocal<ILogChannelSvc>();

	/**
	 * Cohorte Logger.
	 */
	@Requires
	IIsolateLoggerSvc pLogger;

	/**
	 * Cohorte Dirs service
	 */
	@Requires
	private IPlatformDirsSvc pPlatformDirsSvc;

	/**
	 *
	 */
	public CCpntThreadContext() {
		super();
	}

	@Override
	public ILogChannelSvc getDebugChannel() {

		return pDebugChannel.get();
	}

	@Override
	public boolean hasDebugChannel() {

		return getDebugChannel() != null;
	}

	@Override
	public void setDebugChannel(ILogChannelSvc aLogChannelSvc) {
		pDebugChannel.set(aLogChannelSvc);
	}

	/**
	 * Called when the component is validated.
	 */
	@Validate
	public void validate() {
		pLogger.logInfo(this, "validate", "validating...");

		try {

			setDebugChannel(null);

			pLogger.logInfo(this, "validate", "hasDebugChannel=[%b]",
					hasDebugChannel());

		} catch (final Exception | Error e) {
			pLogger.logSevere(this, "validate", "ERROR: %s", e);
		}
		pLogger.logInfo(this, "validate", "validated");
	}
}
