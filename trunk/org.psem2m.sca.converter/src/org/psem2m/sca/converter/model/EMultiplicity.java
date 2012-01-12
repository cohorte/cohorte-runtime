/**
 * File:   EMultiplicity.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * @author Thomas Calmant
 * 
 */
public enum EMultiplicity {

    /** 1..n */
    ONE_N,

    /** 1..1 */
    ONE_ONE,

    /** 0..n */
    ZERO_N,

    /** 0..1 */
    ZERO_ONE,
}
