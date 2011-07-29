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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.JSONSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serialises Maps
 * 
 * TODO: if this serialises a superclass does it need to also specify the
 * subclasses?
 */
public class MapSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { Map.class,
      HashMap.class, TreeMap.class, LinkedHashMap.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Map.class
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
    Map map = (Map) o;
    JSONObject obj = new JSONObject();
    JSONObject mapdata = new JSONObject();
    if (ser.getMarshallClassHints())
    {
      try
      {
        obj.put("javaClass", o.getClass().getName());
      }
      catch (JSONException e)
      {
        throw new MarshallException("javaClass not found!", e);
      }
    }
    try
    {
      obj.put("map", mapdata);
      state.push(o,mapdata,"map");
    }
    catch (JSONException e)
    {
      throw new MarshallException("Could not add map to object: "
          + e.getMessage(), e);
    }
    Object key = null;
    try
    {
      Iterator i = map.entrySet().iterator();
      while (i.hasNext())
      {
        Map.Entry ent = (Map.Entry) i.next();
        key = ent.getKey();
        String keyString = key.toString();  // only support String keys

        Object json = ser.marshall(state, mapdata, ent.getValue(), keyString);

        // omit the object entirely if it's a circular reference or duplicate
        // it will be regenerated in the fixups phase
        if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json)
        {
          mapdata.put(keyString, json);
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
    if (!(java_class.equals("java.util.Map")
        || java_class.equals("java.util.AbstractMap")
        || java_class.equals("java.util.LinkedHashMap")
        || java_class.equals("java.util.TreeMap") || java_class
        .equals("java.util.HashMap")))
    {
      throw new UnmarshallException("not a Map");
    }
    JSONObject jsonmap;
    try
    {
      jsonmap = jso.getJSONObject("map");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read map: " + e.getMessage(), e);
    }
    if (jsonmap == null)
    {
      throw new UnmarshallException("map missing");
    }
    ObjectMatch m = new ObjectMatch(-1);
    Iterator i = jsonmap.keys();
    String key = null;
    state.setSerialized(o, m);
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
    AbstractMap abmap;
    if (java_class.equals("java.util.Map")
        || java_class.equals("java.util.AbstractMap")
        || java_class.equals("java.util.HashMap"))
    {
      abmap = new HashMap();
    }
    else if (java_class.equals("java.util.TreeMap"))
    {
      abmap = new TreeMap();
    }
    else if (java_class.equals("java.util.LinkedHashMap"))
    {
      abmap = new LinkedHashMap();
    }
    else
    {
      throw new UnmarshallException("not a Map");
    }
    JSONObject jsonmap;
    try
    {
      jsonmap = jso.getJSONObject("map");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException("Could not read map: " + e.getMessage(), e);
    }
    if (jsonmap == null)
    {
      throw new UnmarshallException("map missing");
    }
    state.setSerialized(o, abmap);
    Iterator i = jsonmap.keys();
    String key = null;
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        abmap.put(key, ser.unmarshall(state, null, jsonmap.get(key)));
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

    return abmap;
  }

}
