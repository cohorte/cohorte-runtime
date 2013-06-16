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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

/**
 * Utilitaires pour la gestion des listes
 * 
 * -> Properties et Collections
 */
/**
 * @author ogattaz
 * 
 */
public class CXListUtils {

	/**
	 * CReation d'une propertie qui contient la somme de aPropB dans aPropA
	 * 
	 * @return nouvelle properties
	 */
	public static Properties addProperties(final Properties aPropA, final Properties aPropB) {

		Properties wResult = null;
		if (aPropB != null && aPropA != null) {
			wResult = new Properties(aPropA);
			wResult.putAll(aPropB);
		} else {
			wResult = new Properties();
		}
		return wResult;
	}

	/**
	 * Ajout de la table sous aRoot (childs) <key>value</key>
	 * 
	 * @return aRoot
	 */
	public static Element addPropertiesToDom(final Properties aProp, final Element aRoot) {

		if (aProp != null) {
			Iterator<Entry<Object, Object>> wIt = aProp.entrySet().iterator();
			while (wIt.hasNext()) {
				Entry<Object, Object> wEnt = wIt.next();
				String wKey = (String) wEnt.getKey();
				if (wKey != null && wKey.length() != 0) {
					CXDomUtils.appendTextChildElmt(aRoot, wKey, (String) wEnt.getValue());
				}
			}
		}
		return aRoot;
	}

	/**
	 * @param aCol
	 * @param aSepLines
	 * @return
	 */
	public static String collectionToString(final Collection<?> aCol, final String aSepLines) {

		StringBuilder wRes = new StringBuilder(2048);
		Iterator<?> wIt = aCol.iterator();
		while (wIt.hasNext()) {
			if (wRes.length() != 0) {
				wRes.append(aSepLines);
			}
			Object wObj = wIt.next();
			if (wObj == null) {
				wRes.append("null");
			} else {
				wRes.append(wObj.toString());
			}
		}
		return wRes.toString();
	}

	/**
	 * Renvoie les entree de B qui ont ete modifiees par rapport e A (ou qui ne
	 * sont pas presentent dans A)
	 * 
	 * @return nouvelle properties
	 */
	public static Properties diffProperties(final Properties aPropA, final Properties aPropB) {

		String wValA;
		Properties wResult = new Properties();
		if (aPropB != null) {
			Iterator<Entry<Object, Object>> wIt = aPropB.entrySet().iterator();
			while (wIt.hasNext()) {
				Entry<Object, Object> wEntB = wIt.next();
				String wKey = (String) wEntB.getKey();
				String wValB = (String) wEntB.getValue();
				if (aPropA != null) {
					wValA = aPropA.getProperty(wKey);
				} else {
					wValA = null;
				}
				if (wValA == null || !wValA.equals(wValB)) {
					wResult.put(wKey, wValB);
				}
			}
		}
		return wResult;
	}

	/**
	 * Ajout des SubString de aLine (separateur aSep) dans aCol
	 * 
	 * @param aCol
	 *            -> Collection
	 * @param aLine
	 *            -> Donnees
	 * @param aSep
	 *            -> Separateur
	 * @return aCol
	 */
	public static Collection<String> loadStrCollection(final Collection<String> aCol,
			final String aLine, final String aSep) {

		if (aCol != null && aLine != null) {
			StringTokenizer wSt = new StringTokenizer(aLine, aSep);
			while (wSt.hasMoreTokens()) {
				aCol.add(wSt.nextToken());
			}
		}
		return aCol;
	}

	/**
	 * Dump the content of a properties using the format "%s=[%s]" and a comma
	 * as separator
	 * 
	 * @param aProperties
	 *            the properties to dump
	 * @return a string containing all the dump of the properties
	 */
	public static String PropertiesToString(final Properties aProperties) {

		return PropertiesToString(aProperties, ",");
	}

	/**
	 * Dump the content of a properties using the format "%s=[%s]" and an
	 * explicit separator
	 * 
	 * @param aProperties
	 *            the properties to dump
	 * @param aSep
	 *            the separator inserted between the properties
	 * @return a string containing all the dump of the properties
	 */
	public static String PropertiesToString(final Properties aProperties, final String aSep) {

		StringBuilder wResult = new StringBuilder();

		if (aProperties != null && aProperties.size() > 0) {
			CXSortListProperties wSortedProps = new CXSortListProperties(aProperties,
					CXSortList.ASCENDING);
			Iterator<Entry<Object, Object>> wIt = wSortedProps.iterator();
			while (wIt.hasNext()) {
				Map.Entry<Object, Object> wEnt = wIt.next();
				if (wResult.length() > 0) {
					wResult.append(aSep);
				}
				wResult.append(String.format("%s=[%s]", wEnt.getKey().toString(), wEnt.getValue()
						.toString()));
			}
		}

		return wResult.toString();
	}

	/**
	 * @return
	 */
	public static String SystemPropertiesToString() {

		return PropertiesToString(System.getProperties());
	}
}
