package org.psem2m.isolates.loggers.shell;

import org.psem2m.isolates.loggers.ILogChannelSvc;

/**
 * MOD_OG_CASE_0000211 Associates a LogChannel to a thread to trace explicitly a
 * request
 *
 * @author ogattaz
 *
 */
public interface IThreadContext {

	/**
	 * @return
	 */
	ILogChannelSvc getDebugChannel();

	/**
	 * @return
	 */
	boolean hasDebugChannel();

	/**
	 * @param aLogChannelSvc
	 */
	void setDebugChannel(final ILogChannelSvc aLogChannelSvc);

}
