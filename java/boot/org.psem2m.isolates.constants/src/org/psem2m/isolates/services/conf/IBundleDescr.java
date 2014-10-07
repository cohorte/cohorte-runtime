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

package org.psem2m.isolates.services.conf;

import java.io.Serializable;

/**
 * Describes a bundle
 *
 * @author Thomas Calmant
 */
public interface IBundleDescr extends Serializable {

    /**
     * The file path as indicated in the configuration file : String. Can be
     * null.
     */
    String BUNDLE_FILE = "file";

    /**
     * The bundle symbolic name : String
     */
    String BUNDLE_NAME = "symbolicName";

    /**
     * If true, the isolate state is valid even if this bundle is not present :
     * Boolean False by default.
     */
    String BUNDLE_OPTIONAL = "optional";

    /**
     * The set of properties declared in the bundle description. (Map String
     * -&gt; String)
     */
    String BUNDLE_PROPERTIES = "properties";

    /**
     * The bundle version as indicated in the configuration file : String. Can
     * be null.
     */
    String BUNDLE_VERSION = "version";
}
