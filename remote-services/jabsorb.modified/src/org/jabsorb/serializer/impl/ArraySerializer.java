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

package org.jabsorb.serializer.impl;

import java.lang.reflect.Array;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Responsible for serialising Java arrays
 */
public class ArraySerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * The classes that this can serialise
   */
  private final static Class[] _serializableClasses = new Class[] {
      int[].class, short[].class, long[].class, float[].class, double[].class,
      boolean[].class, Integer[].class, Short[].class, Long[].class,
      Float[].class, Double[].class, Boolean[].class, String[].class };

  /**
   * The class that this serialises to
   */
  private final static Class[] _JSONClasses = new Class[] { JSONArray.class };

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    Class cc = clazz.getComponentType();
    return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || 
      jsonClazz==JSONArray.class) && (clazz.isArray() && !cc.isPrimitive())) || 
      (clazz==java.lang.Object.class && jsonClazz == JSONArray.class));
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONArray jso = (JSONArray) o;
    Class cc = clazz.getComponentType();
    int i = 0;
    ObjectMatch m = new ObjectMatch(-1);
    state.setSerialized(o, m);
    try
    {
      for (; i < jso.length(); i++)
      {
        m.setMismatch(ser.tryUnmarshall(state, cc, jso.get(i)).max(m).getMismatch());
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage()
          + " not found in json object", e);
    }
    return m;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONArray jso = (JSONArray) o;
    Class cc = clazz.getComponentType();
    int i = 0;
    try
    {
      // TODO: Is there a nicer way of doing this without all the ifs?
      if (clazz == int[].class)
      {
        int arr[] = new int[jso.length()];
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ((Number) ser.unmarshall(state, cc, jso.get(i))).intValue();
        }
        return arr;
      }
      else if (clazz == byte[].class)
      {
        byte arr[] = new byte[jso.length()];
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ((Number) ser.unmarshall(state, cc, jso.get(i))).byteValue();
        }
        return arr;
      }
      else if (clazz == short[].class)
      {
        short arr[] = new short[jso.length()];
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ((Number) ser.unmarshall(state, cc, jso.get(i)))
              .shortValue();
        }
        return arr;
      }
      else if (clazz == long[].class)
      {
        long arr[] = new long[jso.length()];
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ((Number) ser.unmarshall(state, cc, jso.get(i))).longValue();
        }
        return arr;
      }
      else if (clazz == float[].class)
      {
        float arr[] = new float[jso.length()];
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ((Number) ser.unmarshall(state, cc, jso.get(i)))
              .floatValue();
        }
        return arr;
      }
      else if (clazz == double[].class)
      {
        double arr[] = new double[jso.length()];
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ((Number) ser.unmarshall(state, cc, jso.get(i)))
              .doubleValue();
        }
        return arr;
      }
      else if (clazz == char[].class)
      {
        char arr[] = new char[jso.length()];
        for (; i < jso.length(); i++)
        {
          arr[i] = ((String) ser.unmarshall(state, cc, jso.get(i))).charAt(0);
        }
        return arr;
      }
      else if (clazz == boolean[].class)
      {
        boolean arr[] = new boolean[jso.length()];
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ((Boolean) ser.unmarshall(state, cc, jso.get(i)))
              .booleanValue();
        }
        return arr;
      }
      else
      {
        Object arr[] = (Object[]) Array.newInstance(
            clazz==java.lang.Object.class?java.lang.Object.class:cc,
            jso.length());
        state.setSerialized(o, arr);
        for (; i < jso.length(); i++)
        {
          arr[i] = ser.unmarshall(state, cc, jso.get(i));
        }
        return arr;
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("element " + i + " " + e.getMessage()
          + " not found in json object", e);
    }
  }

  public Object marshall(SerializerState state, Object p, Object o)
      throws MarshallException
  {
    try
    {
      JSONArray arr = new JSONArray();
      if (o instanceof int[])
      {
        int a[] = (int[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof long[])
      {
        long a[] = (long[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof short[])
      {
        short a[] = (short[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof byte[])
      {
        byte a[] = (byte[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof float[])
      {
        float a[] = (float[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof double[])
      {
        double a[] = (double[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof char[])
      {
        char a[] = (char[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof boolean[])
      {
        boolean a[] = (boolean[]) o;
        for (int i = 0; i < a.length; i++)
        {
          arr.put(a[i]);
        }
      }
      else if (o instanceof Object[])
      {
        Object a[] = (Object[]) o;
        for (int i = 0; i < a.length; i++)
        {
          Object json = ser.marshall(state, o, a[i], new Integer(i));
          if (JSONSerializer.CIRC_REF_OR_DUPLICATE == json )
          {
            // if dup or circ ref found, put a null slot in
            // the array to maintain the array numbering for the fixups
            arr.put(JSONObject.NULL);
          }
          else
          {
            arr.put(json);
          }
        }
      }
      return arr;

    }
    catch (JSONException e)
    {
      throw new MarshallException(e.getMessage() + " threw json exception", e);
    }

  }
}
