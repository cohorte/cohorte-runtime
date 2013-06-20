package org.psem2m.utilities.rsrc;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.psem2m.utilities.scripting.CXJsObjectBase;

/**
 * Class Ressource provider
 * 
 * @author ogattaz
 * 
 */
public abstract class CXRsrcProvider extends CXJsObjectBase implements Cloneable {

	private int pCacheExpires = 0;
	private int pConnectTimeoutMs = 0;
	private CXRsrcUriDir pDefaultDirectory = new CXRsrcUriDir("");
	private Charset pDefCharset = null;
	private int pReadTimeoutMs = 0;

	public CXRsrcProvider(Charset aDefCharset) {
		this(0, 0, aDefCharset);
	}

	/**
	 * @param aProv
	 */
	protected CXRsrcProvider(CXRsrcProvider aProv) {
		super();
		if (aProv != null) {
			pReadTimeoutMs = aProv.pReadTimeoutMs;
			pConnectTimeoutMs = aProv.pConnectTimeoutMs;
			pDefaultDirectory = aProv.pDefaultDirectory.clone();
			pCacheExpires = aProv.pCacheExpires;
			pDefCharset = aProv.pDefCharset;
		}
	}

	/**
	 * @param aReadTimeOutMs
	 * @param aConnectTimeOutMs
	 * @param aDefCharset
	 */
	public CXRsrcProvider(int aReadTimeOutMs, int aConnectTimeOutMs, Charset aDefCharset) {
		this(aReadTimeOutMs, aConnectTimeOutMs, null, aDefCharset);
	}

	/**
	 * @param aReadTimeOutMs
	 * @param aConnectTimeOutMs
	 * @param aDir
	 * @param aDefCharset
	 */
	public CXRsrcProvider(int aReadTimeOutMs, int aConnectTimeOutMs, CXRsrcUriDir aDir,
			Charset aDefCharset) {
		pDefCharset = aDefCharset == null ? Charset.defaultCharset() : aDefCharset;
		pReadTimeoutMs = aReadTimeOutMs;
		pConnectTimeoutMs = aConnectTimeOutMs;
		setDefaultDirectory(aDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsObjectBase#addDescriptionInBuffer(
	 * java.lang.Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = aSB == null ? new StringBuilder(1024) : aSB;
		descrAddLine(aSB, "DefaultDirectory", pDefaultDirectory.getPath());
		descrAddProp(aSB, "ReadTimeout", String.valueOf(pReadTimeoutMs));
		descrAddProp(aSB, "ConnectTimeout", String.valueOf(pConnectTimeoutMs));
		descrAddLine(aSB, "CacheExpires", pCacheExpires);
		return aSB;
	}

	/**
	 * @return
	 */
	public boolean cacheExpires() {
		return pCacheExpires > 0;
	}

	/**
	 * @return
	 */
	public int cacheGetExpiresSec() {
		return pCacheExpires;
	}

	/**
	 * @return
	 */
	public boolean cacheIsNoCache() {
		return pCacheExpires == 0;
	}

	/**
	 * @return
	 */
	public boolean cacheNeverExpires() {
		return pCacheExpires < 0;
	}

	/**
	 * @param aRsrc
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean checkTimeStamp(CXRsrc<?> aRsrc) throws java.io.IOException {
		if (aRsrc == null) {
			return true;
		}
		return aRsrc.getTimeStampSyst() == getTimeStamp(aRsrc);
	}

	/**
	 * @param aPath
	 * @param aFulPath
	 * @return
	 * @throws Exception
	 */
	private CXRsrcUriPath checkUriPath(CXRsrcUriPath aPath, boolean aFulPath) throws Exception {
		if (aPath == null || !aPath.isValid()) {
			throw new Exception((aPath == null ? "Null" : "empty") + " resource path");
		}
		if (!aPath.hasName()) {
			throw new Exception("Bad resource path[" + aPath.getFullPath() + "]");
		}
		if (!aFulPath && !pDefaultDirectory.isEmpty()) {
			aPath = new CXRsrcUriPath(pDefaultDirectory, aPath);
		}
		return aPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract CXRsrcProvider clone();

	/**
	 * @param aCnx
	 * @return
	 */
	protected String connectionToString(URLConnection aCnx) {
		StringBuilder wBuf = new StringBuilder();
		wBuf.append("URL [").append(aCnx.getURL().toString()).append("\n");
		wBuf.append("ModifiedSince [").append(String.valueOf(aCnx.getIfModifiedSince()))
				.append("]\n");
		wBuf.append("Expiration [").append(String.valueOf(aCnx.getExpiration())).append("]\n");
		wBuf.append("LastModified [").append(String.valueOf(aCnx.getLastModified())).append("]\n");
		wBuf.append("UseCache [").append(String.valueOf(aCnx.getUseCaches())).append("]\n");
		wBuf.append("ReadTimeout [").append(String.valueOf(aCnx.getReadTimeout())).append("]\n");
		wBuf.append("ConnectTimeout [").append(String.valueOf(aCnx.getConnectTimeout()))
				.append("]\n");
		wBuf.append("Encoding [").append(aCnx.getContentEncoding()).append("]\n");
		wBuf.append("ContentLength [").append(String.valueOf(aCnx.getContentLength()))
				.append("]\n");
		wBuf.append("HEADER\n");
		try {
			Iterator<Entry<String, List<String>>> wIt = aCnx.getHeaderFields().entrySet()
					.iterator();
			while (wIt.hasNext()) {
				Entry<String, List<String>> wEntry = wIt.next();
				wBuf.append("  ");
				if (wEntry.getKey() != null) {
					wBuf.append(wEntry.getKey());
				}
				Iterator<String> wIt1 = wEntry.getValue().iterator();
				while (wIt1.hasNext()) {
					wBuf.append("[").append(wIt1.next()).append("]");
				}
				wBuf.append("\n");
			}
		} catch (Exception e) {
			wBuf.append("Error [").append(e.getMessage()).append("]\n");
		}
		wBuf.append("PROPERTIES\n");
		try {
			Iterator<Entry<String, List<String>>> wIt = aCnx.getRequestProperties().entrySet()
					.iterator();
			while (wIt.hasNext()) {
				Entry<String, List<String>> wEntry = wIt.next();
				wBuf.append("  ");
				if (wEntry.getKey() != null) {
					wBuf.append(wEntry.getKey());
				}
				Iterator<String> wIt1 = wEntry.getValue().iterator();
				while (wIt1.hasNext()) {
					wBuf.append("[").append(wIt1.next()).append("]");
				}
				wBuf.append("\n");
			}
		} catch (Exception e) {
			wBuf.append("Error [").append(e.getMessage()).append("]\n");
		}
		return wBuf.toString();
	}

	/**
	 * @param aPath
	 *            Chemin par rapport au repertoire par defaut
	 * @return
	 */
	public boolean existsDef(String aPath) {
		return existsFulPath(pDefaultDirectory.isEmpty() ? new CXRsrcUriPath(aPath)
				: new CXRsrcUriPath(pDefaultDirectory, aPath));
	}

	/**
	 * @param aPath
	 *            Full path
	 * @return
	 */
	protected abstract boolean existsFulPath(CXRsrcUriPath aPath);

	/**
	 * @return
	 */
	public int getConnectTimeout() {
		return pConnectTimeoutMs;
	}

	/**
	 * @return
	 */
	public Charset getDefCharset() {
		return pDefCharset;
	}

	/**
	 * @return
	 */
	public CXRsrcUriDir getDefDirectory() {
		return pDefaultDirectory;
	}

	/**
	 * @return
	 */
	public int getReadTimeout() {
		return pReadTimeoutMs;
	}

	/**
	 * Lit le timestamp courant de aRsrc
	 * 
	 * @param aRsrc
	 * @return 0 si aRsrc
	 * @throws java.io.IOException
	 */
	public long getTimeStamp(CXRsrc<?> aRsrc) throws java.io.IOException {
		if (aRsrc == null) {
			return 0;
		}
		URL wUrl = urlNew(aRsrc.getPath());
		URLConnection wCnx = openConnection(wUrl);
		return wCnx.getLastModified();
	}

	/**
	 * @param aPath
	 * @return
	 */
	public String getUrlStrDef(String aPath) {
		return aPath == null ? null : new CXRsrcUriPath(pDefaultDirectory, aPath)
				.getUrlStr(urlGetAddress());
	}

	/**
	 * @return True si acces fichier en local du serveur - False si access
	 *         remote (http)
	 */
	public abstract boolean isLocal();

	/**
	 * @return
	 */
	public boolean isValid() {
		return true;
	}

	/**
	 * @param aUrl
	 * @return
	 * @throws java.io.IOException
	 */
	protected URLConnection openConnection(URL aUrl) throws java.io.IOException {
		URLConnection wCnx = aUrl.openConnection();
		// Indication de lecture seule
		wCnx.setDoInput(true);
		wCnx.setDoOutput(false);
		if (pReadTimeoutMs != 0) {
			wCnx.setReadTimeout(pReadTimeoutMs);
		}
		if (pConnectTimeoutMs != 0) {
			wCnx.setConnectTimeout(pConnectTimeoutMs);
		}
		return wCnx;
	}

	/**
	 * Lecture d'une ressource deja lue -> est un fullpath et non pas un path
	 * par rapport au repertoire par defaut
	 * 
	 * @param aRsrc
	 * @param aCheckTimeStamp
	 * @return
	 * @throws Exception
	 */
	public CXRsrc<?> rsrcRead(CXRsrc<?> aRsrc, boolean aCheckTimeStamp) throws Exception {
		assert aRsrc != null : "Null resource";
		if (aRsrc.isText()) {
			return rsrcReadTxt(aRsrc.getPath(), aCheckTimeStamp ? aRsrc.getTimeStampSyst() : 0,
					false, true);
		} else {
			return rsrcReadByte(aRsrc.getPath(), aCheckTimeStamp ? aRsrc.getTimeStampSyst() : 0,
					false, true);
		}
	}

	/**
	 * Lecture text ou byte en fonction du mime-type
	 * 
	 * @param aRsrcPath
	 * @return
	 * @throws Exception
	 */
	public CXRsrc<?> rsrcRead(CXRsrcUriPath aRsrcPath) throws Exception {
		return rsrcRead(aRsrcPath.getFullPath(), 0);
	}

	/**
	 * @param aPath
	 * @param aTimeStamp
	 * @param aForceSecondes
	 * @return
	 * @throws Exception
	 */
	public CXRsrc<?> rsrcRead(CXRsrcUriPath aPath, long aTimeStamp, boolean aForceSecondes)
			throws Exception {
		return rsrcRead(aPath, aTimeStamp, aForceSecondes, false);
	}

	/**
	 * @param aPath
	 * @param aTimeStamp
	 * @param aForceSecondes
	 * @param aFulPath
	 * @return
	 * @throws Exception
	 */
	private CXRsrc<?> rsrcRead(CXRsrcUriPath aPath, long aTimeStamp, boolean aForceSecondes,
			boolean aFulPath) throws Exception {
		if (aPath != null) {
			CXMimeType wMime = aPath.getMimeType();
			if (wMime != null && wMime.isText()) {
				return rsrcReadTxt(aPath, aTimeStamp, aForceSecondes, aFulPath);
			} else {
				return rsrcReadByte(aPath, aTimeStamp, aForceSecondes, aFulPath);
			}
		} else {
			throw new Exception((aPath == null ? "Null" : "empty") + " resource path");
		}
	}

	/**
	 * Lecture text ou byte en fonction du mime-type
	 * 
	 * @param aRsrcPath
	 * @return
	 * @throws Exception
	 */
	public CXRsrc<?> rsrcRead(String aRsrcPath) throws Exception {
		return rsrcRead(aRsrcPath, 0);
	}

	/**
	 * Lecture text ou byte en fonction du mime-type Path = String et Timestamp
	 * (>0 --> Check - <=0 no check)
	 * 
	 * @param aRsrcPath
	 * @param aTimeStampSyst
	 * @return
	 * @throws Exception
	 */
	public CXRsrc<?> rsrcRead(String aRsrcPath, long aTimeStampSyst) throws Exception {
		return rsrcRead(aRsrcPath, aTimeStampSyst, false);
	}

	/**
	 * @param aRsrcPath
	 * @param aTimeStampSyst
	 * @param aForceSecond
	 * @return
	 * @throws Exception
	 */
	public CXRsrc<?> rsrcRead(String aRsrcPath, long aTimeStampSyst, boolean aForceSecond)
			throws Exception {
		return rsrcRead(new CXRsrcUriPath(aRsrcPath), aTimeStampSyst, aForceSecond);
	}

	/**
	 * @param aRsrcPath
	 * @return
	 * @throws Exception
	 */
	public CXRsrcByte rsrcReadByte(CXRsrcUriPath aRsrcPath) throws Exception {
		return rsrcReadByte(aRsrcPath.getFullPath(), 0);
	}

	/**
	 * @param aPath
	 * @param aTimeStamp
	 * @param aForceSecondes
	 * @return
	 * @throws Exception
	 */
	public CXRsrcByte rsrcReadByte(CXRsrcUriPath aPath, long aTimeStamp, boolean aForceSecondes)
			throws Exception {
		return rsrcReadByte(aPath, aTimeStamp, aForceSecondes, false);
	}

	/**
	 * 
	 * 
	 * @param aPath
	 * @param aTimeStamp
	 *            si >0 --> Check , si <=0 no check)
	 * @param aForceSecondes
	 *            true - Le time stamp lu est converti en secondes
	 *            (millisecondes par defaut)
	 * @param aFulPath
	 * @return
	 * @throws Exception
	 */
	private CXRsrcByte rsrcReadByte(CXRsrcUriPath aPath, long aTimeStamp, boolean aForceSecondes,
			boolean aFulPath) throws Exception {
		CXRsrcByte wRsrc = null;
		URL wUrl = null;
		try {
			aPath = checkUriPath(aPath, aFulPath);
			boolean wCheckTimeStamp = aTimeStamp > 0;
			wUrl = urlNew(aPath);
			URLConnection wCnx = openConnection(wUrl);

			long wCurTimeStamp = wCnx.getLastModified();
			if (aForceSecondes) {
				wCurTimeStamp = (wCurTimeStamp / 1000) * 1000;
				aTimeStamp = (aTimeStamp / 1000) * 1000;
			}
			wCheckTimeStamp = wCheckTimeStamp && wCurTimeStamp > 0;
			if (wCheckTimeStamp && aTimeStamp == wCurTimeStamp) {
				return null;
			} else {
				wRsrc = new CXRsrcByte(aPath, CXRsrcByteReader.readAll(wCnx), wCurTimeStamp);
			}
		} catch (Exception e) {
			throwExcepReadByte(
					aPath == null ? "null" : wUrl == null ? aPath.getFullPath() : wUrl.toString(),
					e);
		}
		return wRsrc;
	}

	/**
	 * @param aRsrcPath
	 * @return
	 * @throws Exception
	 */
	public CXRsrcByte rsrcReadByte(String aRsrcPath) throws Exception {
		return rsrcReadByte(aRsrcPath, 0);
	}

	/**
	 * Path = String et Timestamp (>0 --> Check - <=0 no check)
	 * 
	 * @param aRsrcPath
	 * @param aTimeStampSyst
	 * @return
	 * @throws Exception
	 */
	public CXRsrcByte rsrcReadByte(String aRsrcPath, long aTimeStampSyst) throws Exception {
		return rsrcReadByte(aRsrcPath, aTimeStampSyst, false);
	}

	/**
	 * @param aRsrcPath
	 * @param aTimeStampSyst
	 * @param aForceSecond
	 * @return
	 * @throws Exception
	 */
	public CXRsrcByte rsrcReadByte(String aRsrcPath, long aTimeStampSyst, boolean aForceSecond)
			throws Exception {
		return rsrcReadByte(new CXRsrcUriPath(aRsrcPath), aTimeStampSyst, aForceSecond);
	}

	/**
	 * @param aRsrcPath
	 * @return
	 * @throws Exception
	 */
	public CXRsrcText rsrcReadTxt(CXRsrcUriPath aRsrcPath) throws Exception {
		return rsrcReadTxt(aRsrcPath.getFullPath(), 0);
	}

	/**
	 * @param aPath
	 * @param aTimeStamp
	 * @param aForceSecondes
	 * @return
	 * @throws Exception
	 */
	public CXRsrcText rsrcReadTxt(CXRsrcUriPath aPath, long aTimeStamp, boolean aForceSecondes)
			throws Exception {
		return rsrcReadTxt(aPath, aTimeStamp, aForceSecondes, false);
	}

	/**
	 * @param aPath
	 * @param aTimeStamp
	 * @param aForceSecondes
	 * @param aFulPath
	 * @return
	 * @throws Exception
	 */
	private CXRsrcText rsrcReadTxt(CXRsrcUriPath aPath, long aTimeStamp, boolean aForceSecondes,
			boolean aFulPath) throws Exception {
		CXRsrcText wRsrc = null;
		URL wUrl = null;
		try {
			URLConnection wCnx = null;
			aPath = checkUriPath(aPath, aFulPath);
			boolean wCheckTimeStamp = aTimeStamp > 0;
			wUrl = urlNew(aPath);
			wCnx = openConnection(wUrl);
			long wCurTimeStamp = wCnx.getLastModified();
			if (aForceSecondes) {
				wCurTimeStamp = (wCurTimeStamp / 1000) * 1000;
				aTimeStamp = (aTimeStamp / 1000) * 1000;
			}
			wCheckTimeStamp = wCheckTimeStamp && wCurTimeStamp > 0;
			if (wCheckTimeStamp && aTimeStamp == wCurTimeStamp) {
				return null;
			} else {
				// X3 n'ecrit pas le BOM -> On precise l'encoding - Toujours
				// Utf8
				CXRsrcTextReadInfo wInfo = CXRsrcTextUnicodeReader.readAll(wCnx, pDefCharset);
				wRsrc = new CXRsrcText(aPath, wInfo, wCurTimeStamp);
			}
		} catch (Exception e) {
			throwExcepReadText(
					aPath == null ? "null" : wUrl == null ? aPath.getFullPath() : wUrl.toString(),
					e);
		}
		return wRsrc;
	}

	/**
	 * @param aRsrcPath
	 * @return
	 * @throws Exception
	 */
	public CXRsrcText rsrcReadTxt(String aRsrcPath) throws Exception {
		return rsrcReadTxt(aRsrcPath, 0);
	}

	/**
	 * Path = String et Timestamp (>0 --> Check - <=0 no check)
	 * 
	 * @param aRsrcPath
	 * @param aTimeStampSyst
	 * @return
	 * @throws Exception
	 */
	public CXRsrcText rsrcReadTxt(String aRsrcPath, long aTimeStampSyst) throws Exception {
		return rsrcReadTxt(aRsrcPath, aTimeStampSyst, false);
	}

	/**
	 * @param aRsrcPath
	 * @param aTimeStampSyst
	 * @param aForceSecond
	 * @return
	 * @throws Exception
	 */
	public CXRsrcText rsrcReadTxt(String aRsrcPath, long aTimeStampSyst, boolean aForceSecond)
			throws Exception {
		return rsrcReadTxt(new CXRsrcUriPath(aRsrcPath), aTimeStampSyst, aForceSecond);
	}

	/**
	 * @param aExp
	 */
	public void setCacheExpires(int aExp) {
		pCacheExpires = aExp;
	}

	/**
	 * @param aConnectTimeOutMs
	 */
	public void setConnectTimeout(int aConnectTimeOutMs) {
		pConnectTimeoutMs = aConnectTimeOutMs;
	}

	/**
	 * @param aDir
	 */
	public void setDefaultDirectory(CXRsrcUriDir aDir) {
		pDefaultDirectory = aDir == null ? new CXRsrcUriDir("") : aDir;
	}

	/**
	 * @param aPath
	 */
	public void setDefaultDirectory(String aPath) {
		setDefaultDirectory(new CXRsrcUriDir(aPath));
	}

	/**
	 * @param aReadTimeOutMs
	 */
	public void setReadTimeout(int aReadTimeOutMs) {
		pReadTimeoutMs = aReadTimeOutMs;
	}

	/**
	 * @param aUrl
	 * @param e
	 * @throws Exception
	 */
	protected void throwExcepReadByte(String aUrl, Exception e) throws Exception {
		throw new Exception("Error reading byte resource[" + aUrl + "]", e);
	}

	/**
	 * @param aUrl
	 * @param e
	 * @throws Exception
	 */
	protected void throwExcepReadText(String aUrl, Exception e) throws Exception {
		throw new Exception("Error reading text resource[" + aUrl + "]", e);
	}

	/**
	 * Renvoie l'adresse de l'url (http://host:Port...) Renvoie "" ou "/" si
	 * file provider
	 * 
	 * @return
	 */
	public abstract String urlGetAddress();

	protected URL urlNew(CXRsrcUriPath aPath) throws MalformedURLException {
		return new URL(aPath.getUrlStr(urlGetAddress()));
	}
}
