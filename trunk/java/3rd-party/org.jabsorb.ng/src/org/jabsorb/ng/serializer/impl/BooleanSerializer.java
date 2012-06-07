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
 * Serialiess Boolean values
 */
public class BooleanSerializer extends AbstractSerializer {
    /**
     * Classes that this can serialise to.
     */
    private static Class<?>[] _JSONClasses = new Class<?>[] { Boolean.class,
            String.class };

    /**
     * Classes that this can serialise.
     */
    private static Class<?>[] _serializableClasses = new Class<?>[] {
            boolean.class, Boolean.class };

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

    @Override
    public ObjectMatch tryUnmarshall(final SerializerState state,
            final Class<?> clazz, final Object jso) throws UnmarshallException {

        final ObjectMatch toReturn;
        if (jso instanceof String) {
            // TODO: Boolean parses stuff as ignoreCase(x)=="true" as true or
            // anything else as false. I'm pretty sure in this case it this
            // should
            // only be javascript true or false strings, because otherwise
            // this will catch string passed to it.
            if (jso.equals("true") || jso.equals("false")) {
                toReturn = ObjectMatch.OKAY;
            } else {
                toReturn = ObjectMatch.ROUGHLY_SIMILAR;
            }
        } else if (jso instanceof Boolean) {
            toReturn = ObjectMatch.OKAY;
        } else {
            toReturn = ObjectMatch.ROUGHLY_SIMILAR;
        }
        state.setSerialized(jso, toReturn);
        return toReturn;
    }

    @Override
    public Object unmarshall(final SerializerState state, final Class<?> clazz,
            final Object jso) throws UnmarshallException {

        Boolean returnValue = Boolean.FALSE;

        if (jso instanceof String) {
            try {
                returnValue = new Boolean((String) jso);
            } catch (final Exception e) {
                throw new UnmarshallException("Cannot convert " + jso
                        + " to Boolean", e);
            }
        } else if (jso instanceof Boolean || clazz == boolean.class) {
            returnValue = (Boolean) jso;
        }

        state.setSerialized(jso, returnValue);
        return returnValue;
    }
}
