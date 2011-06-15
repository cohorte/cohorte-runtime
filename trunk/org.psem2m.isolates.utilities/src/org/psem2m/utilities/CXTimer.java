package org.psem2m.utilities;

public class CXTimer implements IXDescriber {

	public final static String DESCR_EL = "[";
	public final static String DESCR_MS = "ms]";
	public final static String DESCR_NS = "[Not started]";
	public final static String DESCR_SEP = "[";
	public final static String FMT_HEAP_SIZE = "%,3.0f";
	public final static String FMT_MICRO_SEC = "%6.3f";
	public final static String FMT_MILI_SEC = "%06d";

	public static String heapSizeToStr(long aHeapSize) {
		return String.format(FMT_HEAP_SIZE, new Double(aHeapSize));
	}

	public static String nanoSecToMsStr(long aNanoSec) {
		Double wDbl = new Double((double) aNanoSec / (double) 1000000);
		return String.format(FMT_MILI_SEC, wDbl.longValue());
	}

	public static String nanoSecToSecStr(long aNanoSec) {
		Double wDbl = new Double((double) aNanoSec / (double) 1000000000);
		return String.format(FMT_MICRO_SEC, wDbl);
	}

	private String pDescr = null;
	private long pHeapStart = 0;
	private long pHeapStop = 0;

	// Temps au declenchement du timer
	private long pStartNano = 0;

	private long pStopNano = 0;
	// Nanotime pour calcul du temps ecoule depuis le depart
	private long pTimeRefNano = 0;

	public CXTimer() {
	}

	public CXTimer(boolean aStart) {
		this(null, 0, aStart);
	}

	public CXTimer(long aTimeRefNano) {
		this(null, aTimeRefNano, false);
	}

	public CXTimer(long aTimeRefNano, boolean aStart) {
		this(null, aTimeRefNano, aStart);
	}

	public CXTimer(String aDescr, boolean aStart) {
		this(aDescr, 0, aStart);
	}

	public CXTimer(String aDescr, long aTimeRefNano) {
		this(aDescr, aTimeRefNano, false);
	}

	public CXTimer(String aDescr, long aTimeRefNano, boolean aStart) {
		pDescr = aDescr;
		reset(aTimeRefNano);
		if (aStart)
			start();
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		try {
			if (!isStarted())
				return aBuffer.append(pDescr).append(DESCR_NS);
			else {
				aBuffer.append('[').append(getStartAtMsStr()).append(']')
						.append(pDescr == null ? "" : pDescr).append(DESCR_SEP)
						.append(getDurationStrMs()).append(DESCR_MS);
				if (isStopped())
					aBuffer.append(DESCR_SEP).append(getStopAtMsStr())
							.append(DESCR_MS);
				return aBuffer;
			}
		} catch (Exception e) {
			return new StringBuilder().append(CXException.eInString(e));
		}
	}

	public int calcDescriptionLength() {
		return 128;
	}

	public String getDescr() {
		return pDescr;
	}

	/**
	 * @return
	 */
	public double getDurationMs() {
		return (double) getDurationNs() / 1000000;
	}

	public long getDurationNs() {
		if (pStartNano == 0)
			return 0;
		return (pStopNano > 0 ? pStopNano : System.nanoTime()) - pStartNano;
	}

	public String getDurationStrMs() {
		return nanoSecToMsStr(getDurationNs());
	}

	public String getHeapDelta() {
		return heapSizeToStr((pHeapStop == 0 ? Runtime.getRuntime()
				.freeMemory() : pHeapStop) - pHeapStart);
	}

	public String getHeapStart() {
		return heapSizeToStr(pHeapStart);
	}

	public String getHeapStop() {
		return heapSizeToStr(pHeapStop == 0 ? Runtime.getRuntime().freeMemory()
				: pHeapStop);
	}

	public String getStartAtMsStr() {
		return nanoSecToMsStr(getStartAtNano());
	}

	public long getStartAtNano() {
		return pStartNano - pTimeRefNano;
	}

	public String getStartAtSecStr() {
		return nanoSecToSecStr(getStartAtNano());
	}

	public String getStopAtMsStr() {
		return nanoSecToMsStr(getStopAtNano());
	}

	public long getStopAtNano() {
		return pStopNano - pTimeRefNano;
	}

	public String getStopAtSecStr() {
		return nanoSecToSecStr(getStopAtNano());
	}

	public boolean isCounting() {
		return isStarted() && !isStopped();
	}

	public boolean isStarted() {
		return pStartNano > 0;
	}

	public boolean isStopped() {
		return pStopNano > 0;
	}

	public void reset(long aTimeRefNano) {
		pHeapStart = 0;
		pHeapStop = 0;
		pStartNano = 0;
		pStopNano = 0;
		pTimeRefNano = aTimeRefNano > 0 ? aTimeRefNano : 0;
	}

	public CXTimer start() {
		pHeapStart = Runtime.getRuntime().freeMemory();
		pStartNano = System.nanoTime();
		pStopNano = 0;
		return this;
	}

	public CXTimer start(long aTimeRefNano) {
		pTimeRefNano = aTimeRefNano > 0 ? aTimeRefNano : 0;
		start();
		return this;
	}

	public long stop() {
		pHeapStop = Runtime.getRuntime().freeMemory();
		pStopNano = System.nanoTime();
		return getDurationNs();
	}

	public String stopStrMs() {
		pStopNano = System.nanoTime();
		return getDurationStrMs();
	}

	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();

	}
}
