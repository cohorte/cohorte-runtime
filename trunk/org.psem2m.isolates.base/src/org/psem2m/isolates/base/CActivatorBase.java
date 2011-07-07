package org.psem2m.isolates.base;

import java.util.logging.Level;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.psem2m.utilities.CXJavaRunContext;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CActivityFormaterBasic;
import org.psem2m.utilities.logging.CLogFormater;
import org.psem2m.utilities.logging.IActivityLoggerBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public abstract class CActivatorBase extends CXObjectBase implements
		IActivatorBase, IActivityLoggerBase, BundleActivator {

	public static String LIB_BNDL_ID = "BundleId";
	public static String LIB_POJO_ID = "PojoId";

	private BundleContext context;

	/**
	 * The formater which format jthe log line
	 * 
	 * <pre>
	 * TimeStamp     TimeStamp      TimeStamp  TimeStamp    Level    Thread name       Instance id          Method               LogLine
	 * (millis)      (nano)         (date)     (hhmmss.sss) 
	 * 1309180295049;00000065317000;2011/06/27;15:11:35:049;INFO   ;   FelixStartLevel;CIsolateLogger_2236 ;__validatePojo      ;EnvContext:
	 * </pe>
	 **/
	private final CActivityFormaterBasic pCActivityFormaterBasic;

	/** The formater which converts the array of objects to a line **/
	private final CLogFormater pLogFormater;

	/**
	 * Explicit default constructor
	 */
	public CActivatorBase() {
		super();

		pCActivityFormaterBasic = new CActivityFormaterBasic();
		pCActivityFormaterBasic.acceptMultiline(true);

		pLogFormater = new CLogFormater();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(final Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_BNDL_ID, getBundleId());
		return aBuffer;
	}

	/**
	 * @return the id of the bundle
	 */
	@Override
	public abstract String getBundleId();

	/**
	 * @return the BundleContext of the bundle
	 */
	public BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogDebugOn()
	 */
	@Override
	public boolean isLogDebugOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#isLoggable(java.util.logging
	 * .Level)
	 */
	@Override
	public boolean isLoggable(final Level aLevel) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogInfoOn()
	 */
	@Override
	public boolean isLogInfoOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogSevereOn()
	 */
	@Override
	public boolean isLogSevereOn() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#log(java.util.logging.Level,
	 * java.lang.Object, java.lang.CharSequence, java.lang.Object[])
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.logging.IActivityLogger#isLogWarningOn()
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

		System.out.println(pCActivityFormaterBasic.format(
				System.currentTimeMillis(), aLevel,
				pLogFormater.getWhoObjectId(aWho), wWhat.toString(),
				pLogFormater.formatLogLine(aInfos)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.IActivityLogger#logDebug(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
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
	 * org.psem2m.utilities.logging.IActivityLogger#logInfo(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
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
	 * org.psem2m.utilities.logging.IActivityLogger#logSevere(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
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
	 * org.psem2m.utilities.logging.IActivityLogger#logWarn(java.lang.Object,
	 * java.lang.CharSequence, java.lang.Object[])
	 */
	@Override
	public void logWarn(final Object aWho, final CharSequence aWhat,
			final Object... aInfos) {
		this.log(Level.WARNING, aWho, aWhat, aInfos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		context = bundleContext;
		logInfo(this, "start", "START", toDescription());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext bundleContext) throws Exception {
		logInfo(this, "stop", "STOP", toDescription());
		context = null;
	}

}
