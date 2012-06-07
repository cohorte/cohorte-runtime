package org.psem2m.utilities.teststools;

import java.io.File;
import java.util.Properties;

import org.psem2m.utilities.CXBytesUtils;
import org.psem2m.utilities.CXObjectBase;

public abstract class CBaseTester extends CXObjectBase {

	protected final static String[] NOFIXE = { "no fixe" };

	protected final static String NOVERSION = "no version";

	public String CR = "\n";

	public String EMPTY = "";

	private String[] pArgs = null;

	private Properties pConfig = null;

	public CBaseTester(final String[] args) throws Exception {
		pArgs = initArgs(args);
	}

	public void addOneArgsParam(final StringBuilder aSB, final String aName,
			final boolean aBoolean) {
		addOneArgsParam(aSB, aName, (aBoolean) ? "yes" : "no");
	}

	public void addOneArgsParam(final StringBuilder aSB, final String aName,
			final int aValue) {
		addOneArgsParam(aSB, aName, String.valueOf(aValue));
	}

	public void addOneArgsParam(final StringBuilder aSB, final String aName,
			final long aValue) {
		addOneArgsParam(aSB, aName, String.valueOf(aValue));
	}

	public void addOneArgsParam(final StringBuilder aSB, final String aName,
			final String aValue) {
		aSB.append(aName).append('=').append(aValue).append('&');
	}

	private String displayBinaryProperty(final String aProperty,
			final String aComment) {
		String wBinary = CXBytesUtils.byteBufferBin2Hexa(System.getProperty(
				aProperty).getBytes());
		return displayProperty(aProperty, wBinary, aComment);
	}

	private String displayPathProperty(final String aProperty,
			final String aComment) {
		String wPath = "\n"
				+ System.getProperty(aProperty).replace(File.pathSeparatorChar,
						'\n') + "\n";
		return displayProperty(aProperty, wPath, aComment);
	}

	private String displayProperty(final String aProperty, final String aValue,
			final String aComment) {
		StringBuilder wSB = new StringBuilder();
		wSB.append("- ");
		wSB.append(aProperty);
		wSB.append("=[");
		wSB.append(aValue);
		wSB.append("] ");
		wSB.append(aComment);
		wSB.append("\n");
		return wSB.toString();
	}

	String displaySystemInfos() {
		StringBuilder wSB = new StringBuilder();
		wSB.append(displaySystemProperty("file.separator",
				"\tFile separator, for example='/'"));
		wSB.append(displayPathProperty("java.class.path", "\tJava classpath"));
		wSB.append(displaySystemProperty("java.class.version",
				"\tJava class version number"));
		wSB.append(displaySystemProperty("java.home",
				"\tJava installation directory"));
		wSB.append(displaySystemProperty("java.vendor",
				"\tJava vendor-specific string"));
		wSB.append(displaySystemProperty("java.vendor.url", "\tJava vendor URL"));
		wSB.append(displaySystemProperty("java.version",
				"\tJava version number"));
		wSB.append(displayBinaryProperty("line.separator", "\tLine separator"));
		wSB.append(displaySystemProperty("os.arch",
				"\tOperating system architecture"));
		wSB.append(displaySystemProperty("os.name", "\tOperating system name"));
		wSB.append(displaySystemProperty("os.version",
				"\tOperating system version"));
		wSB.append(displaySystemProperty("path.separator",
				"\tPath separator, for example=':'"));
		wSB.append(displaySystemProperty("user.dir",
				"\tUser's current working directory"));
		wSB.append(displaySystemProperty("user.home", "\tUser home directory"));
		wSB.append(displaySystemProperty("user.name", "\tUser account name"));
		return wSB.toString();
	}

	private String displaySystemProperty(final String aProperty,
			final String aComment) {
		return displayProperty(aProperty, System.getProperty(aProperty),
				aComment);
	}

	/**
	 * @param aIdx
	 * @return
	 */
	public String getArg(final int aIdx) {
		return pArgs[aIdx];
	}

	/**
	 * @param aIdx
	 * @return
	 */
	public int getArgInt(final int aIdx) {
		return Integer.parseInt(pArgs[aIdx]);
	}

	/**
	 * @return
	 */
	public String[] getArgs() {
		return pArgs;
	}

	/**
	 * @return
	 */
	protected Properties getConfig() {
		return pConfig;
	}

	/**
	 * @return
	 */
	public String[] getCorrections() {
		return new String[0];
	}

	/**
	 * @return
	 */
	public int getNbArgs() {
		return pArgs.length;
	}

	/**
	 * @return
	 */
	public String getVersion() {
		return "?";
	}

	/**
	 * @return
	 */
	public boolean hasArgs() {
		return pArgs != null && pArgs.length > 0;
	}

	/**
	 * @param aArgValue
	 * @return
	 */
	public boolean hasArgValue(final String aArgValue) {
		if (!hasArgs() || aArgValue == null || aArgValue.isEmpty()) {
			return false;
		}
		for (String wArg : pArgs) {
			if (wArg != null && wArg.contains(aArgValue)) {
				return true;
			}
		}
		return false;
	}

	public String[] initArgs(final String[] args) throws Exception {
		return args; // do other thing in extended classes
	}

	void setConfig(final Properties aConfig) {
		pConfig = aConfig;
	}

}