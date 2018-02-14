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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Registers the OSGi utility commands
 *
 * @author Thomas Calmant
 */
public class Activator implements BundleActivator {

	private static BundleContext sBundleContext;

	private static Activator sMe;

	/**
	 * @return
	 */
	public static BundleContext getContext() {
		return sBundleContext;
	}

	/**
	 * @return
	 */
	public static Activator getSingleton() {
		return sMe;
	}

	/**
	 *
	 */
	public Activator() {
		super();
		sMe = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		sBundleContext = context;
		final Bundle wBundle = context.getBundle();
		System.out.printf("%50s | Bundle=[%50s][%s] started\n", "Activator.start()", wBundle.getSymbolicName(),
				wBundle.getVersion());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		System.out.printf("%50s | Bundle=[%50s] stopped\n", "Activator.stop()", context.getBundle().getSymbolicName());

		sBundleContext = null;
	}
}