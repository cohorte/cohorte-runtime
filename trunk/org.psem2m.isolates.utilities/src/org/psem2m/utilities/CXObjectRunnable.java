package org.psem2m.utilities;

public abstract class CXObjectRunnable extends CXObjectBase implements Runnable {

	public final static boolean OFF = false;
	public final static boolean ON = true;
	private boolean pContinue = ON;
	private long pStartTime;
	private Thread pThread;

	public CXObjectRunnable(IXObjectBase aParent) {
		super(aParent);
	}

	public CXObjectRunnable(IXObjectBase aParent, String aIdentifier) {
		super(aParent, aIdentifier);
	}

	protected synchronized boolean getContinue() {
		return pContinue;
	}

	protected long getDuration() {
		return System.currentTimeMillis() - pStartTime;
	}

	protected String getFormatedDuration() {
		return new java.util.Date(getDuration()).toString();
	}

	protected void initStartTime() {
		pStartTime = System.currentTimeMillis();
	}

	protected synchronized void setContinue(boolean aOn) {
		pContinue = aOn;
	}

	/**
	 * @param aThread
	 */
	public void startRun(Thread aThread) {
		pThread = aThread;
		pThread.start();
	}

	/**
	 * @param aTimeOut
	 */
	public void stopRun() throws Exception {
		setContinue(OFF);

		if (pThread != Thread.currentThread() && pThread.isAlive())
			pThread.join();
	}

	/**
	 * @param aDuration
	 */
	protected void tempo(long aDuration) {
		try {
			if (pThread == Thread.currentThread())
				Thread.sleep(aDuration);
		} catch (Exception e) {
			// ...
		}
	}

}
