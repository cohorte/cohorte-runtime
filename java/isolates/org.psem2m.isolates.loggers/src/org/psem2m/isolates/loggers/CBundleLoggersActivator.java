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

package org.psem2m.isolates.loggers;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.psem2m.isolates.base.activators.CActivatorBase;
import org.psem2m.isolates.base.activators.IActivatorBase;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public class CBundleLoggersActivator extends CActivatorBase implements
		IActivatorBase {

	/** first instance **/
	private static CBundleLoggersActivator sSingleton = null;

	/**
	 * @return
	 */
	public static CBundleLoggersActivator getInstance() {

		return sSingleton;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext bundleContext) throws Exception {

		// Store the singleton reference
		sSingleton = this;

		super.start(bundleContext);

		final Bundle wBundle = bundleContext.getBundle();
		System.out.printf("%50s | Bundle=[%50s][%s] started\n",
				"Activator.start()", wBundle.getSymbolicName(),
				wBundle.getVersion());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext bundleContext) throws Exception {

		super.stop(bundleContext);

		System.out.printf("%50s | Bundle=[%50s] stopped\n", "Activator.stop()",
				bundleContext.getBundle().getSymbolicName());

		// Forget the singleton reference
		sSingleton = null;
	}
}
