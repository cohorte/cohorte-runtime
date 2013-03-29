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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCResult;
import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.serializer.FixUp;
import org.jabsorb.ng.serializer.SerializerState;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * [START HERE] A factory to create proxies for access to remote Jabsorb
 * services.
 * 
 * TODO: enhance code readability
 * 
 * TODO: add a logger
 */
public class Client implements InvocationHandler {

    /** The class loader to use */
    private ClassLoader pClassLoader;

    /** Maintain a unique id for each message */
    private final AtomicInteger pId = new AtomicInteger();

    /** Proxy object -&gt; remote key */
    private final Map<Object, String> pProxyMap = new HashMap<Object, String>();

    /** The Jabsorb serializer */
    private JSONSerializer pSerializer;

    /** The underlying HTTP session */
    private ISession pSession;

    /**
     * Create a client given a pSession
     * 
     * @param aSession
     *            -- transport pSession to use for this connection
     */
    public Client(final ISession aSession) {

        this(aSession, null);
    }

    /**
     * Create a client given a session
     * 
     * @param aSession
     *            -- transport pSession to use for this connection
     * @param aClassLoader
     *            -- Serializer class loader
     */
    public Client(final ISession aSession, final ClassLoader aClassLoader) {

        try {
            if (aClassLoader == null) {
                pClassLoader = getClass().getClassLoader();

            } else {
                pClassLoader = aClassLoader;
            }

            pSession = aSession;
            pSerializer = new JSONSerializer(pClassLoader);
            pSerializer.registerDefaultSerializers();

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

        pProxyMap.remove(proxy);
    }

    /**
     * Tries to get the requested constructor for the given class
     * 
     * @param aClass
     *            A class
     * @param aParameters
     *            The requested constructor parameters
     * @return The found constructor, null on error
     */
    private <T> Constructor<T> getConstructor(final Class<T> aClass,
            final Class<?>... aParameters) {

        try {
            return aClass.getConstructor(aParameters);

        } catch (final SecurityException ex) {
            // Illegal access
            System.out
                    .println(MessageFormat.format(
                            "Illegal access to a constructor of {0}",
                            aClass.getName()));

        } catch (final NoSuchMethodException ex) {
            // Constructor not found, ignore
        }

        return null;
    }

    /**
     * Retrieves the next message ID
     * 
     * @return the next message ID
     */
    private int getId() {

        return pId.getAndIncrement();
    }

    /**
     * Allow access to the serializer
     * 
     * @return The serializer for this class
     */
    public JSONSerializer getSerializer() {

        return pSerializer;
    }

    /**
     * This method is public because of the inheritance from the
     * InvokationHandler -- should never be called directly.
     */
    @Override
    public Object invoke(final Object proxyObj, final Method method,
            final Object[] args) throws Throwable {

        final String methodName = method.getName();
        if (methodName.equals("hashCode")) {
            return new Integer(System.identityHashCode(proxyObj));
        } else if (methodName.equals("equals")) {
            return (proxyObj == args[0] ? Boolean.TRUE : Boolean.FALSE);
        } else if (methodName.equals("toString")) {
            return proxyObj.getClass().getName() + '@'
                    + Integer.toHexString(proxyObj.hashCode());
        }
        return invoke(pProxyMap.get(proxyObj), method.getName(), args,
                method.getReturnType());
    }

    private Object invoke(final String objectTag, final String methodName,
            final Object[] args, final Class<?> returnType) throws Throwable {

        final int id = getId();
        final JSONObject message = new JSONObject();
        String methodTag = objectTag == null ? "" : objectTag + ".";
        methodTag += methodName;
        message.put("method", methodTag);

        {
            final SerializerState state = new SerializerState();

            if (args != null) {

                final JSONArray params = (JSONArray) pSerializer.marshall(
                        state, /* parent */
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

        final JSONObject responseMessage = pSession.sendAndReceive(message);

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
        return pSerializer.unmarshall(new SerializerState(), returnType,
                rawResult);
    }

    /**
     * Tries to make a Throwable object from the given class name and message
     * 
     * @param aJavaClass
     *            A Throwable class name
     * @param aMessage
     *            An error message
     * @return The Throwable instance, or null
     */
    private Throwable makeThrowable(final String aJavaClass,
            final String aMessage) {

        try {
            // Try to find the class
            @SuppressWarnings("unchecked")
            final Class<? extends Throwable> clazz = (Class<? extends Throwable>) pClassLoader
                    .loadClass(aJavaClass);
            if (!Throwable.class.isAssignableFrom(clazz)) {
                // Not an exception class
                return null;
            }

            // 'message' constructor
            Constructor<? extends Throwable> ctor = getConstructor(clazz,
                    String.class);
            if (ctor != null) {
                return ctor.newInstance(aMessage);
            }

            // default constructor
            ctor = getConstructor(clazz, (Class<?>[]) null);
            if (ctor != null) {
                return ctor.newInstance((Object[]) null);
            }

        } catch (final ClassNotFoundException ex) {
            // Class not found...
            System.out.println(MessageFormat.format(
                    "Exception class not found: {0}", aJavaClass));

        } catch (final IllegalArgumentException ex) {
            // Invalid message
            System.err.println("Invalid argument for the exception");
            ex.printStackTrace();

        } catch (final InstantiationException ex) {
            // Error instantiating the exception
            System.err.println("Error instantiating the exception");
            ex.printStackTrace();

        } catch (final IllegalAccessException ex) {
            // Can't access the class
            System.err.println("Can't instantiate the exception");
            ex.printStackTrace();

        } catch (final InvocationTargetException ex) {
            // Error calling the exception constructor
            System.err.println("Error calling the exception constructor");
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Create a proxy for communicating with the remote service.
     * 
     * @param aKey
     *            the remote object key
     * @param aClass
     *            the class of the interface the remote object should adhere to
     * @return created proxy
     */
    public Object openProxy(final String aKey, final ClassLoader aClassLoader,
            final Class<?>[] aClasses) {

        final Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                aClassLoader, aClasses, this);
        pProxyMap.put(proxy, aKey);
        return proxy;
    }

    /**
     * Generate and throw exception based on the data in the 'responseMessage'
     * 
     * @throws Throwable
     *             Throws the correct exception object, or an
     *             {@link ErrorResponse}.
     */
    protected void processException(final JSONObject responseMessage)
            throws Throwable {

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

            Throwable throwable = null;
            if (error.has("javaClass")) {
                // Throw an exception according to the javaClass entry
                final String exceptionClass = error.getString("javaClass");

                // Make a complete message
                final StringBuilder traceMessage = new StringBuilder(msg);
                if (trace != null) {
                    traceMessage.append("\nTrace:\n").append(trace);
                }

                // Instantiate the exception object
                throwable = makeThrowable(exceptionClass,
                        traceMessage.toString());
            }

            if (throwable == null) {
                // Default Jabsorb exception
                throwable = new ErrorResponse(code, msg, trace);
            }

            throw throwable;

        } else {
            throw new ErrorResponse(new Integer(JSONRPCResult.CODE_ERR_PARSE),
                    MessageFormat.format("Unknown response: {0}",
                            responseMessage.toString(2)), null);
        }
    }
}
