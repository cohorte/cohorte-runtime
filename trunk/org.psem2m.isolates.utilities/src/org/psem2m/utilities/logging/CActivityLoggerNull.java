package org.psem2m.utilities.logging;

import java.util.logging.Level;

import org.psem2m.utilities.CXStringUtils;

public class CActivityLoggerNull implements IActivityLogger {

	public static CActivityLoggerNull getInstance() {
		return new CActivityLoggerNull();
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		return CXStringUtils.appendStringsInBuff(aBuffer, getClass()
				.getSimpleName(), String.valueOf(hashCode()));
	}

	/**
	 * @return
	 */
	public int calcDescriptionLength() {
		return 128;
	}

	@Override
	public void close() {
	}

	@Override
	public IActivityRequester getRequester() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLogDebugOn() {
		return false;
	}

	@Override
	public boolean isLoggable(Level aLevel) {
		return false;
	}

	@Override
	public boolean isLogInfoOn() {
		return false;
	}

	@Override
	public boolean isLogSevereOn() {
		return false;
	}

	/**
	 * @return
	 */
	@Override
	public boolean isLogWarningOn() {
		return false;
	}

	/**
	 * @return
	 */
	protected boolean isOpened() {
		return false;
	}

	@Override
	public void log(Level aLevel, Object aWho, CharSequence aWhat,
			Object... aInfos) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logDebug(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logDebug(Object aWho, CharSequence aWhat, Object... aInfos) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logInfo(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logInfo(Object aWho, CharSequence aWhat, Object... aInfos) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logSevere(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logSevere(Object aWho, CharSequence aWhat, Object... aInfos) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logWarn(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logWarn(Object aWho, CharSequence aWhat, Object... aInfos) {
	}

	@Override
	public CLogLineBuffer popLogLineBuffer() {
		return new CLogLineBuffer();
	}

	@Override
	public void pushLogLineBuffer(CLogLineBuffer aLoggerLineBuffer) {

	}

	@Override
	public String toDescription() {
		return null;
	}

}
