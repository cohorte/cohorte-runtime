/**
 * 
 */
package org.psem2m.isolates.base.boot;

import java.io.Serializable;

/**
 * Bean used for isolate information transmission between the bootstrap and the
 * isolate runner.
 * 
 * @author Thomas Calmant
 */
public class IsolateStatus implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The isolate agent has successfully prepared the isolate */
    public static final int STATE_AGENT_DONE = 10;

    /** Bundles are installed */
    public static final int STATE_BUNDLES_INSTALLED = 2;

    /** Bundles are started */
    public static final int STATE_BUNDLES_STARTED = 4;

    /** A failure occurred */
    public static final int STATE_FAILURE = -1;

    /** Framework is loaded */
    public static final int STATE_FRAMEWORK_LOADED = 1;

    /** Framework is started */
    public static final int STATE_FRAMEWORK_STARTED = 3;

    /** Bootstrap is reading the configuration. Framework is not yet loaded */
    public static final int STATE_READ_CONF = 0;

    /** Framework is stopped */
    public static final int STATE_STOPPED = 100;

    /** Framework is stopping */
    public static final int STATE_STOPPING = 99;

    /** The source isolate ID */
    private final String pIsolateId;

    /** Isolate start progress */
    private final double pProgress;

    /** Isolate state */
    private final int pState;

    /**
     * Sets up the bean
     * 
     * @param aIsolateId
     *            Source isolate ID
     * @param aState
     *            Isolate state
     * @param aProgress
     *            Isolate start completion
     */
    public IsolateStatus(final String aIsolateId, final int aState,
	    final double aProgress) {

	pIsolateId = aIsolateId;
	pState = aState;
	pProgress = aProgress;
    }

    /**
     * Retrieves the source isolate ID
     * 
     * @return The source isolate ID
     */
    public String getIsolateId() {
	return pIsolateId;
    }

    /**
     * Retrieves the stored isolate progress level
     * 
     * @return The isolate start progress level
     */
    public double getProgress() {
	return pProgress;
    }

    /**
     * Retrieves the stored isolate state
     * 
     * @return The isolate state
     */
    public int getState() {
	return pState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

	StringBuilder builder = new StringBuilder("IsolateStatus(");
	builder.append("isolate=").append(pIsolateId);
	builder.append(", state=").append(pState);
	builder.append(", progress=").append(pProgress);
	builder.append(")");

	return builder.toString();
    }
}
