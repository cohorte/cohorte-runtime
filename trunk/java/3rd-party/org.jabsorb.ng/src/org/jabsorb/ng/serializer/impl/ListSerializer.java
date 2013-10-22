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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

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
 * Serializes lists
 * 
 * TODO: if this serializes a superclass does it need to also specify the
 * subclasses?
 */
public class ListSerializer extends AbstractSerializer {

    /**
     * Classes that this can serialize to.
     */
    private static final Class<?>[] _JSONClasses = new Class<?>[] { JSONObject.class };

    /**
     * Classes that this can serialize.
     */
    private static final Class<?>[] _serializableClasses = new Class<?>[] {
            List.class, ArrayList.class, LinkedList.class, Vector.class };

    /**
     * Unique serialization id.
     */
    private static final long serialVersionUID = 2;

    @Override
    public boolean canSerialize(final Class<?> clazz, final Class<?> jsonClazz) {

        return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && List.class
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

        final List<?> list = (List<?>) o;
        final JSONObject obj = new JSONObject();
        final JSONArray arr = new JSONArray();

        // TODO: this same block is done everywhere.
        // Have a single function to do it.
        if (ser.getMarshallClassHints()) {
            try {
                obj.put("javaClass", o.getClass().getName());
            } catch (final JSONException e) {
                throw new MarshallException("javaClass not found!", e);
            }
        }

        try {
            obj.put("list", arr);
            state.push(o, arr, "list");

        } catch (final JSONException e) {
            throw new MarshallException("Error setting list: " + e, e);
        }

        int index = 0;
        try {
            for (final Object item : list) {
                // Convert the item
                final Object json = ser.marshall(state, arr, item, index);

                // Check for circular references
                if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json) {
                    arr.put(json);
                } else {
                    // put a slot where the object would go, so it can be fixed
                    // up properly in the fix up phase
                    arr.put(JSONObject.NULL);
                }

                index++;
            }

        } catch (final MarshallException e) {
            throw new MarshallException("element " + index, e);

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
            throw new UnmarshallException("not a List");
        }

        // JSON Format check
        JSONArray jsonlist;
        try {
            jsonlist = jso.getJSONArray("list");
        } catch (final JSONException e) {
            throw new UnmarshallException("Could not read list: "
                    + e.getMessage(), e);
        }
        if (jsonlist == null) {
            throw new UnmarshallException("list missing");
        }

        // Content check
        final ObjectMatch m = new ObjectMatch(-1);
        state.setSerialized(o, m);

        int idx = 0;
        try {
            for (idx = 0; idx < jsonlist.length(); idx++) {
                m.setMismatch(ser.tryUnmarshall(state, null, jsonlist.get(idx))
                        .max(m).getMismatch());
            }
        } catch (final UnmarshallException e) {
            throw new UnmarshallException("element " + idx + " "
                    + e.getMessage(), e);
        } catch (final JSONException e) {
            throw new UnmarshallException("element " + idx + " "
                    + e.getMessage(), e);
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

        // Create the list
        final AbstractList<Object> ablist;
        if (java_class.equals("java.util.List")
                || java_class.equals("java.util.AbstractList")
                || java_class.equals("java.util.ArrayList")) {
            ablist = new ArrayList<Object>();
        } else if (java_class.equals("java.util.LinkedList")) {
            ablist = new LinkedList<Object>();
        } else if (java_class.equals("java.util.Vector")) {
            ablist = new Vector<Object>();
        } else {
            throw new UnmarshallException("not a List");
        }

        // Parse the JSON list
        JSONArray jsonlist;
        try {
            jsonlist = jso.getJSONArray("list");

        } catch (final JSONException e) {
            throw new UnmarshallException("Could not read list: "
                    + e.getMessage(), e);
        }

        if (jsonlist == null) {
            throw new UnmarshallException("list missing");
        }

        state.setSerialized(o, ablist);

        int idx = 0;
        try {
            for (idx = 0; idx < jsonlist.length(); idx++) {
                ablist.add(ser.unmarshall(state, null, jsonlist.get(idx)));
            }
        } catch (final UnmarshallException e) {
            throw new UnmarshallException("element " + idx + " "
                    + e.getMessage(), e);
        } catch (final JSONException e) {
            throw new UnmarshallException("element " + idx + " "
                    + e.getMessage(), e);
        }
        return ablist;
    }
}
