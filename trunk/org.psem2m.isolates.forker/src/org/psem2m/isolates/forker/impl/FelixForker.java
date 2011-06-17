/**
 * File:   FelixForker.java
 * Author: Thomas Calmant
 * Date:   17 juin 2011
 */
package org.psem2m.isolates.forker.impl;

import org.psem2m.isolates.commons.PlatformConfiguration;
import org.psem2m.isolates.commons.forker.ProcessConfiguration;
import org.psem2m.isolates.forker.Activator;

/**
 * Starts Felix frameworks
 * 
 * @author Thomas Calmant
 */
public class FelixForker extends AbstractForker {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.isolates.forker.impl.AbstractForker#doRunProcess(org.psem2m
     * .isolates.commons.IPlatformConfiguration,
     * org.psem2m.isolates.commons.forker.IProcessConfiguration)
     */
    @Override
    protected Process doRunProcess(
	    final PlatformConfiguration aPlatformConfiguration,
	    final ProcessConfiguration aProcessConfiguration) {

	Activator.getLogger().logDebug(this, "runProcess", "Trying to run : ",
		aPlatformConfiguration, aProcessConfiguration);

	return null;
    }

}
