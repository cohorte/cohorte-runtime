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

import java.lang.reflect.AccessibleObject;

/**
 * Interface to be implemented by objects registered for invocation callbacks
 * that include error information.
 */
public interface ErrorInvocationCallback extends InvocationCallback
{
  /**
   * Listener for exceptions thrown from an RPC service.
   * 
   * @param context The transport context (the HttpServletRequest object in the
   *          case of the HTTP transport).
   * @param instance The object instance or null if it is a static method.
   * @param accessibleObject Method/constructor that failed the invocation.
   * @param error Error resulting from the invocation.
   */
  public void invocationError(Object context, Object instance, AccessibleObject accessibleObject,
      Throwable error);
}
