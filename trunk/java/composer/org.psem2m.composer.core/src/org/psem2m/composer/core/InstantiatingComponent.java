/**
 * File:   InstantiatingComponent.java
 * Author: Thomas Calmant
 * Date:   29 nov. 2011
 */
package org.psem2m.composer.core;

import org.psem2m.composer.model.ComponentBean;

/**
 * Represents an instantiating component
 * 
 * @author Thomas Calmant
 */
public class InstantiatingComponent {

    /** The instantiating component */
    private final ComponentBean pComponent;

    /** The host of the given component */
    private String pHostIsolate;

    /** The time stamp of the instantiation signal */
    private long pRequestTimeStamp;

    /**
     * Sets up the instantiating component
     * 
     * @param aComponentBean
     *            The instantiating component
     */
    public InstantiatingComponent(final ComponentBean aComponentBean) {

        pComponent = aComponentBean;
        pRequestTimeStamp = -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (aObj instanceof CharSequence) {
            // A bean name
            return pComponent.getName().equals(aObj);
        }

        if (aObj instanceof ComponentBean) {
            // A bean
            return pComponent.equals(aObj);
        }

        if (aObj instanceof InstantiatingComponent) {
            // Only test on component
            return pComponent
                    .equals(((InstantiatingComponent) aObj).pComponent);
        }

        // Invalid type
        return false;
    }

    /**
     * Retrieves the underlying component bean
     * 
     * @return The underlying component bean
     */
    public ComponentBean getComponent() {

        return pComponent;
    }

    /**
     * Retrieves the ID of the component host. Can return null.
     * 
     * @return The ID of the component host
     */
    public String getIsolate() {

        return pHostIsolate;
    }

    /**
     * Returns the time stamp of the last call to {@link #requestSent()}, in
     * milliseconds.
     * 
     * @return The last request time stamp (in milliseconds)
     */
    public long getRequestTimeStamp() {

        return pRequestTimeStamp;
    }

    /**
     * Resets the request time stamp, using {@link System#currentTimeMillis()}
     */
    public void requestSent() {

        pRequestTimeStamp = System.currentTimeMillis();
    }

    /**
     * Sets the ID of the component host.
     * 
     * @param aIsolate
     *            The ID of the component host (can be null)
     */
    public void setIsolate(final String aIsolate) {

        pHostIsolate = aIsolate;
    }
}
