package org.psem2m.utilities.logging;

import java.util.logging.Formatter;

import org.psem2m.utilities.CXStringUtils;

abstract class CActivityFormater extends Formatter {

	/**
	 * long: The long data type is a 64-bit signed two's complement integer.
	 * 
	 * It has a minimum value of -9,223,372,036,854,775,808 and a maximum value
	 * of 9,223,372,036,854,775,807
	 * 
	 * 1 an : 365*24*3600*1000000000 => 32850000000000000 nanosecondes => 17
	 * chiffres
	 * 
	 * 1 jour : 24*3600*1000000000 => 86400000000000 nanosecondes => 14 chiffres
	 * 
	 * 1 heure : 3600*1000000000 => 3600000000000 nanosecondes => 13 chiffres
	 */
	private final static long DAY = 86400000000000L;

	/** 1 day duration => 14 digits **/
	private final static int LENGTH_NANO = 14;

	final static char REPLACE_COLUMN = '_';

	final static String SEP_DATE = "/";
	final static char SEP_LINE = '\n';
	final static char SEP_MILLI = '.';
	final static String SEP_TIME = ":";

	final static int SIZE_LOG_THREADNAME = 18;

	StringBuilder pSB = new StringBuilder(128);

	/**
	 * Differences in successive calls that span greater than approximately 292
	 * years (2<sup>63</sup> nanoseconds) will not accurately compute elapsed
	 * time due to numerical overflow.
	 */
	private long pStartTime = 0;

	private final CLogTools pTools = CLogTools.getInstance();

	/**
	 * 
	 */
	public CActivityFormater() {
		super();
		initStartTime();
	}

	/**
	 * @param aSB
	 * @param aThreadName
	 * @return
	 */
	StringBuilder addThreadNameInLogLine(StringBuilder aSB, String aThreadName) {
		aSB.append(pTools.strAdjustRight(aThreadName, SIZE_LOG_THREADNAME, ' '));
		return aSB;
	}

	/**
	 * @return
	 */
	String getFormatedNanoSecs() {
		return CXStringUtils.strAdjustRight(getMaximizedNanos(), LENGTH_NANO);
	}

	/**
	 * @return
	 */
	private long getMaximizedNanos() {
		long wDuration = getNanos();
		if (wDuration > getNanosInDay()) {
			initStartTime();
			wDuration = getNanos();
		}
		return wDuration;
	}

	/**
	 * @return
	 */
	private long getNanos() {
		return System.nanoTime() - pStartTime;
	}

	/**
	 * @return
	 */
	private long getNanosInDay() {
		return DAY;
	}

	/**
   * 
   */
	private void initStartTime() {
		pStartTime = System.nanoTime();
	}
}
