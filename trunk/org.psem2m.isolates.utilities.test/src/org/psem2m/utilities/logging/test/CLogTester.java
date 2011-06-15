package org.psem2m.utilities.logging.test;

import java.util.StringTokenizer;

import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.CActivityLoggerStd;
import org.psem2m.utilities.logging.CActivityLoggerXml;
import org.psem2m.utilities.logging.CLogToolsException;
import org.psem2m.utilities.logging.IActivityLogger;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * @author ogattaz
 * 
 */
public class CLogTester extends CConsoleTester {

	private final static String CMDE_LOG = "log";

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			CConsoleTester.main(new CLogTester(args));
		} catch (Throwable e) {
			System.out.println("main(): EXCEPTION\n");
			System.out.println(CLogToolsException.getInstance().eInString(e));
			System.exit(1);
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public CLogTester(final String[] args) throws Exception {
		super(args);
	}

	@Override
	protected void buildHelp(final StringBuilder aHelp) {
		this.addHelpTitle(aHelp, "CLogTester");
		this.addHelpLine(aHelp,
				"log [logKind [NbLines [FileSize [FileCount]]]]");
		this.addHelpSubLine(aHelp,
				"tests the three log formats (logKind \"log\" or \"txt\" or \"xml\")");
		this.addHelpSubLine(aHelp,
				"generate the log files in the folder \"${user.dir}/files/logging\"");
		this.addHelpSubLine(aHelp, "ex: log xml 5000 1m 10");
	}

	/**
	 * @return true : the dommand is done!
	 */
	private boolean doCmdeLog(final StringTokenizer aST) throws Exception {

		this.logInfo(CMDE_LOG + " begin");

		CXFileDir wLogDir = new CXFileDir(getUserDir(), "files");
		wLogDir = new CXFileDir(wLogDir, "logging");

		String wKindLog = "txt";
		if (aST.hasMoreTokens()) {
			wKindLog = aST.nextToken();
		}

		int wNbLog = 1000;
		if (aST.hasMoreTokens()) {
			wNbLog = Integer.parseInt(aST.nextToken());
		}

		logInfo("NbLog=[" + wNbLog + "]");

		String wFileSize = "1m";
		if (aST.hasMoreTokens()) {
			wFileSize = aST.nextToken();
		}

		logInfo("FileSize=[" + wFileSize + "]");

		int wFileLimit = sizeToLimit(wFileSize);
		logInfo("FileLimit=[" + wFileLimit + "]");

		int wFileCount = 10;
		if (aST.hasMoreTokens()) {
			wFileCount = Integer.parseInt(aST.nextToken());
		}

		logInfo("FileCount=[" + wFileCount + "]");

		testOneLogger(wLogDir, wKindLog, wNbLog, wFileLimit, wFileCount);

		logInfoAndPrompt(CMDE_LOG + " end");
		return true;
	}

	@Override
	public boolean monitorCommand(final String aCommand, final String aLine,
			final StringTokenizer aST) throws Exception {

		if ("log".equalsIgnoreCase(aCommand)) {
			return doCmdeLog(aST);
		}

		return false;
	}

	/**
	 * 10 => 10 bytes 10k => 10 kilo bytes 10m => 10 mega bytes
	 * 
	 * @param aFileSize
	 * @return
	 * @throws Exception
	 */
	private int sizeToLimit(String aFileSize) throws Exception {
		aFileSize = aFileSize.toLowerCase();
		int wFileLimit = 100000;
		int wMultiplicateur = 1;

		int wPos = aFileSize.indexOf('m');
		if (wPos > -1) {
			aFileSize = aFileSize.substring(0, wPos);
			wMultiplicateur = 1000000;
		}
		wPos = aFileSize.indexOf('k');
		if (wPos > -1) {
			aFileSize = aFileSize.substring(0, wPos);
			wMultiplicateur = 1000;
		}

		int wNb = Integer.parseInt(aFileSize);

		wFileLimit = wNb * wMultiplicateur;

		return wFileLimit;
	}

	/**
	 * @param aSleepTime
	 */
	private void tempo(final long aSleepTime) {
		try {
			Thread.sleep(aSleepTime);
		} catch (Throwable e) {
			// nothing
		}
	}

	/**
	 * @param wLogDir
	 * @param aKindLog
	 * @param aNbLog
	 * @param aFileLimit
	 * @param aFileCount
	 * @throws Exception
	 */
	private void testOneLogger(final CXFileDir wLogDir, final String aKindLog,
			final int aNbLog, final int aFileLimit, final int aFileCount)
			throws Exception {

		String wLoggerNameBasic = getClass().getSimpleName() + '-' + aKindLog;
		logPair("LoggerName", wLoggerNameBasic);
		CXFile wLogFileTxt = new CXFile(wLogDir, "CLogTester_log_%g."
				+ aKindLog);
		logInfo("FilePathPattern=[" + wLogFileTxt.getAbsolutePath() + "]");
		IActivityLogger wLogger;

		if ("xml".equalsIgnoreCase(aKindLog)) {
			wLogger = CActivityLoggerXml.newLogger(wLoggerNameBasic,
					wLogFileTxt.getAbsolutePath(), IActivityLogger.ALL,
					aFileLimit, aFileCount);
		} else if ("log".equalsIgnoreCase(aKindLog)) {
			wLogger = CActivityLoggerStd.newLogger(wLoggerNameBasic,
					wLogFileTxt.getAbsolutePath(), IActivityLogger.ALL,
					aFileLimit, aFileCount);
		} else {
			wLogger = CActivityLoggerBasic.newLogger(wLoggerNameBasic,
					wLogFileTxt.getAbsolutePath(), IActivityLogger.ALL,
					aFileLimit, aFileCount);
		}
		logInfo(wLogger.toDescription());
		useLogger(wLogger, aNbLog);

		wLogger.close();
	}

	/**
	 * 14w_036 - Fiche 45445 - WebService : connexions du pool ne se libèrent
	 * pas
	 * 
	 * @param aLogger
	 * @param aNbLogs
	 */
	private void useLogger(final IActivityLogger aLogger, final int aNbLogs) {
		String wFormat = "Test le log numéro [%d]";
		String wLine;
		int wI = 0;
		while (wI < aNbLogs) {

			wLine = String.format(wFormat, wI);
			aLogger.logInfo(this, "logInfo", wLine);
			wI++;
			tempo(2);
			wLine = String.format(wFormat, wI);
			aLogger.logWarn(this, "logWarn", wLine);
			wI++;
			tempo(1);
			wLine = String.format(wFormat, wI);
			aLogger.logSevere(this, "logError", wLine, new Exception(wLine));
			wI++;
			tempo(1);
		}
	}

}
