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
		Double wDbl = new Double((double) aNanoSec / (double) 1000000);
		return String.format(FMT_MILI_SEC, wDbl.longValue());
	}

	private long pHeapStart = 0;
	private long pHeapStop = 0;
	private String pName = null;

	/** Temps au declenchement du timer **/
	private long pStartNano = 0;

	private long pStopNano = 0;
	/** Nanotime pour calcul du temps ecoule depuis le depart **/
	private long pTimeRefNano = 0;

	/**
	 * 
	 */
	public CXTimer() {
		super();
	}

	/**
	 * @param aStart
	 */
	public CXTimer(final boolean aStart) {
		this(null, 0, aStart);
	}

	/**
	 * @param aTimeRefNano
	 */
	public CXTimer(final long aTimeRefNano) {
		this(null, aTimeRefNano, false);
	}

	/**
	 * @param aTimeRefNano
	 * @param aStart
	 */
	public CXTimer(final long aTimeRefNano, final boolean aStart) {
		this(null, aTimeRefNano, aStart);
	}

	/**
	 * @param aDescr
	 * @param aStart
	 */
	public CXTimer(final String aDescr, final boolean aStart) {
		this(aDescr, 0, aStart);
	}

	/**
	 * @param aDescr
	 * @param aTimeRefNano
	 */
	public CXTimer(final String aDescr, final long aTimeRefNano) {
		this(aDescr, aTimeRefNano, false);
	}

	/**
	 * @param aName
	 * @param aTimeRefNano
	 * @param aStart
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
	 * @return
	 */
	public double getDurationMs() {
		return (double) getDurationNs() / 1000000;
	}

	/**
	 * @return
	 */
	public long getDurationNs() {
		if (pStartNano == 0) {
			return 0;
		}
		return (pStopNano > 0 ? pStopNano : System.nanoTime()) - pStartNano;
	}

	/**
	 * @return
	 */
	public String getDurationStrMicroSec() {
		return nanoSecToMicroSecStr(getDurationNs());
	}

	/**
	 * @return
	 */
	public String getDurationStrMilliSec() {
		return nanoSecToMilliSecStr(getDurationNs());
	}

	/**
	 * @return
	 */
	public String getHeapDelta() {
		return heapSizeToStr((pHeapStop == 0 ? Runtime.getRuntime()
				.freeMemory() : pHeapStop) - pHeapStart);
	}

	/**
	 * @return
	 */
	public String getHeapStart() {
		return heapSizeToStr(pHeapStart);
	}

	/**
	 * @return
	 */
	public String getHeapStop() {
		return heapSizeToStr(pHeapStop == 0 ? Runtime.getRuntime().freeMemory()
				: pHeapStop);
	}

	/**
	 * @return
	 */
	public String getName() {
		return pName;
	}

	/**
	 * @return
	 */
	public String getStartAtMsStr() {
		return nanoSecToMilliSecStr(getStartAtNano());
	}

	/**
	 * @return
	 */
	public long getStartAtNano() {
		return pStartNano - pTimeRefNano;
	}

	/**
	 * @return
	 */
	public String getStartAtSecStr() {
		return nanoSecToMicroSecStr(getStartAtNano());
	}

	/**
	 * @return
	 */
	public String getStopAtMsStr() {
		return nanoSecToMilliSecStr(getStopAtNano());
	}

	/**
	 * @return
	 */
	public long getStopAtNano() {
		return pStopNano - pTimeRefNano;
	}

	/**
	 * @return
	 */
	public String getStopAtSecStr() {
		return nanoSecToMicroSecStr(getStopAtNano());
	}

	/**
	 * @return
	 */
	public boolean isCounting() {
		return isStarted() && !isStopped();
	}

	/**
	 * @return
	 */
	public boolean isStarted() {
		return pStartNano > 0;
	}

	/**
	 * @return
	 */
	public boolean isStopped() {
		return pStopNano > 0;
	}

	/**
	 * @param aTimeRefNano
	 */
	public void reset(final long aTimeRefNano) {
		pHeapStart = 0;
		pHeapStop = 0;
		pStartNano = 0;
		pStopNano = 0;
		pTimeRefNano = aTimeRefNano > 0 ? aTimeRefNano : 0;
	}

	/**
	 * @return
	 */
	public CXTimer start() {
		pHeapStart = Runtime.getRuntime().freeMemory();
		pStartNano = System.nanoTime();
		pStopNano = 0;
		return this;
	}

	/**
	 * @param aTimeRefNano
	 * @return
	 */
	public CXTimer start(final long aTimeRefNano) {
		pTimeRefNano = aTimeRefNano > 0 ? aTimeRefNano : 0;
		start();
		return this;
	}

	/**
	 * @return
	 */
	public long stop() {
		pHeapStop = Runtime.getRuntime().freeMemory();
		pStopNano = System.nanoTime();
		return getDurationNs();
	}

	/**
	 * @return
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
