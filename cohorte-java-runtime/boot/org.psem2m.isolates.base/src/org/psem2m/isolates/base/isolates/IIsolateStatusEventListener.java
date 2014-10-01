/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.psem2m.isolates.base.isolates;

import org.psem2m.isolates.base.isolates.boot.IsolateStatus;

/**
 * Represents an isolate event listener, notified when an isolate status
 * information is received.
 *
 * @author Thomas Calmant
 */
public interface IIsolateStatusEventListener {

    /**
     * Notifies the listener that an isolate has been declared lost
     * 
     * @param aIsolateId
     *            The lost isolate ID
     */
    void handleIsolateLost(String aIsolateId);

    /**
     * Notifies the listener that an IsolateStatus object as been received, or
     * that an isolate has been lost (the isolate status is null)
     * 
     * @param aSenderId
     *            The source isolate ID
     * @param aIsolateStatus
     *            The received object, null if the isolate has been lost
     */
    void handleIsolateStatusEvent(String aSenderId, IsolateStatus aIsolateStatus);
}
