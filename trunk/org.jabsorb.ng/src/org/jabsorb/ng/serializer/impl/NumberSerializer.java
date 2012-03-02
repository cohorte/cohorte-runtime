/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jabsorb.ng.serializer.impl;

import java.math.BigDecimal;

import org.jabsorb.ng.serializer.AbstractSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.ObjectMatch;
import org.jabsorb.ng.serializer.SerializerState;
import org.jabsorb.ng.serializer.UnmarshallException;

/**
 * Serialises numeric values
 */
public class NumberSerializer extends AbstractSerializer {
    /**
     * Classes that this can serialise to.
     */
    private static Class<?>[] _JSONClasses = new Class<?>[] { Integer.class,
            Byte.class, Short.class, Long.class, Float.class, Double.class,
            BigDecimal.class, String.class };

    /**
     * Classes that this can serialise.
     */
    private static Class<?>[] _serializableClasses = new Class<?>[] {
            Integer.class, Byte.class, Short.class, Long.class, Float.class,
            Double.class, BigDecimal.class };

    /**
     * Unique serialisation id.
     */
    private final static long serialVersionUID = 2;

    @Override
    public Class<?>[] getJSONClasses() {

        return _JSONClasses;
    }

    @Override
    public Class<?>[] getSerializableClasses() {

        return _serializableClasses;
    }

    @Override
    public Object marshall(final SerializerState state, final Object p,
            final Object o) throws MarshallException {

        return o;
    }

    /**
     * Converts a javascript object to a Java number
     * 
     * @param clazz
     *            The class of the Java object that it should be converted to
     * @param jso
     *            The javascript object
     * @return A Java primitive type in its java.lang wrapper.
     * @throws NumberFormatException
     *             If clazz is numeric and jso does not parse into a number.
     */
    public Object toNumber(final Class<?> clazz, final Object jso)
            throws NumberFormatException {

        // TODO: isn't this largely a dupe of PrimitiveSerialiser.toPrimitive()?
        // We should probably have just one method that does this, or have one
        // use
        // the other
        if (clazz == Integer.class) {
            if (jso instanceof String) {
                return new Integer((String) jso);
            }
            return new Integer(((Number) jso).intValue());
        } else if (clazz == Long.class) {
            if (jso instanceof String) {
                return new Long((String) jso);
            }
            return new Long(((Number) jso).longValue());
        } else if (clazz == Short.class) {
            if (jso instanceof String) {
                return new Short((String) jso);
            }
            return new Short(((Number) jso).shortValue());
        } else if (clazz == Byte.class) {
            if (jso instanceof String) {
                return new Byte((String) jso);
            }
            return new Byte(((Number) jso).byteValue());
        } else if (clazz == Float.class) {
            if (jso instanceof String) {
                return new Float((String) jso);
            }
            return new Float(((Number) jso).floatValue());
        } else if (clazz == Double.class) {
            if (jso instanceof String) {
                return new Double((String) jso);
            }
            return new Double(((Number) jso).doubleValue());
        } else if (clazz == BigDecimal.class) {
            if (jso instanceof String) {
                return new BigDecimal((String) jso);
            }
            return new BigDecimal(((Number) jso).doubleValue()); // hmmm?
        }
        return null;
    }

    @Override
    public ObjectMatch tryUnmarshall(final SerializerState state,
            final Class<?> clazz, final Object jso) throws UnmarshallException {

        try {
            toNumber(clazz, jso);
        } catch (final NumberFormatException e) {
            throw new UnmarshallException("not a number", e);
        }
        state.setSerialized(jso, ObjectMatch.OKAY);
        return ObjectMatch.OKAY;
    }

    @Override
    public Object unmarshall(final SerializerState state, final Class<?> clazz,
            final Object jso) throws UnmarshallException {

        try {
            if (jso == null || "".equals(jso)) {
                return null;
            }
            final Object num = toNumber(clazz, jso);
            state.setSerialized(jso, num);
            return num;
        } catch (final NumberFormatException e) {
            throw new UnmarshallException("cannot convert object " + jso
                    + " to type " + clazz.getName(), e);
        }
    }

}
