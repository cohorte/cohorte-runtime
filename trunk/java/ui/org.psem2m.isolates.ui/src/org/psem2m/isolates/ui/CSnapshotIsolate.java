package org.psem2m.isolates.ui;

import org.psem2m.signals.HostAccess;

/**
 * @author ogattaz
 * 
 */
public class CSnapshotIsolate extends CSnapshotAbstract {

    /** Isolate access */
    private HostAccess pHostAccess;

    /** Isolate UID */
    private final String pUID;

    /**
     * @param aUID
     *            Isolate UID
     * @param aName
     *            Isolate name
     */
    public CSnapshotIsolate(final String aUID, final String aName) {

        super(aName);
        pUID = aUID;
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
    public int getIndexOfChild(final CSnapshotAbstract aChild) {

        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.ui.CSnapshotAbstract#getTextInfo()
     */
    @Override
    public String getTextInfo() {

        return String.format("Isolate:\nUID=[%s]\nName=[%s]\nHostAccess=%s",
                pUID, getName(), pHostAccess);
    }

    /**
     * @return the uID
     */
    public String getUID() {

        return pUID;
    }

    /**
     * @param aHostAccess
     */
    void setHostAccess(final HostAccess aHostAccess) {

        pHostAccess = aHostAccess;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format("%s: %s", getName(), pUID);
    }

}
