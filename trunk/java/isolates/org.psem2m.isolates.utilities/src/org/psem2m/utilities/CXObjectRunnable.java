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
 * @author ogattaz
 * 
 */
public abstract class CXObjectRunnable extends CXObjectBase implements Runnable {

	public static final boolean OFF = false;
	public static final boolean ON = true;

	private boolean pContinue = ON;
	private long pStartTime;
	private Thread pThread;

	/**
	 * @param aParent
	 */
	public CXObjectRunnable(IXObjectBase aParent) {
		super(aParent);
	}

	/**
	 * @param aParent
	 * @param aIdentifier
	 */
	public CXObjectRunnable(IXObjectBase aParent, String aIdentifier) {
		super(aParent, aIdentifier);
	}

	/**
	 * @return
	 */
	protected synchronized boolean getContinue() {
		return pContinue;
	}

	/**
	 * @return
	 */
	protected long getDuration() {
		return System.currentTimeMillis() - pStartTime;
	}

	/**
	 * @return
	 */
	protected String getFormatedDuration() {
		return new java.util.Date(getDuration()).toString();
	}

	/**
	 * 
	 */
	protected void initStartTime() {
		pStartTime = System.currentTimeMillis();
	}

	/**
	 * @param aOn
	 */
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

		if (pThread != Thread.currentThread() && pThread.isAlive()) {
			pThread.join();
		}
	}

	/**
	 * @param aDuration
	 */
	protected void tempo(long aDuration) {
		try {
			if (pThread == Thread.currentThread()) {
				Thread.sleep(aDuration);
			}
		} catch (Exception e) {
			// ...
		}
	}

}
