package org.psem2m.utilities.rsrc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXJavaCallerContext;

/**
 * @author ogattaz
 * 
 */
public class CXMimeType {

    /**
     * @author ogattaz
     * 
     */
    public static enum EXType {
        APPLICATION("application"), AUDIO("audio"), FONT("font"), IMAGE("image"), MESSAGE(
                "message"), MODEL("model"), MULTIPART("multipart"), PLUGIN(
                "plugin"), TEXT("text"), VIDEO("video"), XWORLD("x-world");

        /**
         * @param aMimeType
         * @return
         */
        private static EXType getType(String aMimeType) {

            if (aMimeType == null) {
                return null;
            }
            for (EXType xTyp : EXType.values()) {
                if (xTyp.isType(aMimeType)) {
                    return xTyp;
                }
            }
            return null;
        }

        private String pId;

        /**
         * @param aId
         */
        private EXType(String aId) {

            pId = aId;
        }

        /**
         * @return
         */
        public String getId() {

            return pId;
        }

        /**
         * @return
         */
        public boolean isApplication() {

            return this == APPLICATION;
        }

        /**
         * @return
         */
        public boolean isAudio() {

            return this == AUDIO;
        }

        /**
         * @return
         */
        public boolean isFont() {

            return this == FONT;
        }

        /**
         * @return
         */
        public boolean isImage() {

            return this == IMAGE;
        }

        /**
         * @return
         */
        public boolean isMessage() {

            return this == MESSAGE;
        }

        /**
         * @return
         */
        public boolean isModel() {

            return this == MODEL;
        }

        /**
         * @return
         */
        public boolean isMultiPart() {

            return this == MULTIPART;
        }

        /**
         * @return
         */
        public boolean isPlugin() {

            return this == PLUGIN;
        }

        /**
         * @return
         */
        public boolean isText() {

            return this == TEXT;
        }

        /**
         * @param aMimeType
         * @return
         */
        private boolean isType(String aMimeType) {

            return aMimeType != null && aMimeType.indexOf(pId + "/") >= 0;
        }

        /**
         * @return
         */
        public boolean isVideo() {

            return this == VIDEO;
        }

        /**
         * @return
         */
        public boolean isXWorld() {

            return this == XWORLD;
        }
    }

    private final static String ADDON_DEF_EXT = "_def";
    private final static String ADDON_IS_TEXT = "_text";

    private final static String MESS_ERR_CCL = "Can't get caller of package [%s]. \nException:\n%s";

    private static HashMap<String, CXMimeType> sExtToMime = new HashMap<String, CXMimeType>(
            250);
    private static HashMap<String, CXMimeType> sMimeTypes = new HashMap<String, CXMimeType>(
            200);

    private static boolean sTest = false;
    // LOADING
    static {
        try {
            int wPos = CXMimeType.class.getName().indexOf(
                    CXMimeType.class.getSimpleName());
            if (wPos != -1 && wPos != 0) {
                String wPath = CXMimeType.class.getPackage().getName();

                String wResourceId = wPath + ".PXExtToMime";
                if (sTest) {
                    log("ResourceId=[%s]", wResourceId);
                }

                ClassLoader wClassLoader = getCallerClassLoader(CXMimeType.class);
                if (sTest) {
                    log("wClassLoader=[%s]", wClassLoader.toString());
                }

                ResourceBundle wBndl = ResourceBundle.getBundle(wResourceId,
                        Locale.getDefault(), wClassLoader);

                if (sTest) {
                    log("ResourceBundleLoaded=[%b]", (wBndl != null));
                }

                for (Iterator<String> xIt = wBndl.keySet().iterator(); xIt
                        .hasNext();) {
                    String wExt = xIt.next();
                    String wMime = wBndl.getString(wExt);
                    CXMimeType wMimeType = getMimeType(wMime);
                    if (wMimeType == null) {
                        wMimeType = new CXMimeType(wExt, wMime);
                        if (wMimeType.isValid()) {
                            sExtToMime.put(wExt, wMimeType);
                            sMimeTypes
                                    .put(wMimeType.getIdentifier(), wMimeType);
                        } else if (sTest) {
                            log("MimeType [%s] - Ext[%s] is not valid", wMime,
                                    wExt);
                        }
                    } else {
                        if (!wMimeType.addExt(wExt, wMime) && sTest) {
                            log("MimeType [%s] - Ext[%s] is not valid", wMime,
                                    wExt);
                        }
                    }
                }
                if (sTest) {
                    log(getMimeTypeDescr());
                }
            }
        } catch (Throwable e) {
            log(CXException.eInString(e));
        }
    }

    /**
     * return the class loader of a class.
     * 
     * Used to be able to load a resourcebundle stored un the jar of a bundle
     * 
     * @param aResourceId
     * @return
     */
    protected static ClassLoader getCallerClassLoader(Class<?> aClass) {

        String wPackageName = aClass.getPackage().getName();

        try {

            Class<?> wCallerClass = CXJavaCallerContext.getCaller(wPackageName);
            return wCallerClass.getClassLoader();
        } catch (Throwable e) {
            String wMess = String.format(MESS_ERR_CCL, wPackageName,
                    CXException.eInString(e));
            log(wMess);
            return null;
        }
    }

    /**
     * @param aMime
     * @return
     */
    public static CXMimeType getMimeType(String aMime) {

        if (aMime == null) {
            return null;
        }
        return sMimeTypes.get(aMime.toLowerCase());
    }

    /**
     * @return
     */
    public static String getMimeTypeDescr() {

        CXMimeType[] wArray = sMimeTypes.values().toArray(
                new CXMimeType[sMimeTypes.size()]);
        Arrays.sort(wArray, new Comparator<CXMimeType>() {
            @Override
            public int compare(CXMimeType o1, CXMimeType o2) {

                if (o1 == null || o2 == null) {
                    return 1;
                }
                return o1.getIdentifier().compareTo(o2.getIdentifier());
            }
        });
        StringBuilder wSB = new StringBuilder();
        for (CXMimeType xMime : wArray) {
            if (wSB.length() != 0) {
                wSB.append('\n');
            }
            wSB.append(xMime.toStringDescr());
        }
        return wSB.toString();
    }

    /**
     * @param aExt
     * @return
     */
    public static CXMimeType getMimeTypeFromExt(String aExt) {

        if (aExt == null) {
            return null;
        }
        return sExtToMime.get(aExt.toLowerCase());
    }

    /**
     * @param aLine
     */
    private static void log(String aFormat, Object... aArgs) {

        String wLine = aFormat;
        if (aArgs != null && aArgs.length > 0) {
            wLine = String.format(aFormat, aArgs);
        }

        System.out.println(String.format(
                "+++ org.psem2m.utilities.rsrc.CXMimeType %s",
                wLine.replace('\n', '§')));
    }

    private String pDefExt = null;

    private final ArrayList<String> pExtensions = new ArrayList<String>(2);

    private boolean pIsText = false;

    private String pMimeType = null;

    private EXType pType = null;

    /**
     * @param aExt
     * @param aMimeType
     */
    private CXMimeType(String aExt, String aMimeType) {

        if (aMimeType != null && !aMimeType.isEmpty()) {
            this.addExt(aExt, aMimeType);
        }
    }

    /**
     * @param aExt
     * @param aMimeType
     * @return
     */
    private boolean addExt(String aExt, String aMimeType) {

        if (aExt != null && !aExt.isEmpty() && aMimeType != null
                && !aMimeType.isEmpty()) {
            aExt = aExt.toLowerCase().trim();
            aMimeType = aMimeType.toLowerCase().trim();
            int wPos = aMimeType.indexOf('_');
            boolean wForceText = false;
            if (wPos != -1) {
                if (wPos < 5) {
                    return false;
                }
                String wAddOn = aMimeType.substring(wPos);
                aMimeType = aMimeType.substring(0, wPos);
                if (wAddOn.indexOf(ADDON_DEF_EXT) >= 0) {
                    if (pDefExt != null && sTest) {
                        System.out
                                .println("Mime has already default extension ["
                                        + aExt + "] Mime[" + aMimeType + "]");
                    }
                    pDefExt = aExt;
                }
                wForceText = wAddOn.indexOf(ADDON_IS_TEXT) >= 0;
            }
            if (hasExt(aExt)) {
                if (sTest) {
                    System.out.println("Extension already exists [" + aExt
                            + "]");
                }
                return false;
            }
            if (pType == null) {
                pMimeType = aMimeType;
                pType = EXType.getType(pMimeType);
                if (pType != null) {
                    pIsText = pType.isText() || wForceText;
                } else if (sTest) {
                    System.out.println("Unknown type mime[" + pMimeType + "]");
                }
            }
            pExtensions.add(aExt);
            return true;
        } else {
            return false;
        }
    }

    public String getDefaultExt() {

        return pDefExt == null ? pExtensions.get(0) : pDefExt;
    }

    // Full mime type : type/subtype
    public String getIdentifier() {

        return pMimeType;
    }

    // text/application...
    public EXType getType() {

        return pType;
    }

    public boolean hasExt(String aExt) {

        if (aExt == null) {
            return false;
        }
        aExt = aExt.toLowerCase().trim();
        for (String xExt : pExtensions) {
            if (xExt.equals(aExt)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBinary() {

        return !pIsText;
    }

    public boolean isText() {

        return pIsText;
    }

    private boolean isValid() {

        return pMimeType != null && pMimeType.length() != 0 && pType != null;
    }

    @Override
    public String toString() {

        return pMimeType;
    }

    // enum

    public String toStringDescr() {

        StringBuilder wSB = new StringBuilder().append(pMimeType)
                .append(" - isText[").append(pIsText).append("] - defExt[")
                .append(getDefaultExt()).append("]");
        if (pExtensions.size() > 1) {
            StringBuilder wTmp = new StringBuilder();
            String wDefExt = getDefaultExt();
            for (String xExt : pExtensions) {
                if (!xExt.equals(wDefExt)) {
                    if (wTmp.length() != 0) {
                        wTmp.append(',');
                    }
                    wTmp.append(xExt);
                }
            }
            wSB.append(" - others[").append(wTmp).append("]");
        }
        if (sTest
                && !pIsText
                && (pMimeType.indexOf("htm") != -1 || pMimeType.indexOf("xml") != -1)) {
            wSB.append(" - !!! Could be a text file");
        }
        return wSB.toString();
    }
}
