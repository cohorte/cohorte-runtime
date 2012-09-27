/**
 * File:   FakeTemp.java
 * Author: Thomas Calmant
 * Date:   26 sept. 2012
 */
package org.psem2m.demo.sensors.impl;

import java.util.Random;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.demo.sensors.ISensor;

/**
 * Java implementation of a fake sensor
 * 
 * @author Thomas Calmant
 */
@Component(name = "java-fake-temp-factory")
@Provides(specifications = ISensor.class)
public class FakeTemp implements ISensor {

    /** Last temperature */
    @ServiceProperty(name = "temper.value.last")
    private float pLast;

    /** The maximal temperature */
    @ServiceProperty(name = "temper.value.max", value = "30")
    private float pMax;

    /** The minimal temperature */
    @ServiceProperty(name = "temper.value.min", value = "10")
    private float pMin;

    /** The name of the component */
    @Property(name = "instance.name")
    private String pName;

    /** The randomizer */
    private Random pRandom;

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.sensors.ISensor#getName()
     */
    @Override
    public String getName() {

        return pName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.sensors.ISensor#getUnit()
     */
    @Override
    public String getUnit() {

        return "°C";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.demo.sensors.ISensor#getValue()
     */
    @Override
    public synchronized double getValue() {

        if (pLast == 0) {
            // First time we play
            pLast = pRandom.nextFloat() * (pMax - pMin) + pMin;

        } else {
            // Random modification
            final float delta = pRandom.nextFloat() * 1.5f;
            final boolean add = pRandom.nextBoolean();

            if (add) {
                pLast += delta;

            } else {
                pLast -= delta;
            }
        }

        // Normalize...
        if (pLast < pMin) {
            pLast = pMin;

        } else if (pLast > pMax) {
            pLast = pMax;
        }

        return pLast;
    }

    /**
     * Component invalidation
     */
    @Invalidate
    public void invalidate() {

        pLast = 0;
        pRandom = null;
    }

    /**
     * Component validation
     */
    @Validate
    public void validate() {

        pLast = 0;
        pRandom = new Random();
    }
}
