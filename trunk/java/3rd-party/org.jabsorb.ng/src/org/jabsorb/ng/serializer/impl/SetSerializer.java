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

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.serializer.AbstractSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.ObjectMatch;
import org.jabsorb.ng.serializer.SerializerState;
import org.jabsorb.ng.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serializes Sets
 * 
 * PATCH by T. Calmant: set contains an array instead of a key -&gt; value
 * object
 * 
 * TODO: if this serializes a superclass does it need to also specify the
 * subclasses?
 */
public class SetSerializer extends AbstractSerializer {

    /**
     * Classes that this can serialize to.
     */
    private static final Class<?>[] _JSONClasses = new Class<?>[] { JSONObject.class };

    /**
     * Classes that this can serialize.
     */
    private static final Class<?>[] _serializableClasses = new Class<?>[] {
            Set.class, HashSet.class, TreeSet.class, LinkedHashSet.class };

    /**
     * Unique serialization id.
     */
    private static final long serialVersionUID = 3;

    @Override
    public boolean canSerialize(final Class<?> clazz, final Class<?> jsonClazz) {

        return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Set.class
                .isAssignableFrom(clazz)));
    }

    /**
     * Tests if the given class name can be serialized
     * 
     * @param aClassName
     *            The class name to test
     * @return True if the class can be handled by this serializer
     */
    private boolean classNameCheck(final String aClassName) {

        for (final Class<?> clazz : getSerializableClasses()) {
            if (aClassName.equals(clazz)) {
                return true;
            }
        }

        return false;
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

        final Set<?> set = (Set<?>) o;

        final JSONObject obj = new JSONObject();
        final JSONArray setdata = new JSONArray();
        if (ser.getMarshallClassHints()) {
            try {
                obj.put("javaClass", o.getClass().getName());
            } catch (final JSONException e) {
                throw new MarshallException("javaClass not found!", e);
            }
        }
        try {
            obj.put("set", setdata);
            state.push(o, setdata, "set");
        } catch (final JSONException e) {
            throw new MarshallException("Could not set 'set': "
                    + e.getMessage(), e);
        }

        Object value = null;
        int idx = 0;
        final Iterator<?> i = set.iterator();
        try {
            while (i.hasNext()) {
                value = i.next();
                final Object json = ser.marshall(state, setdata, value, idx);

                // omit the object entirely if it's a circular reference or
                // duplicate
                // it will be regenerated in the fixups phase
                if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json) {
                    setdata.put(idx, json);
                }

                idx++;
            }
        } catch (final MarshallException e) {
            throw new MarshallException("set value " + value + " "
                    + e.getMessage(), e);
        } catch (final JSONException e) {
            throw new MarshallException("set value " + value + " "
                    + e.getMessage(), e);
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
            throw new UnmarshallException("not a Set");
        }

        // JSON Format check
        JSONArray jsonset;
        try {
            jsonset = jso.getJSONArray("set");
        } catch (final JSONException e) {
            throw new UnmarshallException("set missing", e);
        }

        if (jsonset == null) {
            throw new UnmarshallException("set missing");
        }

        // Content check
        final ObjectMatch m = new ObjectMatch(-1);
        state.setSerialized(o, m);

        int idx = 0;
        try {
            for (idx = 0; idx < jsonset.length(); idx++) {
                m.setMismatch(ser.tryUnmarshall(state, null, jsonset.get(idx))
                        .max(m).getMismatch());
            }

        } catch (final UnmarshallException e) {
            throw new UnmarshallException(
                    "index " + idx + " " + e.getMessage(), e);
        } catch (final JSONException e) {
            throw new UnmarshallException(
                    "index " + idx + " " + e.getMessage(), e);
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

        // Create the set
        AbstractSet<Object> abset = null;
        if (java_class.equals("java.util.Set")
                || java_class.equals("java.util.AbstractSet")
                || java_class.equals("java.util.HashSet")) {
            abset = new HashSet<Object>();
        } else if (java_class.equals("java.util.TreeSet")) {
            abset = new TreeSet<Object>();
        } else if (java_class.equals("java.util.LinkedHashSet")) {
            abset = new LinkedHashSet<Object>();
        } else {
            throw new UnmarshallException("not a Set");
        }

        // Parse the JSON set
        JSONArray jsonset;
        try {
            jsonset = jso.getJSONArray("set");

        } catch (final JSONException e) {
            throw new UnmarshallException("set missing", e);
        }

        if (jsonset == null) {
            throw new UnmarshallException("set missing");
        }

        state.setSerialized(o, abset);

        int idx = 0;
        try {
            for (idx = 0; idx < jsonset.length(); idx++) {
                final Object setElement = jsonset.get(idx);
                abset.add(ser.unmarshall(state, null, setElement));
            }
        } catch (final UnmarshallException e) {
            throw new UnmarshallException(
                    "index " + idx + " " + e.getMessage(), e);
        } catch (final JSONException e) {
            throw new UnmarshallException(
                    "index " + idx + " " + e.getMessage(), e);
        }
        return abset;
    }
}
