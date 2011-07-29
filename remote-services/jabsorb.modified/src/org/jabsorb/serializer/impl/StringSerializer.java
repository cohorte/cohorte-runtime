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

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;

/**
 * Serialises String values
 */
public class StringSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { String.class,
      char.class, Character.class, byte[].class, char[].class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { String.class,
      Integer.class };

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
    if (o instanceof Character)
    {
      return o.toString();
    }
    else if (o instanceof byte[])
    {
      return new String((byte[]) o);
    }
    else if (o instanceof char[])
    {
      return new String((char[]) o);
    }
    else
    {
      return o;
    }
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
      Object jso) throws UnmarshallException
  {
    //For some reason getClass can be String but getClasses will return an 
    //empty array. This catches this.
    if(jso.getClass().equals(String.class))
    {
      return ObjectMatch.OKAY;
    }
    Class classes[] = jso.getClass().getClasses();
    for(int i=0;i<classes.length;i++)
    {
      if(classes[i].equals(String.class))
      {
        state.setSerialized(jso, ObjectMatch.OKAY);
        return ObjectMatch.OKAY;
      }
    }
    
    state.setSerialized(jso, ObjectMatch.SIMILAR);
    return ObjectMatch.SIMILAR;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object jso)
      throws UnmarshallException
  {
    Object returnValue;
    String val = jso instanceof String ? (String) jso : jso.toString();
    if (clazz == char.class)
    {
      returnValue = new Character(val.charAt(0));
    }
    else if (clazz == byte[].class)
    {
      returnValue = val.getBytes();
    }
    else if (clazz == char[].class)
    {
      returnValue = val.toCharArray();
    }
    else
    {
      returnValue = val;
    }
    state.setSerialized(jso, returnValue);
    return returnValue;
  }

}
