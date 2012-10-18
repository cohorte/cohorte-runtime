/**
 * File:   ComponentBean.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.psem2m.composer.EComponentState;

/**
 * PSEM2M Composer component description bean
 * 
 * @author Thomas Calmant
 */
public class ComponentBean extends AbstractModelBean implements Serializable,
        Comparable<ComponentBean> {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** Forced "@Requires" field filters */
    private final Map<String, String> pFieldFilters = new HashMap<String, String>();

    /** The host isolate ID */
    private String pIsolate;

    /** Flag indicating the bean name has already been computed */
    private boolean pNameComputed = false;

    /** Component properties */
    private final Map<String, String> pProperties = new HashMap<String, String>();

    /** Component state */
    private EComponentState pState;

    /** The component type */
    private String pType;

    /** Component wires */
    private final Map<String, String> pWires = new HashMap<String, String>();

    /**
     * Default constructor
     */
    public ComponentBean() {

        // Does nothing...
        super();
        pState = EComponentState.WAITING;
    }

    /**
     * Copy constructor
     * 
     * @param aComponentBean
     *            Component to copy
     */
    public ComponentBean(final ComponentBean aComponentBean) {

        super(aComponentBean);

        // Make a copy of all members
        pFieldFilters.putAll(aComponentBean.pFieldFilters);
        pIsolate = aComponentBean.pIsolate;
        pNameComputed = aComponentBean.pNameComputed;
        pProperties.putAll(aComponentBean.pProperties);
        pState = aComponentBean.pState;
        pType = aComponentBean.pType;
        pWires.putAll(aComponentBean.pWires);
    }

    /**
     * Component comparison
     * 
     * A component is greater than null. Components are compared by name and by
     * type.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public final int compareTo(final ComponentBean aOther) {

        if (aOther == null) {
            // We're greater than null
            return 1;
        }

        if (equals(aOther)) {
            // Same object
            return 0;

        } else {
            // Different object, use name ordering
            final int nameDiff = safeCompareTo(getName(), aOther.getName());
            if (nameDiff == 0) {
                // WARNING: different beans with same name (should never happen)
                return safeCompareTo(pType, aOther.pType);
            }

            return nameDiff;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.model.IModelBean#computeName()
     */
    @Override
    public void computeName() {

        if (pNameComputed) {
            // Name already computed
            return;
        }

        final String parentName = getParentName();
        if (getParentName() != null) {
            // Compute the new name
            final StringBuilder builder = new StringBuilder();
            builder.append(parentName);
            builder.append(".");
            builder.append(getName());

            setName(builder.toString());

            // Update the computation flag
            pNameComputed = true;
        }
    }

    /**
     * Component beans are equals if they have the same name and the same type
     * 
     * @see org.psem2m.composer.model.AbstractModelBean#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (aObj instanceof ComponentBean) {

            if (!super.equals(aObj)) {
                // Name equality failed
                return false;
            }

            // Type equality
            final ComponentBean other = (ComponentBean) aObj;
            return safeEquals(pType, other.pType);
        }

        return false;
    }

    /**
     * Escapes special characters from the given LDAP filter
     * 
     * Inspired from <a href=
     * "https://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java"
     * >Preventing LDAP Injection in Java</a>
     * 
     * @param aFilter
     *            LDAP filter to escape
     * @return The filtered LDAP filter
     */
    public final String escapeLDAPSearchFilter(final String aFilter) {

        // Prepare the string builder
        final StringBuilder builder = new StringBuilder(aFilter.length());

        for (final char curChar : aFilter.toCharArray()) {

            switch (curChar) {
            case '\\':
                builder.append("\\5c");
                break;

            case '*':
                builder.append("\\2a");
                break;

            case '(':
                builder.append("\\28");
                break;

            case ')':
                builder.append("\\29");
                break;

            case '\u0000':
                builder.append("\\00");
                break;

            default:
                builder.append(curChar);
            }
        }

        return builder.toString();
    }

    /**
     * Retrieves the forced fields filters map
     * 
     * @return the forced fields filters map
     */
    public Map<String, String> getFieldsFilters() {

        return pFieldFilters;
    }

    /**
     * Retrieves the component host isolate
     * 
     * @return the host isolate
     */
    public String getIsolate() {

        return pIsolate;
    }

    /**
     * Retrieves the component properties map
     * 
     * @return the component properties map
     */
    public Map<String, String> getProperties() {

        return pProperties;
    }

    /**
     * Retrieves the component state
     * 
     * @return the component state
     */
    @Override
    public EComponentState getState() {

        return pState;
    }

    /**
     * Retrieves the component type name
     * 
     * @return the component type name
     */
    public String getType() {

        return pType;
    }

    /**
     * Retrieves the component wires
     * 
     * @return the component wires
     */
    public Map<String, String> getWires() {

        return pWires;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.model.IModelBean#linkWires(org.psem2m.composer.model
     * .ComponentsSetBean)
     */
    @Override
    public boolean linkWires(final ComponentsSetBean aCallingParent) {

        // Be optimistic
        boolean result = true;

        for (final Entry<String, String> wire : pWires.entrySet()) {

            final String fieldName = wire.getKey();
            final String componentName = wire.getValue();

            final ComponentBean targetComponent = aCallingParent
                    .findComponent(componentName);
            if (targetComponent != null) {
                // Prepare the field filter
                final StringBuilder builder = new StringBuilder();
                builder.append("(instance.name=");

                // Escape the component name to avoid LDAP injection...
                builder.append(escapeLDAPSearchFilter(targetComponent.getName()));
                builder.append(")");

                pFieldFilters.put(fieldName, builder.toString());

            } else {
                // Wire can't be linked, fail
                result = false;
            }
        }

        return result;
    }

    /**
     * Sets the selection filter for the given field
     * 
     * @param aField
     *            A field name
     * @param aFilter
     *            A LDAP OSGi service filter (can be null)
     */
    public void setFieldFilter(final String aField, final String aFilter) {

        if (aField == null || aField.trim().isEmpty()) {
            // Ignore invalid field names
            return;
        }

        pFieldFilters.put(aField.trim(), aFilter);
    }

    /**
     * Sets the forced fields filters map
     * 
     * @param aFilters
     *            the forced fields filters map
     */
    public void setFieldsFilters(final Map<String, String> aFilters) {

        pFieldFilters.clear();

        if (aFilters != null) {
            pFieldFilters.putAll(aFilters);
        }
    }

    /**
     * Sets the host isolate
     * 
     * @param aIsolate
     *            the host isolate
     */
    public void setIsolate(final String aIsolate) {

        pIsolate = aIsolate;
    }

    /**
     * Sets the new component properties
     * 
     * @param aProperties
     *            New component properties
     */
    public void setProperties(final Map<String, String> aProperties) {

        pProperties.clear();

        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }
    }

    /**
     * Sets the component state
     * 
     * @param aState
     *            the new component state
     */
    public void setState(final EComponentState aState) {

        if (aState != null) {
            pState = aState;
        }
    }

    /**
     * Sets the component type name
     * 
     * @param aType
     *            the component type name
     */
    public void setType(final String aType) {

        pType = aType;
    }

    /**
     * Sets a component field wire
     * 
     * @param aFieldName
     *            A source field name
     * @param aWireTarget
     *            A wire target
     */
    public void setWire(final String aFieldName, final String aWireTarget) {

        if (aFieldName != null && aWireTarget != null) {
            pWires.put(aFieldName, aWireTarget);
        }
    }

    /**
     * Sets the component wires
     * 
     * @param aWires
     *            the component wires
     */
    public void setWires(final Map<String, String> aWires) {

        pWires.clear();

        if (aWires != null) {
            pWires.putAll(aWires);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("Component(");
        builder.append("Name=").append(getName());
        builder.append(", Type=").append(pType);
        builder.append(", Isolate=").append(pIsolate);
        builder.append(", Parent=").append(getParentName());
        builder.append(")");

        return builder.toString();
    }
}
