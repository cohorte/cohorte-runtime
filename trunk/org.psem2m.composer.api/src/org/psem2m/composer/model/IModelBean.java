/**
 * File:   IModelBean.java
 * Author: Thomas Calmant
 * Date:   3 nov. 2011
 */
package org.psem2m.composer.model;

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
}
