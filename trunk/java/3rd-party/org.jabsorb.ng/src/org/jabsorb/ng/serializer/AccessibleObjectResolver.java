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

package org.jabsorb.ng.serializer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jabsorb.ng.ExceptionTransformer;
import org.jabsorb.ng.JSONRPCResult;
import org.jabsorb.ng.JSONSerializer;
import org.jabsorb.ng.callback.CallbackController;
import org.jabsorb.ng.localarg.LocalArgController;
import org.jabsorb.ng.logging.ILogger;
import org.jabsorb.ng.logging.LoggerFactory;
import org.jabsorb.ng.reflect.AccessibleObjectKey;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Attempts to resolve a set of arguments to the best possible
 * method/constructor.
 */
public class AccessibleObjectResolver {
    /**
     * The logger for this class
     */
    private final static ILogger log = LoggerFactory
            .getLogger(AccessibleObjectResolver.class);

    /**
     * This is used to order the preference of primitives, as used when
     * overloading a method. Eg, with a(int x) and a(float x), a(1) should call
     * a(int x).
     */
    private static final Map<String, Integer> primitiveRankings;

    static {
        // Ranks the primitives
        int counter = 0;
        primitiveRankings = new HashMap<String, Integer>();
        primitiveRankings.put("byte", new Integer(counter++));
        primitiveRankings.put("short", new Integer(counter++));
        primitiveRankings.put("int", new Integer(counter++));
        primitiveRankings.put("long", new Integer(counter++));
        primitiveRankings.put("float", new Integer(counter++));
        primitiveRankings.put("double", new Integer(counter++));
        primitiveRankings.put("boolean", new Integer(counter++));
    }

    /**
     * Display a method call argument signature for a method as a String for
     * debugging/logging purposes. The string contains the comma separated list
     * of argument types that the given method takes.
     * 
     * @param accessibleObject
     *            Method instance to display the argument signature for.
     * @return the argument signature for the method, as a String.
     */
    private static String argSignature(final AccessibleObject accessibleObject) {

        Class<?>[] param;
        if (accessibleObject instanceof Method) {
            param = ((Method) accessibleObject).getParameterTypes();
        } else
        // if(accessibleObject instanceof Constructor)
        {
            param = ((Constructor<?>) accessibleObject).getParameterTypes();
        }

        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < param.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(param[i].getName());
        }
        return buf.toString();
    }

    /**
     * Creates a signature for an array of arguments
     * 
     * @param arguments
     *            The argumnts
     * @return A comma seperated string listing the arguments
     */
    private static String argSignature(final JSONArray arguments) {

        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < arguments.length(); i += 1) {
            if (i > 0) {
                buf.append(",");
            }
            Object jso;

            try {
                jso = arguments.get(i);
            } catch (final JSONException e) {
                throw (NoSuchElementException) new NoSuchElementException(
                        e.getMessage()).initCause(e);
            }

            if (jso == null) {
                buf.append("java.lang.Object");
            } else if (jso instanceof String) {
                buf.append("java.lang.String");
            } else if (jso instanceof Number) {
                buf.append("java.lang.Number");
            } else if (jso instanceof JSONArray) {
                buf.append("java.lang.Object[]");
            } else {
                buf.append("java.lang.Object");
            }
        }
        return buf.toString();
    }

    /**
     * Returns the more fit of the two method candidates
     * 
     * @param methodCandidate
     *            One of the methodCandidates to compare
     * @param methodCandidate1
     *            The other of the methodCandidates to compare
     * @return The better of the two candidates
     */
    private static AccessibleObjectCandidate betterSignature(
            final AccessibleObjectCandidate methodCandidate,
            final AccessibleObjectCandidate methodCandidate1) {

        final Class<?>[] parameters = methodCandidate.getParameterTypes();
        final Class<?>[] parameters1 = methodCandidate1.getParameterTypes();

        int c = 0, c1 = 0;
        for (int i = 0; i < parameters.length; i++) {
            final Class<?> parameterClass = parameters[i];
            final Class<?> parameterClass1 = parameters1[i];
            if (parameterClass != parameterClass1) {
                // We need to do a special check first between the classes,
                // because
                // isAssignableFrom() doesn't work between primitives.
                if (parameterClass.isPrimitive()
                        && parameterClass1.isPrimitive()) {
                    if (primitiveRankings.get(parameterClass.getName())
                            .intValue() < primitiveRankings.get(
                            parameterClass1.getName()).intValue()) {
                        c++;
                    } else {
                        c1++;
                    }
                } else if (parameterClass.isAssignableFrom(parameterClass1)) {
                    c1++;
                } else {
                    c++;
                }
            }
        }
        if (c1 > c) {
            return methodCandidate1;
        }

        return methodCandidate;
    }

    /**
     * Calls a method/constructor
     * 
     * @param accessibleObject
     *            = The method/constructor to invoke
     * @param context
     *            The context of the caller. This will be the servlet request
     *            and response objects in an http servlet call environment.
     *            These are used to insert local arguments (e.g. the request,
     *            response or session,etc.) when found in the java method call
     *            argument signature.
     * @param arguments
     *            The arguments to the method/object
     * @param javascriptObject
     *            The object on which the method is called
     * @param requestId
     *            The id of the request that asked for this invocation.
     * @param serializer
     *            The main json serialiser.
     * @param cbc
     *            The main callback controller
     * @param exceptionTransformer
     *            Renders the exception into a suitable text format
     * @return The result of the call transformed into json in a JSONRPCResult
     */
    public static JSONRPCResult invokeAccessibleObject(
            final AccessibleObject accessibleObject, final Object context[],
            final JSONArray arguments, final Object javascriptObject,
            final Object requestId, final JSONSerializer serializer,
            final CallbackController cbc,
            final ExceptionTransformer exceptionTransformer) {

        JSONRPCResult result;
        // Call the method
        try {
            final boolean isConstructor = accessibleObject instanceof Constructor;

            if (log.isDebugEnabled()) {
                if (!isConstructor) {
                    log.debug("invoking "
                            + ((Method) accessibleObject).getReturnType()
                                    .getName() + " "
                            + ((Method) accessibleObject).getName() + "("
                            + argSignature(accessibleObject) + ")");
                } else {
                    log.debug("invoking "
                            + ((Constructor<?>) accessibleObject).getName()
                            + " " + "(" + argSignature(accessibleObject) + ")");
                }
            }

            final Class<?>[] parameterTypes;

            if (isConstructor) {
                parameterTypes = ((Constructor<?>) accessibleObject)
                        .getParameterTypes();
            } else {
                parameterTypes = ((Method) accessibleObject)
                        .getParameterTypes();
            }

            // Unmarshall arguments
            final Object javaArgs[] = AccessibleObjectResolver.unmarshallArgs(
                    context, parameterTypes, arguments, serializer);

            // Call pre invoke callbacks
            if (cbc != null) {
                for (int i = 0; i < context.length; i++) {
                    cbc.preInvokeCallback(context[i], javascriptObject,
                            accessibleObject, javaArgs);
                }
            }

            // Invoke the method
            final Object returnObj;
            if (isConstructor) {
                returnObj = ((Constructor<?>) accessibleObject)
                        .newInstance(javaArgs);
            } else {
                returnObj = ((Method) accessibleObject).invoke(
                        javascriptObject, javaArgs);
            }
            // Call post invoke callbacks
            if (cbc != null) {
                for (int i = 0; i < context.length; i++) {
                    cbc.postInvokeCallback(context[i], javascriptObject,
                            accessibleObject, returnObj);
                }
            }

            // Marshall the result
            final SerializerState serializerState = new SerializerState();
            final Object json = serializer.marshall(serializerState, null,
                    returnObj, "r");
            result = new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId,
                    json, serializerState.getFixUps());

            // Handle exceptions creating exception results and
            // calling error callbacks
        } catch (final UnmarshallException e) {
            if (cbc != null) {
                for (int i = 0; i < context.length; i++) {
                    cbc.errorCallback(context[i], javascriptObject,
                            accessibleObject, e);
                }
            }

            e.printStackTrace();

            result = new JSONRPCResult(JSONRPCResult.CODE_ERR_UNMARSHALL,
                    requestId, e.getMessage());
        } catch (final MarshallException e) {
            if (cbc != null) {
                for (int i = 0; i < context.length; i++) {
                    cbc.errorCallback(context[i], javascriptObject,
                            accessibleObject, e);
                }
            }
            result = new JSONRPCResult(JSONRPCResult.CODE_ERR_MARSHALL,
                    requestId, e.getMessage());
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }

            // handle Jetty continuations-- this is kind of a hack
            // but at least this will work without requiring jetty as
            // a dependent library
            if ("org.mortbay.jetty.RetryRequest".equals(e.getClass().getName())) {
                throw (RuntimeException) e;
            }

            if (cbc != null) {
                for (int i = 0; i < context.length; i++) {
                    cbc.errorCallback(context[i], javascriptObject,
                            accessibleObject, e);
                }
            }
            result = new JSONRPCResult(JSONRPCResult.CODE_REMOTE_EXCEPTION,
                    requestId, exceptionTransformer.transform(e));
        }
        return result;
    }

    /**
     * Resolve which method the caller is requesting
     * <p/>
     * If a method with the requested number of arguments does not exist at all,
     * null will be returned.
     * <p/>
     * If the object or class (for static methods) being invoked contains more
     * than one overloaded methods that match the method key signature, find the
     * closest matching method to invoke according to the JSON arguments being
     * passed in.
     * 
     * @param aMethodMap
     *            Map keyed by MethodKey objects and the values will be either a
     *            Method object, or an array of Method objects, if there is more
     *            than one possible method that can be invoked matching the
     *            MethodKey.
     * @param methodName
     *            method name being called.
     * @param arguments
     *            JSON arguments to the method, as a JSONArray.
     * @param serializer
     *            The main json serialiser.
     * @return the Method that most closely matches the call signature, or null
     *         if there is not a match.
     */
    public static AccessibleObject resolveMethod(
            final Map<AccessibleObjectKey, List<? extends AccessibleObject>> aMethodMap,
            final String methodName, final JSONArray arguments,
            final JSONSerializer serializer) {

        // first, match soley by the method name and number of arguments passed
        // in
        // if there is a single match, return the single match
        // if there is no match at all, return null
        // if there are multiple matches, fall through to the second matching
        // phase
        // below
        final AccessibleObjectKey mk = new AccessibleObjectKey(methodName,
                arguments.length());
        // of AccessibleObject
        final List<? extends AccessibleObject> accessibleObjects = aMethodMap
                .get(mk);
        if (accessibleObjects == null || accessibleObjects.size() == 0) {
            return null;
        } else if (accessibleObjects.size() == 1) {
            return accessibleObjects.get(0);
        } else {
            // second matching phase: there were overloaded methods on the
            // object
            // we are invoking so try and find the best match based on the types
            // of
            // the arguments passed in.

            // try and unmarshall the arguments against each candidate method
            // to determine which one matches the best

            final List<AccessibleObjectCandidate> candidate = new ArrayList<AccessibleObjectCandidate>();
            if (log.isDebugEnabled()) {
                log.debug("looking for method " + methodName + "("
                        + argSignature(arguments) + ")");
            }
            for (int i = 0; i < accessibleObjects.size(); i++) {
                final AccessibleObject accessibleObject = accessibleObjects
                        .get(i);
                Class<?>[] parameterTypes = null;
                if (accessibleObject instanceof Method) {
                    parameterTypes = ((Method) accessibleObject)
                            .getParameterTypes();
                } else if (accessibleObject instanceof Constructor) {
                    parameterTypes = ((Constructor<?>) accessibleObject)
                            .getParameterTypes();
                }

                try {
                    candidate.add(tryUnmarshallArgs(accessibleObject,
                            arguments, parameterTypes, serializer));
                    if (log.isDebugEnabled()) {
                        log.debug("+++ possible match with method "
                                + methodName + "("
                                + argSignature(accessibleObject) + ")");
                    }
                } catch (final Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("xxx " + e.getMessage() + " in " + methodName
                                + "(" + argSignature(accessibleObject) + ")");
                    }
                }
            }
            // now search through all the candidates and find one which matches
            // the json arguments the closest
            AccessibleObjectCandidate best = null;
            for (int i = 0; i < candidate.size(); i++) {
                final AccessibleObjectCandidate c = candidate.get(i);
                if (best == null) {
                    best = c;
                    continue;
                }
                final ObjectMatch bestMatch = best.getMatch();
                final ObjectMatch cMatch = c.getMatch();
                if (bestMatch.getMismatch() > cMatch.getMismatch()) {
                    best = c;
                } else if (bestMatch.getMismatch() == cMatch.getMismatch()) {
                    best = betterSignature(best, c);
                }
            }
            if (best != null) {
                final AccessibleObject ao = best.getAccessibleObject();
                if (log.isDebugEnabled()) {
                    log.debug("found method " + methodName + "("
                            + argSignature(ao) + ")");
                }
                return ao;
            }
        }

        return null;
    }

    /**
     * Tries to unmarshall the arguments to a method
     * 
     * @param accessibleObject
     *            The method/constructor to unmarshall the arguments for.
     * @param arguments
     *            The arguments to unmarshall
     * @param parameterTypes
     *            The parameters of the method/construcot
     * @param serializer
     *            The main json serialiser.
     * @return The MethodCandidate that should suit the arguements and method.
     * @throws UnmarshallException
     *             If one of the arguments cannot be unmarshalled
     */
    private static AccessibleObjectCandidate tryUnmarshallArgs(
            final AccessibleObject accessibleObject, final JSONArray arguments,
            final Class<?>[] parameterTypes, final JSONSerializer serializer)
            throws UnmarshallException {

        int i = 0;
        final ObjectMatch[] matches = new ObjectMatch[parameterTypes.length];
        try {
            int nonLocalArgIndex = 0;
            for (; i < parameterTypes.length; i++) {
                final SerializerState serialiserState = new SerializerState();
                if (LocalArgController.isLocalArg(parameterTypes[i])) {
                    // TODO: do this on the actual candidate?
                    matches[i] = ObjectMatch.OKAY;
                } else {
                    matches[i] = serializer.tryUnmarshall(serialiserState,
                            parameterTypes[i],
                            arguments.get(nonLocalArgIndex++));
                }
            }
        } catch (final JSONException e) {
            throw (NoSuchElementException) new NoSuchElementException(
                    e.getMessage()).initCause(e);
        } catch (final UnmarshallException e) {
            throw new UnmarshallException("arg " + (i + 1) + " "
                    + e.getMessage(), e);
        }
        final AccessibleObjectCandidate candidate = new AccessibleObjectCandidate(
                accessibleObject, parameterTypes, matches);

        return candidate;
    }

    /**
     * Convert the arguments to a method call from json into java objects to be
     * used for invoking the method, later.
     * 
     * @param context
     *            the context of the caller. This will be the servlet request
     *            and response objects in an http servlet call environment.
     *            These are used to insert local arguments (e.g. the request,
     *            response or session,etc.) when found in the java method call
     *            argument signature.
     * @param param
     *            the classes of the arguments to the function.
     * @param arguments
     *            the arguments from the caller, in json format.
     * @param serializer
     *            The main json serializer.
     * @return the java arguments as unmarshalled from json.
     * @throws UnmarshallException
     *             if there is a problem unmarshalling the arguments.
     */
    private static Object[] unmarshallArgs(final Object context[],
            final Class<?>[] param, final JSONArray arguments,
            final JSONSerializer serializer) throws UnmarshallException {

        final Object javaArgs[] = new Object[param.length];
        int i = 0, j = 0;
        try {
            for (; i < param.length; i++) {
                final SerializerState serializerState = new SerializerState();
                if (LocalArgController.isLocalArg(param[i])) {
                    javaArgs[i] = LocalArgController.resolveLocalArg(context,
                            param[i]);
                } else {
                    javaArgs[i] = serializer.unmarshall(serializerState,
                            param[i], arguments.get(j++));
                }
            }
        } catch (final JSONException e) {
            throw (NoSuchElementException) new NoSuchElementException(
                    e.getMessage()).initCause(e);
        } catch (final UnmarshallException e) {
            throw new UnmarshallException("arg " + (i + 1)
                    + " could not unmarshall", e);
        }

        return javaArgs;
    }
}
