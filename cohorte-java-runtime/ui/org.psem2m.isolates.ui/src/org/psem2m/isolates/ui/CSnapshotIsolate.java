package org.psem2m.isolates.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cohorte.herald.Access;
import org.cohorte.herald.Peer;

/**
 * @author ogattaz
 *
 */
public class CSnapshotIsolate extends CSnapshotAbstract {

    /** Isolate Name */
    private final String pIsolateName;

    /** Isolate UID */
    private final String pIsolateUid;

    /** Local peer flag */
    private final boolean pLocal;

    /** The Peer bean */
    private final Peer pPeer;

    /**
     * Sets up the snapshot
     *
     * @param aPeer
     *            The peer bean containing all information
     */
    public CSnapshotIsolate(final Peer aPeer) {

        this(aPeer, false);
    }

    /**
     * Sets up the snapshot
     *
     * @param aPeer
     *            The peer bean containing all information
     * @param aLocal
     *            If True, this is the local peer information
     */
    CSnapshotIsolate(final Peer aPeer, final boolean aLocal) {

        super(aPeer.getUid());
        pPeer = aPeer;
        pIsolateUid = aPeer.getUid();
        pIsolateName = aPeer.getName();
        pLocal = aLocal;
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

    /**
     * @return the isolateName
     */
    public String getIsolateName() {

        return pIsolateName;
    }

    /**
     * @return the isolateUid
     */
    public String getIsolateUid() {

        return pIsolateUid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.psem2m.isolates.ui.CSnapshotAbstract#getTextInfo()
     */
    @Override
    public String getTextInfo() {

        String text;
        if (pLocal) {
            text = "Local Isolate:\n";
        } else {
            text = "Peer Isolate:\n";
        }

        text += String.format("UID=[%s]\nName=[%s]\n", pIsolateUid,
                pIsolateName);

        // Sort the list of accesses IDs
        final Collection<String> accessesCollection = pPeer.getAccesses();
        final List<String> accessesList = new ArrayList<String>(
                accessesCollection);
        Collections.sort(accessesList);

        for (final String accessId : accessesList) {
            final Access access = pPeer.getAccess(accessId);
            text += "- " + accessId + ": " + access + "\n";
        }

        return text;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String prefix = "";
        if (pLocal) {
            prefix += "(Local) ";
        }

        return prefix + pIsolateName + ": " + pIsolateUid;
    }
}
