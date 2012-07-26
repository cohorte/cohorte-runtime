/**
 * File:   WaitingSignal.java
 * Author: "Thomas Calmant"
 * Date:   19 juin 2012
 */
package org.psem2m.signals;

import java.util.concurrent.Future;

import org.psem2m.signals.ISignalBroadcaster.ESendMode;
import org.psem2m.signals.ISignalDirectory.EBaseGroup;

/**
 * Represents a signal in the waiting list
 * 
 * @author Thomas Calmant
 */
public class WaitingSignal implements IWaitingSignal {

    /** Access for sendTo() */
    private final HostAccess pAccess;

    /** A directory base group */
    private final EBaseGroup pBaseGroup;

    /** The raw signal content */
    private final Object pContent;

    /** Results of a call to fire() */
    private String[] pFireResult;

    /** The target groups */
    private final String[] pGroups;

    /** The target isolates */
    private final String[] pIsolates;

    /** The signal listener to call back */
    private final IWaitingSignalListener pListener;

    /** The signal sending mode */
    private final ESendMode pMode;

    /** The signal name */
    private final String pName;

    /** Result of a call to post() */
    private Future<ISignalSendResult> pPostResult;

    /** Result of a call to send() */
    private ISignalSendResult pSendResult;

    /** Results of a call to sendTo() */
    private Object[] pSendToResult;

    /** Time to live in waiting list */
    private long pTTL;

    /**
     * Sets up the signal bean.
     * 
     * Only one of aAccess, aIsolates or aGroups must be given.
     * 
     * @param aSignalName
     *            Signal name
     * @param aContent
     *            Raw signal content
     * @param aListener
     *            The waiting signal listener
     * @param aMode
     *            The signal sending mode
     * @param aTTL
     *            Time to live in the waiting list (in seconds)
     * @param aGroup
     *            A directory base group
     */
    public WaitingSignal(final String aSignalName, final Object aContent,
            final IWaitingSignalListener aListener, final ESendMode aMode,
            final long aTTL, final EBaseGroup aGroup) {

        pName = aSignalName;
        pContent = aContent;
        pListener = aListener;
        pMode = aMode;
        pTTL = aTTL;

        pAccess = null;
        pBaseGroup = aGroup;
        pGroups = null;
        pIsolates = null;
    }

    /**
     * Sets up the signal bean.
     * 
     * Only one of aAccess, aIsolates or aGroups must be given.
     * 
     * @param aSignalName
     *            Signal name
     * @param aContent
     *            Raw signal content
     * @param aListener
     *            The waiting signal listener
     * @param aMode
     *            The signal sending mode
     * @param aTTL
     *            Time to live in the waiting list (in seconds)
     * @param aAccess
     *            An access to one isolate (implies a call to sendTo())
     */
    public WaitingSignal(final String aSignalName, final Object aContent,
            final IWaitingSignalListener aListener, final ESendMode aMode,
            final long aTTL, final HostAccess aAccess) {

        pName = aSignalName;
        pContent = aContent;
        pListener = aListener;
        pMode = aMode;
        pTTL = aTTL;

        pAccess = aAccess;
        pBaseGroup = null;
        pGroups = null;
        pIsolates = null;
    }

    /**
     * Sets up the signal bean.
     * 
     * Only one of aAccess, aIsolates or aGroups must be given.
     * 
     * @param aSignalName
     *            Signal name
     * @param aContent
     *            Raw signal content
     * @param aListener
     *            The waiting signal listener
     * @param aMode
     *            The signal sending mode
     * @param aTTL
     *            Time to live in the waiting list (in seconds)
     * @param aIsolates
     *            Target isolates
     * @param aGroups
     *            Target groups
     */
    public WaitingSignal(final String aSignalName, final Object aContent,
            final IWaitingSignalListener aListener, final ESendMode aMode,
            final long aTTL, final String[] aIsolates, final String[] aGroups) {

        pName = aSignalName;
        pContent = aContent;
        pListener = aListener;
        pMode = aMode;
        pTTL = aTTL;

        pAccess = null;
        pBaseGroup = null;

        if (aGroups != null) {
            pGroups = aGroups;
            pIsolates = null;

        } else {
            pGroups = null;
            pIsolates = aIsolates;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#decreaseTTL(long)
     */
    @Override
    public boolean decreaseTTL(final long aDelta) {

        pTTL -= aDelta;
        return pTTL <= 0;
    }

    /**
     * Notifies the listener, if any, that the signal has been sent
     */
    public void fireSuccessEvent() {

        if (pListener != null) {
            pListener.waitingSignalSent(this);
        }
    }

    /**
     * Notifies the listener, if any, of a timeout
     */
    public void fireTimeoutEvent() {

        if (pListener != null) {
            pListener.waitingSignalTimeout(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getAccess()
     */
    @Override
    public HostAccess getAccess() {

        return pAccess;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getContent()
     */
    @Override
    public Object getContent() {

        return pContent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getFireResult()
     */
    @Override
    public String[] getFireResult() {

        return pFireResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getGroup()
     */
    @Override
    public EBaseGroup getGroup() {

        return pBaseGroup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getGroups()
     */
    @Override
    public String[] getGroups() {

        return pGroups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getIsolates()
     */
    @Override
    public String[] getIsolates() {

        return pIsolates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getMode()
     */
    @Override
    public ESendMode getMode() {

        return pMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getName()
     */
    @Override
    public String getName() {

        return pName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getPostResult()
     */
    @Override
    public Future<ISignalSendResult> getPostResult() {

        return pPostResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getSendResult()
     */
    @Override
    public ISignalSendResult getSendResult() {

        return pSendResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.signals.IWaitingSignal#getSendToResult()
     */
    @Override
    public Object[] getSendToResult() {

        return pSendToResult;
    }

    /**
     * @param aFireResult
     *            the fireResults to set
     */
    public void setFireResult(final String[] aFireResult) {

        pFireResult = aFireResult;
    }

    /**
     * @param aPostResult
     *            the postResult to set
     */
    public void setPostResult(final Future<ISignalSendResult> aPostResult) {

        pPostResult = aPostResult;
    }

    /**
     * @param aSendResult
     *            the sendResult to set
     */
    public void setSendResult(final ISignalSendResult aSendResult) {

        pSendResult = aSendResult;
    }

    /**
     * @param aSendToResult
     *            the sendToResult to set
     */
    public void setSendToResult(final Object[] aSendToResult) {

        pSendToResult = aSendToResult;
    }
}
