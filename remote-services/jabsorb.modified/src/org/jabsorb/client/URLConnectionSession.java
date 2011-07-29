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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Transport based on URLConnection
 *
 */
public class URLConnectionSession implements Session
{
  URL url;

  /**
   * Create a URLConnection transport
   * @param url 
   */
  URLConnectionSession(URL url)
  {
    this.url = url;
  }

  public void close()
  {
  }

  public JSONObject sendAndReceive(JSONObject message)
  {
    try
    {
      URLConnection connection= url.openConnection();
      connection.setDoOutput(true);
      // As per http://java.sun.com/docs/books/tutorial/networking/urls/readingWriting.html
      Writer request = new OutputStreamWriter(connection.getOutputStream());
      request.write(message.toString());
      request.close();
      // TODO the following sequence of reading a string out of output stream is too complicated
      // there must be a simpler way
      StringBuffer builder = new StringBuffer(1024);
      char[] buffer = new char[1024];
      Reader reader = new InputStreamReader(connection.getInputStream());
      while (true)
      {
        int bytesRead = reader.read(buffer);
        if (bytesRead < 0)
          break;
        builder.append(buffer, 0, bytesRead);
      }
      reader.close();
      JSONTokener tokener = new JSONTokener(builder.toString());
      Object rawResponseMessage = tokener.nextValue();
      JSONObject responseMessage = (JSONObject) rawResponseMessage;
      if (responseMessage == null)
        throw new ClientError("Invalid response type - "
            + rawResponseMessage.getClass());
      return responseMessage;
    }
    catch (IOException ex)
    {
      throw new ClientError(ex);
    }
    catch (JSONException ex)
    {
      throw new ClientError(ex);
    }
  }

}
