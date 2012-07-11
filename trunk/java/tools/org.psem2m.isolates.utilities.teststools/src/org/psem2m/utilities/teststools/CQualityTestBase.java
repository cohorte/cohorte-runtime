package org.psem2m.utilities.teststools;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import junit.framework.TestCase;

import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CActivityFormaterBasic;
import org.psem2m.utilities.logging.CLogLineTextBuilder;
import org.psem2m.utilities.logging.IActivityFormater;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlaTech - ogattaz
 * 
 */
public class CQualityTestBase extends TestCase implements IActivityLoggerBase {

	final static String SEP_TIME = ":";

	public final static String[] START_ARGS = { "-junit" };

	private final IActivityFormater pActivityFormater = CActivityFormaterBasic
			.getInstance();

	private final CLogLineTextBuilder pLogLineTextBuilder = CLogLineTextBuilder
			.getInstance();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(final Appendable aBuffer) {
		CXStringUtils.appendKeyValInBuff(
				aBuffer,
				"Id",
				getClass().getSimpleName() + '_'
						+ CXStringUtils.strAdjustRight(this.hashCode(), 4));
		return aBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogDebugOn()
	 */
	@Override
	public boolean isLogDebugOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#isLoggable(java.util
	 * .logging.Level)
	 */
	@Override
	public boolean isLoggable(final Level aLevel) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogInfoOn()
	 */
	@Override
	public boolean isLogInfoOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogSevereOn()
	 */
	@Override
	public boolean isLogSevereOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLoggerBase#isLogWarningOn()
	 */
	@Override
	public boolean isLogWarningOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
	 * .Level, java.lang.Object, java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void log(final Level aLevel, final Object aWho,
			final CharSequence aWhat, final Object... aInfos) {

		CharSequence wWhat = (aWhat != null) ? aWhat : CXJavaRunContext
				.getPreCallingMethod();

		String wLine = pActivityFormater.format(System.currentTimeMillis(),
				aLevel, pLogLineTextBuilder.buildWhoObjectId(aWho),
				wWhat.toString(),
				"%% " + pLogLineTextBuilder.buildLogLine(aInfos));
		writeLog(wLine);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#log(java.util.logging
	 * .LogRecord)
	 */
	@Override
	public void log(final LogRecord record) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logDebug(java.lang.Object
	 * , java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logDebug(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.FINE, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logInfo(java.lang.Object
	 * , java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logInfo(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.INFO, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logSevere(java.lang.
	 * Object, java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logSevere(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.SEVERE, aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLoggerBase#logWarn(java.lang.Object
	 * , java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logWarn(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.WARNING, aWho, aWhat, aInfos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXDescriber#toDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(new StringBuilder()).toString();
	}

	/**
	 * @param aLine
	 */
	protected void writeLog(final String aLine) {
		System.out.println(aLine);
	}

}
