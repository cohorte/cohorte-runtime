/**
 * File:   StoredEvent.java
 * Author: Thomas Calmant
 * Date:   23 nov. 2011
 */
package org.psem2m.composer.core;

import org.psem2m.composer.CompositionEvent;
import org.psem2m.composer.EComponentState;
import org.psem2m.composer.model.ComponentBean;

/**
 * Stores a logged event
 * 
 * @author Thomas Calmant
 */
public class StoredEvent {

    /**
     * Represents the kind of the logged event
     * 
     * @author Thomas Calmant
     */
    public enum ELoggedEventType {

        /** Component event */
        COMPONENT_EVENT,

        /** Composition event */
        COMPOSITION_EVENT,
    }

    /** The changed component */
    private ComponentBean pComponent;

    /** The stored composition event */
    private CompositionEvent pCompositionEvent;

    /** The new component state */
    private EComponentState pState;

    /** The logged event type */
    private final ELoggedEventType pType;

    /**
     * Sets up a "component changed" event
     * 
     * @param aComponent
     *            The changed component
     * @param aState
     *            Its new state
     */
    public StoredEvent(final ComponentBean aComponent,
            final EComponentState aState) {

        pType = ELoggedEventType.COMPONENT_EVENT;
        pComponent = aComponent;
        pState = aState;
    }

    /**
     * Sets up a logged composition event
     * 
     * @param aCompositionEvent
     *            A composition event
     */
    public StoredEvent(final CompositionEvent aCompositionEvent) {

        pType = ELoggedEventType.COMPOSITION_EVENT;
        pCompositionEvent = aCompositionEvent;
    }

    /**
     * Retrieves the changed component. Valid for
     * {@link ELoggedEventType#COMPONENT_EVENT}.
     * 
     * @return the changed component
     */
    public ComponentBean getComponent() {

        return pComponent;
    }

    /**
     * Retrieves the stored composition event. Valid for
     * {@link ELoggedEventType#COMPOSITION_EVENT}
     * 
     * @return the stored composition event
     */
    public CompositionEvent getCompositionEvent() {

        return pCompositionEvent;
    }

    /**
     * Retrieves the new component state. Valid for
     * {@link ELoggedEventType#COMPONENT_EVENT}.
     * 
     * @return the new component state
     */
    public EComponentState getState() {

        return pState;
    }

    /**
     * Retrieves the logged event type
     * 
     * @return the event type
     */
    public ELoggedEventType getType() {

        return pType;
    }
}
