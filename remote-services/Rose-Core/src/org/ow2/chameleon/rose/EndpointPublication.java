/*
 * Copyright 2009 OW2 Chameleon Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.ow2.chameleon.rose;

/**
 * All endpoints which required to be published (via discovery) must have a
 * local EndpointPublication service which contains their
 * RemoteServiceDesciprtion. The Publication of this service is handled by the
 * ServiceExporterHandler if used.
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public interface EndpointPublication {

    String KEY_REMOTE_DESCRIPTION = "endpoint.remote.service.description";

    String KEY_OBJECTCLASS = "endpoint.objectclass";
}
