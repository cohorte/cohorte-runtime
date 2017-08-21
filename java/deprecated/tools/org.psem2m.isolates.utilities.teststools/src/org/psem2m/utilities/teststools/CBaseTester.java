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
package org.psem2m.utilities.teststools;

import java.io.File;
import java.util.Properties;

import org.psem2m.utilities.CXBytesUtils;
import org.psem2m.utilities.CXObjectBase;

/**
 * @author ogattaz
 * 
 */
public abstract class CBaseTester extends CXObjectBase {

	protected final static String[] NOFIXE = { "no fixe" };

	protected final static String NOVERSION = "no version";

	public String CR = "\n";

	public String EMPTY = "";

	private String[] pArgs = null;

	private Properties pConfig = null;

	/**
	 * @param args
	 * @throws Exception
	 */
	public CBaseTester(final String[] args) throws Exception {
		pArgs = initArgs(args);
	}

	/**
	 * @param aSB
	 * @param aName
	 * @param aBoolean
	 */
	public void addOneArgsParam(final StringBuilder aSB, final String aName, final boolean aBoolean) {
		addOneArgsParam(aSB, aName, (aBoolean) ? "yes" : "no");
	}

	/**
	 * @param aSB
	 * @param aName
	 * @param aValue
	 */
	public void addOneArgsParam(final StringBuilder aSB, final String aName, final int aValue) {
		addOneArgsParam(aSB, aName, String.valueOf(aValue));
	}

	/**
	 * @param aSB
	 * @param aName
	 * @param aValue
	 */
	public void addOneArgsParam(final StringBuilder aSB, final String aName, final long aValue) {
		addOneArgsParam(aSB, aName, String.valueOf(aValue));
	}

	/**
	 * @param aSB
	 * @param aName
	 * @param aValue
	 */
	public void addOneArgsParam(final StringBuilder aSB, final String aName, final String aValue) {
		aSB.append(aName).append('=').append(aValue).append('&');
	}

	/**
	 * @param aProperty
	 * @param aComment
	 * @return
	 */
	private String displayBinaryProperty(final String aProperty, final String aComment) {
		String wBinary = CXBytesUtils.byteBufferBin2Hexa(System.getProperty(aProperty).getBytes());
		return displayProperty(aProperty, wBinary, aComment);
	}

	/**
	 * @param aProperty
	 * @param aComment
	 * @return
	 */
	private String displayPathProperty(final String aProperty, final String aComment) {
		String wPath = "\n" + System.getProperty(aProperty).replace(File.pathSeparatorChar, '\n')
				+ "\n";
		return displayProperty(aProperty, wPath, aComment);
	}

	/**
	 * @param aProperty
	 * @param aValue
	 * @param aComment
	 * @return
	 */
	private String displayProperty(final String aProperty, final String aValue,
			final String aComment) {
		StringBuilder wSB = new StringBuilder();
		wSB.append("- ");
		wSB.append(aProperty);
		wSB.append("=[");
		wSB.append(aValue);
		wSB.append("]\t");
		wSB.append(aComment);
		wSB.append("\n");
		return wSB.toString();
	}

	/**
	 * @return
	 */
	String displaySystemInfos() {
		StringBuilder wSB = new StringBuilder();
		wSB.append(displaySystemProperty("file.separator", "File separator, for example='/'"));
		wSB.append(displayPathProperty("java.class.path", "Java classpath"));
		wSB.append(displaySystemProperty("java.class.version", "Java class version number"));
		wSB.append(displaySystemProperty("java.home", "Java installation directory"));
		wSB.append(displaySystemProperty("java.vendor", "Java vendor-specific string"));
		wSB.append(displaySystemProperty("java.vendor.url", "Java vendor URL"));
		wSB.append(displaySystemProperty("java.version", "Java version number"));
		wSB.append(displayBinaryProperty("line.separator", "Line separator"));
		wSB.append(displaySystemProperty("os.arch", "Operating system architecture"));
		wSB.append(displaySystemProperty("os.name", "Operating system name"));
		wSB.append(displaySystemProperty("os.version", "Operating system version"));
		wSB.append(displaySystemProperty("path.separator", "Path separator, for example=':'"));
		wSB.append(displaySystemProperty("user.dir", "User's current working directory"));
		wSB.append(displaySystemProperty("user.home", "User home directory"));
		wSB.append(displaySystemProperty("user.name", "User account name"));
		return wSB.toString();
	}

	/**
	 * @param aProperty
	 * @param aComment
	 * @return
	 */
	private String displaySystemProperty(final String aProperty, final String aComment) {
		return displayProperty(aProperty, System.getProperty(aProperty), aComment);
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

	/**
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String[] initArgs(final String[] args) throws Exception {
		return args; // do other thing in extended classes
	}

	/**
	 * @param aConfig
	 */
	void setConfig(final Properties aConfig) {
		pConfig = aConfig;
	}

}