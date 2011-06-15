package org.psem2m.utilities.logging;

import org.psem2m.utilities.CXObjectBase;

abstract class CActivityObject extends CXObjectBase {

	/**
	 * @param aParent
	 * @param ixIdentifier
	 */
	CActivityObject(CActivityObject aParent, String aIdentifier) {
		super(aParent, aIdentifier);

	}

	/**
	 * @param ixIdentifier
	 */
	CActivityObject(String aIdentifier) {
		super(aIdentifier);
	}

	/**
	 * @return
	 */
	CActivityObject getActivityObjectParent() {
		return (CActivityObject) super.getParent();
	}

	/**
	 * @return
	 */
	boolean hasActivityObjectParent() {
		return super.hasParent() && getParent() instanceof CActivityObject;
	}

	/**
	 * @return
	 */
	boolean isTraceDebugOn() {
		return false;
	}

	/**
	 * @param aWho
	 * @param aMethod
	 * @param aLineBuffer
	 */
	void traceDebug(Object aWho, CharSequence aMethod, CharSequence aLineBuffer) {
		if (hasActivityObjectParent())
			getActivityObjectParent().traceDebug(aWho, aMethod, aLineBuffer);
	}

}
