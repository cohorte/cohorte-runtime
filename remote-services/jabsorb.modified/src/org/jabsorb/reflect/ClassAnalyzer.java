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

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.localarg.LocalArgController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A &quot;factory&quot; for producing ClassData information from Class objects.
 * Gathers the ClassData information via reflection and internally caches it.
 */
public class ClassAnalyzer
{
  /**
   * The logger for this class
   */
  private final static Logger log = LoggerFactory
      .getLogger(ClassAnalyzer.class);

  /**
   * Classes that have been analysed
   * 
   * key: Clazz, val ClassData
   */
  private static Map classCache = new HashMap();

  /**
   * <p>
   * Get ClassData containing information on public methods that can be invoked
   * for a given class.
   * </p>
   * <p>
   * The ClassData will be cached, and multiple calls to getClassData for the
   * same class will return the same cached ClassData object (unless
   * invalidateCache is called to clear the cache.)
   * </p>
   * 
   * @param clazz class to get ClassData for.
   * 
   * @return ClassData object for the given class.
   */
  public static ClassData getClassData(Class clazz)
  {
    ClassData cd;
    synchronized (classCache)
    {
      cd = (ClassData) classCache.get(clazz);
      if (cd == null)
      {
        cd = analyzeClass(clazz);
        classCache.put(clazz, cd);
      }
    }
    return cd;
  }

  /**
   * Empty the internal cache of ClassData information.
   */
  public static void invalidateCache()
  {
    classCache = new HashMap();
  }

  /**
   * Analyze a class and create a ClassData object containing all of the public
   * methods (both static and non-static) in the class.
   * 
   * @param clazz class to be analyzed.
   * 
   * @return a ClassData object containing all the public static and non-static
   *         methods that can be invoked on the class.
   */
  private static ClassData analyzeClass(Class clazz)
  {
    log.info("analyzing " + clazz.getName());
    final List constructors = new ArrayList(Arrays.asList(clazz
        .getConstructors()));
    final List memberMethods = new ArrayList();
    final List staticMethods = new ArrayList();
    {
      final Method methods[] = clazz.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
        if (Modifier.isStatic(methods[i].getModifiers()))
        {
          staticMethods.add(methods[i]);
        }
        else
        {
          memberMethods.add(methods[i]);
        }
      }
    }

    ClassData cd = new ClassData(clazz, createMap(memberMethods, false),
        createMap(staticMethods, false), createMap(constructors, true));

    return cd;
  }

  /**
   * Creates a mapping of AccessibleObjectKey to a Collection which contains all
   * the AccessibleObjects which have the same amount of arguments. This takes
   * into account LocalArgResolvers, discounting them from the argument size.
   * 
   * @param accessibleObjects The objects to put into the map
   * @param isConstructor Whether the objects are methods or constructors
   * @return Map of AccessibleObjectKey to a Collection of AccessibleObjects
   */
  private static Map createMap(Collection accessibleObjects,
      boolean isConstructor)
  {
    final Map map = new HashMap();
    for (final Iterator i = accessibleObjects.iterator(); i.hasNext();)
    {
      final Member accessibleObject = (Member) i.next();

      if (!Modifier.isPublic(accessibleObject.getModifiers()))
        continue;

      final AccessibleObjectKey accessibleObjectKey;
      {
        // argCount determines the key
        int argCount = 0;
        {
          // The parameters determine the size of argCount
          final Class[] param;
          if (isConstructor)
          {
            param = ((Constructor) accessibleObject).getParameterTypes();
          }
          else
          {
            // If it is a method and the method was defined in Object(), skip
            // it.
            if (((Method) accessibleObject).getDeclaringClass() == Object.class)
            {
              continue;
            }
            param = ((Method) accessibleObject).getParameterTypes();
          }
          // don't count locally resolved args
          for (int n = 0; n < param.length; n++)
          {
            if (LocalArgController.isLocalArg(param[n]))
              continue;
            argCount++;
          }

          if (isConstructor)
          {
            // Since there is only one constructor name, we don't need to put a
            // name in.
            accessibleObjectKey = new AccessibleObjectKey(
                JSONRPCBridge.CONSTRUCTOR_FLAG, argCount);
          }
          else
          {
            // The key is the methods name and arg count
            accessibleObjectKey = new AccessibleObjectKey(
                ((Method) accessibleObject).getName(), argCount);
          }
        }
      }
      List marr = (ArrayList) map.get(accessibleObjectKey);
      if (marr == null)
      {
        marr = new ArrayList();
        map.put(accessibleObjectKey, marr);
      }
      
      marr.add(accessibleObject);
    }
    return map;
  }
}
