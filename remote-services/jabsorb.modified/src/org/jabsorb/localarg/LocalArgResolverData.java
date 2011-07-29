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

package org.jabsorb.localarg;

/**
 * Data holder for this LocalArgResolver.
 */
class LocalArgResolverData
{
  /**
   * The user defined class that resolves the and returns the method argument
   * using transport context information
   */
  private final LocalArgResolver argResolver;

  /**
   * The class to be resolved locally
   */
  private final Class argClazz;

  /**
   * The type of transport Context object the callback is interested in eg.
   * HttpServletRequest.class for the servlet transport
   */
  private final Class contextInterface;

  /**
   * Create a new data holder
   * 
   * @param argResolver The user defined class that resolves the and returns the
   *          method argument using transport context information
   * @param argClazz The class to be resolved locally
   * @param contextInterface The type of transport Context object the callback
   *          is interested in eg. HttpServletRequest.class for the servlet
   *          transport
   */
  public LocalArgResolverData(LocalArgResolver argResolver, Class argClazz,
      Class contextInterface)
  {
    this.argResolver = argResolver;
    this.argClazz = argClazz;
    this.contextInterface = contextInterface;
  }

  public boolean equals(Object o)
  {
    LocalArgResolverData cmp = (LocalArgResolverData) o;
    return (argResolver.equals(cmp.argResolver)
        && argClazz.equals(cmp.argClazz) && contextInterface
        .equals(cmp.contextInterface));
  }

  public int hashCode()
  {
    return argResolver.hashCode() * argClazz.hashCode()
        * contextInterface.hashCode();
  }

  /**
   * Whether this object's context can understand the given object
   * 
   * @param context The object to test
   * @return Whether the contextInterface isAssignableFrom the given object
   */
  public boolean understands(Object context)
  {
    return contextInterface.isAssignableFrom(context.getClass());
  }

  /**
   * Gets the argResolver
   * 
   * @return LocalArgResolver
   */
  LocalArgResolver getArgResolver()
  {
    return argResolver;
  }
}