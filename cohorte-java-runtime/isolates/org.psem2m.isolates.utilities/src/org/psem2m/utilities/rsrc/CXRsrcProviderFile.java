package org.psem2m.utilities.rsrc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.psem2m.utilities.files.CXFileDir;

/**
 * Class fournisseur de ressource fichier
 * 
 * @author ogattaz
 * 
 */
public class CXRsrcProviderFile extends CXRsrcProvider {

	public static final String NETWORK_DRIVE = "//";
	public static final String URL_FILE = "file";

	// Utiliser pour les newtwork drives (\\fdalbo\temp)
	private String pAddress = null;

	/**
	 * @param aDefaultPath
	 * @param aDefCharset
	 * @throws Exception
	 */
	public CXRsrcProviderFile(CXFileDir aDefaultPath, Charset aDefCharset) throws Exception {
		super(aDefCharset);
		setDefaultDirectoryCheck(aDefaultPath == null ? null : aDefaultPath.getAbsolutePath());
	}

	/**
	 * @param aProv
	 */
	protected CXRsrcProviderFile(CXRsrcProviderFile aProv) {
		super(aProv);
		if (aProv != null) {
			pAddress = aProv.pAddress;
		}
	}

	/**
	 * @param aDefaultPath
	 * @param aDefCharset
	 * @throws Exception
	 */
	public CXRsrcProviderFile(String aDefaultPath, Charset aDefCharset) throws Exception {
		super(aDefCharset);
		setDefaultDirectoryCheck(aDefaultPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrcProvider#addDescriptionInBuffer(java.
	 * lang.Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		return super.addDescriptionInBuffer(aSB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcProvider#clone()
	 */
	@Override
	public CXRsrcProviderFile clone() {
		return new CXRsrcProviderFile(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrcProvider#existsFulPath(org.psem2m.utilities
	 * .rsrc.CXRsrcUriPath)
	 */
	@Override
	protected boolean existsFulPath(CXRsrcUriPath aPath) {
		try {
			URLConnection wCnx = openConnection(urlNew(aPath));
			wCnx.connect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * True si acces fichier en local du serveur - False si access remote (http)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcProvider#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrcProvider#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL aUrl) throws IOException {
		return super.openConnection(aUrl);
	}

	/**
	 * Surchargee pour check
	 * 
	 * @param aDefaultPath
	 * @throws Exception
	 */
	public void setDefaultDirectoryCheck(String aDefaultPath) throws Exception {
		String wDefDir = null;
		String wInput = aDefaultPath == null ? null : aDefaultPath.trim();
		if (wInput != null && wInput.length() != 0) {
			File wFile = new File(wInput);
			if (!wFile.isDirectory()) {
				throwExcepBadDir(aDefaultPath);
			}
			if (!wFile.exists()) {
				throwExcepDirNotFound(aDefaultPath);
			}
			// System.out.println(wFile.toURI().toURL().toString());
			wDefDir = wFile.toURI().toURL().getPath();
		} else {
			throwExcepDirEmpty();
		}
		// Dans le cas d'un network drive l'url doit etre file://fdalbo/d$/temp
		// -> La classe CXtdRsrcUrlDir qui represente un path vers un repertoire
		// supprime le double / (/fdalbo/d$/temp)
		// -> On met a jour pAddress avec '/' pour le rajouter lorsqu'on
		// construit le path complet de l'url avec la method abstraite
		// urlGetAddress
		if (wDefDir.startsWith(NETWORK_DRIVE)) {
			pAddress = CXRsrcUriDir.SEPARATOR_STR;
		}
		super.setDefaultDirectory(wDefDir);
	}

	/**
	 * @param aPath
	 * @throws Exception
	 */
	protected void throwExcepBadDir(String aPath) throws Exception {
		throw new Exception("Bad directory path [" + aPath + "]");
	}

	/**
	 * @throws Exception
	 */
	protected void throwExcepDirEmpty() throws Exception {
		throw new Exception("Empty directory path");
	}

	/**
	 * Pur surcharge des exceptions
	 * 
	 * @param aPath
	 * @throws Exception
	 */
	protected void throwExcepDirNotFound(String aPath) throws Exception {
		throw new Exception("Directory not found [" + aPath + "]");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.rsrc.CXRsrcProvider#urlGetAddress()
	 */
	@Override
	public String urlGetAddress() {
		return pAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.rsrc.CXRsrcProvider#urlNew(org.psem2m.utilities.
	 * rsrc.CXRsrcUriPath)
	 */
	@Override
	protected URL urlNew(CXRsrcUriPath aPath) throws MalformedURLException {
		return new URL(URL_FILE, "", aPath.getUrlStr(urlGetAddress()));
	}
}
