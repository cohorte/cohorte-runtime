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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.JSONSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises Hashtables
 * 
 * TODO: why not use a map serialiser?
 */
public class DictionarySerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { Hashtable.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Dictionary.class
        .isAssignableFrom(clazz)));
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Object marshall(SerializerState state, Object p, Object o)
      throws MarshallException
  {
    Dictionary ht = (Dictionary) o;
    JSONObject obj = new JSONObject();
    JSONObject mapdata = new JSONObject();

    try
    {
      if (ser.getMarshallClassHints())
      {
        obj.put("javaClass", o.getClass().getName());
      }
      obj.put("map", mapdata);
      state.push(o,mapdata,"map");
    }
    catch (JSONException e)
    {
      throw new MarshallException("Could not put data"+ e.getMessage(), e);
    }
    Object key = null;

    try
    {
      Enumeration en = ht.keys();
      while (en.hasMoreElements())
      {
        key = en.nextElement();
        String keyString = key.toString();  // only support String keys

        Object json = ser.marshall(state, mapdata, ht.get(key), keyString);

        // omit the object entirely if it's a circular reference or duplicate
        // it will be regenerated in the fixups phase
        if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json)
        {
          mapdata.put(keyString,json );
        }

      }
    }
    catch (MarshallException e)
    {
      throw new MarshallException("map key " + key + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new MarshallException("map key " + key + " " + e.getMessage(), e);
    }
    finally
    {
      state.pop();
    }
    return obj;
  }

  // TODO: try unMarshall and unMarshall share 90% code. Put in into an
  // intermediate function.
  // TODO: Also cache the result somehow so that an unmarshall
  // following a tryUnmarshall doesn't do the same work twice!
  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class;
    try
    {
      java_class = jso.getString("javaClass");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read javaClass", e);
    }
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    if (!(java_class.equals("java.util.Dictionary") || java_class
        .equals("java.util.Hashtable")))
    {
      throw new UnmarshallException("not a Dictionary");
    }
    JSONObject jsonmap;
    try
    {
      jsonmap = jso.getJSONObject("map");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("map missing", e);
    }
    if (jsonmap == null)
    {
      throw new UnmarshallException("map missing");
    }
    ObjectMatch m = new ObjectMatch(-1);
    state.setSerialized(o, m);

    Iterator i = jsonmap.keys();
    String key = null;
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        m.setMismatch(ser.tryUnmarshall(state, null, jsonmap.get(key)).max(m).getMismatch());
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }

    return m;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class;
    try
    {
      java_class = jso.getString("javaClass");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read javaClass", e);
    }
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    Hashtable ht;
    if (java_class.equals("java.util.Dictionary")
        || java_class.equals("java.util.Hashtable"))
    {
      ht = new Hashtable();
    }
    else
    {
      throw new UnmarshallException("not a Dictionary");
    }
    JSONObject jsonmap;
    try
    {
      jsonmap = jso.getJSONObject("map");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("map missing", e);
    }
    if (jsonmap == null)
    {
      throw new UnmarshallException("map missing");
    }

    state.setSerialized(o, ht);

    Iterator i = jsonmap.keys();
    String key = null;
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        ht.put(key, ser.unmarshall(state, null, jsonmap.get(key)));
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
    }
    return ht;
  }
}
