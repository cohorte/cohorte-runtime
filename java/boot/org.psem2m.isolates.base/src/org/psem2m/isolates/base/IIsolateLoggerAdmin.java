package org.psem2m.isolates.base;

/**
 * MOD_OG_20160906
 *
 * @author ogattaz
 *
 */
public interface IIsolateLoggerAdmin {

	/**
	 * the toppic of the herald messages hraed by the component
	 * CIsolateLoggerGogoCommand
	 */
	String ISOLATES_LOGGER_MESSAGE = "cohorte/runtime/isolates/logger";

	/**
	 * <pre>
	 * LoggerName=[cohorte.isolate.iotagregator.IOTA-GREG-ATOR-ISOL-1+4]
	 * CurrentLevel=[ALL]
	 * FilePathPattern=[/Users/ogattaz/workspaces/Cohorte_IoT_Pack_git/server/cohorte-base/log/Log-iotagregator-%g.txt]
	 * FileCountMax=[10]
	 * FileSizeLimit=[10485760]
	 * NbExistingFiles=[1]
	 * File( 0) Path=[/Users/ogattaz/workspaces/Cohorte_IoT_Pack_git/server/cohorte-base/log/Log-iotagregator-0.txt] LastModified=[2016-09-09T12:45:58.0000000+0200] Size=[66649]
	 * </pre>
	 *
	 * <pre>
	 * {
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
	 * @param aMimeType
	 *            text/plain, application/json, null
	 * @return text of Json text according the given MimeType
	 * @see (Source: RFC 4627).
	 */
	String getLoggerInfos(final String aMimeType);

	/**
	 * Set the log level of the isolatelogger
	 *
	 * @param aLevelName
	 */
	void setLevel(String aLevelName);

	/**
	 * Set the log level of the all the isolateloggers
	 *
	 * @param aLevelName
	 */
	void setLevelAll(String aLevelName);

}
