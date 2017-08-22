/**
 * File:   EmbeddedResourceResolver.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.core.resolver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.psem2m.sca.converter.core.SCAConstants;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * An LSResourceResolver that looks into the local JAR file to find schemas, if
 * possible
 * 
 * @author Thomas Calmant
 */
public class EmbeddedResourceResolver implements LSResourceResolver {

    /** The current class loader */
    private ClassLoader pLoader;

    /** Known name spaces files */
    private final Map<String, String> pNamespaces;

    /**
     * Default constructor
     */
    public EmbeddedResourceResolver() {

        pLoader = getClass().getClassLoader();

        pNamespaces = new HashMap<String, String>();
        pNamespaces.put(SCAConstants.SCA_NS, "sca-1.1-cd06.xsd");
        pNamespaces.put(SCAConstants.PSEM2M_NS, "psem2m-sca.xsd");
        pNamespaces.put("http://tuscany.apache.org/xmlns/sca/1.1",
                "tuscany-sca-1.1.xsd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public LSInput resolveResource(final String aType,
            final String aNamespaceURI, final String aPublicId,
            final String aSystemId, final String aBaseURI) {

        final String systemId;

        if (aSystemId != null) {
            // Given name space
            systemId = aSystemId;

        } else if (pNamespaces.containsKey(aNamespaceURI)) {
            // Known name space
            systemId = pNamespaces.get(aNamespaceURI);

        } else {
            // Unknown name space
            System.err.println("Unknown XML NS : " + aNamespaceURI);
            return null;
        }

        // Extract the file name from the URI
        final StringBuilder fileName = new StringBuilder();

        // Try to get the schema
        fileName.append(EmbeddedResourceResolver.class.getPackage().getName()
                .replace('.', '/'));
        fileName.append("/schemas/");

        final int lastSlash = systemId.lastIndexOf('/') + 1;
        if (lastSlash == 0) {
            fileName.append(systemId);

        } else {
            fileName.append(systemId.substring(lastSlash));
        }

        InputStream schemaStream = pLoader.getResourceAsStream(fileName
                .toString());

        if (schemaStream == null) {
            // Not found in JAR
            try {
                // .. in the working directory
                schemaStream = new FileInputStream(fileName.toString());

            } catch (final FileNotFoundException e) {
                // Not found
            }
        }

        if (schemaStream != null) {
            // Schema found
            return new StreamInput(aPublicId, aSystemId, aBaseURI, schemaStream);
        }

        return null;
    }
}
