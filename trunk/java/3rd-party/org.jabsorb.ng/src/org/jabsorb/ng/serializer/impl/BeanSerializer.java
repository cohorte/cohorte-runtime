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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jabsorb.ng.JSONSerializer;
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
 * Serialises java beans that are known to have readable and writable properties
 */
public class BeanSerializer extends AbstractSerializer {
    /**
     * Stores the readable and writable properties for the Bean.
     */
    protected static class BeanData {
        // TODO: Legacy comment. WTF?
        // in absence of getters and setters, these fields are
        // public to allow subclasses to access.
        /**
         * The bean info for a certain bean
         */
        public BeanInfo beanInfo;

        /**
         * The readable properties of the bean.
         */
        public Map<String, Method> readableProps;

        /**
         * The writable properties of the bean.
         */
        public Map<String, Method> writableProps;
    }

    /**
     * Classes that this can serialise to.
     * 
     * TODO: Yay for bloat!
     */
    private static Class<?>[] _JSONClasses = new Class<?>[] {};

    /**
     * Classes that this can serialise.
     * 
     * TODO: Yay for bloat!
     */
    private static Class<?>[] _serializableClasses = new Class<?>[] {};

    /**
     * Caches analysed beans
     */
    private static HashMap<Class<? extends Object>, BeanData> beanCache = new HashMap<Class<? extends Object>, BeanData>();

    /**
     * The logger for this class
     */
    private final static ILogger log = LoggerFactory
            .getLogger(BeanSerializer.class);

    /**
     * Unique serialisation id.
     */
    private final static long serialVersionUID = 2;

    /**
     * Analyses a bean, returning a BeanData with the data extracted from it.
     * 
     * @param clazz
     *            The class of the bean to analyse
     * @return A populated BeanData
     * @throws IntrospectionException
     *             If a problem occurs during getting the bean info.
     */
    public static BeanData analyzeBean(final Class<? extends Object> clazz)
            throws IntrospectionException {

        log.info("analyzing " + clazz.getName());
        final BeanData bd = new BeanData();
        bd.beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        final PropertyDescriptor props[] = bd.beanInfo.getPropertyDescriptors();
        bd.readableProps = new HashMap<String, Method>();
        bd.writableProps = new HashMap<String, Method>();
        for (int i = 0; i < props.length; i++) {
            // This is declared by enums and shouldn't be shown.
            if (props[i].getName().equals("declaringClass")) {
                continue;
            }
            if (props[i].getWriteMethod() != null) {
                bd.writableProps.put(props[i].getName(),
                        props[i].getWriteMethod());
            }
            if (props[i].getReadMethod() != null) {
                bd.readableProps.put(props[i].getName(),
                        props[i].getReadMethod());
            }
        }
        return bd;
    }

    /**
     * Gets the bean data from cache if possible, otherwise analyses the bean.
     * 
     * @param clazz
     *            The class of the bean to analyse
     * @return A populated BeanData
     * @throws IntrospectionException
     *             If a problem occurs during getting the bean info.
     */
    public static BeanData getBeanData(final Class<? extends Object> clazz)
            throws IntrospectionException {

        BeanData bd;
        synchronized (beanCache) {
            bd = beanCache.get(clazz);
            if (bd == null) {
                bd = analyzeBean(clazz);
                beanCache.put(clazz, bd);
            }
        }
        return bd;
    }

    @Override
    public boolean canSerialize(final Class<?> clazz, final Class<?> jsonClazz) {

        return (!clazz.isArray() && !clazz.isPrimitive()
                && !clazz.isInterface() && (jsonClazz == null || jsonClazz == JSONObject.class));
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

        BeanData bd;
        try {
            bd = getBeanData(o.getClass());
        } catch (final IntrospectionException e) {
            throw new MarshallException(o.getClass().getName()
                    + " is not a bean", e);
        }

        final JSONObject val = new JSONObject();
        if (ser.getMarshallClassHints()) {
            try {
                val.put("javaClass", o.getClass().getName());
            } catch (final JSONException e) {
                throw new MarshallException("JSONException: " + e.getMessage(),
                        e);
            }
        }

        final Object args[] = new Object[0];
        Object result;
        for (final Entry<String, Method> ent : bd.readableProps.entrySet()) {
            final String prop = ent.getKey();
            final Method getMethod = ent.getValue();
            if (log.isDebugEnabled()) {
                log.debug("invoking " + getMethod.getName() + "()");
            }
            try {
                result = getMethod.invoke(o, args);
            } catch (Throwable e) {
                if (e instanceof InvocationTargetException) {
                    e = ((InvocationTargetException) e).getTargetException();
                }
                throw new MarshallException("bean " + o.getClass().getName()
                        + " can't invoke " + getMethod.getName() + ": "
                        + e.getMessage(), e);
            }
            try {
                if (result != null || ser.getMarshallNullAttributes()) {
                    try {
                        final Object json = ser
                                .marshall(state, o, result, prop);

                        // omit the object entirely if it's a circular reference
                        // or duplicate
                        // it will be regenerated in the fixups phase
                        if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json) {
                            val.put(prop, json);
                        }
                    } catch (final JSONException e) {
                        throw new MarshallException("JSONException: "
                                + e.getMessage(), e);
                    }
                }
            } catch (final MarshallException e) {
                throw new MarshallException("bean " + o.getClass().getName()
                        + " " + e.getMessage(), e);
            }
        }

        return val;
    }

    public ObjectMatch tryUnmarshall(final SerializerState state,
            final Class<?> clazz, final Object o) throws UnmarshallException {

        final JSONObject jso = (JSONObject) o;
        BeanData bd;
        try {
            bd = getBeanData(clazz);
        } catch (final IntrospectionException e) {
            throw new UnmarshallException(clazz.getName() + " is not a bean", e);
        }

        int match = 0;
        int mismatch = 0;
        for (final String prop : bd.writableProps.keySet()) {
            if (jso.has(prop)) {
                match++;
            } else {
                mismatch++;
            }
        }
        if (match == 0) {
            throw new UnmarshallException("bean has no matches");
        }

        // create a concrete ObjectMatch that is always returned in order to
        // satisfy circular reference requirements
        final ObjectMatch returnValue = new ObjectMatch(-1);
        state.setSerialized(o, returnValue);

        ObjectMatch m = null;
        ObjectMatch tmp;
        final Iterator<?> i = jso.keys();
        while (i.hasNext()) {
            final String field = (String) i.next();
            final Method setMethod = bd.writableProps.get(field);
            if (setMethod != null) {
                try {
                    final Class<?> param[] = setMethod.getParameterTypes();
                    if (param.length != 1) {
                        throw new UnmarshallException("bean " + clazz.getName()
                                + " method " + setMethod.getName()
                                + " does not have one arg");
                    }
                    tmp = ser.tryUnmarshall(state, param[0], jso.get(field));
                    if (tmp != null) {
                        if (m == null) {
                            m = tmp;
                        } else {
                            m = m.max(tmp);
                        }
                    }
                } catch (final UnmarshallException e) {
                    throw new UnmarshallException("bean " + clazz.getName()
                            + " " + e.getMessage(), e);
                } catch (final JSONException e) {
                    throw new UnmarshallException("bean " + clazz.getName()
                            + " " + e.getMessage(), e);
                }
            } else {
                mismatch++;
            }
        }
        if (m != null) {
            returnValue.setMismatch(m.max(new ObjectMatch(mismatch))
                    .getMismatch());
        } else {
            returnValue.setMismatch(mismatch);
        }
        return returnValue;
    }

    public Object unmarshall(final SerializerState state, final Class<?> clazz,
            final Object o) throws UnmarshallException {

        final JSONObject jso = (JSONObject) o;
        BeanData bd;
        try {
            bd = getBeanData(clazz);
        } catch (final IntrospectionException e) {
            throw new UnmarshallException(clazz.getName() + " is not a bean", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("instantiating " + clazz.getName());
        }
        Object instance;
        try {
            instance = clazz.newInstance();
        } catch (final InstantiationException e) {
            throw new UnmarshallException("could not instantiate bean of type "
                    + clazz.getName() + ", make sure it has a no argument "
                    + "constructor and that it is not an interface or "
                    + "abstract class", e);
        } catch (final IllegalAccessException e) {
            throw new UnmarshallException("could not instantiate bean of type "
                    + clazz.getName(), e);
        } catch (final RuntimeException e) {
            throw new UnmarshallException("could not instantiate bean of type "
                    + clazz.getName(), e);
        }
        state.setSerialized(o, instance);
        final Object invokeArgs[] = new Object[1];
        Object fieldVal;
        final Iterator<?> i = jso.keys();
        while (i.hasNext()) {
            final String field = (String) i.next();
            final Method setMethod = bd.writableProps.get(field);
            if (setMethod != null) {
                try {
                    final Class<?> param[] = setMethod.getParameterTypes();
                    fieldVal = ser.unmarshall(state, param[0], jso.get(field));
                } catch (final UnmarshallException e) {
                    throw new UnmarshallException(
                            "could not unmarshall field \"" + field
                                    + "\" of bean " + clazz.getName(), e);
                } catch (final JSONException e) {
                    throw new UnmarshallException(
                            "could not unmarshall field \"" + field
                                    + "\" of bean " + clazz.getName(), e);
                }
                if (log.isDebugEnabled()) {
                    log.debug("invoking " + setMethod.getName() + "("
                            + fieldVal + ")");
                }
                invokeArgs[0] = fieldVal;
                try {
                    setMethod.invoke(instance, invokeArgs);
                } catch (Throwable e) {
                    if (e instanceof InvocationTargetException) {
                        e = ((InvocationTargetException) e)
                                .getTargetException();
                    }
                    throw new UnmarshallException("bean " + clazz.getName()
                            + "can't invoke " + setMethod.getName() + ": "
                            + e.getMessage(), e);
                }
            }
        }
        return instance;
    }
}
