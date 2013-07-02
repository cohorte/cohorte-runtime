/**
 * File:   PelixMulticastPacket.java
 * Author: Thomas Calmant
 * Date:   28 juin 2013
 */
package org.cohorte.remote.multicast.beans;

import java.util.Map;

import org.cohorte.remote.multicast.IPacketConstants;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the content of an end point event packet sent over multicast by
 * Pelix remote services.
 * 
 * @author Thomas Calmant
 */
public class PelixMulticastPacket {

    /** Access to the dispatcher servlet */
    private final String pAccessPath;

    /** Port to access the dispatcher servlet */
    private final int pAccessPort;

    /** Kind of event */
    private final String pEvent;

    /** New properties (in case of an update event) */
    private final Map<String, Object> pNewProperties;

    /** Raw JSON object */
    private final JSONObject pRaw;

    /** Sender UID */
    private final String pSender;

    /** End point UID */
    private final String pUID;

    /**
     * Constructs the end point according to the JSON object
     * 
     * @param aJsonObject
     *            A JSON object
     * @throws JSONException
     *             Error parsing the JSON object
     */
    public PelixMulticastPacket(final JSONObject aJsonObject)
            throws JSONException {

        // Keep a reference to the raw object
        pRaw = aJsonObject;

        // Basic
        pSender = aJsonObject.getString(IPacketConstants.KEY_SENDER);
        pEvent = aJsonObject.getString(IPacketConstants.KEY_EVENT);
        pUID = aJsonObject.getString(IPacketConstants.KEY_ENDPOINT_UID);

        // Access
        final JSONObject access = aJsonObject
                .getJSONObject(IPacketConstants.KEY_ACCESS);
        pAccessPath = access.getString(IPacketConstants.KEY_ACCESS_PATH);
        pAccessPort = access.getInt(IPacketConstants.KEY_ACCESS_PORT);

        // Extra properties
        pNewProperties = ParseUtils.jsonToMap(aJsonObject
                .optJSONObject(IPacketConstants.KEY_ENDPOINT_NEW_PROPERTIES));
    }

    /**
     * @return the accessPath
     */
    public String getAccessPath() {

        return pAccessPath;
    }

    /**
     * @return the accessPort
     */
    public int getAccessPort() {

        return pAccessPort;
    }

    /**
     * @return the event
     */
    public String getEvent() {

        return pEvent;
    }

    /**
     * @return the new end point properties (in case of update)
     */
    public Map<String, Object> getNewProperties() {

        return pNewProperties;
    }

    /**
     * @return the raw JSON object
     */
    public JSONObject getRaw() {

        return pRaw;
    }

    /**
     * @return the sender
     */
    public String getSender() {

        return pSender;
    }

    /**
     * @return the end point UID
     */
    public String getUID() {

        return pUID;
    }

    /**
     * Tests if this packet comes from the given sender
     * 
     * @param aSender
     *            The UID of a sender
     * @return True if this packet comes from sender
     */
    public boolean isFromSender(final String aSender) {

        return pSender.equals(aSender);
    }
}
