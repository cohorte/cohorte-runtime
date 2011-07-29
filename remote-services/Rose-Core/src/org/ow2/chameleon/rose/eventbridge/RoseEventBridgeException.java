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
package org.ow2.chameleon.rose.eventbridge;

/**
 * @author <a href="mailto:chameleon-dev@ow2.org">Chameleon Project Team</a>
 */
public class RoseEventBridgeException extends Exception {

    /**
     * Final serial UID
     */
    private static final long serialVersionUID = 5974391363790243689L;

    public RoseEventBridgeException() {
        super("JmsBridgeException: ");
    }

    public RoseEventBridgeException(String message) {
        super("JmsBridgeException: " + message);
    }

    public RoseEventBridgeException(String message, Throwable cause) {
        super("JmsBridgeException: " + message, cause);
    }

}
