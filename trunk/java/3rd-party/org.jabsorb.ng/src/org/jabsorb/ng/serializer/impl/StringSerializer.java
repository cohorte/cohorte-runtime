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

import org.jabsorb.ng.serializer.AbstractSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.ObjectMatch;
import org.jabsorb.ng.serializer.SerializerState;
import org.jabsorb.ng.serializer.UnmarshallException;

/**
 * Serialises String values
 */
public class StringSerializer extends AbstractSerializer {
    /**
     * Classes that this can serialise to.
     */
    private static Class<?>[] _JSONClasses = new Class<?>[] { String.class,
            Integer.class };

    /**
     * Classes that this can serialise.
     */
    private static Class<?>[] _serializableClasses = new Class<?>[] {
            String.class, char.class, Character.class, byte[].class,
            char[].class };

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

        if (o instanceof Character) {
            return o.toString();
        } else if (o instanceof byte[]) {
            return new String((byte[]) o);
        } else if (o instanceof char[]) {
            return new String((char[]) o);
        } else {
            return o;
        }
    }

    @Override
    public ObjectMatch tryUnmarshall(final SerializerState state,
            final Class<?> clazz, final Object jso) throws UnmarshallException {

        // For some reason getClass can be String but getClasses will return an
        // empty array. This catches this.
        if (jso.getClass().equals(String.class)) {
            return ObjectMatch.OKAY;
        }
        final Class<?> classes[] = jso.getClass().getClasses();
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].equals(String.class)) {
                state.setSerialized(jso, ObjectMatch.OKAY);
                return ObjectMatch.OKAY;
            }
        }

        state.setSerialized(jso, ObjectMatch.SIMILAR);
        return ObjectMatch.SIMILAR;
    }

    @Override
    public Object unmarshall(final SerializerState state, final Class<?> clazz,
            final Object jso) throws UnmarshallException {

        Object returnValue;
        final String val = jso instanceof String ? (String) jso : jso
                .toString();
        if (clazz == char.class) {
            returnValue = new Character(val.charAt(0));
        } else if (clazz == byte[].class) {
            returnValue = val.getBytes();
        } else if (clazz == char[].class) {
            returnValue = val.toCharArray();
        } else {
            returnValue = val;
        }
        state.setSerialized(jso, returnValue);
        return returnValue;
    }

}
