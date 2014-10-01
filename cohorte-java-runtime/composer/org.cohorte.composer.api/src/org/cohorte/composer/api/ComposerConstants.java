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

package org.cohorte.composer.api;

/**
 * Isolate composer constants
 *
 * @author Thomas Calmant
 */
public interface ComposerConstants {

    /** Instance name */
    String PROP_INSTANCE_NAME = "instance.name";

    /** Name of the hosting isolate */
    String PROP_ISOLATE_NAME = "cohorte.composer.isolate.name";

    /** Name of the hosting node */
    String PROP_NODE_NAME = "cohorte.composer.node.name";

    /** UID of the hosting node */
    String PROP_NODE_UID = "cohorte.composer.node.uid";

    /** Name of the isolate composer specification in Python */
    String SYNONYM_ISOLATE_COMPOSER = "python:/cohorte.composer.isolate";
}
