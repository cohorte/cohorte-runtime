package org.psem2m.utilities.teststools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.IXDescriber;
import org.psem2m.utilities.files.CXFileDir;

/**
 * @author ogattaz
 * 
 */
public abstract class CConsoleTester extends CBaseTester {

	public final static String APPDIR_NAME = "appdir";

	private static final String CMDE_ARGS = "args";

	private static final String CMDE_CONFIG = "config";

	private static final String CMDE_HELP = "help";

	private static final String CMDE_HELP2 = "?";

	private static final String CMDE_INFOS = "infos";

	private static final String CMDE_QUIT = "quit";

	private static final String CMDE_QUIT2 = "close";

	private static final String CMDE_QUIT3 = "exit";

	private static final String CMDE_REDO = "r";

	protected static final String CMDE_TEST = "test";

	private static final String CMDE_VERS = "vers";

	private final static String HELP_LINE_PREFIX = "  -";

	private final static String HELP_SUBLINE_PREFIX = "    .";

	private final static String HELP_TITLE_PREFIX = "---";

	private static final String PROMPT = "=>";

	private final static String SYS_PROPERTY_FILEENCODING = "file.encoding";

	private final static String SYS_PROPERTY_OSNAME = "os.name";

	/**
	 * @param aSB
	 * @param aId
	 * @param aValue
	 * @return
	 */
	public static StringBuilder addDescrInSB(final StringBuilder aSB,
			final String aId, final boolean aValue) {
		return addDescrInSB(aSB, aId, String.valueOf(aValue));
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aValue
	 * @return
	 */
	public static StringBuilder addDescrInSB(final StringBuilder aSB,
			final String aId, final int aValue) {
		return addDescrInSB(aSB, aId, String.valueOf(aValue));
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aValue
	 * @return
	 */
	public static StringBuilder addDescrInSB(final StringBuilder aSB,
			final String aId, final long aValue) {
		return addDescrInSB(aSB, aId, String.valueOf(aValue));
	}

	/**
	 * @param aSB
	 * @param aId
	 * @param aValue
	 * @return
	 */
	public static StringBuilder addDescrInSB(final StringBuilder aSB,
			final String aId, final String aValue) {
		aSB.append(aId);
		aSB.append('=');
		aSB.append('[');
		aSB.append(aValue);
		aSB.append(']');
		return aSB;
	}

	/**
	 * @return
	 */
	public static String dumpSupportedEncodings() {
		Iterator<String> wEncodings = getSupportedEncodings();
		StringBuilder wSB = new StringBuilder(256);
		while (wEncodings.hasNext()) {
			if (wSB.length() > 0) {
				wSB.append(',');
			}
			wSB.append(wEncodings.next());
		}
		return wSB.toString();
	}

	/**
	 * @return
	 */
	public static String getOsFileEncoding() {
		return System.getProperty(SYS_PROPERTY_FILEENCODING);
	}

	/**
	 * @return
	 */
	public static String getOsName() {
		return System.getProperty(SYS_PROPERTY_OSNAME);
	}

	/**
	 * @return
	 */
	public static Iterator<String> getSupportedEncodings() {
		return java.nio.charset.Charset.availableCharsets().keySet().iterator();
	}

	/**
	 * @return
	 */
	public static CXFileDir getUserDir() {
		return new CXFileDir(System.getProperty("user.dir"));
	}

	/**
	 * @param aConsoleTester
	 */
	public static void main(final CConsoleTester aConsoleTester) {
		int wErrorLevel = 0;
		try {
			aConsoleTester.initTester();
			aConsoleTester.monitor();
			wErrorLevel = 0;
		} catch (Throwable e) {
			System.out.println("CConsoleTester.main : EXCEPTION:");
			System.out.println(CXException.eInString(e));
			wErrorLevel = 1;
		}

		if (aConsoleTester != null) {
			aConsoleTester.destroy();
		}

		if (wErrorLevel > 0) {
			waitForUser();
		}
		System.exit(wErrorLevel);
	}

	/**
   * 
   */
	private static void waitForUser() {
		try {
			Thread.currentThread().wait(1000);
		} catch (Exception e) {
		}
		System.out.println(" \"ENTER\" to continue...");
		BufferedReader wInput = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			wInput.readLine();
		} catch (IOException e) {
		}
	}

	private String pLastLine;

	/**
	 * @param args
	 * @throws CTesterException
	 */
	public CConsoleTester(final String[] args) throws Exception {
		super(args);
		dumpArgs();
	}

	/**
	 * @param aHelp
	 * @param aLine
	 * @return
	 */
	protected StringBuilder addHelpLine(final StringBuilder aHelp,
			final String... aLineParts) {
		return addInHelpSB(aHelp, HELP_LINE_PREFIX, aLineParts);
	}

	/**
	 * @param aHelp
	 * @param aLine
	 * @return
	 */
	protected StringBuilder addHelpSubLine(final StringBuilder aHelp,
			final String... aLineParts) {
		return addInHelpSB(aHelp, HELP_SUBLINE_PREFIX, aLineParts);
	}

	/**
	 * @param aHelp
	 * @param aLine
	 * @return
	 */
	protected StringBuilder addHelpTitle(final StringBuilder aHelp,
			final String... aLineParts) {
		return addInHelpSB(aHelp, HELP_TITLE_PREFIX, aLineParts);
	}

	/**
	 * @param aHelp
	 * @param aPrefix
	 * @param aLine
	 * @return
	 */
	private StringBuilder addInHelpSB(final StringBuilder aHelp,
			final String aPrefix, final String... aLineParts) {
		aHelp.append(aPrefix);
		int wMax = (aLineParts != null) ? aLineParts.length : 0;
		int wI = 0;
		String wStr;
		boolean wNewLine;
		while (wI < wMax) {
			wStr = aLineParts[wI];
			wNewLine = wStr != null && wStr.length() > 0
					&& wStr.charAt(0) == '\n';
			if (wNewLine) {
				aHelp.append('\n').append(aPrefix);
				wStr = wStr.substring(1);
			}
			aHelp.append(' ').append(wStr);
			wI++;
		}
		return aHelp.append('\n');
	}

	/**
   * 
   */
	public void addLine() {
		logInfo(EMPTY);
	}

	/**
	 * @param aDescriber
	 */
	public void addLine(final IXDescriber aDescriber) {
		try {
			logInfo(aDescriber.toDescription());
		} catch (Exception e) {
			logInfo(e);
		}
	}

	/**
	 * @param aHelp
	 */
	protected void buildHelp(final StringBuilder aHelp) {

	}

	/**
	 * @return
	 */
	private String buildVers() {
		StringBuilder wSB = new StringBuilder(256);
		CXStringUtils.appendKeyValInBuff(wSB, "Version", getVersion());

		String[] wCorrections = getCorrections();
		if (wCorrections != null) {
			for (int wI = 0; wI < wCorrections.length; wI++) {
				wSB.append('\n');
				CXStringUtils.appendKeyValInBuff(wSB, "(" + wI + ")",
						wCorrections[wI]);
			}
		}
		return wSB.toString();
	}

	/**
	 * 16w_000
	 * 
	 * @param aLine
	 * @return
	 */
	private String cleanLine(final String aLine) {
		return CXStringUtils.strKeepCharGreaterThan(aLine, ' ');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXObjectBase#destroy()
	 */
	@Override
	public void destroy() {

	}

	/**
   * 
   */
	private void displayArgs() {
		StringBuilder wSB = new StringBuilder();
		if (getArgs() != null) {
			int wMax = getArgs().length;
			wSB.append("- Arguments:")
					.append(CXStringUtils.formatKeyValueInString("NbArg(s)",
							wMax)).append('\n');
			for (int wI = 0; wI < wMax; wI++) {
				if (wI > 0) {
					wSB.append('\n');
				}
				wSB.append(getArgs()[wI]);
			}
		} else {
			wSB.append("- no arguments");
		}
		logInfoAndPrompt(wSB.toString());
	}

	/**
   * 
   */
	protected void displayConfig() {
		StringBuilder wSB = new StringBuilder();
		if (getConfig() != null) {
			int wI = 0;
			wSB.append("- Config:")
					.append(CXStringUtils.formatKeyValueInString(
							"NbProperty(ies)", getConfig().size()))
					.append('\n');
			Enumeration<?> wNames = getConfig().propertyNames();
			Enumeration<?> wElements = getConfig().elements();

			while (wNames.hasMoreElements()) {
				if (wI > 0) {
					wSB.append('\n');
				}
				CXStringUtils.appendKeyValInBuff(wSB,
						(String) wNames.nextElement(),
						(String) wElements.nextElement());
				wI++;
			}
		} else {
			wSB.append("- no config");
		}
		logInfoAndPrompt(wSB.toString());
	}

	private void displayHelp() {
		StringBuilder wSB = new StringBuilder(256);

		buildHelp(wSB);

		addHelpTitle(wSB, "Les commandes de base:");
		addHelpLine(wSB, CMDE_HELP, ": listes les commandes disponibles.");
		addHelpLine(wSB, CMDE_REDO, ": 'redo'");
		addHelpLine(wSB, CMDE_CONFIG, ": la configuratiion.");
		addHelpLine(wSB, CMDE_ARGS, ": les arguments du programme de test.");
		addHelpLine(wSB, CMDE_INFOS, ": les proprietes de la JVM.");
		addHelpLine(wSB, CMDE_QUIT, "/", CMDE_QUIT2, "/", CMDE_QUIT3);
		addHelpLine(wSB, CMDE_VERS, ": la version du programme de test.");

		logInfoAndPrompt(wSB.toString());
	}

	private void displayInfos() {
		StringBuilder wSB = new StringBuilder();
		wSB.append("- Les proprietes de la JVM :\n").append(
				displaySystemInfos());
		logInfoAndPrompt(wSB.toString());
	}

	/**
   * 
   */
	private void displayVers() {
		logInfo(buildVers());
		logInfoAndPrompt("OK");
	}

	/**
   * 
   */
	private void dumpArgs() {
		if (hasArgs()) {
			int wMax = getArgs().length;
			int wI = 0;
			while (wI < wMax) {
				logPair("Args[" + wI + ']', getArgs()[wI]);
				wI++;
			}
		} else {
			logInfo("No Args");
		}
	}

	/**
	 * pour executer des ligne de commande au demararge du moniteur
	 * 
	 * @return
	 */
	protected void execInitLines() {
		// a overwrider ...
	}

	/**
	 * @param aLine
	 * @return
	 */
	public boolean execLine(String aLine) throws Exception {
		boolean wRun = true;
		StringTokenizer wST;
		String wCommande;

		// decoupage de la ligne de commande
		wST = new StringTokenizer(aLine);
		// extraction de la commande
		if (wST.hasMoreTokens()) {

			wCommande = (String) wST.nextElement();

			if (isCommand(wCommande, CMDE_REDO)) {
				aLine = pLastLine;
				wST = new StringTokenizer(aLine);
				wCommande = (String) wST.nextElement();
			} else {
				memoCommand(aLine);
			}

			// lancement du traitement
			if (isCommand(wCommande, CMDE_ARGS)) {
				displayArgs();
			} else if (isCommand(wCommande, CMDE_CONFIG)) {
				displayConfig();
			} else if (isCommand(wCommande, CMDE_HELP, CMDE_HELP2)) {
				displayHelp();
			} else if (isCommand(wCommande, CMDE_INFOS)) {
				displayInfos();
			} else if (isCommand(wCommande, CMDE_QUIT, CMDE_QUIT2, CMDE_QUIT3)) {
				wRun = false;
			} else if (isCommand(wCommande, CMDE_VERS)) {
				displayVers();
			} else {
				if (!monitorCommand(wCommande, aLine, wST)) {
					logInfoAndPrompt("Unknown command [" + wCommande + "]");
				}
			}
		} else {
			displayHelp();
		}

		return wRun;
	}

	/**
	 * @param aNbMillisecs
	 * @return
	 */
	protected boolean execTempo(final int aNbMillisecs) {
		try {
			Thread.sleep(aNbMillisecs);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @param aClassName
	 * @return
	 * @throws Exception
	 */
	public Class<?> getClassForName(final String aClassName) throws Exception {
		return Class.forName(aClassName);
	}

	/**
	 * @param aObj
	 * @return
	 */
	public Class<?> getClassOfObj(final Object aObj) {
		return aObj.getClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adonix.util.tester.CBaseTester#initTester()
	 */
	public void initTester() throws Exception {

	}

	/**
	 * @param aObj
	 * @return
	 */
	public boolean isClassArray(final Object aObj) {
		return aObj.getClass().isArray();
	}

	/**
	 * @param command
	 * @param aCommandDefs
	 * @return
	 */
	public boolean isCommand(final String command, final String... aCommandDefs) {
		for (String wCommandDef : aCommandDefs) {
			if (wCommandDef.equalsIgnoreCase(command)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param aString
	 */
	public void logInfo(final String aString) {
		System.out.println(aString);
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 */
	public void logInfo(final String aFormat, final Object... aArgs) {
		logInfo(String.format(aFormat, aArgs));
	}

	/**
	 * @param aString
	 */
	public void logInfo(final Throwable e) {
		logInfo(CXException.eInString(e));
	}

	/**
	 * @param aGravite
	 * @param aLine
	 */
	public void logInfoAndPrompt(final String aLine) {
		if (aLine.length() > 0) {
			logInfo(aLine);
		}
		if (!hasArgValue("junit")) {
			logInfo(PROMPT);
		}
	}

	/**
   * 
   */
	public void logLineSeparator() {
		logLineSeparator(80);
	}

	/**
	 * @param aLen
	 */
	public void logLineSeparator(final int aLen) {
		logInfo(CXStringUtils.strFromChar('=', aLen));
	}

	/**
	 * @param aId
	 * @param aValue
	 */
	public void logPair(final String aId, final boolean aValue) {
		logInfo(CXStringUtils.formatKeyValueInString(aId, aValue));
	}

	/**
	 * @param aId
	 * @param aValue
	 */
	public void logPair(final String aId, final int aValue) {
		logInfo(CXStringUtils.formatKeyValueInString(aId, aValue));
	}

	/**
	 * @param aId
	 * @param aValue
	 */
	public void logPair(final String aId, final String aValue) {
		logInfo(CXStringUtils.formatKeyValueInString(aId, aValue));
	}

	private void memoCommand(final String aLine) {
		pLastLine = aLine;
	}

	/**
	 * 
	 */
	private void monitor() {
		logInfo(this.getClass().toString() + ".monitor()");
		logInfo(buildVers());

		execInitLines();

		boolean wRun = true;
		String wLine;

		BufferedReader wInput = new BufferedReader(new InputStreamReader(
				System.in));

		logInfoAndPrompt("ready...");

		while (wRun) {

			try {
				wLine = wInput.readLine();
			} catch (IOException e) {
				logInfo(this.getClass().toString()
						+ ".monitor() => readLine error.");
				logInfo(e);
				wLine = CMDE_QUIT;
			}
			try {
				wRun = execLine(cleanLine(wLine));
			} catch (Throwable e) {
				logInfo(e);
				wRun = true;
			}

		}
		logInfo(this.getClass().toString() + ".monitor() : end");
	}

	/**
	 * @param aCommand
	 * @param aLine
	 * @param aST
	 * @return
	 * @throws Exception
	 */
	public abstract boolean monitorCommand(String aCommand, String aLine,
			StringTokenizer aST) throws Exception;

	/**
	 * @param aAAAMMJJ
	 * @return
	 */
	public Date newDateFromAAAAMMJJ(final String aAAAMMJJ) {
		int wYear = Integer.parseInt(aAAAMMJJ.substring(0, 4));
		int wMonth = Integer.parseInt(aAAAMMJJ.substring(4, 6)) - 1;
		int wDay = Integer.parseInt(aAAAMMJJ.substring(6));
		Calendar wCalendar = Calendar.getInstance();
		wCalendar.set(wYear, wMonth, wDay);
		return wCalendar.getTime();
	}

}
