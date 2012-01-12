/**
 * File:   EMultiplicity.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.model;

/**
 * Normalizes the multiplicity of a SCA reference
 * 
 * @author Thomas Calmant
 */
public enum EMultiplicity {

    /** 1..n : At least one target must be linked */
    ONE_N,

    /** 1..1 : A single target must be linked */
    ONE_ONE,

    /** 0..n : Multiple targets can be linked */
    ZERO_N,

    /** 0..1 : At most one target can be linked */
    ZERO_ONE,
}
