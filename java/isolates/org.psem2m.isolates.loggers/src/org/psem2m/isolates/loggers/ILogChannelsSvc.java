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

import java.io.File;
import java.util.List;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 *
 */
public interface ILogChannelsSvc {

	boolean CREATE_IF_NOT_EXIXT = true;

	// 10 files
	int LOGCHANNEL_FILE_COUNT_MAX = 10;
	// 10 Mo
	int LOGCHANNEL_FILE_SIZE = 1024 * 1024 * 10;

	/**
	 * Close the channel and clean all the files. .
	 *
	 * @param aChannelId
	 *            the channel id of the logger
	 * @return the number of deleted files
	 * @throws CLogChannelException
	 *             if the channel isn't closed or if IO error
	 */
	int cleanLogChannelFiles(String aChannelId) throws CLogChannelException;

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
	 * @return the instance of Logger corresponding to the channel id. Create
	 *         the channel if it doesn't exist
	 * @throws CLogChannelException
	 *             if an error occurs during the creation of the channel
	 */
	ILogChannelSvc getLogChannel(String aChannelId) throws CLogChannelException;

	/**
	 * @param aChannelId
	 *            the channel id of the logger to retrieve
	 * @param aFileSize
	 *            the size of each file
	 * @param aFileCount
	 *            the number of files of the channel
	 * @param aMultiline
	 *            the flag to controle the format of the log : monoline or
	 *            multilines
	 * @return the instance of Logger corresponding to the channel id
	 * @throws CLogChannelException
	 *             if an error occurs during the creation of the channel
	 */
	ILogChannelSvc getLogChannel(String aChannelId, final int aFileSize,
			final int aFileCount, final boolean aMultiline)
			throws CLogChannelException;

	/**
	 * @param aChannelId
	 *            the channel id of the logger
	 * @param aFileIdx
	 * @return the instance of the file
	 * @throws CLogChannelException
	 *             if the channel or if the specified file doesn't exist
	 */
	File getLogChannelFile(String aChannelId, final int aFileIdx)
			throws CLogChannelException;

	/**
	 * @param aChannelId
	 *            the channel id of the logger
	 * @return the list of the files of the logger
	 * @throws CLogChannelException
	 *             if the channel or if the specified file doesn't exist
	 */
	List<File> getLogChannelFiles(String aChannelId)
			throws CLogChannelException;

	/**
	 * Closes and deletes the channel but leaves the files in place.
	 *
	 *
	 * @param aChannelId
	 *            the channel id of the logger
	 * @return true if the chanel is removed
	 * @throws CLogChannelException
	 *             if the channel doesn't exist.
	 */
	boolean removeLogChannel(String aChannelId) throws CLogChannelException;

}
