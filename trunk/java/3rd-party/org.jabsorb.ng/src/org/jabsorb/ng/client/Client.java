/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Client, a Java client extension to JSON-RPC-Java
 * (C) Copyright CodeBistro 2007, Sasha Ovsankin <sasha at codebistro dot com>
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
package org.jabsorb.ng.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCResult;
import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.serializer.FixUp;
import org.jabsorb.ng.serializer.SerializerState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * [START HERE] A factory to create proxies for access to remote Jabsorb
 * services.
 */
public class Client implements InvocationHandler {

    /** Manual instantiation of HashMap<String, Object> */
    private static class ProxyMap extends HashMap<Object, String> {

        private static final long serialVersionUID = 1L;

        public String getString(final Object key) {

            return super.get(key);
        }
    }

    /**
     * Maintain a unique id for each message
     */
    private int id = 0;

    private final ProxyMap proxyMap = new ProxyMap();

    private JSONSerializer serializer;

    private ISession session;

    /**
     * Create a client given a session
     * 
     * @param session
     *            -- transport session to use for this connection
     */
    public Client(final ISession session) {

        this(session, null);
    }

    /**
     * Create a client given a session
     * 
     * @param session
     *            -- transport session to use for this connection
     * @param classLoader
     *            -- Serializer class loader
     */
    public Client(final ISession session, final ClassLoader classLoader) {

        try {
            this.session = session;
            serializer = new JSONSerializer(classLoader);
            serializer.registerDefaultSerializers();
        } catch (final Exception e) {
            throw new ClientError(e);
        }
    }

    /**
     * Dispose of the proxy that is no longer needed
     * 
     * @param proxy
     */
    public void closeProxy(final Object proxy) {

        proxyMap.remove(proxy);
    }

    private synchronized int getId() {

        return id++;
    }

    /**
     * Allow access to the serializer
     * 
     * @return The serializer for this class
     */
    public JSONSerializer getSerializer() {

        return serializer;
    }

    /**
     * This method is public because of the inheritance from the
     * InvokationHandler -- should never be called directly.
     */
    @Override
    public Object invoke(final Object proxyObj, final Method method,
            final Object[] args) throws Exception {

        final String methodName = method.getName();
        if (methodName.equals("hashCode")) {
            return new Integer(System.identityHashCode(proxyObj));
        } else if (methodName.equals("equals")) {
            return (proxyObj == args[0] ? Boolean.TRUE : Boolean.FALSE);
        } else if (methodName.equals("toString")) {
            return proxyObj.getClass().getName() + '@'
                    + Integer.toHexString(proxyObj.hashCode());
        }
        return invoke(proxyMap.getString(proxyObj), method.getName(), args,
                method.getReturnType());
    }

    private Object invoke(final String objectTag, final String methodName,
            final Object[] args, final Class<?> returnType) throws Exception {

        final int id = getId();
        final JSONObject message = new JSONObject();
        String methodTag = objectTag == null ? "" : objectTag + ".";
        methodTag += methodName;
        message.put("method", methodTag);

        {
            final SerializerState state = new SerializerState();

            if (args != null) {

                final JSONArray params = (JSONArray) serializer.marshall(state, /* parent */
                        null, args, "params");

                if ((state.getFixUps() != null)
                        && (state.getFixUps().size() > 0)) {
                    final JSONArray fixups = new JSONArray();
                    for (final Iterator<FixUp> i = state.getFixUps().iterator(); i
                            .hasNext();) {
                        final FixUp fixup = i.next();
                        fixups.put(fixup.toJSONArray());
                    }
                    message.put("fixups", fixups);
                }
                message.put("params", params);
            } else {
                message.put("params", new JSONArray());
            }
        }
        message.put("id", id);

        final JSONObject responseMessage = session.sendAndReceive(message);

        if (!responseMessage.has("result")) {
            processException(responseMessage);
        }
        final Object rawResult = responseMessage.get("result");
        if (rawResult == null) {
            processException(responseMessage);
        }
        if (returnType.equals(Void.TYPE)) {
            return null;
        }

        {
            final JSONArray fixups = responseMessage.optJSONArray("fixups");

            if (fixups != null) {
                for (int i = 0; i < fixups.length(); i++) {
                    final JSONArray assignment = fixups.getJSONArray(i);
                    final JSONArray fixup = assignment.getJSONArray(0);
                    final JSONArray original = assignment.getJSONArray(1);
                    JSONRPCBridge.applyFixup(rawResult, fixup, original);
                }
            }
        }
        return serializer.unmarshall(new SerializerState(), returnType,
                rawResult);
    }

    /**
     * Create a proxy for communicating with the remote service.
     * 
     * @param key
     *            the remote object key
     * @param klass
     *            the class of the interface the remote object should adhere to
     * @return created proxy
     */
    public Object openProxy(final String key, final Class<?> klass) {

        final Object result = java.lang.reflect.Proxy.newProxyInstance(
                klass.getClassLoader(), new Class[] { klass }, this);
        proxyMap.put(result, key);
        return result;
    }

    /**
     * Generate and throw exception based on the data in the 'responseMessage'
     */
    protected void processException(final JSONObject responseMessage)
            throws JSONException {

        final JSONObject error = (JSONObject) responseMessage.get("error");
        if (error != null) {
            final Integer code = new Integer(
                    error.has("code") ? error.getInt("code") : 0);
            final String trace = error.has("trace") ? error.getString("trace")
                    : null;

            String msg;
            if (error.has("message")) {
                msg = error.getString("message");
            } else if (error.has("msg")) {
                msg = error.getString("msg");
            } else {
                msg = null;
            }

            // TODO: throw an exception according to the javaClass entry

            throw new ErrorResponse(code, msg, trace);
        } else {
            throw new ErrorResponse(new Integer(JSONRPCResult.CODE_ERR_PARSE),
                    "Unknown response:" + responseMessage.toString(2), null);
        }
    }

}
