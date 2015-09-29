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

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Registers the OSGi utility commands
 *
 * @author Thomas Calmant
 */
public class Activator implements BundleActivator {

	/** The service registration */
	private ServiceRegistration<OsgiCommands> pRegistration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext aContext) {

		// Prepare the object
		final OsgiCommands osgiCommands = new OsgiCommands(aContext);

		// Set up properties
		final Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("osgi.command.scope", "cohorte");
		properties.put("osgi.command.function", osgiCommands.getCommands());

		// Register the service
		pRegistration = aContext.registerService(OsgiCommands.class,
				osgiCommands, properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext aContext) {

		// Unregister the service
		if (pRegistration != null) {
			pRegistration.unregister();
			pRegistration = null;
		}
	}
}
