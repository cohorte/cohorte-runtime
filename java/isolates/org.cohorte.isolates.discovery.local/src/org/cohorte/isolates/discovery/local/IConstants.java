/**
 * Copyright 2016 Cohorte Technologies (ex. isandlaTech)
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

package org.cohorte.isolates.discovery.local;

/**
 * Constants used by the Local Discovery implementation
 *
 * @author Bassem Debbabi
 */
public interface IConstants {
	
	/** Name of the Local discovery component factory */
    String FACTORY_DISCOVERY_LOCAL = "cohorte-local-discovery-factory";
        
    /** Name of the Local discovery component instance */
    String INSTANCE_DISCOVERY_LOCAL = "cohorte-local-discovery";
     
    /** Herald message subject used to retrieve the list of neighbor peers */ 
    String SUBJECT_GET_NEIGHBORS_LIST = "cohorte/local/discovery/get_neighbors_list";
}
