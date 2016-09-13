package org.psem2m.isolates.base;

import org.psem2m.isolates.base.internal.CIsolateLoggerChannel;

/**
 * MOD_OG_20160906
 * 
 * @author ogattaz
 *
 */
public interface IIsolateBaseActivator {

	/**
	 * @return the instance of the IsolateLoggerChannel (wrapped by the service
	 *         IIsolateLoggerSvc
	 */
	CIsolateLoggerChannel getIsolateLoggerChannel();

	/**
	 * @return true if the CIsolateLoggerChannel instance exists
	 */
	boolean hasIsolateLoggerChannel();

	/**
	 * @return true if the CIsolateLoggerSvc instance exists
	 */
	boolean hasIsolateLoggerSvc();

}
