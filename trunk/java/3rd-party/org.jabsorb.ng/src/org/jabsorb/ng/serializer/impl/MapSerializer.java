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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.serializer.AbstractSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.ObjectMatch;
import org.jabsorb.ng.serializer.SerializerState;
import org.jabsorb.ng.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serializes Maps
 * 
 * TODO: if this serializes a superclass does it need to also specify the
 * subclasses?
 */
public class MapSerializer extends AbstractSerializer {

    /**
     * Classes that this can serialize to.
     */
    private static Class<?>[] _JSONClasses = new Class<?>[] { JSONObject.class };

    /**
     * Classes that this can serialize.
     */
    private static Class<?>[] _serializableClasses = new Class<?>[] {
            Map.class, HashMap.class, TreeMap.class, LinkedHashMap.class };

    /**
     * Unique serialization id.
     */
    private final static long serialVersionUID = 2;

    @Override
    public boolean canSerialize(final Class<?> clazz, final Class<?> jsonClazz) {

        return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Map.class
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

        final Map<?, ?> map = (Map<?, ?>) o;
        final JSONObject obj = new JSONObject();
        final JSONObject mapdata = new JSONObject();

        if (ser.getMarshallClassHints()) {
            try {
                obj.put("javaClass", o.getClass().getName());
            } catch (final JSONException e) {
                throw new MarshallException("javaClass not found!", e);
            }
        }

        try {
            obj.put("map", mapdata);
            state.push(o, mapdata, "map");
        } catch (final JSONException e) {
            throw new MarshallException("Could not add map to object: "
                    + e.getMessage(), e);
        }

        Object key = null;
        try {
            for (final Entry<?, ?> entry : map.entrySet()) {
                key = entry.getKey();
                final String keyString = key.toString(); // only support String
                                                         // keys

                final Object json = ser.marshall(state, mapdata,
                        entry.getValue(), keyString);

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
            // FIXME: previous version also handled Properties
            throw new UnmarshallException("not a Map");
        }

        // JSON Format check
        JSONObject jsonmap;
        try {
            jsonmap = jso.getJSONObject("map");
        } catch (final JSONException e) {
            throw new UnmarshallException("Could not read map: "
                    + e.getMessage(), e);
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

        // Create the map
        final Map<Object, Object> abmap;
        if (java_class.equals("java.util.Map")
                || java_class.equals("java.util.AbstractMap")
                || java_class.equals("java.util.HashMap")) {
            abmap = new HashMap<Object, Object>();
        } else if (java_class.equals("java.util.TreeMap")) {
            abmap = new TreeMap<Object, Object>();
        } else if (java_class.equals("java.util.LinkedHashMap")) {
            abmap = new LinkedHashMap<Object, Object>();
        } else if (java_class.equals("java.util.Properties")) {
            abmap = new Properties();
        } else {
            throw new UnmarshallException("not a Map");
        }

        // Parse the JSON map
        JSONObject jsonmap;
        try {
            jsonmap = jso.getJSONObject("map");
        } catch (final JSONException e) {
            throw new UnmarshallException("Could not read map: "
                    + e.getMessage(), e);
        }
        if (jsonmap == null) {
            throw new UnmarshallException("map missing");
        }

        state.setSerialized(o, abmap);

        final Iterator<?> i = jsonmap.keys();
        String key = null;
        try {
            while (i.hasNext()) {
                key = (String) i.next();
                abmap.put(key, ser.unmarshall(state, null, jsonmap.get(key)));
            }
        } catch (final UnmarshallException e) {
            throw new UnmarshallException("key " + key + " " + e.getMessage(),
                    e);
        } catch (final JSONException e) {
            throw new UnmarshallException("key " + key + " " + e.getMessage(),
                    e);
        }

        return abmap;
    }
}
