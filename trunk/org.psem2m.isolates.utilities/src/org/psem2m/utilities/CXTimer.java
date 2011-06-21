/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.utilities;

/**
 * 
 * 
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CXTimer implements IXDescriber {

	public final static String DESCR_EL = "[";
	public final static String DESCR_MS = "ms]";
	public final static String DESCR_NS = "[Not started]";
	public final static String DESCR_SEP = "[";
	public final static String FMT_HEAP_SIZE = "%,3.0f";
	public final static String FMT_MICRO_SEC = "%6.3f";
	public final static String FMT_MILI_SEC = "%06d";

	public final static boolean START = true;

	/**
	 * @param aHeapSize
	 * @return
	 */
	public static String heapSizeToStr(final long aHeapSize) {
		return String.format(FMT_HEAP_SIZE, new Double(aHeapSize));
	}

	/**
	 * @param aNanoSec
	 * @return
	 */
	public static String nanoSecToMicroSecStr(final long aNanoSec) {
		Double wDbl = new Double((double) aNanoSec / (double) 1000000000);
		return String.format(FMT_MICRO_SEC, wDbl);
	}

	/**
	 * @param aNanoSec
	 * @return
	 */
	public static String nanoSecToMilliSecStr(final long aNanoSec) {
		Double wDbl = new Double((double) aNanoSec / (double) 1000000000);
		return String.format(FMT_MILI_SEC, wDbl.longValue());
	}

	private long pHeapStart = 0;
	private long pHeapStop = 0;
	private String pName = null;

	/** Temps au declenchement du timer **/
	private long pStartNano = 0;
	/** Temps à l'arrêt du timer **/
	private long pStopNano = 0;
	/** Nanotime pour calcul du temps ecoule depuis le depart **/
	private long pTimeRefNano = 0;

	/**
	 * instanciate a not started timer.
	 */
	public CXTimer() {
		this(null, 0, false);
	}

	/**
	 * instanciate a started or a not started timer.
	 * 
	 * @param aStarted
	 *            starts the timer if true
	 */
	public CXTimer(final boolean aStarted) {
		this(null, 0, aStarted);
	}

	/**
	 * instanciate a not started timer.
	 * 
	 * @param aTimeRefNano
	 *            a time reference (to have an other then the start time of the
	 *            JVM)
	 */
	public CXTimer(final long aTimeRefNano) {
		this(null, aTimeRefNano, false);
	}

	/**
	 * instanciate a started or a not started timer.
	 * 
	 * @param aTimeRefNano
	 *            a time reference (to have an other than the start time of the
	 *            JVM)
	 * @param aStart
	 *            starts the timer if true
	 */
	public CXTimer(final long aTimeRefNano, final boolean aStart) {
		this(null, aTimeRefNano, aStart);
	}

	/**
	 * instanciate a started or a not started timer.
	 * 
	 * @param aName
	 *            the name of the timer
	 * @param aStart
	 *            starts the timer if true
	 */
	public CXTimer(final String aName, final boolean aStart) {
		this(aName, 0, aStart);
	}

	/**
	 * instanciate a not started timer.
	 * 
	 * @param aName
	 *            the name of the timer
	 * @param aTimeRefNano
	 *            a time reference (to have an other than the start time of the
	 *            JVM)
	 */
	public CXTimer(final String aName, final long aTimeRefNano) {
		this(aName, aTimeRefNano, false);
	}

	/**
	 * instanciate a started or a not started timer.
	 * 
	 * @param aName
	 *            the name of the timer
	 * 
	 * @param aTimeRefNano
	 *            a time reference (to have an other than the start time of the
	 *            JVM)
	 * @param aStart
	 *            starts the timer if true
	 */
	public CXTimer(final String aName, final long aTimeRefNano,
			final boolean aStart) {
		pName = aName;
		reset(aTimeRefNano);
		if (aStart) {
			start();
		}
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
		try {
			if (!isStarted()) {
				return aBuffer.append(pName).append(DESCR_NS);
			} else {
				aBuffer.append('[').append(getStartAtMsStr()).append(']')
						.append(pName == null ? "" : pName).append(DESCR_SEP)
						.append(getDurationStrMilliSec()).append(DESCR_MS);
				if (isStopped()) {
					aBuffer.append(DESCR_SEP).append(getStopAtMsStr())
							.append(DESCR_MS);
				}
				return aBuffer;
			}
		} catch (Exception e) {
			return new StringBuilder().append(CXException.eInString(e));
		}
	}

	/**
	 * @return
	 */
	public int calcDescriptionLength() {
		return 128;
	}

	/**
	 * @return the duration in milliseconds
	 */
	public double getDurationMs() {
		return (double) getDurationNs() / 1000000;
	}

	/**
	 * @return the duration in nanoseconds
	 */
	public long getDurationNs() {
		if (pStartNano == 0) {
			return 0;
		}
		return (pStopNano > 0 ? pStopNano : System.nanoTime()) - pStartNano;
	}

	/**
	 * @return a formated string ("%06d") containing the duration in
	 *         milliseconds
	 */
	public String getDurationStrMicroSec() {
		return nanoSecToMicroSecStr(getDurationNs());
	}

	/**
	 * @return a formated string ("%6.3f") containing the duration in in
	 *         milliseconds with microesconds
	 */
	public String getDurationStrMilliSec() {
		return nanoSecToMilliSecStr(getDurationNs());
	}

	/**
	 * @return the delta between the sizes of the heap collected at the start
	 *         and stop times of the timer.
	 */
	public String getHeapDelta() {
		return heapSizeToStr((pHeapStop == 0 ? Runtime.getRuntime()
				.freeMemory() : pHeapStop) - pHeapStart);
	}

	/**
	 * @return the size of the heap collected at the start time of the timer.
	 */
	public String getHeapStart() {
		return heapSizeToStr(pHeapStart);
	}

	/**
	 * @return the size of the heap collected at the stop time of the timer.
	 */
	public String getHeapStop() {
		return heapSizeToStr(pHeapStop == 0 ? Runtime.getRuntime().freeMemory()
				: pHeapStop);
	}

	/**
	 * @return the name of the timer
	 */
	public String getName() {
		return pName;
	}

	/**
	 * @return the start time since the reference time, formated ("%06d") with
	 *         milliseconds
	 */
	public String getStartAtMsStr() {
		return nanoSecToMilliSecStr(getStartAtNano());
	}

	/**
	 * @return the start time since the reference time in nanoseconds
	 */
	public long getStartAtNano() {
		return pStartNano - pTimeRefNano;
	}

	/**
	 * @return the start time since the reference time, formated ("%6.3f") with
	 *         milliseconds and microseconds
	 */
	public String getStartAtSecStr() {
		return nanoSecToMicroSecStr(getStartAtNano());
	}

	/**
	 * @return the stop time since the reference time, formated ("%06d") with
	 *         milliseconds
	 */
	public String getStopAtMsStr() {
		return nanoSecToMilliSecStr(getStopAtNano());
	}

	/**
	 * @return the stop time since the reference time in nanoseconds
	 */
	public long getStopAtNano() {
		return pStopNano - pTimeRefNano;
	}

	/**
	 * @return the stop time since the reference time, formated ("%6.3f") with
	 *         milliseconds and microseconds
	 */
	public String getStopAtSecStr() {
		return nanoSecToMicroSecStr(getStopAtNano());
	}

	/**
	 * @return true if the timer is started and not stopped
	 */
	public boolean isCounting() {
		return isStarted() && !isStopped();
	}

	/**
	 * @return true if the timer is started
	 */
	public boolean isStarted() {
		return pStartNano > 0;
	}

	/**
	 * @return true if the timer is stopped
	 */
	public boolean isStopped() {
		return pStopNano > 0;
	}

	/**
	 * resets the timer (raz all the memorized times)
	 */
	public void reset() {
		reset(0);
	}

	/**
	 * resets the timer (raz all the memorized times) and sets the reference
	 * time
	 * 
	 * @param aTimeRefNano
	 *            a time reference (to have an other than the start time of the
	 *            JVM)
	 */
	public void reset(final long aTimeRefNano) {
		pHeapStart = 0;
		pHeapStop = 0;
		pStartNano = 0;
		pStopNano = 0;
		pTimeRefNano = aTimeRefNano > 0 ? aTimeRefNano : 0;
	}

	/**
	 * start the timer
	 * 
	 * @return the timer
	 */
	public CXTimer start() {
		pHeapStart = Runtime.getRuntime().freeMemory();
		pStartNano = System.nanoTime();
		pStopNano = 0;
		return this;
	}

	/**
	 * start the timer with a new reference time
	 * 
	 * @param aTimeRefNano
	 *            a time reference (to have an other than the start time of the
	 *            JVM)
	 * @return the timer
	 */
	public CXTimer start(final long aTimeRefNano) {
		pTimeRefNano = aTimeRefNano > 0 ? aTimeRefNano : 0;
		start();
		return this;
	}

	/**
	 * stop the timer
	 * 
	 * @return the number of nanosecond passed since the start time
	 */
	public long stop() {
		pHeapStop = Runtime.getRuntime().freeMemory();
		pStopNano = System.nanoTime();
		return getDurationNs();
	}

	/**
	 * @return the formated number of milliseconds and microseconds ("%6.3f")
	 *         passed since the start time
	 */
	public String stopStrMicroSec() {
		pStopNano = System.nanoTime();
		return getDurationStrMilliSec();
	}

	/**
	 * @return the formated number of milliseconds ("%06d") passed since the
	 *         start time
	 */
	public String stopStrMs() {
		pStopNano = System.nanoTime();
		return getDurationStrMilliSec();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXDescriber#toDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();

	}
}
