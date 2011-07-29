/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Client, a Java client extension to JSON-RPC-Java
 * (C) Copyright CodeBistro 2007, Sasha Ovsankin <sasha at codebistro dot com>
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
package org.jabsorb.client;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jabsorb.client.TransportRegistry.SessionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport session straightforwardly implemented in HTTP. As compared to the built-in 
 * URLConnectionSession, it allows more control over HTTP transport parameters, for
 * example, proxies and the support for HTTPS.
 * 
 * <p>To use this transport you need to first register it in the TransportRegistry, for example: <p>
 * <code>
 * 		HTTPSession.register(TransportRegistry.i());
 * </code>
 */
public class HTTPSession implements Session
{
  private final static Logger log = LoggerFactory.getLogger(HTTPSession.class);

  protected HttpClient  client;

  protected HttpState   state;

  protected URI         uri;

  public HTTPSession(URI uri)
  {
    this.uri = uri;
  }

  /**
   * An option to set state from the outside. for example, to provide existing
   * session parameters.
   */
  public void setState(HttpState state)
  {
    this.state = state;
  }

  /**
   * As per JSON-RPC Working Draft
   * http://json-rpc.org/wd/JSON-RPC-1-1-WD-20060807.html#RequestHeaders
   */
  static final String JSON_CONTENT_TYPE = "application/json";

  public JSONObject sendAndReceive(JSONObject message)
  {
    try
    {
      if (log.isDebugEnabled())
      {
        log.debug("Sending: " + message.toString(2));
      }
      PostMethod postMethod = new PostMethod(uri.toString());
      postMethod.setRequestHeader("Content-Type", "text/plain");

      RequestEntity requestEntity = new StringRequestEntity(message.toString(),
          JSON_CONTENT_TYPE, null);
      postMethod.setRequestEntity(requestEntity);
//      http().getHostConfiguration().setProxy(proxyHost, proxyPort);
      http().executeMethod(null, postMethod, state);
      int statusCode = postMethod.getStatusCode();
      if (statusCode != HttpStatus.SC_OK)
        throw new ClientError("HTTP Status - "
            + HttpStatus.getStatusText(statusCode) + " (" + statusCode + ")");
      JSONTokener tokener = new JSONTokener(postMethod
          .getResponseBodyAsString());
      Object rawResponseMessage = tokener.nextValue();
      JSONObject responseMessage = (JSONObject) rawResponseMessage;
      if (responseMessage == null)
        throw new ClientError("Invalid response type - "
            + rawResponseMessage.getClass());
      return responseMessage;
    }
    catch (HttpException e)
    {
      throw new ClientError(e);
    }
    catch (IOException e)
    {
      throw new ClientError(e);
    }
    catch (JSONException e)
    {
      throw new ClientError(e);
    }
  }
  
  /**
   * Expose commons-httpclient host configuration, for
   * setting configuration parameters like proxy.
   * 
   * @return host configuration of the current HttpClient object 
   */
  public HostConfiguration getHostConfiguration() {
	  return http().getHostConfiguration();
  }

  HttpClient http()
  {
    if (client == null)
    {
      client = new HttpClient();
      if (state == null)
      {
        state = new HttpState();
      }
      client.setState(state);
    }
    return client;
  }

  public void close()
  {
    state.clear();
    state = null;
  }

  static class Factory implements SessionFactory
  {
    public Session newSession(URI uri)
    {
      return new HTTPSession(uri);
    }
  }

  /**
   * Register this transport in 'registry'
   */
  public static void register(TransportRegistry registry)
  {
    registry.registerTransport("http", new Factory());
  }
}
