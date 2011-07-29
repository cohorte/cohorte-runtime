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

package org.jabsorb.reflect;

import java.util.HashMap;
import java.util.Map;

/**
 * Information on the public methods of a class as reflected from the Class
 * itself. This is produced by the ClassAnalyzer and used in the JSONRPCBridge
 * for resolving classes and methods to invoke through json-rpc.
 */
public class ClassData
{
  /**
   * The class that this ClassData maps.
   */
  private final Class clazz;

  /**
   * Map of public instance methods. Key is a AccessibleObjectKey object, value
   * is an List of Method.
   */
  private final Map methodMap;

  /**
   * Map of public static methods. Key is a AccessibleObjectKey object, value is
   * an List of Method.
   */
  private final Map staticMethodMap;

  /**
   * Map of public constructors. Key is a AccessibleObjectKey object, value is
   * an List of Constructor.
   */
  private final Map constructorMap;

  /**
   * Creates a new ClassData
   * 
   * @param clazz The class that this ClassData maps.
   * @param methodMap Map of public instance methods. Static methods do not go
   *          here. Key is a AccessibleObjectKey object, value is an List of
   *          Method.
   * @param staticMethodMap Map of public static methods. Key is a
   *          AccessibleObjectKey object, value is an List of Method.
   * @param constructorMap Map of public constructors. Key is a
   *          AccessibleObjectKey object, value is an List of Constructor.
   */
  public ClassData(Class clazz, Map methodMap, Map staticMethodMap,
      Map constructorMap)
  {
    this.clazz = clazz;
    this.methodMap = new HashMap(methodMap);
    this.methodMap.putAll(staticMethodMap);
    this.staticMethodMap = new HashMap(staticMethodMap);
    this.constructorMap = new HashMap(constructorMap);
  }

  /**
   * Get the class that this ClassData maps.
   * 
   * @return the class that this ClassData maps.
   */
  public Class getClazz()
  {
    return clazz;
  }

  /**
   * Get the Map of public constructors that can be invoked for the class. The
   * key of the Map is a AccessibleObjectKey object and the value is a list of
   * Constructor objects.
   * 
   * @return Map of static methods that can be invoked for the class.
   */
  public Map getConstructorMap()
  {
    return constructorMap;
  }

  /**
   * Get the Map of public methods (both static and non-static) that can be
   * invoked for the class. This is *NOT* just the method map that was passed in
   * the constructor, but is concatenated with the static methods as well. The
   * keys of the Map will be AccessibleObjectKey objects and the values will be
   * a List of Method objects.
   * 
   * @return Map of public instance methods which can be invoked for the class.
   *         this ClassData.
   */
  public Map getMethodMap()
  {
    return methodMap;
  }

  /**
   * Get the Map of public static methods that can be invoked for the class. The
   * key of the Map is a AccessibleObjectKey object and the value is a list of
   * Method objects.
   * 
   * @return Map of static methods that can be invoked for the class.
   */
  public Map getStaticMethodMap()
  {
    return staticMethodMap;
  }
}
