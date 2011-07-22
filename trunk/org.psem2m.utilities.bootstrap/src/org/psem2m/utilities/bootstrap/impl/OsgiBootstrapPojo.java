/**
 * File:   OsgiBootstrapPure.java
 * Author: Thomas Calmant
 * Date:   6 juil. 2011
 */
package org.psem2m.utilities.bootstrap.impl;

import org.psem2m.utilities.bootstrap.streams.MessageSender;

/**
 * Bootstrap for OSGi frameworks
 * 
 * @author Thomas Calmant
 */
public class OsgiBootstrapPojo extends OsgiBootstrapPure {

    /**
     * Default constructor (for iPOJO)
     */
    public OsgiBootstrapPojo() {

	// Use a default message sender
	super(new MessageSender(System.out), null, null);
    }
}
