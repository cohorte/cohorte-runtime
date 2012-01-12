/**
 * File:   IElementContainer.java
 * Author: Thomas Calmant
 * Date:   10 janv. 2012
 */
package org.psem2m.sca.converter.model;

import org.psem2m.sca.converter.core.QName;

/**
 * @author Thomas Calmant
 * 
 */
public interface IElementContainer {

    /**
     * Retrieves the first reference with the given name, null if not found
     * 
     * @param aReferenceName
     *            A reference name
     * @return The found reference, or null
     */
    Reference getReference(final QName aReferenceName);

    /**
     * Retrieves the first service with the given name, null if not found
     * 
     * @param aServiceName
     *            A service name
     * @return The found service, or null
     */
    public Service getService(final QName aServiceName);
}
