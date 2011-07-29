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

package org.jabsorb.callback;

import java.io.Serializable;

/**
 * Container class for information about callbacks and the transport they are
 * associated with.
 */
public class CallbackData implements Serializable
{
  /**
   * Generated version id.
   */
  private final static long serialVersionUID = 2;

  /**
   * The object implementing the InvocationCallback Interface
   */
  private final InvocationCallback cb;

  /**
   * The type of transport Context interface the callback is interested in eg.
   * HttpServletRequest.class for the servlet transport.
   */
  private final Class contextInterface;

  /**
   * Creates a new Callback data holder
   * 
   * @param cb The object implementing the InvocationCallback Interface
   * @param contextInterface The type of transport Context interface the
   *          callback is interested in eg. HttpServletRequest.class for the
   *          servlet transport.
   */
  public CallbackData(InvocationCallback cb, Class contextInterface)
  {
    this.cb = cb;
    this.contextInterface = contextInterface;
  }

  /**
   * Whether two CallbackDatas are the same.
   * 
   * @param o The object to compare with this one.
   * @return true If they share the same callback and contextInterface
   */
  public boolean equals(Object o)
  {
    CallbackData cmp = (CallbackData) o;
    return (cb.equals(cmp.cb) && contextInterface.equals(cmp.contextInterface));
  }

  /**
   * Gets the callback
   * 
   * @return InvocationCallback
   */
  public InvocationCallback getCallback()
  {
    return cb;
  }

  /**
   * Creates a hash for this object.
   * 
   * @return a unique key.
   */
  public int hashCode()
  {
    return cb.hashCode() * contextInterface.hashCode();
  }

  /**
   * Whether the context for this object can be used with the given object
   * 
   * @param context The object to test
   * @return Whether the context for this object is assignable from the given
   *         object.
   */
  public boolean understands(Object context)
  {
    return contextInterface.isAssignableFrom(context.getClass());
  }
}
