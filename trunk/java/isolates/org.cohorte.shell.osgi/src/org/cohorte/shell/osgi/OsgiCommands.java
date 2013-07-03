/**
 * File:   OsgiCommands.java
 * Author: Thomas Calmant
 * Date:   3 juil. 2013
 */
package org.cohorte.shell.osgi;

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
