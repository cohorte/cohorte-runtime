/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.utilities.logging.test;

import java.util.StringTokenizer;
import java.util.logging.Level;

import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.CActivityLoggerStd;
import org.psem2m.utilities.logging.CActivityLoggerXml;
import org.psem2m.utilities.logging.CLogToolsException;
import org.psem2m.utilities.logging.IActivityLogger;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public class CLogTester extends CConsoleTester {

	final static String CMDE_LOG = "log";

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.teststools.CConsoleTester#buildHelp(java.lang.
	 * StringBuilder)
	 */
	@Override
	protected void buildHelp(final StringBuilder aHelp) {
		addHelpTitle(aHelp, "CLogTester");
		addHelpLine(aHelp, "log [logKind [NbLines [FileSize [FileCount]]]]");
		addHelpSubLine(aHelp,
				"tests the three log formats (logKind \"log\" or \"txt\" or \"xml\")");
		addHelpSubLine(aHelp,
				"generate the log files in the folder \"${user.dir}/files/logging\"");
		addHelpSubLine(aHelp, "ex: log xml 5000 1m 10");
	}

	private String buildLogContent(final int aIdx, final Level alevel) {
		return String.format("log line [%d][%s]", aIdx, alevel);

	}

	/**
	 * @return true : the dommand is done!
	 */
	private boolean doCmdeLog(final StringTokenizer aST) throws Exception {

		this.logInfo(CMDE_LOG + " begin");

		CXFileDir wLogDir = new CXFileDir(getUserDir(), "var");
		wLogDir = new CXFileDir(wLogDir, "log");
		if (!wLogDir.exists()) {
			wLogDir.createHierarchy();
		}

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
		String wLine;
		int wI = 0;
		while (wI < aNbLogs) {

			wLine = buildLogContent(wI, Level.FINE);
			aLogger.logDebug(this, null, wLine);
			wI++;
			tempo(2);

			wLine = buildLogContent(wI, Level.INFO);
			aLogger.logInfo(this, "logInfo", wLine);
			wI++;
			tempo(2);
			wLine = buildLogContent(wI, Level.WARNING);
			aLogger.logWarn(this, "logWarn", wLine);
			wI++;
			tempo(1);
			wLine = buildLogContent(wI, Level.SEVERE);
			aLogger.logSevere(this, "logError", wLine, new Exception(wLine));
			wI++;
			tempo(1);
		}
	}

}
