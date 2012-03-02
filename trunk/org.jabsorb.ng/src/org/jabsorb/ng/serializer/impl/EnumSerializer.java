/**
 * File:   EnumSerializer.java
 * Author: Thomas Calmant
 * Date:   28 juil. 2011
 */
package org.jabsorb.ng.serializer.impl;

import org.jabsorb.ng.serializer.AbstractSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.ObjectMatch;
import org.jabsorb.ng.serializer.SerializerState;
import org.jabsorb.ng.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Thomas Calmant
 * 
 */
public class EnumSerializer extends AbstractSerializer {

    /** Enum value key */
    public static final String ENUM_VALUE = "enumValue";

    /**
     * Classes that this can serialise to.
     */
    private static final Class<?>[] JSON_CLASSES = new Class<?>[] { JSONObject.class };

    /**
     * Classes that this can serialise.
     */
    private static final Class<?>[] SERIALIZABLE_CLASSES = new Class<?>[] { Enum.class };

    /** Serial Version UID */
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.serializer.Serializer#canSerialize(java.lang.Class,
     * java.lang.Class)
     */
    @Override
    public boolean canSerialize(final Class<?> aClazz, final Class<?> aJsonClazz) {

        return (aClazz.isEnum())
                && (aJsonClazz == null || aJsonClazz == JSONObject.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.serializer.Serializer#getJSONClasses()
     */
    @Override
    public Class<?>[] getJSONClasses() {

        return JSON_CLASSES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.serializer.Serializer#getSerializableClasses()
     */
    @Override
    public Class<?>[] getSerializableClasses() {

        return SERIALIZABLE_CLASSES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.serializer.Serializer#marshall(org.jabsorb.serializer.
     * SerializerState, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object marshall(final SerializerState aState, final Object aParent,
            final Object aObject) throws MarshallException {

        if (!(aObject instanceof Enum)) {
            throw new MarshallException("Invalid Enum class : "
                    + aObject.getClass().getName());
        }

        final JSONObject val = new JSONObject();

        // Set the class name
        try {
            if (ser.getMarshallClassHints()) {
                // Store the type
                val.put("javaClass", aObject.getClass().getName());
            }

            // Store the value
            val.put(ENUM_VALUE, aObject.toString());

        } catch (final JSONException e) {
            throw new MarshallException("JSONException: " + e.getMessage(), e);
        }

        return val;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jabsorb.serializer.Serializer#tryUnmarshall(org.jabsorb.serializer
     * .SerializerState, java.lang.Class, java.lang.Object)
     */
    @Override
    public ObjectMatch tryUnmarshall(final SerializerState aState,
            final Class<?> aClazz, final Object aJson)
            throws UnmarshallException {

        final JSONObject json = (JSONObject) aJson;

        if (!(aClazz.isEnum())) {
            throw new UnmarshallException(aClazz.getName()
                    + " is not an enumeration");
        }

        // Try to find the value
        if (!json.has(ENUM_VALUE)) {
            throw new UnmarshallException("JSONObject has no " + ENUM_VALUE
                    + " field");
        }

        return ObjectMatch.OKAY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jabsorb.serializer.Serializer#unmarshall(org.jabsorb.serializer.
     * SerializerState, java.lang.Class, java.lang.Object)
     */
    @Override
    public Object unmarshall(final SerializerState aState,
            final Class<?> aClazz, final Object aJson)
            throws UnmarshallException {

        final JSONObject json = (JSONObject) aJson;
        String enumValue;

        try {
            enumValue = json.get(ENUM_VALUE).toString();

        } catch (final JSONException e) {
            throw new UnmarshallException("JSONException: " + e.getMessage(), e);
        }

        try {
            return Enum.valueOf((Class<Enum>) aClazz, enumValue);

        } catch (final IllegalArgumentException e) {
            throw new UnmarshallException("EnumException : " + enumValue
                    + " is not a correct value for " + aClazz.getName());
        }
    }
}
