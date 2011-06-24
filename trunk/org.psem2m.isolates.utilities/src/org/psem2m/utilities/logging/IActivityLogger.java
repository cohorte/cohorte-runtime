package org.psem2m.utilities.logging;

import org.psem2m.utilities.IXDescriber;

/**
 * @author Adonix Grenoble
 * @version 140
 */
public interface IActivityLogger extends IActivityLoggerBase, IXDescriber {

	/**
	   * 
	   */
	public void close();

	/**
	 * @return
	 */
	public IActivityRequester getRequester();

	/**
	 * @return
	 */
	public CLogLineBuffer popLogLineBuffer();

	/**
	 * @param aLoggerLineBuffer
	 */
	public void pushLogLineBuffer(CLogLineBuffer aLoggerLineBuffer);
}
