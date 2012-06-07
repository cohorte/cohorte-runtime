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

import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.logging.ILogger;
import org.jabsorb.ng.logging.LoggerFactory;
import org.jabsorb.ng.serializer.AbstractSerializer;
import org.jabsorb.ng.serializer.MarshallException;
import org.jabsorb.ng.serializer.ObjectMatch;
import org.jabsorb.ng.serializer.SerializerState;
import org.jabsorb.ng.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises classes that have been registered on the bridge as references or
 * callable references.
 */
public class ReferenceSerializer extends AbstractSerializer {
    /**
     * Classes that this can serialise to.
     */
    private static Class<?>[] _JSONClasses = new Class<?>[] {};

    /**
     * Classes that this can serialise.
     */
    private static Class<?>[] _serializableClasses = new Class<?>[] {};

    /**
     * The logger for this class
     */
    private final static ILogger log = LoggerFactory
            .getLogger(ReferenceSerializer.class);

    /**
     * Unique serialisation id.
     */
    private final static long serialVersionUID = 2;

    /**
     * A reference to the bridge
     */
    private JSONRPCBridge bridge;

    /**
     * Creates a new ReferenceSerializer
     * 
     * @param bridge
     *            The bridge to determine if a class is a reference.
     * 
     *            TODO: Should reference detection be abstracted out into
     *            another class?
     */
    public ReferenceSerializer(final JSONRPCBridge bridge) {

        this.bridge = bridge;
    }

    @Override
    public boolean canSerialize(final Class<?> clazz, final Class<?> jsonClazz) {

        return (!clazz.isArray()
                && !clazz.isPrimitive()
                && !clazz.isInterface()
                && (bridge.isReference(clazz) || bridge
                        .isCallableReference(clazz)) && (jsonClazz == null || jsonClazz == JSONObject.class));
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

        final Class<?> clazz = o.getClass();
        final Integer identity = new Integer(System.identityHashCode(o));
        if (bridge.isReference(clazz)) {
            if (log.isDebugEnabled()) {
                log.debug("marshalling reference to object " + identity
                        + " of class " + clazz.getName());
            }
            bridge.addReference(o);
            final JSONObject jso = new JSONObject();
            try {
                jso.put("JSONRPCType", "Reference");
                jso.put("javaClass", clazz.getName());
                jso.put("objectID", identity);
            } catch (final JSONException e) {
                throw new MarshallException(e.getMessage(), e);
            }
            return jso;
        } else if (bridge.isCallableReference(clazz)) {
            if (log.isDebugEnabled()) {
                log.debug("marshalling callable reference to object "
                        + identity + " of class " + clazz.getName());
            }
            bridge.registerObject(identity, o);
            bridge.addReference(o);

            final JSONObject jso = new JSONObject();
            try {
                jso.put("JSONRPCType", "CallableReference");
                jso.put("javaClass", clazz.getName());
                jso.put("objectID", identity);
            } catch (final JSONException e) {
                throw new MarshallException(e.getMessage(), e);
            }

            return jso;
        }
        return null;
    }

    @Override
    public ObjectMatch tryUnmarshall(final SerializerState state,
            final Class<?> clazz, final Object o) throws UnmarshallException {

        state.setSerialized(o, ObjectMatch.OKAY);
        return ObjectMatch.OKAY;
    }

    @Override
    public Object unmarshall(final SerializerState state, final Class<?> clazz,
            final Object o) throws UnmarshallException {

        final JSONObject jso = (JSONObject) o;
        Object ref = null;
        String json_type;
        int object_id;
        try {
            json_type = jso.getString("JSONRPCType");
            object_id = jso.getInt("objectID");
        } catch (final JSONException e) {
            throw new UnmarshallException(e.getMessage(), e);
        }
        if (json_type != null) {
            if ((json_type.equals("Reference"))
                    || (json_type.equals("CallableReference"))) {
                ref = bridge.getReference(object_id);
            }
        }
        state.setSerialized(o, ref);
        return ref;
    }

}
