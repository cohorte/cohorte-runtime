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

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.jabsorb.serializer.FixUp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Container for a JSON-RPC result message. This includes successful results,
 * error results, and remote exceptions results.
 */
public class JSONRPCResult
{
  /**
   * Denotes that the call was a success
   */
  public final static int CODE_SUCCESS = 0;

  /**
   * Denotes that an exception was thrown on the server
   */
  public final static int CODE_REMOTE_EXCEPTION = 490;

  /**
   * Denotes that an error occured while parsing the request.
   */
  public final static int CODE_ERR_PARSE = 590;

  /**
   * Denotes (when calling a constructor) that no method was found with the
   * given name/arguments.
   */
  public final static int CODE_ERR_NOCONSTRUCTOR = 594;
  
  /**
   * Denotes (when using a callable reference) that no method was found with the
   * given name and number of arguments.
   */
  public final static int CODE_ERR_NOMETHOD = 591;
 
  /**
   * Denotes that an error occured while unmarshalling the request.
   */
  public final static int CODE_ERR_UNMARSHALL = 592;

  /**
   * Denotes that an error occured while marshalling the response.
   */
  public final static int CODE_ERR_MARSHALL = 593;

  /**
   * Denotes that an error occured while applying the fixup data for circular references/duplicates.
   */
  public final static int CODE_ERR_FIXUP = 594;

  /**
   * The error method shown when an error occured while parsing the request.
   */
  public final static String MSG_ERR_PARSE = "couldn't parse request arguments";
  
  /**
   * The error method shown when no constructor was found with the given name.
   */
  public static final String MSG_ERR_NOCONSTRUCTOR = "constructor not found";
  
  /**
   * The error method shown when no method was found with the given name and number of arguments.
   */
  public final static String MSG_ERR_NOMETHOD = "method with the requested number of arguments not found (session may" +
      " have timed out)";

  /**
   * The error method shown when something in the fixups was amiss.
   */
  public final static String MSG_ERR_FIXUP = "invalid or unexpected data in fixups";

  /**
   * The result of the call
   */
  private Object result;

  /**
   * The id of the response.
   */
  private Object id;

  /**
   * Optional fixup entries to run against the result in order to reconstitute duplicate and / or circular references
   * that were detected.
   * This is a List of FixUp objects.
   * @see FixUp
   */
  private List fixUps;

  /**
   * An error code if a problem occured (CODE_SUCCESS otherwise)
   */
  private int errorCode;

  /**
   * Creates a new JSONRPCResult without fixups (for backward compatibility to json-rpc and json-rpc-java.
   *
   * @param errorCode An error code if a problem occured (CODE_SUCCESS
   *          otherwise)
   * @param id The id of the response.
   * @param o The result of the call
   */
  public JSONRPCResult(int errorCode, Object id, Object o)
  {
    this.errorCode = errorCode;
    this.id = id;
    this.result = o;
  }

  /**
   * Creates a new JSONRPCResult with fixUps.
   * 
   * @param errorCode An error code if a problem occured (CODE_SUCCESS
   *          otherwise)
   * @param id The id of the response.
   * @param o The result of the call
   * @param fixUps optional list of FixUp objects needed to resolve circular refs and duplicates.
   */
  public JSONRPCResult(int errorCode, Object id, Object o, List fixUps)
  {
    this.errorCode = errorCode;
    this.id = id;
    this.result = o;
    this.fixUps = fixUps;
  }

  /**
   * Gets the error code
   * 
   * @return the error code
   */
  public int getErrorCode()
  {
    return errorCode;
  }

  /**
   * Gets the id of the response.
   * 
   * @return the id of the response.
   */
  public Object getId()
  {
    return id;
  }

  /**
   * Gets the result of the call
   * 
   * @return the result
   */
  public Object getResult()
  {
    return result;
  }

  public String toString()
  {
    JSONObject o = new JSONObject();

    try
    {
      if (errorCode == CODE_SUCCESS)
      {
        o.put("id", id);
        o.put("result", result);
        if (fixUps != null && fixUps.size()>0)
        {
          JSONArray fixups = new JSONArray();
          for (Iterator i=fixUps.iterator(); i.hasNext();)
          {
            FixUp fixup = (FixUp) i.next();
            fixups.put(fixup.toJSONArray());
          }
          o.put("fixups",fixups);
        }
      }
      else if (errorCode == CODE_REMOTE_EXCEPTION)
      {
        o.put("id", id); 
        if (result instanceof Throwable)
        {
          Throwable e = (Throwable) result;
          CharArrayWriter caw = new CharArrayWriter();
          e.printStackTrace(new PrintWriter(caw));
          JSONObject err = new JSONObject();
          err.put("code", new Integer(errorCode));
          err.put("msg", e.getMessage());
          err.put("trace", caw.toString());
          o.put("error", err);
        }
        else
        {
          // When using a customized implementation of ExceptionTransformer
          // an error result may be something other than Throwable. In this
          // case, it has to be a JSON compatible object, we will just store it
          // to the 'error' property of the response. 
          o.put("error", result);
        }
      }
      else
      {
        JSONObject err = new JSONObject();
        err.put("code", new Integer(errorCode));
        err.put("msg", result);
        o.put("id", id);
        o.put("error", err);
      }
    }
    catch (JSONException e)
    {
      // this would have been a null pointer exception in the previous json.org library.
      throw (RuntimeException) new RuntimeException(e.getMessage()).initCause(e);
    }
    return o.toString();
  }
}
