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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.serializer.AbstractSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.ObjectMatch;
import org.jabsorb.ng.serializer.SerializerState;
import org.jabsorb.ng.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serializes Dictionaries and Properties
 */
public class DictionarySerializer extends AbstractSerializer {

    /**
     * Classes that this can serialize to.
     */
    private static Class<?>[] _JSONClasses = new Class<?>[] { JSONObject.class };

    /**
     * Classes that this can serialize.
     */
    private static Class<?>[] _serializableClasses = new Class<?>[] {
            Properties.class, Dictionary.class, Hashtable.class };

    /**
     * Unique serialization id.
     */
    private final static long serialVersionUID = 2;

    @Override
    public boolean canSerialize(final Class<?> clazz, final Class<?> jsonClazz) {

        return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Dictionary.class
                .isAssignableFrom(clazz)));
    }

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

        final Dictionary<?, ?> dictionary = (Dictionary<?, ?>) o;
        final JSONObject obj = new JSONObject();
        final JSONObject mapdata = new JSONObject();

        try {
            if (ser.getMarshallClassHints()) {
                obj.put("javaClass", o.getClass().getName());
            }
            obj.put("map", mapdata);
            state.push(o, mapdata, "map");
        } catch (final JSONException e) {
            throw new MarshallException("Could not put data" + e.getMessage(),
                    e);
        }

        Object key = null;
        try {
            final Enumeration<?> en = dictionary.keys();
            while (en.hasMoreElements()) {
                key = en.nextElement();
                final String keyString = key.toString(); // only support String
                                                         // keys

                final Object json = ser.marshall(state, mapdata,
                        dictionary.get(key), keyString);

                // omit the object entirely if it's a circular reference or
                // duplicate
                // it will be regenerated in the fixups phase
                if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json) {
                    mapdata.put(keyString, json);
                }
            }
        } catch (final MarshallException e) {
            throw new MarshallException(
                    "map key " + key + " " + e.getMessage(), e);
        } catch (final JSONException e) {
            throw new MarshallException(
                    "map key " + key + " " + e.getMessage(), e);
        } finally {
            state.pop();
        }
        return obj;
    }

    // TODO: try unMarshall and unMarshall share 90% code. Put in into an
    // intermediate function.
    // TODO: Also cache the result somehow so that an unmarshall
    // following a tryUnmarshall doesn't do the same work twice!
    @Override
    public ObjectMatch tryUnmarshall(final SerializerState state,
            final Class<?> clazz, final Object o) throws UnmarshallException {

        final JSONObject jso = (JSONObject) o;
        String java_class;

        // Hint presence
        try {
            java_class = jso.getString("javaClass");
        } catch (final JSONException e) {
            throw new UnmarshallException("Could not read javaClass", e);
        }
        if (java_class == null) {
            throw new UnmarshallException("no type hint");
        }

        // Class compatibility check
        if (!classNameCheck(java_class)) {
            throw new UnmarshallException("not a Dictionary");
        }

        // JSON Format check
        JSONObject jsonmap;
        try {
            jsonmap = jso.getJSONObject("map");
        } catch (final JSONException e) {
            throw new UnmarshallException("map missing", e);
        }
        if (jsonmap == null) {
            throw new UnmarshallException("map missing");
        }

        // Content check
        final ObjectMatch m = new ObjectMatch(-1);
        state.setSerialized(o, m);

        final Iterator<?> i = jsonmap.keys();
        String key = null;
        try {
            while (i.hasNext()) {
                key = (String) i.next();
                m.setMismatch(ser.tryUnmarshall(state, null, jsonmap.get(key))
                        .max(m).getMismatch());
            }
        } catch (final UnmarshallException e) {
            throw new UnmarshallException("key " + key + " " + e.getMessage(),
                    e);
        } catch (final JSONException e) {
            throw new UnmarshallException("key " + key + " " + e.getMessage(),
                    e);
        }

        return m;
    }

    @Override
    public Object unmarshall(final SerializerState state, final Class<?> clazz,
            final Object o) throws UnmarshallException {

        final JSONObject jso = (JSONObject) o;
        String java_class;

        // Hint check
        try {
            java_class = jso.getString("javaClass");
        } catch (final JSONException e) {
            throw new UnmarshallException("Could not read javaClass", e);
        }

        if (java_class == null) {
            throw new UnmarshallException("no type hint");
        }

        // Create the dictionary
        Hashtable<Object, Object> dictionary;
        if (java_class.equals("java.util.Dictionary")
                || java_class.equals("java.util.Hashtable")) {
            dictionary = new Hashtable<Object, Object>();
        } else if (java_class.equals("java.util.Properties")) {
            dictionary = new Properties();
        } else {
            throw new UnmarshallException("not a Dictionary");
        }

        // Parse the JSON map
        JSONObject jsonmap;
        try {
            jsonmap = jso.getJSONObject("map");
        } catch (final JSONException e) {
            throw new UnmarshallException("map missing", e);
        }
        if (jsonmap == null) {
            throw new UnmarshallException("map missing");
        }

        state.setSerialized(o, dictionary);

        final Iterator<?> i = jsonmap.keys();
        String key = null;
        try {
            while (i.hasNext()) {
                key = (String) i.next();
                dictionary.put(key,
                        ser.unmarshall(state, null, jsonmap.get(key)));
            }
        } catch (final UnmarshallException e) {
            throw new UnmarshallException("key " + key + " " + e.getMessage(),
                    e);
        } catch (final JSONException e) {
            throw new UnmarshallException("key " + key + " " + e.getMessage(),
                    e);
        }
        return dictionary;
    }
}
