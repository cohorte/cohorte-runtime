package org.psem2m.utilities.logging;

import java.util.logging.Level;

import org.psem2m.utilities.CXStringUtils;

public class CActivityRequestFilter extends CActivityObject {

	private final static String LABEL_LEVEL = "Level";

	private final static String LABEL_METHOD = "Method";

	private final static String LABEL_NBMAX = "NbMax";

	private final static String LABEL_TIMESTART = "TimeStart";

	private final static String LABEL_TIMESTOP = "TimeStart";

	/**
	 * @param aMax
	 * @return
	 */
	public static CActivityRequestFilter newFilter(int aMax) {
		CActivityRequestFilter wFilter = new CActivityRequestFilter();
		wFilter.setNbMax(aMax);
		return wFilter;
	}

	/**
	 * @param aTimeStart
	 * @param aTimeStop
	 * @param aLevel
	 * @param aMethod
	 * @param aNbMax
	 */
	public static CActivityRequestFilter newFilter(long aTimeStart,
			long aTimeStop, String aLevel, String aMethod, int aNbMaxLogRecord) {
		CActivityRequestFilter wFilter = new CActivityRequestFilter();
		wFilter.setTimeStart(aTimeStart);
		wFilter.setTimeStop(aTimeStop);
		wFilter.setLevel(aLevel);
		wFilter.setMethod(aMethod);
		wFilter.setNbMax(aNbMaxLogRecord);
		return wFilter;
	}

	/**
	 * @param aLevelName
	 * @param aMax
	 * @return
	 */
	public static CActivityRequestFilter newLevelFilter(String aLevelName,
			int aMax) {
		CActivityRequestFilter wFilter = new CActivityRequestFilter();
		wFilter.setLevel(aLevelName);
		wFilter.setNbMax(aMax);
		return wFilter;
	}

	/**
	 * @param aMethodName
	 * @param aMax
	 * @return
	 */
	public static CActivityRequestFilter newMethodFilter(String aMethodName,
			int aMax) {
		CActivityRequestFilter wFilter = new CActivityRequestFilter();
		wFilter.setMethod(aMethodName);
		wFilter.setNbMax(aMax);
		return wFilter;
	}

	private Level pLevel = null;

	private String pMethod = null;

	private int pNbMaxLogRecord = -1;

	private long pTimeStart = -1;

	private long pTimeStop = -1;

	/**
	 * 
	 */
	private CActivityRequestFilter() {
		super(CActivityRequestFilter.class.getSimpleName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.logging.CActivityObject#addDescriptionInBuffer(java.lang.
	 * Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValsInBuff(aBuffer, LABEL_TIMESTART,
				hasTimeStart(), pTimeStart);
		CXStringUtils.appendKeyValsInBuff(aBuffer, LABEL_TIMESTOP,
				hasTimeStop(), pTimeStop);
		CXStringUtils.appendKeyValsInBuff(aBuffer, LABEL_LEVEL, hasLevel(),
				getLevelName());
		CXStringUtils.appendKeyValsInBuff(aBuffer, LABEL_METHOD, hasMethod(),
				pMethod);
		CXStringUtils.appendKeyValsInBuff(aBuffer, LABEL_NBMAX,
				hasNbMaxLogRecord(), pNbMaxLogRecord);
		return aBuffer;
	}

	@Override
	public void destroy() {
		// ...
	}

	/**
	 * @return
	 */
	public Level getLevel() {
		return pLevel;
	}

	/**
	 * @return
	 */
	public String getLevelName() {
		return pLevel.getName();
	}

	/**
	 * @return
	 */
	public String getMethod() {
		return pMethod;
	}

	/**
	 * @return
	 */
	public int getNbMaxLogRecord() {
		return pNbMaxLogRecord;
	}

	/**
	 * @return
	 */
	public long getTimeStart() {
		return pTimeStart;
	}

	/**
	 * @return
	 */
	public long getTimeStop() {
		return pTimeStop;
	}

	/**
	 * @return
	 */
	public boolean hasLevel() {
		return pLevel != Level.OFF;
	}

	/**
	 * @return
	 */
	public boolean hasMethod() {
		return pMethod != null;
	}

	/**
	 * @return
	 */
	public boolean hasNbMaxLogRecord() {
		return pNbMaxLogRecord > -1;
	}

	/**
	 * @return
	 */
	public boolean hasTimeStart() {
		return pTimeStart > -1;
	}

	/**
	 * @return
	 */
	public boolean hasTimeStop() {
		return pTimeStop > -1;
	}

	/**
	 * @param aLevel
	 * @return
	 */
	boolean isLevelOK(Level aLevel) {
		return !hasLevel() || getLevel().intValue() <= aLevel.intValue();
	}

	/**
	 * @param aMethod
	 * @return
	 */
	boolean isMethodOK(String aMethod) {
		return !hasMethod() || getMethod().equalsIgnoreCase(aMethod);
	}

	/**
	 * @param aTimeStamp
	 * @return vrai si le timestamp fourni est inf�rieur au "TimeStart" s'il y
	 *         en a un
	 */
	boolean isOutOfTime(long aTimeStamp) {
		return hasTimeStart() && aTimeStamp < getTimeStart();

	}

	/**
	 * @param aTimeStamp
	 * @return vrai si le timestamp fourni est compris entre le "TimeStart" et
	 *         "TimeStop" dans le cas ou il y a des bornes temporelles
	 */
	boolean isTimeOK(long aTimeStamp) {
		boolean wStartOk = (!hasTimeStart() || aTimeStamp >= getTimeStart());
		boolean wStopOk = (!hasTimeStop() || aTimeStamp <= getTimeStop());
		return wStartOk && wStopOk;
	}

	/**
	 * @param level
	 */
	public void setLevel(String aLevelName) {
		pLevel = CActivityUtils.levelToLevel(aLevelName);
	}

	/**
	 * @param level
	 */
	public void setMethod(String aMethod) {
		pMethod = aMethod;
	}

	/**
	 * @param nbMax
	 */
	public void setNbMax(int aNbMaxLogRecord) {
		pNbMaxLogRecord = aNbMaxLogRecord;
	}

	/**
	 * @param timeStart
	 */
	public void setTimeStart(long aTimeStart) {
		pTimeStart = aTimeStart;
	}

	/**
	 * @param timeStop
	 */
	public void setTimeStop(long aTimeStop) {
		pTimeStop = aTimeStop;
	}
}
