package org.psem2m.utilities;
//
import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
//
import org.psem2m.utilities.files.CXFileText;
import org.w3c.dom.Element;
/**
 * Utilitaires pour la gestion des listes
 * 
 * -> Properties et Collections
 */
public class CXListUtils
{
	// Options pour readProperties
	public static final int PROP_UNCHANGE = 0;
	public static final int PROP_UPPERCASE = 1;
	public static final int PROP_LOWERCASE = 2;

	
	public static String Syst_Properties()
	{
		String wResult = new String();
		Set<Entry<Object,Object>> wPro = System.getProperties().entrySet();
		Iterator<Entry<Object,Object>> wIt = wPro.iterator();
		while (wIt.hasNext())
		{
			Map.Entry<Object,Object> wEnt =  wIt.next();
			wResult += wEnt.getKey() + "=" + wEnt.getValue() + "\n";
		}
		return wResult;
	}
	
	public static Properties getPropertiesFromFile(String aPath) throws Exception
	{
		return getPropertiesFromFile(new CXFileText(aPath));
	}
	
	public static Properties getPropertiesFromFile(CXFileText aFile) throws Exception
	{
		return getPropertiesFromFile(aFile, PROP_UNCHANGE, PROP_UNCHANGE);
	}
	
	public static Properties getPropertiesFromFileLowCase(CXFileText aFile) throws Exception
	{
		return getPropertiesFromFile(aFile, PROP_LOWERCASE, PROP_LOWERCASE);
	}
	
	public static Properties getPropertiesFromFileUpperCase(CXFileText aFile) throws Exception
	{
		return getPropertiesFromFile(aFile, PROP_UPPERCASE, PROP_UPPERCASE);
	}
	
	// aKeyUppercase=true --> Cles en majuscules - sinon minuscule
	public static Properties getPropertiesFromFile(CXFileText aFile, int aOptionKey, int aOptionValue) throws Exception
	{
		Properties wResult = new Properties();
		aFile.openReadLine();
		String wLine = aFile.readLine();
		while (wLine != null)
		{
			wLine = CXStringUtils.strFullTrim(wLine);
			if (wLine.length() != 0 && !wLine.startsWith("#"))
			{
				// Key 
				String wKey = CXStringUtils.strLeft(wLine, "=");
				if (aOptionKey == PROP_UNCHANGE)
        {
          
        }
				else if (aOptionKey == PROP_UPPERCASE)
					{
          wKey = wKey.toUpperCase();
          }
				else if (aOptionKey == PROP_LOWERCASE)
					{
          wKey = wKey.toLowerCase();
          }
				// Value
				String wValue = CXStringUtils.strRight(wLine, "=");
				if (aOptionValue == PROP_UNCHANGE){
          
        }
				else if (aOptionValue == PROP_UPPERCASE)
					{
          wValue = wValue.toUpperCase();
          }
				else if (aOptionValue == PROP_LOWERCASE)
					{
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
	

	/**
	 * Ajout de la table sous aRoot (childs) <key>value</key>
	 * @return	aRoot
	 */
	public static Element addPropertiesToDom(Properties aProp, Element aRoot)
	{
		if (aProp != null)
		{
			Iterator<Entry<Object,Object>> wIt = aProp.entrySet().iterator();
			while (wIt.hasNext())
			{
				Entry<Object,Object> wEnt =  wIt.next();
				String wKey = (String) wEnt.getKey();
				if (wKey != null && wKey.length() != 0)
					CXDomUtils.appendTextChildElmt(aRoot, wKey, (String) wEnt.getValue());
			}
		}
		return aRoot;
	}
	/**
	 * CReation d'une propertie qui contient la somme de aPropB dans aPropA
	 * @return	nouvelle properties
	 */
	public static Properties addProperties(Properties aPropA, Properties aPropB)
	{
		Properties wResult = null;
		if (aPropB != null && aPropA != null)
		{
			wResult = new Properties(aPropA);
			wResult.putAll(aPropB);
		}
		else
			wResult = new Properties();
		return wResult;
	}
	/**
	 * Renvoie les entree de B qui ont ete modifiees par rapport e A (ou qui ne sont pas presentent dans A)
	 * @return	nouvelle properties
	 */
	public static Properties diffProperties(Properties aPropA, Properties aPropB)
	{
		String wValA;
		Properties wResult = new Properties();
		if (aPropB != null)
		{
			Iterator<Entry<Object,Object>> wIt = aPropB.entrySet().iterator();
			while (wIt.hasNext())
			{
				Entry<Object,Object> wEntB = wIt.next();
				String wKey = (String) wEntB.getKey();
				String wValB = (String) wEntB.getValue();
				if (aPropA != null)
					wValA = aPropA.getProperty(wKey);
				else
					wValA = null;
				if (wValA == null || !wValA.equals(wValB))
					wResult.put(wKey, wValB);
			}
		}
		return wResult;
	}
	/**
	 * Ajout des SubString de aLine (separateur aSep) dans aCol
	 * @param aCol 	-> Collection
	 * @param aLine	-> Donnees
	 * @param aSep		-> Separateur
	 * @return	aCol
	 */
	public static Collection<String> loadStrCollection(Collection<String> aCol, String aLine, String aSep)
	{
		if (aCol != null && aLine != null)
		{
			StringTokenizer wSt = new StringTokenizer(aLine, aSep);
			while (wSt.hasMoreTokens())
				aCol.add(wSt.nextToken());
		}
		return aCol;
	}
	
	public static String collectionToString(Collection<?> aCol, String aSepLines)
	{
		StringBuilder wRes = new StringBuilder(2048);
		Iterator<?> wIt = aCol.iterator();
		while (wIt.hasNext())
		{
			if (wRes.length() != 0)
				wRes.append(aSepLines);
			Object wObj = wIt.next();
			if (wObj == null)
				wRes.append("null");
			else
				wRes.append(wObj.toString());
		}
		return wRes.toString();
	}
}
