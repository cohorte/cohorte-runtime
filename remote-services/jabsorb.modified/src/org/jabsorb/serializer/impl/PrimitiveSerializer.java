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
 * Serializes primitive Java values
 */
public class PrimitiveSerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { int.class,
      byte.class, short.class, long.class, float.class, double.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { Integer.class,
      Byte.class, Short.class, Long.class, Float.class, Double.class,
      String.class };

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  /**
   * Converts a javascript object to a Java object
   * 
   * @param clazz The class of the Java object that it should be converted to
   * @param jso The javascript object
   * @return A Java primitive type in its java.lang wrapper.
   * @throws NumberFormatException If clazz is numeric and jso does not parse
   *           into a number.
   */
  public Object toPrimitive(Class clazz, Object jso)
      throws NumberFormatException
  {
    // TODO: is there a better way of doing this instead of all the if elses?
    if (int.class.equals(clazz))
    {
      //TODO: Should something be done about these early returns?
      if (jso instanceof String)
      {
        return new Integer((String) jso);
      }
      //Handle it if it is out of the range of the number
      Number n = (Number) jso;
      if(n.longValue()!=n.intValue())
      {
        throw new NumberFormatException("number is too large for an int");
      }
      if(n.floatValue()!=n.intValue())
      {
        throw new NumberFormatException("number is not an integer");
      }
      return new Integer(n.intValue());
    }
    else if (long.class.equals(clazz))
    {
      if (jso instanceof String)
      {
        return new Long((String) jso);
      }
      Number n = (Number) jso;
      if(n.floatValue()!=n.longValue())
      {
        throw new NumberFormatException("number is not an integer");
      }
      return new Long(n.longValue());
    }
    else if (short.class.equals(clazz))
    {
      if (jso instanceof String)
      {
        return new Short((String) jso);
      }
      //Handle it if it is out of the range of the number
      Number n = (Number) jso;
      if(n.longValue()!=n.shortValue())
      {
        //TODO: is appropriate to throw?
        throw new NumberFormatException("number is too large for an short");
      }
      if(n.floatValue()!=n.shortValue())
      {
        throw new NumberFormatException("number is not an integer");
      }
      return new Short(n.shortValue());
    }
    else if (byte.class.equals(clazz))
    {
      if (jso instanceof String)
      {
        return new Byte((String) jso);
      }
      //Handle it if it is out of the range of the number
      Number n = (Number) jso;
      if(n.longValue()!=n.byteValue())
      {
        //TODO: is appropriate to throw?
        throw new NumberFormatException("number is too large for an short");
      }
      if(n.floatValue()!=n.byteValue())
      {
        throw new NumberFormatException("number is not an integer");
      }
      return new Byte(n.byteValue());
    }
    else if (float.class.equals(clazz))
    {
      if (jso instanceof String)
      {
        return new Float((String) jso);
      }
      Number n = (Number) jso;
      if((n.floatValue()>Float.MAX_VALUE)||(n.floatValue()<-Float.MAX_VALUE))
      {
        throw new NumberFormatException("number is too large for a float");
      }
      return new Float(((Number) jso).floatValue());
    }
    else if (double.class.equals(clazz))
    {
      if (jso instanceof String)
      {
        return new Double((String) jso);
      }
      return new Double(((Number) jso).doubleValue());
    }
    
    return null;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
      Object jso) throws UnmarshallException
  {
    try
    {
      //TODO: This should really check the return instead of just waiting for 
      //an exception. If it returns null, it should fail!
      toPrimitive(clazz, jso);
    }
    catch (NumberFormatException e)
    {
      throw new UnmarshallException("not a primitive", e);
    }
    state.setSerialized(jso, ObjectMatch.OKAY);
    return ObjectMatch.OKAY;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object jso)
      throws UnmarshallException
  {
    try
    {
      Object primitive = toPrimitive(clazz, jso);
      state.setSerialized(jso, primitive);
      return primitive;
    }
    catch (NumberFormatException e)
    {
      throw new UnmarshallException("cannot convert object " + jso
          + " to type " + clazz.getName(), e);
    }
  }

  public Object marshall(SerializerState state, Object p, Object o)
      throws MarshallException
  {
    return o;
  }

}
