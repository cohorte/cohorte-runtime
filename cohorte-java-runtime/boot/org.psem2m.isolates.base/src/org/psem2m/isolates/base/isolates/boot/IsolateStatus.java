/**
 * 
 */
package org.psem2m.isolates.base.isolates.boot;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bean used for isolate information transmission between the bootstrap and the
 * isolate runner.
 * 
 * @author Thomas Calmant
 */
public class IsolateStatus implements Serializable {

    /**
     * Next status object Unique ID
     * 
     * System.currentTimeMillis() guarantees that we'll have a UID greater than
     * in previous executions.
     */
    private static AtomicLong pNextStatusUID = new AtomicLong(
            System.currentTimeMillis());

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The isolate agent has successfully prepared the isolate */
    public static final int STATE_AGENT_DONE = 10;

    /** Isolate slave agent is stopped, but the framework is still here */
    public static final int STATE_AGENT_STOPPED = 90;

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

    /** Framework is stopped */
    public static final int STATE_FRAMEWORK_STOPPED = 100;

    /** Framework is stopping */
    public static final int STATE_FRAMEWORK_STOPPING = 99;

    /** Bootstrap is reading the configuration. Framework is not yet loaded */
    public static final int STATE_READ_CONF = 0;

    /** The source isolate ID */
    private String pIsolateId;

    /** Isolate start progress */
    private double pProgress;

    /** Isolate state */
    private int pState;

    /** Status object Unique ID */
    private long pStatusUID;

    /** Time stamp (set in constructor) */
    private long pTimestamp;

    /**
     * Default constructor
     */
    public IsolateStatus() {

        // Do nothing
    }

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
        pTimestamp = System.currentTimeMillis();
        pStatusUID = pNextStatusUID.getAndIncrement();
    }

    /**
     * Adds the given name - value couple to the StringBuilder
     * 
     * @param aBuilder
     *            A String builder
     * @param aName
     *            Field name
     * @param aValue
     *            Field value
     */
    private void addJsonField(final StringBuilder aBuilder, final String aName,
            final Object aValue) {

        aBuilder.append(toJsonString(aName));
        aBuilder.append(":");
        aBuilder.append(toJsonString(aValue));
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

    /**
     * Retrieves the status UID
     * 
     * @return the status UID
     */
    public long getStatusUID() {

        return pStatusUID;
    }

    /**
     * Retrieves the status time stamp
     * 
     * @return the status time stamp
     */
    public long getTimestamp() {

        return pTimestamp;
    }

    /**
     * @param aIsolateId
     *            the isolateId to set
     */
    public void setIsolateId(final String aIsolateId) {

        pIsolateId = aIsolateId;
    }

    /**
     * @param aProgress
     *            the progress to set
     */
    public void setProgress(final double aProgress) {

        pProgress = aProgress;
    }

    /**
     * @param aState
     *            the state to set
     */
    public void setState(final int aState) {

        pState = aState;
    }

    /**
     * @param aStatusUID
     *            the statusUID to set
     */
    public void setStatusUID(final long aStatusUID) {

        pStatusUID = aStatusUID;
    }

    /**
     * @param aTimestamp
     *            the timestamp to set
     */
    public void setTimestamp(final long aTimestamp) {

        pTimestamp = aTimestamp;
    }

    /**
     * Converts the given object to a JSON value (escapes quotes)
     * 
     * @param aValue
     *            An object
     * @return The JSON String corresponding to the value
     */
    private String toJsonString(final Object aValue) {

        final String strValue = String.valueOf(aValue);

        final StringBuilder builder = new StringBuilder(strValue.length());
        builder.append('"');

        builder.append(strValue.replace("\"", "\\\""));

        builder.append('"');
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("{");

        addJsonField(builder, "type", "IsolateStatus");
        builder.append(", ");
        addJsonField(builder, "UID", pStatusUID);
        builder.append(", ");
        addJsonField(builder, "state", pState);
        builder.append(", ");
        addJsonField(builder, "progress", pProgress);
        builder.append(", ");
        addJsonField(builder, "timestamp", pTimestamp);
        builder.append("}");

        return builder.toString();
    }
}
