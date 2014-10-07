/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cohorte.shell.osgi;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Implementations of Gogo shell commands to simplify OSGi debugging
 *
 * @author Thomas Calmant
 */
public class OsgiCommands {

    /** The bundle context */
    private final BundleContext pContext;

    /**
     * Sets up the members
     *
     * @param aContext
     *            The bundle context
     */
    public OsgiCommands(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * Prints the details of the given service reference
     *
     * @param aServiceReference
     *            A service reference
     */
    private void printReference(final ServiceReference<?> aServiceReference) {

        // Extract main properties
        final Long svcId = (Long) aServiceReference
                .getProperty(Constants.SERVICE_ID);
        final String[] specs = (String[]) aServiceReference
                .getProperty(Constants.OBJECTCLASS);
        final Bundle bundle = aServiceReference.getBundle();

        // Print'em
        System.out.println("Service ID: " + svcId);
        System.out.println("Bundle: " + bundle.getSymbolicName() + " ("
                + bundle.getBundleId() + ")");
        System.out.println("Specifications:");
        for (final String spec : specs) {
            System.out.println("\t* " + spec);
        }

        // Print other properties
        System.out.println("Properties:");
        for (final String key : aServiceReference.getPropertyKeys()) {
            System.out.println("\t* " + key);

            // Convert value to a string
            String strValue;
            String strClass;
            final Object rawValue = aServiceReference.getProperty(key);
            if (rawValue == null) {
                // No value
                strClass = "(null)";
                strValue = "null";

            } else {
                // Keep class name
                strClass = rawValue.getClass().getName();
                if (rawValue.getClass().isArray()) {
                    // Convert the array to an array of Object
                    Object[] rawArray;
                    if (rawValue.getClass().getComponentType().isPrimitive()) {
                        // Primitive can't be cast to Object
                        final int length = Array.getLength(rawValue);
                        rawArray = new Object[length];
                        for (int i = 0; i < length; i++) {
                            rawArray[i] = Array.get(rawValue, i);
                        }

                    } else {
                        // Nothing to do
                        rawArray = (Object[]) rawValue;
                    }

                    // Convert the array to strings
                    strValue = Arrays.deepToString(rawArray);

                } else {
                    // Get the string value as is
                    strValue = rawValue.toString();
                }
            }

            System.out.println("\t\t-> " + strValue + " (" + strClass + ")");
        }

        // Service usage
        System.out.println("Service used by:");
        for (final Bundle usingBundle : aServiceReference.getUsingBundles()) {
            System.out.println("\t* " + usingBundle.getSymbolicName() + " ("
                    + usingBundle.getBundleId() + ")");
        }
    }

    /**
     * Prints the ID and the specifications of the given service references on
     * the standard output
     *
     * @param aServiceReferences
     *            Some service references
     */
    private void printReferences(final ServiceReference<?>[] aServiceReferences) {

        // Sort the array
        Arrays.sort(aServiceReferences, new Comparator<ServiceReference<?>>() {

            /*
             * (non-Javadoc)
             *
             * @see java.util.Comparator#compare(java.lang.Object,
             * java.lang.Object)
             */
            @Override
            public int compare(final ServiceReference<?> aReference,
                    final ServiceReference<?> aOther) {

                final Long svcId1 = (Long) aReference
                        .getProperty(Constants.SERVICE_ID);
                final Long svcId2 = (Long) aOther
                        .getProperty(Constants.SERVICE_ID);

                return svcId1.compareTo(svcId2);
            }
        });

        // Print details
        for (final ServiceReference<?> reference : aServiceReferences) {

            // Extract properties
            final Long svcId = (Long) reference
                    .getProperty(Constants.SERVICE_ID);
            final String[] specs = (String[]) reference
                    .getProperty(Constants.OBJECTCLASS);
            final Bundle bundle = reference.getBundle();

            // Print the result
            System.out.println(String.format("- Service %4d from %s (%d) - %s",
                    svcId, bundle.getSymbolicName(), bundle.getBundleId(),
                    Arrays.asList(specs)));
        }
    }

    /**
     * Prints the references that matches the given specification
     *
     * @param aSpecification
     *            A service specification
     */
    public void providers(final String aSpecification) {

        // Get the references
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = pContext.getAllServiceReferences(
                    aSpecification, null);

        } catch (final InvalidSyntaxException ex) {
            System.err.println("Error retrieving services: " + ex);
            return;
        }

        if (serviceReferences == null || serviceReferences.length == 0) {
            System.out.println("No matching service found");
            return;
        }

        // Print'em
        printReferences(serviceReferences);
    }

    /**
     * Prints the details of the given service
     */
    public void service(final int aServiceId) {

        // Get the matching references
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = pContext.getServiceReferences((String) null,
                    "(" + Constants.SERVICE_ID + "=" + aServiceId + ")");

        } catch (final InvalidSyntaxException ex) {
            System.err.println("Error retrieving services: " + ex);
            return;
        }

        // Check results
        if (serviceReferences == null || serviceReferences.length == 0) {
            System.out.println("Unknown service");
            return;

        } else if (serviceReferences.length > 1) {
            System.out.println("WARNING: too many references found, "
                    + "only the first one will be printed");
        }

        // Print details
        printReference(serviceReferences[0]);
    }

    /**
     * Prints all registered services
     */
    public void services() {

        // Get the references
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = pContext.getAllServiceReferences(null, null);

        } catch (final InvalidSyntaxException ex) {
            System.err.println("Error retrieving services: " + ex);
            return;
        }

        if (serviceReferences == null || serviceReferences.length == 0) {
            System.out.println("No service registered");
            return;
        }

        // Print'em
        printReferences(serviceReferences);
        System.out.println("Found " + serviceReferences.length + " services");
    }

    /**
     * Prints the services registered by the given bundle
     *
     * @param aBundle
     *            A bundle
     */
    public void services(final Bundle aBundle) {

        // Get the references
        final ServiceReference<?>[] serviceReferences = aBundle
                .getRegisteredServices();
        if (serviceReferences == null || serviceReferences.length == 0) {
            System.out.println("No service registered by "
                    + aBundle.getSymbolicName());
            return;
        }

        // Print'em
        printReferences(serviceReferences);
        System.out.println(aBundle.getSymbolicName() + " registered "
                + serviceReferences.length + " services");
    }
}
