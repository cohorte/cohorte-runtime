/**
 * File:   ComposerRules.java
 * Author: "Thomas Calmant"
 * Date:   30 janv. 2013
 */
package org.cohorte.composer.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.cohorte.composer.api.IComposerRules;

/**
 * @author "Thomas Calmant"
 * 
 */
@Component(name = "cohorte-composer-rules-factory")
@Provides(specifications = IComposerRules.class)
public class ComposerRules implements IComposerRules {

}
