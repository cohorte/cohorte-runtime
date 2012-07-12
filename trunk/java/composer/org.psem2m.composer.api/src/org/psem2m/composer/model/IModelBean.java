/**
 * File:   IModelBean.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.model;

import org.psem2m.composer.EComponentState;

/**
 * Defines the common methods of Composer model beans
 * 
 * @author Thomas Calmant
 */
public interface IModelBean {

    /**
     * Updates the bean name by prefixing it with the name of its parent
     */
    void computeName();

    /**
     * Retrieves the name of the bean
     * 
     * @return the name of the bean
     */
    String getName();

    /**
     * Retrieves the name of the parent of the bean. Returns null on the root.
     * 
     * @return the name of the parent of the bean
     */
    String getParentName();

    /**
     * Retrieves the name of the root component set in the hierarchy, i.e. the
     * name of the whole component set
     * 
     * @return the name of the component set
     */
    String getRootName();

    /**
     * Retrieves the state of the model bean
     * 
     * @return The state of the model bean
     */
    EComponentState getState();

    /**
     * Recursively asks the beans to link internal wires
     * 
     * @param aCallingParent
     *            The component set asking for wires linking
     * 
     * @return True if all wires are linked
     */
    boolean linkWires(ComponentsSetBean aCallingParent);

    /**
     * Sets the bean name
     * 
     * @param aName
     *            the bean name
     */
    void setName(String aName);

    /**
     * Sets the name of the parent of the bean
     * 
     * @param aParentName
     *            the name of the parent of the bean
     */
    void setParentName(String aParentName);

    /**
     * Sets the name of the root component set in the hierarchy, i.e. the name
     * of the whole component set
     * 
     * @param aRootName
     *            the name of the component set
     */
    void setRootName(final String aRootName);
}
