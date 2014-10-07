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

package org.psem2m.isolates.loggers;

import java.util.List;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public interface ILogChannelsSvc {

    /**
     * @return the list of available channels
     */
    List<ILogChannelSvc> getChannels();

    /**
     * @return the list of the IDs of the available channels
     */
    List<String> getChannelsIds();

    /**
     * @param aChannelId
     *            the channel id of the logger to retrieve
     * @return the instance of Logger corresponding to the channel id
     */
    ILogChannelSvc getLogChannel(String aChannelId) throws Exception;
}
