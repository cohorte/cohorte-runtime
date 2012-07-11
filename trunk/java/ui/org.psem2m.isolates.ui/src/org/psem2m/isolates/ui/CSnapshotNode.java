package org.psem2m.isolates.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ogattaz
 * 
 */
public class CSnapshotNode extends CSnapshotAbstract {

	private String pHostName;

	final List<CSnapshotIsolate> pIsolates = new ArrayList<CSnapshotIsolate>();

	/**
	 * @param aState
	 */
	public CSnapshotNode(final String aName) {

		super(aName);
	}

	/**
	 * @param aIsolate
	 */
	public void add(CSnapshotIsolate aIsolate) {
		pIsolates.add(aIsolate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.ui.CSnapshotAbstract#getChild(int)
	 */
	@Override
	public CSnapshotIsolate getChild(final int aIdx) {
		return pIsolates.get(aIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.ui.CSnapshotAbstract#getChildCount()
	 */
	@Override
	public int getChildCount() {
		return pIsolates.size();
	}

	/**
	 * @return
	 */
	String getHostName() {
		return pHostName;
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
		return getIndexOfChild(aChild.getName());
	}
	/**
	 * @param aIsolateId
	 * @return
	 */
	public int getIndexOfChild(String aIsolateId) {

		int wIdx = 0;
		for (CSnapshotIsolate wChild : pIsolates) {
			if (wChild.getName().equals(aIsolateId)) {
				return wIdx;
			}
			wIdx++;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.ui.CSnapshotAbstract#getTextInfo()
	 */
	@Override
	public String getTextInfo() {
		return String.format("Node:\nname=[%s]\nHostName=[%s]", getName(),getHostName());
	}
	
	/**
	 * @param aIsolateId
	 */
	void removeChild(String  aIsolateId){
		
		int wIdx = getIndexOfChild( aIsolateId);
		if (wIdx>-1){
			pIsolates.remove(wIdx);
		}
	}

	/**
	 * @param aHostName
	 */
	void setHostName(String aHostName) {
		pHostName = aHostName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return String.format("node: %s", getName());
	}
}
