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

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serialises classes that have been registered on the bridge as references or
 * callable references.
 */
public class ReferenceSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * The logger for this class
   */
  private final static Logger log = LoggerFactory
      .getLogger(ReferenceSerializer.class);

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] {};

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] {};

  /**
   * A reference to the bridge
   */
  private JSONRPCBridge bridge;

  /**
   * Creates a new ReferenceSerializer
   * 
   * @param bridge The bridge to determine if a class is a reference.
   * 
   * TODO: Should reference detection be abstracted out into another class?
   */
  public ReferenceSerializer(JSONRPCBridge bridge)
  {
    this.bridge = bridge;
  }

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (!clazz.isArray() && !clazz.isPrimitive() && !clazz.isInterface()
        && (bridge.isReference(clazz) || bridge.isCallableReference(clazz)) && (jsonClazz == null || jsonClazz == JSONObject.class));
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
    Class clazz = o.getClass();
    Integer identity = new Integer(System.identityHashCode(o));
    if (bridge.isReference(clazz))
    {
      if (log.isDebugEnabled())
      {
        log.debug("marshalling reference to object " + identity + " of class "
            + clazz.getName());
      }
      bridge.addReference(o);
      JSONObject jso = new JSONObject();
      try
      {
        jso.put("JSONRPCType", "Reference");
        jso.put("javaClass", clazz.getName());
        jso.put("objectID", identity);
      }
      catch (JSONException e)
      {
        throw new MarshallException(e.getMessage(), e);
      }
      return jso;
    }
    else if (bridge.isCallableReference(clazz))
    {
      if (log.isDebugEnabled())
      {
        log.debug("marshalling callable reference to object " + identity
            + " of class " + clazz.getName());
      }
      bridge.registerObject(identity, o);
      bridge.addReference(o);
      
      JSONObject jso = new JSONObject();
      try
      {
        jso.put("JSONRPCType", "CallableReference");
        jso.put("javaClass", clazz.getName());
        jso.put("objectID", identity);
      }
      catch (JSONException e)
      {
        throw new MarshallException(e.getMessage(), e);
      }

      return jso;
    }
    return null;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    state.setSerialized(o, ObjectMatch.OKAY);
    return ObjectMatch.OKAY;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    Object ref = null;
    String json_type;
    int object_id;
    try
    {
      json_type = jso.getString("JSONRPCType");
      object_id = jso.getInt("objectID");
    }
    catch (JSONException e)
    {
      throw new UnmarshallException(e.getMessage(), e);
    }
    if (json_type != null)
    {
      if((json_type.equals("Reference"))||(json_type.equals("CallableReference")))
      {
        ref = bridge.getReference(object_id);
      }
    }
    state.setSerialized(o, ref);
    return ref;
  }

}
