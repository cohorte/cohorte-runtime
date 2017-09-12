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

package org.psem2m.isolates.base;

import org.psem2m.utilities.logging.IActivityLoggerJul;

/**
 * @author ogattaz
 *
 */
public interface IIsolateLoggerSvc extends IActivityLoggerJul {

	/**
	 * MOD_OG_1.0.14
	 *
	 * ATTENTION This name is used in the bundle "org.cohorte.slf4j-OCIL"
	 *
	 * <pre>
	 * @Requires(filter="(julname=org.chohorte.isolate.logger.sv)")
	 * </pre>
	 *
	 * @see org.slf4j.impl.CCpntOcilLoggerFactory
	 */
	String ISOLATE_LOGGER_NAME = "org.chohorte.isolate.logger.svc";

}
