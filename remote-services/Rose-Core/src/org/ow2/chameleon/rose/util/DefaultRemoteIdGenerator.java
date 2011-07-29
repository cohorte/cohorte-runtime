/*
 * Copyright 2009 OW2 Chameleon
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
 */

package org.ow2.chameleon.rose.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.rose.RemoteIdGenerator;

/**
 * This is a default implementation of a RemoteIdGenerator. It uses the local
 * host adress, the serviceexporter name and the serviceid.
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public class DefaultRemoteIdGenerator implements RemoteIdGenerator {

    private static String prefix;

    public DefaultRemoteIdGenerator() {
        try {
            prefix = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            prefix = UUID.randomUUID().toString();
        }
        prefix += "-"+System.getProperty("org.osgi.service.http.port");
    }

    public String generateRemoteId(final ServiceReference sref, final String exporterName) {
        String serviceid = String.valueOf(sref.getProperty(Constants.SERVICE_ID));

        return prefix + "-" + exporterName + "-" + serviceid;
    }

}
