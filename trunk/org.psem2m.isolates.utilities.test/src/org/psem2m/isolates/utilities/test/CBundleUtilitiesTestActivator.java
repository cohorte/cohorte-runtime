package org.psem2m.isolates.utilities.test;

import org.psem2m.isolates.base.CActivatorBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CBundleUtilitiesTestActivator extends CActivatorBase implements
		IBundleUtilitiesTestActivator {

	/** first instance **/
	private static IBundleUtilitiesTestActivator sBundleUtilitiesTesterActivator = null;

	/**
	 * @return
	 */
	public static IBundleUtilitiesTestActivator getInstance() {
		return sBundleUtilitiesTesterActivator;
	}

	/**
	 * Explicit default constructor
	 */
	public CBundleUtilitiesTestActivator() {
		super();
		if (sBundleUtilitiesTesterActivator == null) {
			sBundleUtilitiesTesterActivator = this;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.CActivatorBase#getBundleId()
	 */
	@Override
	public String getBundleId() {
		return getClass().getPackage().getName();
	}

}
