package org.psem2m.utilities;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author isandlaTech - ogattaz
 * 
 */
public class CXResources implements IXDescriber {

    private final static String MESS_ERR_CCL = "Can't get caller of package [%s]. \nException:\n%s";
    private final static String NBSP = "&nbsp;";
    private final static String NOT_LOAD_RES = "can't get key [%s] in the not loaded resource [%s]. Cause:[%s].";

    public static final String RES_COMMAND_PREFIX = "command.";
    public static final String RES_LABEL_PREFIX = "label.";
    public static final String RES_MENU_PREFIX = "menu.";
    public static final String RES_MESSAGE_PREFIX = "message.";

    private static final String RES_SUFFIX = "Resources";

    static final String RES_TITLE_PREFIX = "title.";

    private final static String SPACE = " ";

    private final static String UNKNOWN_REASON = "unknown";

    static final String USER_LANGAGE = "user.language";

    static final String USER_REGION = "user.region";

    private final static char WHY_INFO_SEPARATOR = ',';

    private final static char WHY_SEPARATOR = '\n';

    public final static boolean WITH_CODLANG_PREFIX = true;

    /**
     * @param aResourceId
     * @return
     */
    protected static String extractPackageId(String aResourceId) {

        int wPos = aResourceId.lastIndexOf('.');
        return (wPos != -1) ? aResourceId.substring(0, wPos) : aResourceId;
    }

    /**
     * Extrait l'id d'une ressource du nom d'une classe en supprimant le suffixe
     * "Resources"
     * 
     * Example "CMySubjectResources
     * 
     * @param aCurrentClass
     *            le nom de la classe origine
     * @return l'id de la resource
     */
    protected static String extractResIdFromClassName(Class<?> aCurrentClass) {

        String wResId = aCurrentClass.getName();
        if (wResId.endsWith(RES_SUFFIX)) {
            int wPos = wResId.indexOf(RES_SUFFIX);
            if (wPos > -1)
                wResId = wResId.substring(0, wPos);
        }
        return wResId;
    }

    /**
     * @param format
     * @param args
     * @return
     */
    protected static String formatResourceString(String format, Object... args) {

        try {
            return String.format(format, args);
        } catch (Throwable e) {
            StringBuilder wSB = new StringBuilder();
            wSB.append("Error formating resources string [").append(format)
                    .append(']');
            if (args == null) {
                wSB.append(" without arguments (null).");

            } else if (args.length == 0) {
                wSB.append(" without the arguments (size 0).");

            } else {
                wSB.append(" with the arguments [");
                for (int i = 0; i < args.length; i++) {
                    if (i != 0)
                        wSB.append(',');
                    wSB.append('(').append(i).append(')');
                    if ((args[i] == null)) {
                        wSB.append("null");
                    } else {
                        wSB.append(args[i].getClass().getSimpleName());
                        wSB.append('=');
                        wSB.append(args[i].toString());
                    }
                }
                wSB.append("].");
            }
            return wSB.toString();
        }
    }

    /**
     * @param aResourceId
     *            the full qualified id of the resource (eg:
     *            com.isandlatech.mytexts_fr).
     * @return the classLoader which manages the package of the resource.
     */
    protected static ClassLoader getCallerClassLoader(String aResourceId) {

        try {
            String wPackageId = extractPackageId(aResourceId);

            Class<?> wCallerClass = CXJavaCallerContext.getCaller(wPackageId);

            return wCallerClass.getClassLoader();
        } catch (Throwable e) {
            String wMess = formatResourceString(MESS_ERR_CCL, aResourceId,
                    CXException.eInString(e));
            System.out.println(wMess);
            return null;
        }
    }

    /**
     * Gestion automatique de la recherche du classLoader
     * 
     * @param aResourceId
     *            the full qualified id of the resource (eg:
     *            com.isandlatech.mytexts_fr).
     * @return the classLoader which manages the package of the resource.
     */
    protected static ClassLoader getClassLoader(String aResourceId) {

        if (isOsgiEnv())
            return getCallerClassLoader(aResourceId);
        else
            return getTreadClassLoader();

    }

    /**
     * The context ClassLoader is provided by the creator of the thread for use
     * by code running in this thread when loading classes and resources.
     * 
     * @return Returns the context ClassLoader of the current Thread.
     */
    protected static ClassLoader getTreadClassLoader() {

        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * @return true if the default lancguage is the french one.
     */
    protected static boolean isFrenchDefaultlanguage() {

        return Locale.getDefault().getLanguage()
                .equals(Locale.FRENCH.getLanguage());
    }

    /**
     * 
     * @return true if one key of the system properties starts whith the prefix
     *         "osgi."
     */
    protected static boolean isOsgiEnv() {

        String wOsgiPrefix = "osgi.";
        Set<Object> wKeys = System.getProperties().keySet();
        for (Object wKey : wKeys) {
            if (wKey instanceof String
                    && ((String) wKey).toLowerCase().startsWith(wOsgiPrefix))
                return true;
        }
        return false;
    }

    // Langue demandee
    private Locale pAskedLocale = null;
    // Langue en cours
    private Locale pCurrentLocale = null;
    private final Locale pDefaultLocale = (isFrenchDefaultlanguage()) ? Locale.FRENCH
            : Locale.ENGLISH;
    private String pDefaultNoValue = CXStringUtils.EMPTY;
    protected String pId;
    protected CXResources pLinkedResources = null;
    protected ArrayList<Exception> pLoadExceptions = null;
    protected ResourceBundle pResourceBundle = null;

    protected String pWhy = null;

    /**
	 * 
	 */
    protected CXResources() {

        super();
    }

    /**
     * 
     * 
     * @param aId
     *            identifiant du "ResourceBundle"
     */
    public CXResources(String aId) {

        this(aId, Locale.getDefault());
    }

    /**
     * Gestion automatique de la recherche du classLoader
     * 
     * @param aId
     *            the full qualified id of the resource (eg:
     *            com.isandlatech.mytexts_fr).
     * @param aLocale
     *            "locale" de fonctionnement: fr_FR, ...
     */
    public CXResources(String aId, Locale aLocale) {

        this(aId, aLocale, getClassLoader(aId));
    }

    /**
     * @param aId
     *            the full qualified id of the resource (eg:
     *            com.isandlatech.mytexts_fr).
     * @param aLocale
     *            "locale" de fonctionnement: fr_FR, ...
     * @param aClassLoader
     */
    public CXResources(String aId, Locale aLocale, ClassLoader aClassLoader) {

        this();
        pId = aId;
        setAskedLocale(aLocale);
        boolean wFound = loadResources(aId, getAskedLocale(), aClassLoader);
        // si on ne trouve pas la ressource de la locale, on charge la ressource
        // de la locale defaut "fr_" ou "en_"
        if (!wFound) {
            Locale wAdjustedLocale = pDefaultLocale;
            wFound = loadResources(aId, wAdjustedLocale, aClassLoader);
        }
    }

    /**
     * @param aId
     *            the full qualified id of the resource (eg:
     *            com.isandlatech.mytexts_fr).
     * @param aLocale
     *            "locale" de fonctionnement: fr_FR, ...
     * @param aResourceBundle
     *            le resource bundle deja lu (cf. pour J# )
     */
    protected CXResources(String aId, Locale aLocale,
            ResourceBundle aResourceBundle) {

        this();
        pId = aId;
        setAskedLocale(aLocale);
        pResourceBundle = aResourceBundle;
        // on recupere la locale de la resource chargee pour mettre a jour la
        // locale courante (des fois que la langue ne soit pas celle du
        // parametre "aLocale"
        setCurrentLocale(pResourceBundle.getLocale());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
     * )
     */
    @Override
    public Appendable addDescriptionInBuffer(Appendable aBuffer) {

        CXStringUtils.appendKeyValInBuff(aBuffer, "Id", pId);
        CXStringUtils.appendKeyValInBuff(aBuffer, "Locale",
                pCurrentLocale.toString());
        CXStringUtils.appendKeyValInBuff(aBuffer, "Loaded", isLoaded());
        if (!isLoaded()) {
            CXStringUtils.appendKeyValInBuff(aBuffer, "Why", pWhy);
        } else {
            Enumeration<String> wKeys = pResourceBundle.getKeys();
            String wKey;
            int wI = 0;
            while (wKeys.hasMoreElements()) {
                wKey = wKeys.nextElement();
                CXStringUtils.appendFormatStrInBuff(aBuffer, " (%d)", wI);
                CXStringUtils.appendKeyValInBuff(aBuffer, wKey,
                        getResourceString(wKey));
                wI++;
            }
        }
        return aBuffer;
    }

    public int calcDescriptionLength() {

        return 128;
    }

    /**
     * 
     * @param aKey
     * @param aValues
     * @return
     */
    public String formatMessage(String aKey, Object... aValues) {

        return formatResourceString(
                getResourceString(RES_MESSAGE_PREFIX.concat(aKey)), aValues);
    }

    /**
     * @return
     */
    public Locale getAskedLocale() {

        return pAskedLocale;
    }

    /**
     * @param aKey
     * @return
     */
    public String getCommand(String aKey) {

        return getResourceString(RES_COMMAND_PREFIX.concat(aKey));
    }

    /**
     * @return
     */
    public Locale getCurrentLocale() {

        return pCurrentLocale;
    }

    /**
     * @return
     */
    public Locale getDefaultLocale() {

        return pDefaultLocale;
    }

    private String getDefaultNoValue(String aKey) {

        if (pDefaultNoValue == null)
            pDefaultNoValue = String.format(NOT_LOAD_RES, aKey, pId,
                    getShortWhy());
        return pDefaultNoValue;
    }

    /**
     * @param aKey
     * @return
     */
    public String getId() {

        return pId;
    }

    /**
     * @return
     */
    public Enumeration<String> getKeys() {

        return pResourceBundle.getKeys();
    }

    /**
     * @param aKey
     * @return
     */
    public String getLabel(String aKey) {

        return getResourceString(RES_LABEL_PREFIX.concat(aKey));
    }

    /**
     * @param aKey
     * @param aValues
     * @return
     */
    public String getLabel(String aKey, Object... aValues) {

        return formatResourceString(getLabel(aKey), aValues);
    }

    /**
     * 
     * @param aKey
     * @return
     */
    public String getLabelNoWrap(String aKey) {

        return CXStringUtils.strReplaceAll(getLabel(aKey), SPACE, NBSP);
    }

    /**
     * @param aKey
     * @param aValues
     * @return
     */
    public String getLabelNoWrap(String aKey, Object... aValues) {

        return CXStringUtils
                .strReplaceAll(getLabel(aKey, aValues), SPACE, NBSP);
    }

    /**
     * @return
     */
    public String getLoadException() {

        StringBuilder wSB = new StringBuilder();
        if (pLoadExceptions != null) {
            for (int i = 0; i < pLoadExceptions.size(); i++)
                wSB.append(pLoadExceptions.get(i).getMessage()).append('\n');
        }
        return wSB.toString();
    }

    /**
     * @param aKey
     * @return
     */
    public String getMenuLabel(String aKey) {

        return getResourceString(RES_MENU_PREFIX.concat(aKey));
    }

    public String getMenuLabel(String aKey, Object... aValues) {

        return formatResourceString(getMenuLabel(aKey), aValues);
    }

    /**
     * @param aKey
     * @return
     */
    public String getMessage(String aKey) {

        return getResourceString(RES_MESSAGE_PREFIX.concat(aKey));
    }

    /**
     * @param aKey
     * @param aValues
     * @return
     */
    public String getMessage(String aKey, Object... aValues) {

        return formatResourceString(getMessage((aKey)), aValues);
    }

    /**
     * 14w_006 - Bug 31739 - gestion tableau protection "hasResources()"
     * 
     * @return
     */
    public int getNbKeys() {

        int wNbKeys = 0;
        if (hasResources()) {
            Enumeration<String> wKeys = pResourceBundle.getKeys();
            while (wKeys.hasMoreElements()) {
                wKeys.nextElement();
                wNbKeys++;
            }
        }
        return wNbKeys;
    }

    /**
     * @return
     */
    private ResourceBundle getResources() {

        return pResourceBundle;
    }

    /**
     * @param aKey
     * @return
     */
    protected String getResourceString(String aKey) {

        return getResourceString(aKey, getDefaultNoValue(aKey));
    }

    /**
     * @param aKey
     * @param aNoValue
     *            est la valeur si key non trouvee
     * @return
     */
    protected String getResourceString(String aKey, String aNoValue) {

        if (pResourceBundle == null)
            return aNoValue;
        try {
            return pResourceBundle.getString(aKey);
        } catch (Exception e) {
            if (pLinkedResources != null)
                return pLinkedResources.getResourceString(aKey, aNoValue);
        }
        return aNoValue;
    }

    /**
     * @return
     */
    private String getShortWhy() {

        if (!hasWhy()) {
            return UNKNOWN_REASON;
        }
        int wPos = pWhy.indexOf(WHY_INFO_SEPARATOR);
        if (wPos > -1) {
            return pWhy.substring(0, wPos);
        }
        return pWhy;
    }

    /**
     * @param aKey
     * @return
     */
    public String getStringWithFullKey(String aKey) {

        return getResourceString(aKey);
    }

    /**
     * @param aKey
     * @return
     */
    public String getTitle(String aKey) {

        return getResourceString(RES_TITLE_PREFIX.concat(aKey));
    }

    /**
     * @param aKey
     * @param aValues
     * @return
     */
    public String getTitle(String aKey, Object... aValues) {

        return formatResourceString(getTitle(aKey), aValues);
    }

    /**
     * 
     * @param aKey
     * @return
     */
    public String getTitleNoWrap(String aKey) {

        return CXStringUtils.strReplaceAll(getTitle(aKey), SPACE, NBSP);
    }

    /**
     * @param aKey
     * @param aValues
     * @return
     */
    public String getTitleNoWrap(String aKey, Object... aValues) {

        return CXStringUtils
                .strReplaceAll(getTitle(aKey, aValues), SPACE, NBSP);
    }

    /**
     * @return
     */
    protected String getWhy() {

        return pWhy;
    }

    /**
     * @return
     */
    public boolean hasLoadException() {

        return pLoadExceptions != null;
    }

    /**
     * @return
     */
    public boolean hasResources() {

        return (getResources() != null);
    }

    /**
     * @return
     */
    protected boolean hasWhy() {

        return (pWhy != null);
    }

    /**
     * @param aLocale
     * @return
     */
    public boolean isAskedLocaleEquals(Locale aLocale) {

        return (getAskedLocale().equals(aLocale));
    }

    /**
     * @return
     */
    public boolean isAskedLocaleIsCurrent() {

        return isAskedLocaleEquals(getCurrentLocale());
    }

    /**
     * @param aLocale
     * @return
     */
    public boolean isCurrentLocaleEquals(Locale aLocale) {

        return (getCurrentLocale().equals(aLocale));
    }

    /**
     * @return
     */
    public boolean isLoaded() {

        return hasResources();
    }

    /**
     * Comme le load est appele dans le constructeur cette methode doit
     * seulement renvoyer un ResourceBundle
     * 
     * @param aId
     * @param aLocale
     * @param aClassLoader
     * @return
     */
    protected ResourceBundle loadResourceGetBundle(String aId, Locale aLocale,
            ClassLoader aClassLoader) {

        return ResourceBundle.getBundle(aId, aLocale, aClassLoader);
    }

    /**
     * @param aId
     * @param aLocale
     * @param aClassLoader
     * @return
     */
    private boolean loadResources(String aId, Locale aLocale,
            ClassLoader aClassLoader) {

        try {
            // Pour surcharge du bundle (xml)
            pResourceBundle = loadResourceGetBundle(aId, aLocale, aClassLoader);
            /*
             * Si la resource e ete chargee, alors on recupere la locale de la
             * resource chargee pour mettre e jour la locale courante (des fois
             */
            setCurrentLocale(pResourceBundle.getLocale());
        } catch (Exception e) {
            // Pour traitement des exceptions
            StringBuilder wSB = new StringBuilder();
            if (hasWhy()) {
                wSB.append(getWhy()).append(WHY_SEPARATOR);
            }
            wSB.append(e.getLocalizedMessage());
            if (e instanceof MissingResourceException) {
                wSB.append(WHY_INFO_SEPARATOR).append(" Locale:")
                        .append(aLocale.toString());
                wSB.append(WHY_INFO_SEPARATOR).append(" ClassName:")
                        .append(((MissingResourceException) e).getClassName());
            }
            setWhy(wSB.toString());
            pResourceBundle = null;
            if (pLoadExceptions == null)
                pLoadExceptions = new ArrayList<Exception>();
            pLoadExceptions.add(e);
        }

        return (pResourceBundle != null);
    }

    /**
     * @param aLocale
     */
    protected void setAskedLocale(Locale aLocale) {

        pAskedLocale = aLocale;

        setCurrentLocale(aLocale);
    }

    /**
     * @param aLocale
     */
    protected void setCurrentLocale(Locale aLocale) {

        pCurrentLocale = aLocale;
    }

    /**
     * retourne la liste des Locales supportees
     */
    public void setLinkedResources(CXResources aResources) {

        pLinkedResources = aResources;
    }

    /**
     * @param aWhy
     */
    protected void setWhy(String aWhy) {

        pWhy = aWhy;
    }

    @Override
    public String toDescription() {

        return addDescriptionInBuffer(
                new StringBuilder(calcDescriptionLength())).toString();
    }
}
