package org.psem2m.isolates.ui;

import org.psem2m.signals.HostAccess;

/**
 * @author ogattaz
 * 
 */
public class CSnapshotIsolate extends CSnapshotAbstract {

	private HostAccess pHostAccess;

	/**
	 * @param aState
	 */
	public CSnapshotIsolate(final String aName) {

		super(aName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.ui.CSnapshotAbstract#getChild(int)
	 */
	@Override
	public CSnapshotAbstract getChild(final int aIdx) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.ui.CSnapshotAbstract#getChildCount()
	 */
	@Override
	public int getChildCount() {
		return 0;
	}

	/**
	 * @return
	 */
	HostAccess getHostAccess() {
		return pHostAccess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.ui.CSnapshotAbstract#getIndexOfChild(org.psem2m.isolates
	 * .ui.CSnapshotAbstract)
	 */
	@Override
	public int getIndexOfChild(CSnapshotAbstract aChild) {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.ui.CSnapshotAbstract#getTextInfo()
	 */
	@Override
	public String getTextInfo() {
		return String.format("Isolate:\nname=[%s]\nHostAccess=%s", getName(),getHostAccess());
	}

	/**
	 * @param aHostAccess
	 */
	void setHostAccess(HostAccess aHostAccess) {
		pHostAccess = aHostAccess;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return String.format("isolate: %s", getName());
	}

}
