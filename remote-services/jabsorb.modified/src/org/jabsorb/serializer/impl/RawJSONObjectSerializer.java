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

import java.util.Iterator;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.JSONSerializer;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Formats the Java JSONObject object.
 */
public class RawJSONObjectSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { JSONObject.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

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
    // reprocess the raw json in order to fixup circular references and duplicates
    JSONObject jsonIn = (JSONObject) o;
    JSONObject jsonOut = new JSONObject();
    String key = null;
    try
    {
      Iterator i = jsonIn.keys();
      while (i.hasNext())
      {
        key = (String) i.next();

        Object j = ser.marshall(state, o, jsonIn.get(key), key);

        // omit the object entirely if it's a circular reference or duplicate
        // it will be regenerated in the fixups phase
        if (JSONSerializer.CIRC_REF_OR_DUPLICATE != j)
        {
          jsonOut.put(key, j);
        }
      }
    }
    catch (MarshallException e)
    {
      throw new MarshallException("JSONObject key " + key + " " + e.getMessage(), e);
    }
    catch (JSONException e)
    {
      throw new MarshallException("JSONObject key " + key + " " + e.getMessage(), e);
    }
    return jsonOut;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
      Object jso) throws UnmarshallException
  {
    state.setSerialized(jso, ObjectMatch.OKAY);
    return ObjectMatch.OKAY;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object jso)
      throws UnmarshallException
  {
    state.setSerialized(jso, jso);
    return jso;
  }

}
