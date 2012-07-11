/**
 * File:   RemoteServiceEvent.java
 * Author: Thomas Calmant
 * Date:   26 juil. 2011
 */
package org.psem2m.isolates.services.remote.beans;

import java.io.Serializable;

/**
 * Represents a remote service event
 * 
 * @author Thomas Calmant
 */
public class RemoteServiceEvent implements Serializable {

    /**
     * Kind of service event
     * 
     * @author Thomas Calmant
     */
    public enum ServiceEventType {

        /** The service properties have been modified */
        MODIFIED,

        /** The service have just been registered */
        REGISTERED,

        /** The service have just been unregistered */
        UNREGISTERED,
    }

    /** The serial version UID */
    private static final long serialVersionUID = 1L;

    /** Service event type */
    private ServiceEventType pEventType;

    /** Remote service registration (with end points) */
    private RemoteServiceRegistration pServiceRegistration;

    /**
     * Default constructor
     */
    public RemoteServiceEvent() {

        // Do nothing
    }

    /**
     * Sets up the service event
     * 
     * @param aEventType
     *            Kind of event
     * @param aRegistration
     *            Service registration associated to the event
     */
    public RemoteServiceEvent(final ServiceEventType aEventType,
            final RemoteServiceRegistration aRegistration) {

        pEventType = aEventType;
        pServiceRegistration = aRegistration;
    }

    /**
     * Retrieves the kind of event
     * 
     * @return the kind of event
     */
    public ServiceEventType getEventType() {

        return pEventType;
    }

    /**
     * Retrieves the service registration description associated to the event
     * 
     * @return the service registration description
     */
    public RemoteServiceRegistration getServiceRegistration() {

        return pServiceRegistration;
    }

    /**
     * @param aEventType
     *            the eventType to set
     */
    public void setEventType(final ServiceEventType aEventType) {

        pEventType = aEventType;
    }

    /**
     * @param aServiceRegistration
     *            the serviceRegistration to set
     */
    public void setServiceRegistration(
            final RemoteServiceRegistration aServiceRegistration) {

        pServiceRegistration = aServiceRegistration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("RemoteServiceEvent(");
        builder.append("Event=").append(pEventType);
        builder.append(", Reg=").append(pServiceRegistration);
        builder.append(")");

        return builder.toString();
    }
}
