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

package org.jabsorb.localarg.impl;

import javax.servlet.http.HttpServletRequest;

import org.jabsorb.localarg.LocalArgResolveException;
import org.jabsorb.localarg.LocalArgResolver;

/**
 * A LocalArgResolver implementation that is registered by default on the
 * JSONRPCBridge and will replace an HttpSession argument on a called method
 * with the current session. If no session exists, a session will be created.
 */
public class HttpSessionArgResolver implements LocalArgResolver
{
  public Object resolveArg(Object context) throws LocalArgResolveException
  {
    if (!(context instanceof HttpServletRequest))
    {
      throw new LocalArgResolveException("invalid context");
    }

    HttpServletRequest request = (HttpServletRequest) context;
    return request.getSession();
  }
}
