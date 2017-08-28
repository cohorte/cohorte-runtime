package org.psem2m.isolates.loggers.shell;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * MOD_OG_CASE_0000211 Associates a LogChannel to a thread to trace explicitly a
 * request
 *
 * @author ogattaz
 *
 */
@Component(name = "psem2m-loggers-channels-commands-simulator-Factory")
@Instantiate(name = "psem2m-loggers-channels-commands-simulator")
@Provides(specifications = { CCpntThreadContextTester.class })
public class CCpntThreadContextTester {

	@Requires
	private IIsolateLoggerSvc pLogger;

	@Requires
	private IThreadContext pThreadContext;

	/**
	 *
	 */
	public void myMethod() {

		// use the
		final IActivityLogger wALogger = retreiveLogger();

		final boolean wLogRedirectedToChannel = pThreadContext
				.hasDebugChannel();

		wALogger.logInfo(
				this,
				"myMethod",
				"begin LogInfo in 'ActivityLogger' LogRedirectedToChannel=[%b]...",
				wLogRedirectedToChannel);

		// ...

		wALogger.logInfo(this, "myMethod", "end.");
	}

	/**
	 * @return
	 */
	private IActivityLogger retreiveLogger() {

		if (pThreadContext.hasDebugChannel()) {
			return pThreadContext.getDebugChannel();
		}
		//
		else {
			return pLogger;
		}

	}
}
