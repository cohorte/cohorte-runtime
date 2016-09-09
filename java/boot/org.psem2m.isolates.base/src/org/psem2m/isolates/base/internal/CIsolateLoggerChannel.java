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

package org.psem2m.isolates.base.internal;

import java.util.logging.Formatter;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;
import org.psem2m.utilities.logging.CActivityFileHandler;
import org.psem2m.utilities.logging.CActivityFileText;
import org.psem2m.utilities.logging.CActivityFormaterHuman;
import org.psem2m.utilities.logging.CActivityLoggerBasic;

/**
 * @author ogattaz
 *
 */
public class CIsolateLoggerChannel extends CActivityLoggerBasic {

	/**
	 * @param aLoggerName
	 * @param aFilePathPattern
	 * @param aLevel
	 * @param aFileLimit
	 * @param aFileCount
	 * @throws Exception
	 */
	CIsolateLoggerChannel(final String aLoggerName,
			final String aFilePathPattern, final String aLevel,
			final int aFileLimit, final int aFileCount) throws Exception {

		super(aLoggerName, aFilePathPattern, aLevel, aFileLimit, aFileCount);
		initFileHandler();
		open();
	}

	@Override
	protected void initFileHandler() throws Exception {

		final CActivityFileHandler wFileHandler = new CActivityFileHandler(
				getFilePathPattern(), getFileLimit(), getFileCount());
		wFileHandler.setFormatter((Formatter) CActivityFormaterHuman
				.getInstance());
		super.setFileHandler(wFileHandler);
	}

	/**
	 * <pre>
	 * 	{
	 *   "nbfiles": 1,
	 *   "level": "ALL",
	 *   "name": "cohorte.isolate.iotagregator.IOTA-GREG-ATOR-ISOL-1+4",
	 *   "pattern": "/Users/ogattaz/workspaces/Cohorte_IoT_Pack_git/server/cohorte-base/log/Log-iotagregator-%g.txt",
	 *   "count": 10,
	 *   "limit": 10485760,
	 *   "files": [{
	 *     "path": "/Users/ogattaz/workspaces/Cohorte_IoT_Pack_git/server/cohorte-base/log/Log-iotagregator-0.txt",
	 *     "size": 68113,
	 *     "lastmodified": "2016-09-09T12:47:02.0000000+0200"
	 *   }]
	 * }
	 * </pre>
	 *
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {

		JSONObject wInfos = new JSONObject();

		wInfos.put("name", getLoggerName());
		wInfos.put("level", getLevel().getName());
		wInfos.put("pattern", getFilePathPattern());
		wInfos.put("count", getFileCount());
		wInfos.put("limit", getFileLimit());

		int wNbFile = getFileHandler().getExistingFileNames().size();
		wInfos.put("nbfiles", wNbFile);

		JSONArray wFiles = new JSONArray();
		wInfos.put("files", wFiles);

		for (CActivityFileText wActivityFileText : this.getFileHandler()
				.getExistingFiles()) {

			JSONObject wFile = new JSONObject();
			wFiles.put(wFile);
			wFile.put("path", wActivityFileText.getAbsolutePath());

			String wLastModified = CXDateTime
					.getIso8601TimeStamp(wActivityFileText.lastModified());
			wFile.put("lastmodified", wLastModified);
			wFile.put("size", wActivityFileText.size());
		}

		return wInfos;
	}

	/**
	 * <pre>
	 *  LoggerName=[cohorte.isolate.iotagregator.IOTA-GREG-ATOR-ISOL-1+4]
	 *  FilePathPattern=[/Users/ogattaz/workspaces/Cohorte_IoT_Pack_git/server/cohorte-base/log/Log-iotagregator-%g.txt]
	 *  Pattern=[/Users/ogattaz/workspaces/Cohorte_IoT_Pack_git/server/cohorte-base/log/Log-iotagregator-%g.txt]
	 *  Count=[5]
	 *  ExistingFileNames=[Log-iotagregator-0.txt]
	 *  Path=[/Users/ogattaz/workspaces/Cohorte_IoT_Pack_git/server/cohorte-base/log/Log-iotagregator-0.txt] LastModified=[20160909120034000]
	 * </pre>
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder wSB = new StringBuilder();
		wSB.append(String.format("LoggerName=[%s]", getLoggerName()));
		wSB.append('\n');
		wSB.append(String.format("CurrentLevel=[%s]", getLevel().getName()));
		wSB.append('\n');
		wSB.append(String.format("FilePathPattern=[%s]", getFilePathPattern()));
		wSB.append('\n');
		wSB.append(String.format("FileCountMax=[%s]", getFileCount()));
		wSB.append('\n');
		wSB.append(String.format("FileSizeLimit=[%s]", getFileLimit()));

		int wNbFile = this.getFileHandler().getExistingFileNames().size();
		wSB.append('\n');
		wSB.append(String.format("NbExistingFiles=[%s]", wNbFile));
		int wIdx = 0;
		for (CActivityFileText wActivityFileText : this.getFileHandler()
				.getExistingFiles()) {
			String wLastModified = CXDateTime
					.getIso8601TimeStamp(wActivityFileText.lastModified());

			wSB.append('\n').append(
					String.format(
							"File(%2d) Path=[%s] LastModified=[%s] Size=[%s]",
							wIdx, wActivityFileText.getAbsolutePath(),
							wLastModified, wActivityFileText.size()));
		}

		return wSB.toString();
	}
}
