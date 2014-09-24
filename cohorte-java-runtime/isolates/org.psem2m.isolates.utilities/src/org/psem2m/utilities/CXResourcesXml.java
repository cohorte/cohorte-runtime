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
package org.psem2m.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

/**
 * Load resource from xml properties available in the classpath
 * 
 * 
 * @author isandlaTech - ogattaz
 * 
 */
public class CXResourcesXml extends CXResources {

	private static final String XML = "xml";

	/**
	 * @param aId
	 * @param aLocale
	 */
	public CXResourcesXml(String aId, Locale aLocale) {
		super();
		pId = aId;
		setAskedLocale(aLocale);
		loadResourceXml(aId, aLocale);
	}

	/**
	 * @param aFullId
	 * @return
	 */
	private String buildErrMessUFRS(String aFullId) {
		return String.format("Unable to find resource stream [%s]", aFullId);
	}

	/**
	 * @param aId
	 * @param aSuffix
	 * @param aExtension
	 * @return
	 */
	private String buildFullId(String aId, String aSuffix, String aExtension) {
		StringBuilder wSB = new StringBuilder();
		if (aId != null && !aId.isEmpty()) {
			wSB.append(aId.replace('.', '/'));
		}
		if (aSuffix != null && !aSuffix.isEmpty()) {
			wSB.append('_').append(aSuffix);
		}
		if (aExtension != null && !aExtension.isEmpty()) {
			wSB.append('.').append(aExtension);
		}
		return wSB.toString();
	}

	/**
	 * @param aId
	 * @param aLocale
	 * @return
	 */
	private InputStream findResourceStream(String aId, Locale aLocale) throws Exception {
		ClassLoader wClassLoader = getClassLoader(aId);
		String wFullId;
		// err report
		StringBuilder wErrReport = new StringBuilder();

		wFullId = buildFullId(aId, aLocale.toString(), XML);
		InputStream wResourceStream = wClassLoader.getResourceAsStream(wFullId);

		// if no stream => mess in error report
		if (wResourceStream == null) {
			wErrReport.append(buildErrMessUFRS(wFullId));

			// if the locale (eg. "fr_FR") contains more than the languauge (eg.
			// "fr") => try only with the language
			if (!aLocale.getLanguage().equals(aLocale.toString())) {

				wFullId = buildFullId(aId, aLocale.getLanguage(), XML);
				wResourceStream = wClassLoader.getResourceAsStream(wFullId);

				if (wResourceStream == null) {
					wErrReport.append('\n').append(buildErrMessUFRS(wFullId));
				}
			}
		}
		// if no stream => Exception
		if (wResourceStream == null) {
			throw new Exception(wErrReport.toString());
		}

		return wResourceStream;
	}

	/**
	 * @param aId
	 * @param aLocale
	 * @return
	 */
	private void loadResourceXml(String aId, Locale aLocale) {
		try {
			InputStream wResourceStream = findResourceStream(aId, aLocale);

			// size of the streamed properties => 2 times the size of the xml
			// stream
			int wSize = wResourceStream.available() * 2;

			Properties wProperties = new Properties();
			wProperties.loadFromXML(wResourceStream);
			ByteArrayOutputStream wOuputStream = new ByteArrayOutputStream(wSize);
			wProperties.store(wOuputStream, CXStringUtils.EMPTY);
			ByteArrayInputStream wBAIS = new ByteArrayInputStream(wOuputStream.toByteArray());
			pResourceBundle = new PropertyResourceBundle(wBAIS);

		} catch (Exception e) {
			pWhy = e.getMessage();
			pResourceBundle = null;
		}
	}
}
