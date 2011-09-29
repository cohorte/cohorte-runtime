package org.psem2m.utilities;

//
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import org.psem2m.utilities.files.CXFileText;
import org.w3c.dom.Element;

/**
 * Utilitaires pour la gestion des listes
 * 
 * -> Properties et Collections
 */
public class CXListUtils {

    public static final int PROP_LOWERCASE = 2;
    // Options pour readProperties
    public static final int PROP_UNCHANGE = 0;
    public static final int PROP_UPPERCASE = 1;

    /**
     * CReation d'une propertie qui contient la somme de aPropB dans aPropA
     * 
     * @return nouvelle properties
     */
    public static Properties addProperties(final Properties aPropA,
            final Properties aPropB) {

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
    public static Element addPropertiesToDom(final Properties aProp,
            final Element aRoot) {

        if (aProp != null) {
            Iterator<Entry<Object, Object>> wIt = aProp.entrySet().iterator();
            while (wIt.hasNext()) {
                Entry<Object, Object> wEnt = wIt.next();
                String wKey = (String) wEnt.getKey();
                if (wKey != null && wKey.length() != 0) {
                    CXDomUtils.appendTextChildElmt(aRoot, wKey,
                            (String) wEnt.getValue());
                }
            }
        }
        return aRoot;
    }

    public static String collectionToString(final Collection<?> aCol,
            final String aSepLines) {

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
    public static Properties diffProperties(final Properties aPropA,
            final Properties aPropB) {

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

    public static Properties getPropertiesFromFile(final CXFileText aFile)
            throws Exception {

        return getPropertiesFromFile(aFile, PROP_UNCHANGE, PROP_UNCHANGE);
    }

    // aKeyUppercase=true --> Cles en majuscules - sinon minuscule
    public static Properties getPropertiesFromFile(final CXFileText aFile,
            final int aOptionKey, final int aOptionValue) throws Exception {

        Properties wResult = new Properties();
        aFile.openReadLine();
        String wLine = aFile.readLine();
        while (wLine != null) {
            wLine = CXStringUtils.strFullTrim(wLine);
            if (wLine.length() != 0 && !wLine.startsWith("#")) {
                // Key
                String wKey = CXStringUtils.strLeft(wLine, "=");
                if (aOptionKey == PROP_UNCHANGE) {

                } else if (aOptionKey == PROP_UPPERCASE) {
                    wKey = wKey.toUpperCase();
                } else if (aOptionKey == PROP_LOWERCASE) {
                    wKey = wKey.toLowerCase();
                }
                // Value
                String wValue = CXStringUtils.strRight(wLine, "=");
                if (aOptionValue == PROP_UNCHANGE) {

                } else if (aOptionValue == PROP_UPPERCASE) {
                    wValue = wValue.toUpperCase();
                } else if (aOptionValue == PROP_LOWERCASE) {
                    wValue = wValue.toLowerCase();
                }
                // Put
                wResult.put(wKey, wValue);
            }
            wLine = aFile.readLine();
        }
        aFile.close();
        return wResult;
    }

    public static Properties getPropertiesFromFile(final String aPath)
            throws Exception {

        return getPropertiesFromFile(new CXFileText(aPath));
    }

    public static Properties getPropertiesFromFileLowCase(final CXFileText aFile)
            throws Exception {

        return getPropertiesFromFile(aFile, PROP_LOWERCASE, PROP_LOWERCASE);
    }

    public static Properties getPropertiesFromFileUpperCase(
            final CXFileText aFile) throws Exception {

        return getPropertiesFromFile(aFile, PROP_UPPERCASE, PROP_UPPERCASE);
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
    public static Collection<String> loadStrCollection(
            final Collection<String> aCol, final String aLine, final String aSep) {

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
    public static String PropertiesToString(final Properties aProperties,
            final String aSep) {

        StringBuilder wResult = new StringBuilder();

        if (aProperties != null && aProperties.size() > 0) {
            CXSortListProperties wSortedProps = new CXSortListProperties(
                    aProperties, CXSortList.ASCENDING);
            Iterator<Entry<Object, Object>> wIt = wSortedProps.iterator();
            while (wIt.hasNext()) {
                Map.Entry<Object, Object> wEnt = wIt.next();
                if (wResult.length() > 0) {
                    wResult.append(aSep);
                }
                wResult.append(String.format("%s=[%s]", wEnt.getKey()
                        .toString(), wEnt.getValue().toString()));
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
