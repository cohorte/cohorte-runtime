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

package org.jabsorb.serializer;

/**
 * Thrown by Serializer objects when they are unable to Marshall the Java
 * objects into JSON objects.
 */
public class MarshallException extends Exception
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Creates a new MarshallException.
   * 
   * @param msg the message to be displayed.
   */
  public MarshallException(String msg)
  {
    super(msg);
  }

  /**
   * Creates a new MarshallException.
   * 
   * @param msg   the message to be displayed
   * @param cause the cause.  (A <tt>null</tt> value is permitted, and indicates
   *              that the cause is nonexistent or unknown.)
   */
  public MarshallException(String msg, Throwable cause)
  {
    super(msg,cause);
  }
}
