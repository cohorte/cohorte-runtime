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
import java.util.Properties;

import org.psem2m.composer.ComposerAgentConstants;
import org.psem2m.composer.IpojoConstants;

/**
 * PSEM2M Composer component description bean
 * 
 * @author Thomas Calmant
 */
public class ComponentBean implements Serializable {

    /** Version UID */
    private static final long serialVersionUID = 1L;

    /** The name of the parent composite */
    private String pCompositeName;

    /** Forced "@Requires" field filters */
    private final Map<String, String> pFieldForcedFilters = new HashMap<String, String>();

    /** The host isolate ID */
    private String pIsolate;

    /** The component name */
    private String pName;

    /** The component type */
    private String pType;

    /**
     * Default constructor
     */
    public ComponentBean() {

        // Does nothing...
    }

    /**
     * Generates the iPOJO instance properties of the component.
     * 
     * Sets up the instance name and required fields filters properties
     * 
     * @param aAllFieldIdMapping
     *            A "field <b>name</b>" -&gt; "field <b>ID</b>" map, keys
     *            represents all fields in the component, values (can be null)
     *            their ID if useful
     * 
     * @return The base component properties
     */
    public Properties generateProperties(
            final Map<String, String> aAllFieldIdMapping) {

        final Properties properties = new Properties();

        // Instance name
        properties.put(IpojoConstants.INSTANCE_NAME, pName);

        if (aAllFieldIdMapping != null) {

            // Set requires.filter property
            final Properties requiresFilterProperties = new Properties();
            properties.put(IpojoConstants.REQUIRES_FILTERS,
                    requiresFilterProperties);

            for (final Entry<String, String> pFieldIdEntry : aAllFieldIdMapping
                    .entrySet()) {

                // Field name is constant
                final String fieldName = pFieldIdEntry.getKey();

                // Use the field ID if possible, else the field name
                String fieldId = pFieldIdEntry.getValue();
                if (fieldId == null) {
                    fieldId = fieldName;
                }

                // Compute the field filter
                String filter = null;

                if (pFieldForcedFilters.containsKey(fieldName)) {
                    // Field name found
                    filter = pFieldForcedFilters.get(fieldName);

                } else if (pFieldForcedFilters.containsKey(fieldId)) {
                    // Field ID found
                    filter = pFieldForcedFilters.get(fieldId);

                } else {
                    // Default : filter on the composite name
                    final StringBuilder builder = new StringBuilder();

                    builder.append("(");
                    builder.append(ComposerAgentConstants.COMPOSITE_NAME);
                    builder.append("=");
                    builder.append(pCompositeName);
                    builder.append(")");

                    filter = builder.toString();
                }

                if (filter != null) {
                    // Trim the filter for the next test
                    filter = filter.trim();

                    if (!filter.isEmpty()) {
                        // Non-empty filter, ready to be used
                        requiresFilterProperties.put(fieldId, filter);
                    }
                }
            }
        }

        return properties;
    }

    /**
     * Retrieves the name of the parent composite
     * 
     * @return the name of the parent composite
     */
    public String getCompositeName() {

        return pCompositeName;
    }

    /**
     * Retrieves the forced fields filters map
     * 
     * @return the forced fields filters map
     */
    public Map<String, String> getFieldsFilters() {

        return pFieldForcedFilters;
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
     * Retrieves the component instance name
     * 
     * @return the component instance name
     */
    public String getName() {

        return pName;
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
     * Sets the parent composite name
     * 
     * @param aParentCompositeName
     *            the name of the parent composite
     */
    public void setCompositeName(final String aParentCompositeName) {

        pCompositeName = aParentCompositeName;
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

        pFieldForcedFilters.put(aField.trim(), aFilter);
    }

    /**
     * Sets the forced fields filters map
     * 
     * @param aFilters
     *            the forced fields filters map
     */
    public void setFieldsFilters(final Map<String, String> aFilters) {

        pFieldForcedFilters.clear();

        if (aFilters != null) {
            pFieldForcedFilters.putAll(aFilters);
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
     * Sets the component instance name
     * 
     * @param aName
     *            the instance name
     */
    public void setName(final String aName) {

        pName = aName;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("Component(");
        builder.append("Name=").append(pName);
        builder.append(", Type=").append(pType);
        builder.append(", Isolate=").append(pIsolate);
        builder.append(", Composite=").append(pCompositeName);
        builder.append(")");

        return builder.toString();
    }
}
