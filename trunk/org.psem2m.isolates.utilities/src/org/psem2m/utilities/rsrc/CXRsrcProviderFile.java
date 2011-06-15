package org.psem2m.utilities.rsrc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.psem2m.utilities.files.CXFileDir;

// Class fournisseur de ressource fichier

public class CXRsrcProviderFile extends CXRsrcProvider {

	public final static String NETWORK_DRIVE = "//";
	public final static String URL_FILE = "file";

	// Utiliser pour les newtwork drives (\\fdalbo\temp)
	private String pAddress = null;

	// Constructeurs

	public CXRsrcProviderFile(String aDefaultPath, Charset aDefCharset)
			throws Exception {
		super(aDefCharset);
		setDefaultDirectoryCheck(aDefaultPath);
	}

	public CXRsrcProviderFile(CXFileDir aDefaultPath, Charset aDefCharset)
			throws Exception {
		super(aDefCharset);
		setDefaultDirectoryCheck(aDefaultPath == null ? null : aDefaultPath
				.getAbsolutePath());
	}

	protected CXRsrcProviderFile(CXRsrcProviderFile aProv) {
		super(aProv);
		if (aProv != null)
			pAddress = aProv.pAddress;
	}

	@Override
	public CXRsrcProviderFile clone() {
		return new CXRsrcProviderFile(this);
	}

	// Abstract

	// True si acc�s fichier en local du serveur - False si acc�ss remote (http)
	@Override
	public boolean isLocal() {
		return true;
	}

	// Surcharg�e pour check
	public void setDefaultDirectoryCheck(String aDefaultPath) throws Exception {
		String wDefDir = null;
		String wInput = aDefaultPath == null ? null : aDefaultPath.trim();
		if (wInput != null && wInput.length() != 0) {
			File wFile = new File(wInput);
			if (!wFile.isDirectory())
				throwExcepBadDir(aDefaultPath);
			if (!wFile.exists())
				throwExcepDirNotFound(aDefaultPath);
			// System.out.println(wFile.toURI().toURL().toString());
			wDefDir = wFile.toURI().toURL().getPath();
		} else
			throwExcepDirEmpty();
		// Dans le cas d'un network drive l'url doit �tre file://fdalbo/d$/temp
		// -> La classe CXtdRsrcUrlDir qui repr�sente un path vers un r�pertoire
		// supprime le double / (/fdalbo/d$/temp)
		// -> On met � jour pAddress avec '/' pour le rajouter lorsqu'on
		// construit le path complet de l'url avec la method abstraite
		// urlGetAddress
		if (wDefDir.startsWith(NETWORK_DRIVE))
			pAddress = CXRsrcUriDir.SEPARATOR_STR;
		super.setDefaultDirectory(wDefDir);
	}

	@Override
	public String urlGetAddress() {
		return pAddress;
	}

	// Get and Set

	@Override
	protected URL urlNew(CXRsrcUriPath aPath) throws MalformedURLException {
		return new URL(URL_FILE, "", aPath.getUrlStr(urlGetAddress()));
	}

	@Override
	protected URLConnection openConnection(URL aUrl) throws IOException {
		return super.openConnection(aUrl);
	}

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

	// Pur surcharge des exceptions
	protected void throwExcepDirNotFound(String aPath) throws Exception {
		throw new Exception("Directory not found [" + aPath + "]");
	}

	protected void throwExcepDirEmpty() throws Exception {
		throw new Exception("Empty directory path");
	}

	protected void throwExcepBadDir(String aPath) throws Exception {
		throw new Exception("Bad directory path [" + aPath + "]");
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		return super.addDescriptionInBuffer(aSB);
	}
}
