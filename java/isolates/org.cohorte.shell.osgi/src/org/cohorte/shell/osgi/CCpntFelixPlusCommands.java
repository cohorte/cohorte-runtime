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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.psem2m.isolates.base.CAbstractGoGoCommand;
import org.psem2m.isolates.base.IGoGoCommand;
import org.psem2m.isolates.base.IIsolateLoggerSvc;

/**
 * Implementations of Gogo shell commands to simplify OSGi debugging
 *
 * @author Thomas Calmant
 */
@Component(name = "cohorte-isolate-felixplus-gogocommand-factory")
@Instantiate(name = "cohorte-isolate-felixplus-gogocommand")
@Provides(specifications = { IGoGoCommand.class })
public class CCpntFelixPlusCommands extends CAbstractGoGoCommand implements IGoGoCommand {

	/**
	 * The Gogo commands name. ATTENTION : look at the name of the methods and the
	 * declaration
	 */
	@ServiceProperty(name = "osgi.command.function", value = "{services,providers,service,references,getservice}")
	private String[] pCommands;

	/** The bundle context */
	private final BundleContext pContext;

	/**
	 * Cohorte isolate logger service
	 */
	@Requires
	private IIsolateLoggerSvc pLogger;

	/**
	 * The Gogo commands scope
	 */
	@ServiceProperty(name = "osgi.command.scope", value = "felix+")
	private String pScope;

	/**
	 * Sets up the members
	 *
	 * @param aContext
	 *            The bundle context
	 */
	public CCpntFelixPlusCommands(final BundleContext aContext) {
		super();
		pContext = aContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.IGogoCommand#getCommands()
	 */
	@Override
	public String[] getCommands() {
		return pCommands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.CAbstractGoGoCommand#getLogger()
	 */
	@Override
	public IIsolateLoggerSvc getLogger() {
		return pLogger;
	}

	/**
	 * Gogo command "dumpls"
	 */
	/**
	 * Gets and ungets the service with the given service ID
	 *
	 * @param aServiceID
	 *            Service ID
	 */
	@Descriptor("get service info")
	public void getservice(@Descriptor("the numeric id of the service") final int aServiceID) {
		logTwiceInfo("getservice", "execute  getservice(id:%s)", aServiceID);

		final ServiceReference<?>[] svcRefs;
		try {
			svcRefs = pContext.getServiceReferences((String) null, "(" + Constants.SERVICE_ID + "=" + aServiceID + ")");

			if (svcRefs == null) {
				logTwiceWarn("getservice", "No service found using id [%s] ", aServiceID);
				return;
			}

			final ServiceReference<?> svcRef = svcRefs[0];
			System.out.println("ServiceReference found");
			printReference(svcRef);

			// Get it
			final Object svc = pContext.getService(svcRef);
			System.out.println("Service object: " + svc);
			pContext.ungetService(svcRef);

		} catch (final Exception e) {
			logTwiceSevere("service", e, "Error retrieving service by id [%s]", aServiceID);
		}

	}

	/**
	 * Gets and ungets the service with the given specification
	 *
	 * @param aSpecification
	 *            Service specification
	 */
	public void getservice(final String aSpecification) {
		logTwiceInfo("getservice", "execute  getservice(spec:%s)", aSpecification);

		try {
			final ServiceReference<?> svcRef = pContext.getServiceReference(aSpecification);
			if (svcRef == null) {
				logTwiceWarn("getservice", "No service found using spec [%s] ", aSpecification);
				return;
			}

			printReference(svcRef);

			// Get it
			final Object svc = pContext.getService(svcRef);
			System.out.println("Service impl: " + svc);
			pContext.ungetService(svcRef);

		} catch (final Exception e) {
			logTwiceSevere("service", e, "Error retrieving service by spec [%s]", aSpecification);
		}
	}

	@Invalidate
	public void invalidate() {
		pLogger.logInfo(this, "invalidate", "invalidated");
	}

	/**
	 * Prints the details of the given service reference
	 *
	 * @param aServiceReference
	 *            A service reference
	 */
	private void printReference(final ServiceReference<?> aServiceReference) {

		// Extract main properties
		final Long svcId = (Long) aServiceReference.getProperty(Constants.SERVICE_ID);
		final String[] specs = (String[]) aServiceReference.getProperty(Constants.OBJECTCLASS);
		final Bundle bundle = aServiceReference.getBundle();

		// Print'em
		System.out.println("Service ID: " + svcId);
		System.out.println("Bundle: " + bundle.getSymbolicName() + " (" + bundle.getBundleId() + ")");
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
		final Bundle[] usingBundles = aServiceReference.getUsingBundles();
		if (usingBundles != null) {
			for (final Bundle usingBundle : usingBundles) {
				System.out.println("\t* " + usingBundle.getSymbolicName() + " (" + usingBundle.getBundleId() + ")");
			}
		}
	}

	/**
	 * Prints the ID and the specifications of the given service references on the
	 * standard output
	 *
	 * @param aServiceReferences
	 *            Some service references
	 */
	private String printReferences(final ServiceReference<?>[] aServiceReferences) {

		// Sort the array
		Arrays.sort(aServiceReferences, new Comparator<ServiceReference<?>>() {

			/*
			 * (non-Javadoc)
			 *
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(final ServiceReference<?> aReference, final ServiceReference<?> aOther) {

				final Long svcId1 = (Long) aReference.getProperty(Constants.SERVICE_ID);
				final Long svcId2 = (Long) aOther.getProperty(Constants.SERVICE_ID);

				return svcId1.compareTo(svcId2);
			}
		});
		StringBuilder wSB = new StringBuilder();
		// Print details
		for (final ServiceReference<?> reference : aServiceReferences) {

			// Extract properties
			final Long svcId = (Long) reference.getProperty(Constants.SERVICE_ID);
			final String[] specs = (String[]) reference.getProperty(Constants.OBJECTCLASS);
			final Bundle bundle = reference.getBundle();
			final Object wService = pContext.getService(reference);
			final String wImpl = String.format("%s@%s", wService.getClass().getSimpleName(),
					Integer.toHexString(wService.hashCode()));

			// Print the result
			wSB.append(String.format("\n- service id:[%4d] bdl:[%4d %-50s] impl:[%-50s] sepcs:%s", svcId,
					bundle.getBundleId(), bundle.getSymbolicName(), wImpl, Arrays.asList(specs)));
		}
		return wSB.toString();
	}

	/**
	 * Prints the references that matches the given specification
	 * 
	 * @param aSpecification
	 *            A service specification
	 */
	@Descriptor("Prints the references that matches the given specification")
	public void providers(@Descriptor("A service specification") final String aSpecification) {
		logTwiceInfo("providers", "execute  providers(sepc:%s)", aSpecification);

		// Get the references
		ServiceReference<?>[] serviceReferences;
		try {
			serviceReferences = pContext.getAllServiceReferences(aSpecification, null);

		} catch (final InvalidSyntaxException ex) {
			System.err.println("Error retrieving services: " + ex);
			return;
		}

		if (serviceReferences == null || serviceReferences.length == 0) {
			System.out.println("No matching service found");
			return;
		}

		// Print'em
		logTwiceInfo("providers", printReferences(serviceReferences));
	}

	/**
	 * Prints the result of getServiceReferences()
	 *
	 * @param aSpecification
	 *            Service specification
	 */
	@Descriptor("Prints the list of the service references mathing the given specification ")
	public void references(@Descriptor("The specification of the service") final String aSpecification) {
		logTwiceInfo("references", "execute  references(spec:%s)", aSpecification);
		try {
			final ServiceReference<?>[] serviceReferences = pContext.getAllServiceReferences(aSpecification, null);
			if (serviceReferences == null) {
				logTwiceWarn("references", "No reference found");

			} else {
				// Found some
				for (final ServiceReference<?> svcRef : serviceReferences) {

					String wRanking = String.valueOf(svcRef.getProperty(Constants.SERVICE_RANKING));
					String[] wKeys = svcRef.getPropertyKeys();

					logTwiceInfo("references", "- reference id:[%4s] Ranking:[%-5s] Specs:%-50s Props:%s",
							svcRef.getProperty(Constants.SERVICE_ID), wRanking,
							Arrays.toString((String[]) svcRef.getProperty(Constants.OBJECTCLASS)),
							Arrays.asList(wKeys));
				}
			}
		} catch (final Exception e) {
			logTwiceSevere("references", e, "Error printing references");
		}
	}

	/**
	 * Prints the details of the given service
	 */
	@Descriptor("Prints the details of the given service")
	public void service(final int aServiceId) {
		logTwiceInfo("service", "execute  service(%s)", aServiceId);

		// Get the matching references
		ServiceReference<?>[] serviceReferences;
		try {
			serviceReferences = pContext.getServiceReferences((String) null,
					"(" + Constants.SERVICE_ID + "=" + aServiceId + ")");

			// Check results
			if (serviceReferences == null || serviceReferences.length == 0) {
				logTwiceWarn("service", "Unknown service");
				return;

			}
			if (serviceReferences.length > 1) {
				logTwiceWarn("service", "Too many references found, only the first one will be printed");
			}

			// Print details
			printReference(serviceReferences[0]);

		} catch (final Exception e) {
			logTwiceSevere("service", e, "Error retrieving service [%s]", aServiceId);
		}
	}

	/**
	 * Prints all registered services
	 */
	@Descriptor("Prints all registered services")
	public void services() {
		logTwiceInfo("services", "execute  services()");

		// Get the references
		ServiceReference<?>[] serviceReferences;
		try {
			serviceReferences = pContext.getAllServiceReferences(null, null);

			if (serviceReferences == null || serviceReferences.length == 0) {
				System.out.println("No service registered");
				return;
			}

			// Print'em
			logTwiceInfo("services", printReferences(serviceReferences));
			logTwiceInfo("services", "Found [%d] services", serviceReferences.length);

		} catch (final Exception e) {
			logTwiceSevere("services", e, "Error retrieving services");
		}
	}

	/**
	 * Prints the services registered by the given bundle
	 *
	 * @param aBundle
	 *            A bundle
	 */
	@Descriptor("Prints the services registered by the given bundle")
	public void services(final Bundle aBundle) {
		logTwiceInfo("services", "execute  services(bndl:%s)", aBundle.getSymbolicName());

		// Get the references
		final ServiceReference<?>[] serviceReferences = aBundle.getRegisteredServices();
		if (serviceReferences == null || serviceReferences.length == 0) {
			logTwiceWarn("services", "No service registered by the bundle [%s]", aBundle.getSymbolicName());
			return;
		}

		// Print'em
		logTwiceInfo("services", printReferences(serviceReferences));
		logTwiceInfo("services", "Bundle [%s] registered [%d] services", aBundle.getSymbolicName(),
				serviceReferences.length);
	}

	/**
	 * 
	 */
	@Validate
	public void validate() {
		pLogger.logInfo(this, "validate", "validated");
	}
}
