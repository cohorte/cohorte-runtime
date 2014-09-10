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

    /** The Peer bean */
    private final Peer pPeer;

    /**
     * @param aUID
     *            Isolate UID
     * @param aName
     *            Isolate name
     */
    public CSnapshotIsolate(final Peer aPeer) {

        super(aPeer.getUid());
        pPeer = aPeer;
        pIsolateUid = aPeer.getUid();
        pIsolateName = aPeer.getName();
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

        String text = String.format("Isolate:\nUID=[%s]\nName=[%s]\n",
                pIsolateUid, pIsolateName);

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

        return String.format("%s: %s", pIsolateName, pIsolateUid);
    }
}
