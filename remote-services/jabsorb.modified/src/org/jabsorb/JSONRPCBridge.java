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

package org.jabsorb;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.jabsorb.callback.CallbackController;
import org.jabsorb.callback.InvocationCallback;
import org.jabsorb.localarg.LocalArgController;
import org.jabsorb.localarg.LocalArgResolver;
import org.jabsorb.reflect.AccessibleObjectKey;
import org.jabsorb.reflect.ClassAnalyzer;
import org.jabsorb.reflect.ClassData;
import org.jabsorb.serializer.AccessibleObjectResolver;
import org.jabsorb.serializer.Serializer;
import org.jabsorb.serializer.impl.ReferenceSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a bridge that unmarshalls JSON objects in JSON-RPC
 * request format, invokes a method on the exported object, and then marshalls
 * the resulting Java objects to JSON objects in JSON-RPC result format.
 * </p>
 * <p>
 * There is a global bridge singleton object that allows exporting classes and
 * objects to all HTTP clients. In addition to this, an instance of the
 * JSONRPCBridge can optionally be placed in a users' HttpSession object
 * registered under the attribute "JSONRPCBridge" to allow exporting of classes
 * and objects to specific users. A session specific bridge will delegate
 * requests for objects it does not know about to the global singleton
 * JSONRPCBridge instance.
 * </p>
 * <p>
 * Using session specific bridge instances can improve the security of
 * applications by allowing exporting of certain objects only to specific
 * HttpSessions as well as providing a convenient mechanism for JavaScript
 * clients to access stateful data associated with the current user.
 * </p>
 * <p>
 * You can create a HttpSession specific bridge in JSP with the usebean tag:
 * </p>
 * <code>&lt;jsp:useBean id="JSONRPCBridge" scope="session"
 * class="org.jabsorb.JSONRPCBridge" /&gt;</code>
 * <p>
 * Then export an object for your JSON-RPC client to call methods on:
 * </p>
 * <code>JSONRPCBridge.registerObject("test", testObject);</code>
 * <p>
 * This will make available all public methods of the object as
 * <code>test.&lt;methodnames&gt;</code> to JSON-RPC clients. This approach
 * should generally be performed after an authentication check to only export
 * objects to clients that are authorised to use them.
 * </p>
 * <p>
 * Alternatively, the global bridge singleton object allows exporting of classes
 * and objects to all HTTP clients. It can be fetched with
 * <code>JSONRPCBridge.getGlobalBridge()</code>.
 * </p>
 * <p>
 * To export all public instance methods of an object to <b>all</b> clients:
 * </p>
 * <code>JSONRPCBridge.getGlobalBridge().registerObject("myObject",
 * myObject);</code>
 * <p>
 * To export all public static methods of a class to <b>all</b> clients:
 * </p>
 * <code>JSONRPCBridge.getGlobalBridge().registerClass("MyClass",
 * com.example.MyClass.class);</code>
 */
public class JSONRPCBridge implements Serializable
{
  /**
   * Container for objects of which instances have been made
   */
  private static class ObjectInstance implements Serializable
  {
    /**
     * Unique serialisation id.
     */
    private final static long serialVersionUID = 2;

    /**
     * The object for the instance
     */
    private final Object object;

    /**
     * The class the object is of
     */
    private final Class clazz;

    /**
     * Creates a new ObjectInstance
     * 
     * @param object The object for the instance
     */
    public ObjectInstance(Object object)
    {
      this.object = object;
      this.clazz = object.getClass();
    }

    /**
     * Creates a new ObjectInstance
     * 
     * @param object The object for the instance
     * @param clazz The class the object is of
     */
    public ObjectInstance(Object object, Class clazz)
    {
      if (!clazz.isInstance(object))
      {
        throw new ClassCastException(
            "Attempt to register jsonrpc object with invalid class.");
      }
      this.object = object;
      this.clazz = clazz;
    }

    /**
     * Gets the class the object is of
     * 
     * @return The class the object is of
     */
    public Class getClazz()
    {
      return clazz;
    }

    /**
     * Gets the object for the instance
     * 
     * @return the object for the instance
     */
    public Object getObject()
    {
      return object;
    }
  }

  /**
   * The prefix for callable references, as sent in messages 
   */
  public static final String CALLABLE_REFERENCE_METHOD_PREFIX=".ref";
  
  /**
   * The string identifying constuctor calls
   */
  public static final String CONSTRUCTOR_FLAG = "$constructor";
  
  /**
   * The prefix for objects, as sent in messages
   */
  public static final String OBJECT_METHOD_PREFIX=".obj";
  
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * A simple transformer that makes no change
   */
  private static final ExceptionTransformer IDENTITY_EXCEPTION_TRANSFORMER = new ExceptionTransformer()
  {
    /**
     * Unique serialisation id.
     */
    private final static long serialVersionUID = 2;

    public Object transform(Throwable t)
    {
      return t;
    }
  };

  /**
   * The logger for this class
   */
  private final static Logger log = LoggerFactory
      .getLogger(JSONRPCBridge.class);

  /**
   * Global bridge (for exporting to all users)
   */
  private final static JSONRPCBridge globalBridge = new JSONRPCBridge();

  /**
   * Global JSONSerializer instance
   */
  private static JSONSerializer ser = new JSONSerializer();

  static
  {
    try
    {
      ser.registerDefaultSerializers();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * This method retrieves the global bridge singleton. <p/> It should be used
   * with care as objects should generally be registered within session specific
   * bridges for security reasons.
   * 
   * @return returns the global bridge object.
   */
  public static JSONRPCBridge getGlobalBridge()
  {
    return globalBridge;
  }

  /**
   * Get the global JSONSerializer object.
   * 
   * @return the global JSONSerializer object.
   */
  public static JSONSerializer getSerializer()
  {
    return ser;
  }

  /**
   * Registers a Class to be removed from the exported method signatures and
   * instead be resolved locally using context information from the transport.
   * 
   * @param argClazz The class to be resolved locally
   * @param argResolver The user defined class that resolves the and returns the
   *          method argument using transport context information
   * @param contextInterface The type of transport Context object the callback
   *          is interested in eg. HttpServletRequest.class for the servlet
   *          transport
   */
  public static void registerLocalArgResolver(Class argClazz,
      Class contextInterface, LocalArgResolver argResolver)
  {
    LocalArgController.registerLocalArgResolver(argClazz, contextInterface,
        argResolver);
  }

  /* Inner classes */

  /**
   * Set the global JSONSerializer object.
   * 
   * @param ser the global JSONSerializer object.
   */
  public static void setSerializer(JSONSerializer ser)
  {
    JSONRPCBridge.ser = ser;
  }

  /**
   * Unregisters a LocalArgResolver</b>.
   * 
   * @param argClazz The previously registered local class
   * @param argResolver The previously registered LocalArgResolver object
   * @param contextInterface The previously registered transport Context
   *          interface.
   */
  public static void unregisterLocalArgResolver(Class argClazz,
      Class contextInterface, LocalArgResolver argResolver)
  {
    LocalArgController.unregisterLocalArgResolver(argClazz, contextInterface,
        argResolver);
  }

  /* Implementation */

  /**
   * Create unique method names by appending the given prefix to the keys from
   * the given HashMap and adding them all to the given HashSet.
   * 
   * @param m HashSet to add unique methods to.
   * @param prefix prefix to append to each method name found in the methodMap.
   * @param methodMap a HashMap containing MethodKey keys specifying methods.
   */
  private static void uniqueMethods(Set m, String prefix, Map methodMap)
  {
    Iterator i = methodMap.entrySet().iterator();
    while (i.hasNext())
    {
      Map.Entry mentry = (Map.Entry) i.next();
      AccessibleObjectKey mk = (AccessibleObjectKey) mentry.getKey();
      m.add(prefix + mk.getMethodName());
    }
  }

  /**
   * The functor used to convert exceptions
   */
  private ExceptionTransformer exceptionTransformer = IDENTITY_EXCEPTION_TRANSFORMER;

  /**
   * The callback controller
   */
  private CallbackController cbc = null;

  /**
   * key "exported class name", val Class
   */
  private final Map classMap;

  /**
   * key "exported instance name", val ObjectInstance
   */
  private final Map objectMap;

  /**
   * key Integer hashcode, object held as reference
   */
  private final Map referenceMap;

  /**
   * ReferenceSerializer if enabled
   */
  private final Serializer referenceSerializer;

  /**
   * key clazz, classes that should be returned as References
   */
  private final Set referenceSet;

  /**
   * key clazz, classes that should be returned as CallableReferences
   */
  private final Set callableReferenceSet;

  /**
   * Whether references will be used on the bridge
   */
  private boolean referencesEnabled;

  /**
   * Creates a new bridge.
   */
  public JSONRPCBridge()
  {
    classMap = new HashMap();
    objectMap = new HashMap();
    referenceMap = new HashMap();
    referenceSerializer = new ReferenceSerializer(this);
    referenceSet = new HashSet();
    callableReferenceSet = new HashSet();
    referencesEnabled = false;
  }

  /**
   * Adds a reference to the map of known references
   * 
   * @param o The object to be added
   */
  public void addReference(Object o)
  {
    synchronized (referenceMap)
    {
      referenceMap.put(new Integer(System.identityHashCode(o)), o);
    }
  }

  /**
   * Call a method using a JSON-RPC request object.
   * 
   * @param  context The transport context (the HttpServletRequest and 
   *         HttpServletResponse objects in the case of the HTTP transport).
   *         
   * @param  jsonReq The JSON-RPC request structured as a JSON object tree.
   * 
   * @return a JSONRPCResult object with the result of the invocation or an
   *         error.
   */
  public JSONRPCResult call(Object context[], JSONObject jsonReq)
  {
    // #1: Parse the request
    final String encodedMethod;
    final Object requestId;
    final JSONArray arguments;
    final JSONArray fixups;
    try
    {
      encodedMethod = jsonReq.getString("method");
      arguments = jsonReq.getJSONArray("params");
      requestId = jsonReq.opt("id");
      fixups = jsonReq.optJSONArray("fixups");
    }
    catch (JSONException e)
    {
      log.error("no method or parameters in request");
      return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, null,
          JSONRPCResult.MSG_ERR_NOMETHOD);
    }
    if (log.isDebugEnabled())
    {
      if (fixups != null)
      {
        log.debug("call " + encodedMethod + "(" + arguments + ")"
            + ", requestId=" + requestId);
      }
      else
      {
        log.debug("call " + encodedMethod + "(" + arguments + ")" + ", fixups="
            + fixups + ", requestId=" + requestId);
      }
    }

    // apply the fixups (if any) to the parameters. This will result
    // in a JSONArray that might have circular references-- so
    // the toString method (or anything that internally tries to traverse
    // the JSON (without being aware of this) should not be called after this
    // point

    if (fixups != null)
    {
      try
      {
        for (int i = 0; i < fixups.length(); i++)
        {
          JSONArray assignment = fixups.getJSONArray(i);
          JSONArray fixup = assignment.getJSONArray(0);
          JSONArray original = assignment.getJSONArray(1);
          applyFixup(arguments, fixup, original);
        }
      }
      catch (JSONException e)
      {
        log.error("error applying fixups", e);

        return new JSONRPCResult(JSONRPCResult.CODE_ERR_FIXUP, requestId,
            JSONRPCResult.MSG_ERR_FIXUP + ": " + e.getMessage());
      }
    }

    // #2: Get the name of the class and method from the encodedMethod
    final String className;
    final String methodName;
    {
      StringTokenizer t = new StringTokenizer(encodedMethod, ".");
      if (t.hasMoreElements())
      {
        className = t.nextToken();
      }
      else
      {
        className = null;
      }
      if (t.hasMoreElements())
      {
        methodName = t.nextToken();
      }
      else
      {
        methodName = null;
      }
    }

    // #3: Get the id of the object (if it exists) from the className
    // (in the format: ".obj#<objectID>")
    final int objectID;
    {
      final int objectStartIndex=encodedMethod.indexOf('[');
      final int objectEndIndex=encodedMethod.indexOf(']');
      if (encodedMethod.startsWith(OBJECT_METHOD_PREFIX)&&(objectStartIndex!=-1)&&(objectEndIndex!=-1)&&(objectStartIndex<objectEndIndex))
      {
        objectID = Integer.parseInt(encodedMethod.substring(objectStartIndex+1,objectEndIndex));
      }
      else
      {
        objectID = 0;
      }
    }
    // #4: Handle list method calls
    if ((objectID == 0) && (encodedMethod.equals("system.listMethods")))
    {
      return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId,
          systemListMethods());
    }
   
    // #5: Get the object to act upon and the possible method that could be 
    // called on it
    final Map methodMap;
    final Object javascriptObject;
    final AccessibleObject ao;
    try
    {
      javascriptObject = getObjectContext(objectID, className);
      methodMap = getAccessibleObjectMap(objectID, className, methodName);
      // #6: Resolve the method      
      ao = AccessibleObjectResolver.resolveMethod(
          methodMap, methodName, arguments, ser);
      if (ao == null)
      {
         throw new NoSuchMethodException(JSONRPCResult.MSG_ERR_NOMETHOD);
      }
    }
    catch (NoSuchMethodException e)
    {
      if (e.getMessage().equals(JSONRPCResult.MSG_ERR_NOCONSTRUCTOR))
      {
        return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOCONSTRUCTOR,
            requestId, JSONRPCResult.MSG_ERR_NOCONSTRUCTOR);
      }
      return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, requestId,
          JSONRPCResult.MSG_ERR_NOMETHOD);
    }

    // #7: Call the method
    JSONRPCResult r = AccessibleObjectResolver.invokeAccessibleObject(ao,
        context, arguments, javascriptObject, requestId, ser, cbc,
        exceptionTransformer);
    return r;
  }

  /**
   * Allows references to be used on the bridge
   * 
   * @throws Exception If a serialiser has already been registered for
   *           CallableReferences
   */
  public synchronized void enableReferences() throws Exception
  {
    if (!referencesEnabled)
    {
      registerSerializer(referenceSerializer);
      referencesEnabled = true;
      log.info("enabled references on this bridge");
    }
  }

  /**
   * Get the CallbackController object associated with this bridge.
   * 
   * @return the CallbackController object associated with this bridge.
   */
  public CallbackController getCallbackController()
  {
    return cbc;
  }

  /**
   * Gets a known reference
   * 
   * @param objectId The id of the object to get
   * @return The requested reference
   */
  public Object getReference(int objectId)
  {
    synchronized (referenceMap)
    {
      return referenceMap.get(new Integer(objectId));
    }
  }

  /**
   * Check whether a class is registered as a callable reference type.
   * 
   * @param clazz The class object to check is a callable reference.
   * @return true if it is, false otherwise
   */
  public boolean isCallableReference(Class clazz)
  {
    if (this == globalBridge)
    {
      return false;
    }
    if (!referencesEnabled)
    {
      return false;
    }
    if (callableReferenceSet.contains(clazz))
    {
      return true;
    }
    
    // check if the class implements any interface that is
    // registered as a callable reference...
    Class[] interfaces = clazz.getInterfaces();
    for (int i=0; i<interfaces.length; i++)
    {
      if (callableReferenceSet.contains(interfaces[i]))
      {
        return true;
      }
    }

    // check super classes as well...
    Class superClass = clazz.getSuperclass();
    while (superClass != null)
    {
      if (callableReferenceSet.contains(superClass))
      {
        return true;
      }
      superClass = superClass.getSuperclass();
    }
    
    // should interfaces of each superclass be checked too???
    // not sure...
    
    return globalBridge.isCallableReference(clazz);
  }

  /**
   * Check whether a class is registered as a reference type.
   * 
   * @param clazz The class object to check is a reference.
   * @return true if it is, false otherwise.
   */
  public boolean isReference(Class clazz)
  {
    if (this == globalBridge)
    {
      return false;
    }
    if (!referencesEnabled)
    {
      return false;
    }
    if (referenceSet.contains(clazz))
    {
      return true;
    }
    return globalBridge.isReference(clazz);
  }

  /**
   * Lookup a class that is registered with this bridge.
   * 
   * @param name The registered name of the class to lookup.
   * @return the class for the name
   */
  public Class lookupClass(String name)
  {
    synchronized (classMap)
    {
      return (Class) classMap.get(name);
    }
  }

  /**
   * Lookup an object that is registered with this bridge.
   * 
   * @param key The registered name of the object to lookup.
   * @return The object desired if it exists, else null.
   */
  public Object lookupObject(Object key)
  {
    synchronized (objectMap)
    {
      ObjectInstance oi = (ObjectInstance) objectMap.get(key);
      if (oi != null)
      {
        return oi.getObject();
      }
    }
    return null;
  }

  /**
   * <p>
   * Registers a class to be returned as a callable reference.
   * </p>
   * <p>
   * The JSONBridge will return a callable reference to the JSON-RPC client for
   * registered classes instead of passing them by value. The JSONBridge will
   * take a references to these objects and the JSON-RPC client will create an
   * invocation proxy for objects of this class for which methods will be called
   * on the instance on the server.
   * </p>
   * <p>
   * <p>
   * Note that the global bridge does not support registering of callable 
   * references and attempting to do so will throw an Exception. 
   * These operations are inherently session based and are disabled on the 
   * global bridge because there is currently no safe simple way to garbage 
   * collect such references across the JavaScript/Java barrier.
   * </p>
   * <p>
   * A Callable Reference in JSON format looks like this:
   * </p>
   * <code>{ "javaClass":"org.jabsorb.test.Bar",<br />
   * "objectID":4827452,<br /> "JSONRPCType":"CallableReference" }</code>
   * 
   * @param clazz The class object that should be marshalled as a callable
   *          reference.
   * @throws Exception if this method is called on the global bridge.
   */
  public void registerCallableReference(Class clazz) throws Exception
  {
    if (this == globalBridge)
    {
      throw new Exception("Can't register callable reference on global bridge");
    }
    if (!referencesEnabled)
    {
      enableReferences();
    }
    synchronized (callableReferenceSet)
    {
      callableReferenceSet.add(clazz);
    }
    if (log.isDebugEnabled())
    {
      log.debug("registered callable reference " + clazz.getName());
    }
  }

  /**
   * Registers a callback to be called before and after method invocation
   * 
   * @param callback The object implementing the InvocationCallback Interface
   * @param contextInterface The type of transport Context interface the
   *          callback is interested in eg. HttpServletRequest.class for the
   *          servlet transport.
   */
  public void registerCallback(InvocationCallback callback,
      Class contextInterface)
  {
    if (cbc == null)
    {
      cbc = new CallbackController();
    }
    cbc.registerCallback(callback, contextInterface);
  }

  /**
   * Registers a class to export static methods. <p/> The JSONBridge will export
   * all static methods of the class. This is useful for exporting factory
   * classes that may then return CallableReferences to the JSON-RPC client.
   * <p/> Calling registerClass for a clazz again under the same name will have
   * no effect. <p/> To export instance methods you need to use registerObject.
   * 
   * @param name The name to register the class with.
   * @param clazz The class to export static methods from.
   * @throws Exception If a class is already registed with this name
   */
  public void registerClass(String name, Class clazz) throws Exception
  {
    synchronized (classMap)
    {
      Class exists = (Class) classMap.get(name);
      if (exists != null && exists != clazz)
      {
        throw new Exception("different class registered as " + name);
      }
      if (exists == null)
      {
        classMap.put(name, clazz);
      }
    }
    if (log.isDebugEnabled())
    {
      log.debug("registered class " + clazz.getName() + " as " + name);
    }
  }

  /**
   * Registers an object to export all instance methods and static methods. <p/>
   * The JSONBridge will export all instance methods and static methods of the
   * particular object under the name passed in as a key. <p/> This will make
   * available all methods of the object as
   * <code>&lt;key&gt;.&lt;methodnames&gt;</code> to JSON-RPC clients. <p/>
   * Calling registerObject for a name that already exists will replace the
   * existing entry.
   * 
   * @param key The named prefix to export the object as
   * @param o The object instance to be called upon
   */
  public void registerObject(Object key, Object o)
  {
    ObjectInstance oi = new ObjectInstance(o);
    synchronized (objectMap)
    {
      objectMap.put(key, oi);
    }
    if (log.isDebugEnabled())
    {
      log.debug("registered object " + o.hashCode() + " of class "
          + o.getClass().getName() + " as " + key);
    }
  }

  /**
   * Registers an object to export all instance methods defined by
   * interfaceClass. <p/> The JSONBridge will export all instance methods
   * defined by interfaceClass of the particular object under the name passed in
   * as a key. <p/> This will make available these methods of the object as
   * <code>&lt;key&gt;.&lt;methodnames&gt;</code> to JSON-RPC clients.
   * 
   * @param key The named prefix to export the object as
   * @param o The object instance to be called upon
   * @param interfaceClass The type that this object should be registered as.
   *          <p/> This can be used to restrict the exported methods to the
   *          methods defined in a specific superclass or interface.
   */
  public void registerObject(Object key, Object o, Class interfaceClass)
  {
    ObjectInstance oi = new ObjectInstance(o, interfaceClass);
    synchronized (objectMap)
    {
      objectMap.put(key, oi);
    }
    if (log.isDebugEnabled())
    {
      log.debug("registered object " + o.hashCode() + " of class "
          + interfaceClass.getName() + " as " + key);
    }
  }

  /**
   * Registers a class to be returned by reference and not by value as is done
   * by default. <p/> The JSONBridge will take a references to these objects and
   * return an opaque object to the JSON-RPC client. When the opaque object is
   * passed back through the bridge in subsequent calls, the original object is
   * substitued in calls to Java methods. This should be used for any objects
   * that contain security information or complex types that are not required in
   * the Javascript client but need to be passed as a reference in methods of
   * exported objects. <p/> A Reference in JSON format looks like this: <p/>
   * <code>{ "javaClass":"org.jabsorb.test.Foo",<br />
   * "objectID":5535614,<br /> "JSONRPCType":"Reference" }</code>
   * <p>
   * Note that the global bridge does not support registering of  
   * references and attempting to do so will throw an Exception. 
   * These operations are inherently session based and are disabled on the 
   * global bridge because there is currently no safe simple way to garbage 
   * collect such references across the JavaScript/Java barrier.
   * </p>
   * 
   * @param clazz The class object that should be marshalled as a reference.
   * @throws Exception if this method is called on the global bridge.   
   */
  public void registerReference(Class clazz) throws Exception
  {
    if (this == globalBridge)
    {
      throw new Exception("Can't register reference on global bridge");
    }
    if (!referencesEnabled)
    {
      enableReferences();
    }
    synchronized (referenceSet)
    {
      referenceSet.add(clazz);
    }
    if (log.isDebugEnabled())
    {
      log.debug("registered reference " + clazz.getName());
    }
  }

  /**
   * Register a new serializer on this bridge.
   * 
   * @param serializer A class implementing the Serializer interface (usually
   *          derived from AbstractSerializer).
   * @throws Exception If a serialiser has already been registered that
   *           serialises the same class
   */
  public void registerSerializer(Serializer serializer) throws Exception
  {
    ser.registerSerializer(serializer);
  }

  /**
   * Set the CallbackController object for this bridge.
   * 
   * @param cbc the CallbackController object to be set for this bridge.
   */
  public void setCallbackController(CallbackController cbc)
  {
    this.cbc = cbc;
  }

  /**
   * Sets the exception transformer for the bridge.
   * 
   * @param exceptionTransformer The new exception transformer to use.
   */
  public void setExceptionTransformer(ExceptionTransformer exceptionTransformer)
  {
    this.exceptionTransformer = exceptionTransformer;
  }

  /**
   * Unregisters a callback
   * 
   * @param callback The previously registered InvocationCallback object
   * @param contextInterface The previously registered transport Context
   *          interface.
   */
  public void unregisterCallback(InvocationCallback callback,
      Class contextInterface)
  {
    if (cbc == null)
    {
      return;
    }
    cbc.unregisterCallback(callback, contextInterface);
  }

  /**
   * Unregisters a class exported with registerClass. <p/> The JSONBridge will
   * unexport all static methods of the class.
   * 
   * @param name The registered name of the class to unexport static methods
   *          from.
   */
  public void unregisterClass(String name)
  {
    synchronized (classMap)
    {
      Class clazz = (Class) classMap.get(name);
      if (clazz != null)
      {
        classMap.remove(name);
        if (log.isDebugEnabled())
        {
          log.debug("unregistered class " + clazz.getName() + " from " + name);
        }
      }
    }
  }

  /**
   * Unregisters an object exported with registerObject. <p/> The JSONBridge
   * will unexport all instance methods and static methods of the particular
   * object under the name passed in as a key.
   * 
   * @param key The named prefix of the object to unexport
   */
  public void unregisterObject(Object key)
  {
    synchronized (objectMap)
    {
      ObjectInstance oi = (ObjectInstance) objectMap.get(key);
      if (oi.getObject() != null)
      {
        objectMap.remove(key);
        if (log.isDebugEnabled())
        {
          log.debug("unregistered object " + oi.getObject().hashCode()
              + " of class " + oi.getClazz().getName() + " from " + key);
        }
      }
    }
  }

  /**
   * Add all instance methods that can be invoked on this bridge to a HashSet.
   * 
   * @param m HashSet to add all static methods to.
   */
  private void allInstanceMethods(Set m)
  {
    synchronized (objectMap)
    {
      Iterator i = objectMap.entrySet().iterator();
      while (i.hasNext())
      {
        Map.Entry oientry = (Map.Entry) i.next();
        Object key = oientry.getKey();
        if (!(key instanceof String))
        {
          continue;
        }
        String name = (String) key;
        ObjectInstance oi = (ObjectInstance) oientry.getValue();
        ClassData cd = ClassAnalyzer.getClassData(oi.getClazz());
        uniqueMethods(m, name + ".", cd.getMethodMap());
        uniqueMethods(m, name + ".", cd.getStaticMethodMap());
      }
    }
  }

  /**
   * Add all methods on registered callable references to a HashSet.
   * 
   * @param m Set to add all methods to.
   */
  private void allCallableReferences(Set m)
  {
    synchronized (callableReferenceSet)
    {
      Iterator i = callableReferenceSet.iterator();
      while (i.hasNext())
      {
        Class clazz = (Class) i.next();
 
        ClassData cd = ClassAnalyzer.getClassData(clazz);

        uniqueMethods(m, CALLABLE_REFERENCE_METHOD_PREFIX+"["+clazz.getName()+"].", cd.getStaticMethodMap());
        uniqueMethods(m, CALLABLE_REFERENCE_METHOD_PREFIX+"["+clazz.getName()+"].", cd.getMethodMap());
      }
    }
  }
  
  /**
   * Add all static methods that can be invoked on this bridge to the given
   * HashSet.
   * 
   * @param m HashSet to add all static methods to.
   */
  private void allStaticMethods(Set m)
  {
    synchronized (classMap)
    {
      Iterator i = classMap.entrySet().iterator();
      while (i.hasNext())
      {
        Map.Entry cdentry = (Map.Entry) i.next();
        String name = (String) cdentry.getKey();
        Class clazz = (Class) cdentry.getValue();
        ClassData cd = ClassAnalyzer.getClassData(clazz);
        uniqueMethods(m, name + ".", cd.getStaticMethodMap());
      }
    }
  }

  /**
   * Apply one fixup assigment to the incoming json arguments.
   * 
   * WARNING: the resultant "fixed up" arguments may contain circular references
   * after this operation. That is the whole point of course-- but the JSONArray
   * and JSONObject's themselves aren't aware of circular references when
   * certain methods are called (e.g. toString) so be careful when handling
   * these circular referenced json objects.
   * 
   * @param object the object to apply fixups to.
   * @param fixup the fixup entry.
   * @param original the original value to assign to the fixup.
   * @throws org.json.JSONException if invalid or unexpected fixup data is
   *           encountered.
   */
  public static void applyFixup(Object object, JSONArray fixup,
      JSONArray original) throws JSONException
  {
    int last = fixup.length() - 1;

    if (last < 0)
    {
      throw new JSONException("fixup path must contain at least 1 reference");
    }

    Object originalObject = traverse(object, original, false);
    Object fixupParent = traverse(object, fixup, true);

    // the last ref in the fixup needs to be created
    // it will be either a string or number depending on if the fixupParent is a
    // JSONObject or JSONArray

    if (fixupParent instanceof JSONObject)
    {
      String objRef = fixup.optString(last, null);
      if (objRef == null)
      {
        throw new JSONException("last fixup reference not a string");
      }
      ((JSONObject) fixupParent).put(objRef, originalObject);
    }
    else
    {
      int arrRef = fixup.optInt(last, -1);
      if (arrRef == -1)
      {
        throw new JSONException("last fixup reference not a valid array index");
      }
      ((JSONArray) fixupParent).put(arrRef, originalObject);
    }
  }

  /**
   * Gets the methods that can be called on the given object
   * 
   * @param objectID The id of the object or 0 if it is a class
   * @param className The name of the class of the object - only required if
   *          objectID==0
   * @param methodName The name of method in the request
   * @return A map of AccessibleObjectKeys to a Collection of AccessibleObjects
   * @throws NoSuchMethodException If the method cannot be found in the class 
   */
  private Map getAccessibleObjectMap(final int objectID,
      final String className, final String methodName)
      throws NoSuchMethodException

  {
    final Map methodMap = new HashMap();
    // if it is not an object
    if (objectID == 0)
    {
      final ObjectInstance oi = resolveObject(className);
      final ClassData classData = resolveClass(className);

      // Look up the class, object instance and method objects
      if (oi != null)
      {
        methodMap.putAll(ClassAnalyzer.getClassData(oi.getClazz())
            .getMethodMap());
      }
      // try to get the constructor data
      else if (methodName.equals(CONSTRUCTOR_FLAG))
      {
        try
        {
          methodMap.putAll(ClassAnalyzer.getClassData(lookupClass(className))
              .getConstructorMap());
        }
        catch (Exception e)
        {
          throw new NoSuchMethodException(JSONRPCResult.MSG_ERR_NOCONSTRUCTOR);
        }
      }
      // else it must be static
      else if(classData!=null)
      {
        methodMap.putAll(classData.getStaticMethodMap());
      }
      else
      {
        throw new NoSuchMethodException(JSONRPCResult.MSG_ERR_NOMETHOD);
      }
    }
    // else it is an object, so we can get the member methods
    else
    {
      final ObjectInstance oi = resolveObject(new Integer(objectID));
      if (oi == null)
      {
        throw new NoSuchMethodException();
      }
      ClassData cd = ClassAnalyzer.getClassData(oi.getClazz());
      methodMap.putAll(cd.getMethodMap());
    }
    return methodMap;
  }

  /**
   * Resolves an objectId to an actual object
   * 
   * @param objectID The id of the object to resolve
   * @param className The name of the class of the object
   * @return The object requested
   */
  private Object getObjectContext(final int objectID, final String className)
  {
    final Object objectContext;
    if (objectID == 0)
    {
      final ObjectInstance oi = resolveObject(className);
      if (oi != null)
      {
        objectContext = oi.getObject();
      }
      else
      {
        objectContext = null;
      }
    }
    else
    {
      final ObjectInstance oi = resolveObject(new Integer(objectID));
      if (oi != null)
      {
        objectContext = oi.getObject();
      }
      else
      {
        objectContext = null;
      }
    }
    return objectContext;
  }

  /**
   * Given a previous json object, find the next object under the given index.
   * 
   * @param prev object to find subobject of.
   * @param idx index of sub object to find.
   * @return the next object in a fixup reference chain (prev[idx])
   * 
   * @throws JSONException if something goes wrong.
   */
  private static Object next(Object prev, int idx) throws JSONException
  {
    if (prev == null)
    {
      throw new JSONException("cannot traverse- missing object encountered");
    }

    if (prev instanceof JSONArray)
    {
      return ((JSONArray) prev).get(idx);
    }
    throw new JSONException("not an array");
  }

  /**
   * Given a previous json object, find the next object under the given ref.
   * 
   * @param prev object to find subobject of.
   * @param ref reference of sub object to find.
   * @return the next object in a fixup reference chain (prev[ref])
   * 
   * @throws JSONException if something goes wrong.
   */
  private static Object next(Object prev, String ref) throws JSONException
  {
    if (prev == null)
    {
      throw new JSONException("cannot traverse- missing object encountered");
    }
    if (prev instanceof JSONObject)
    {
      return ((JSONObject) prev).get(ref);
    }
    throw new JSONException("not an object");
  }

  /**
   * Resolves a string to a class
   * 
   * @param className The name of the class to resolve
   * @return The data associated with the className
   */
  private ClassData resolveClass(String className)
  {
    Class clazz;
    ClassData cd = null;

    synchronized (classMap)
    {
      clazz = (Class) classMap.get(className);
    }

    if (clazz != null)
    {
      cd = ClassAnalyzer.getClassData(clazz);
    }

    if (cd != null)
    {
      if (log.isDebugEnabled())
      {
        log.debug("found class " + cd.getClazz().getName() + " named "
            + className);
      }
      return cd;
    }

    if (this != globalBridge)
    {
      return globalBridge.resolveClass(className);
    }

    return null;
  }

  /**
   * Resolve the key to a specified instance object. If an instance object of
   * the requested key is not found, and this is not the global bridge, then
   * look in the global bridge too. <p/> If the key is not found in this bridge
   * or the global bridge, the requested key may be a class method (static
   * method) or may not exist (not registered under the requested key.)
   * 
   * @param key registered object key being requested by caller.
   * @return ObjectInstance that has been registered under this key, in this
   *         bridge or the global bridge.
   */
  private ObjectInstance resolveObject(Object key)
  {
    ObjectInstance oi;
    synchronized (objectMap)
    {
      oi = (ObjectInstance) objectMap.get(key);
    }
    if (log.isDebugEnabled() && oi != null)
    {
      log.debug("found object " + oi.getObject().hashCode() + " of class "
          + oi.getClazz().getName() + " with key " + key);
    }
    if (oi == null && this != globalBridge)
    {
      return globalBridge.resolveObject(key);
    }
    return oi;
  }

  /**
   * Handle "system.listMethods" this is called by the browser side javascript
   * when a new JSONRpcClient object is initialized.
   * 
   * @return A JSONArray containing the names of the system methods.
   */
  private JSONArray systemListMethods()
  {
    Set m = new TreeSet();
    globalBridge.allInstanceMethods(m);
    if (globalBridge != this)
    {
      globalBridge.allStaticMethods(m);
      globalBridge.allInstanceMethods(m);
    }
    allStaticMethods(m);
    allInstanceMethods(m);
    allCallableReferences(m);
    JSONArray methods = new JSONArray();
    Iterator i = m.iterator();
    while (i.hasNext())
    {
      methods.put(i.next());
    }
    return methods;
  }

  /**
   * Traverse a list of references to find the target reference in an original
   * or fixup list.
   * 
   * @param origin origin JSONArray (arguments) to begin traversing at.
   * @param refs JSONArray containing array integer references and or String
   *          object references.
   * @param fixup if true, stop one short of the traversal chain to return the
   *          parent of the fixup rather than the fixup itself (which will be
   *          non-existant)
   * @return either a JSONObject or JSONArray for the Object found at the end of
   *         the traversal.
   * @throws JSONException if something unexpected is found in the data
   */
  private static Object traverse(Object origin, JSONArray refs, boolean fixup)
      throws JSONException
  {
    try
    {
      JSONArray arr=null;
      JSONObject obj=null;
      if(origin instanceof JSONArray)
      {
        arr = (JSONArray)origin;
      }
      else
      {
        obj = (JSONObject)origin;
      }

      // where to stop when traversing
      int stop = refs.length();

      // if looking for the fixup, stop short by one to find the parent of the
      // fixup instead.
      // because the fixup won't exist yet and needs to be created
      if (fixup)
      {
        stop--;
      }

      // find the target object by traversing the list of references
      for (int i = 0; i < stop; i++)
      {
        Object next;
        if (arr == null)
        {
          next = next(obj, refs.optString(i, null));
        }
        else
        {
          next = next(arr, refs.optInt(i, -1));
        }
        if (next instanceof JSONObject)
        {
          obj = (JSONObject) next;
          arr = null;
        }
        else
        {
          obj = null;
          arr = (JSONArray) next;
        }
      }
      if (arr == null)
      {
        return obj;
      }
      return arr;
    }
    catch (Exception e)
    {
      log.error("unexpected exception", e);
      throw new JSONException("unexpected exception");
    }
  }
}