/**
 * File:   InterfacePrefixUtils.java
 * Author: Thomas Calmant
 * Date:   23 mai 2013
 */
package org.psem2m.isolates.services.remote;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class to handle interface prefixes.
 * 
 * Prefixed interface names have the following format: language:/name
 * 
 * Use only one slash (/) character, so that the name can be parsed as URI path
 * instead of a URI host name. This allows to use a larger set of characters in
 * the interface name, e.g. the underscore (_).
 * 
 * @author Thomas Calmant
 */
public class InterfacePrefixUtils {

    /** The Java scheme string */
    private static final String JAVA_SCHEME = "java";

    /**
     * Escapes the interface string: replaces slashes '/' by '%2F'
     * 
     * @param aInterfaceName
     *            An interface name
     * @return The escaped named
     */
    private String escape(final String aInterfaceName) {

        return aInterfaceName.replace("/", "%2F");
    }

    /**
     * Extracts the Java interfaces from the given formatted names
     * 
     * @param aInterfaces
     *            Formatted names of interfaces
     * @return All Java interfaces found in the names
     */
    public Set<String> extractInterfaces(final Collection<String> aInterfaces) {

        return extractInterfaces(aInterfaces, JAVA_SCHEME);
    }

    /**
     * Extracts the interfaces from the given formatted names corresponding to
     * the given language
     * 
     * @param aInterfaces
     *            Formatted names of interfaces
     * @param aLanguage
     *            Name of an implementation language
     * @return All interfaces found in the names corresponding to the given
     *         language
     */
    public Set<String> extractInterfaces(final Collection<String> aInterfaces,
            final String aLanguage) {

        final Set<String> languageInterfaces = new LinkedHashSet<String>();

        for (final String interfaceName : aInterfaces) {
            // Extract the parts
            final String[] parts = extractParts(interfaceName);
            if (parts != null && parts[0].equals(aLanguage)) {
                // Interface language match: just keep the interface name
                languageInterfaces.add(parts[1]);
            }
        }

        return languageInterfaces;
    }

    /**
     * Extracts the Java interfaces from the given formatted names
     * 
     * @param aInterfaces
     *            Formatted names of interfaces
     * @return All Java interfaces found in the names
     */
    public Set<String> extractInterfaces(final String[] aInterfaces) {

        return extractInterfaces(Arrays.asList(aInterfaces), JAVA_SCHEME);
    }

    /**
     * Extract the language and the interface from a "language:/interface"
     * interface name
     * 
     * @param aInterfaceName
     *            The formatted interface name
     * @return An array containing the language (0) and the interface name (1)
     */
    private String[] extractParts(final String aInterfaceName) {

        // Parse the name as a URI
        final URI interfaceParts;
        try {
            interfaceParts = new URI(aInterfaceName);

        } catch (final URISyntaxException ex) {
            // Should not happen, but who knows...
            return null;
        }

        // Extract the interface name
        String interfaceName = interfaceParts.getPath();

        // Extract the language (if given)
        String language = interfaceParts.getScheme();
        if (language == null) {
            // Simple name. Without scheme, the URI contains just a path
            language = JAVA_SCHEME;

        } else {
            // Formatted name: un-escape it, without the starting '/'
            interfaceName = unescape(interfaceName.substring(1));
        }

        return new String[] { language, interfaceName };
    }

    /**
     * Formats a "language://interface" string
     * 
     * @param aLanguage
     *            Interface language
     * @param aInterfaceName
     *            Interface name
     * @return A formatted string
     */
    private String formatName(final String aLanguage,
            final String aInterfaceName) {

        // Escape the slashes (/) in the interface name
        final String escaped = escape(aInterfaceName);

        try {
            // Add a starting '/' to have an absolute path
            return new URI(aLanguage, null, "/" + escaped, null).toString();

        } catch (final URISyntaxException ex) {
            // Should never happen
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Transforms the interfaces names into a URI string, with the interface
     * implementation language as a scheme.
     * 
     * @param aInterfaces
     *            Interface names to transform
     * @return The transformed names
     */
    public Set<String> formatNames(final Set<String> aInterfaces) {

        final Set<String> prefixed = new LinkedHashSet<String>();

        for (final String interfaceName : aInterfaces) {
            // Parse the name
            final String[] parts = extractParts(interfaceName);
            if (parts != null) {
                // (Re-)Format the name
                final String formatted = formatName(parts[0], parts[1]);
                if (formatted != null) {
                    prefixed.add(formatted);
                }
            }
        }

        return prefixed;
    }

    /**
     * Unescapes the interface string: replaces '%2F' by slashes '/'
     * 
     * @param aInterfaceName
     *            An interface name
     * @return The escaped named
     */
    private String unescape(final String aInterfaceName) {

        return aInterfaceName.replace("%2F", "/");
    }
}
