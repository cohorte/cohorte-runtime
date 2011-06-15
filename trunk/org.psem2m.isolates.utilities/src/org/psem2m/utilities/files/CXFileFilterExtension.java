package org.psem2m.utilities.files;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.psem2m.utilities.CXListUtils;
import org.psem2m.utilities.CXStringUtils;



/**
 *  16j_102 - modification du comportement du filtre "CXFileFilterExtension" pour qu'il traite les extensions multiples
 * @author ogattaz
 * 
 */
class CExtension implements Comparable<CExtension> {
	private String pExt;
	private boolean pMultiple;

	CExtension(String aExtension) {
		pExt = aExtension;
		pMultiple = CXStringUtils.countChar(pExt, CXFile.sepExtensionChar) > 0;
	}

	boolean isMultiple() {
		return pMultiple;
	}

	String getExt() {
		return pExt;
	}
	
	@Override
	public int hashCode(){
		return super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CExtension)
			return (pExt != null && pExt.equals(((CExtension) obj).getExt()));
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CExtension aExtension) {
		if (pExt==null)
			return 0;
		else
			return pExt.compareTo(aExtension.getExt());
	}
	
	@Override
	public String toString() {
		return getExt();
	}
}

/**
 * 16j_102 - modification du comportement du filtre "CXFileFilterExtension" pour qu'il traite les extensions multiples
 * 
 * @author ogattaz
 * 
 */
class CExtensions extends HashSet<CExtension> {

	private boolean pHasMultiple = false;

	private static final long serialVersionUID = 3085394256462223179L;

	/**
	 * @param aExtensions
	 * @param aSep
	 */
	CExtensions(String aExtensions, String aSep) {
		if (aExtensions!=null && aSep!=null)
			load(aExtensions, aSep);
	}

	/**
	 * @param aExtensions
	 * @param aSep
	 */
	void load(String aExtensions, String aSep) {
		StringTokenizer wSt = new StringTokenizer(aExtensions, aSep);
		CExtension wExtension;
		while (wSt.hasMoreTokens()) {
			wExtension = new CExtension(wSt.nextToken());
			add(wExtension);
			if (!pHasMultiple)
				pHasMultiple = wExtension.isMultiple();
		}
	}
	
	boolean hasMultipleExtension(){
		return pHasMultiple;
	}

	boolean contains(String aExt) {
		// je necomprend pas pourquoi cela ne fonctionne pas ...
		//return contains(new CExtension(aExt));
		
		if (aExt==null || aExt.length()==0)
			return false;
		Iterator <CExtension> wExtensions = iterator();
		while(wExtensions.hasNext()){
			if (aExt.equals( wExtensions.next().getExt()))
					return true;
		}
		return false;
	}
	
	boolean match(String aFilename){
		if (aFilename==null || aFilename.length()<2 || aFilename.indexOf(CXFile.sepExtensionChar)==-1)
			return false;
		Iterator <CExtension> wExtensions = iterator();
		while(wExtensions.hasNext()){
			if (aFilename.endsWith( wExtensions.next().getExt()))
					return true;
		}
		return false;
	}
	
}


/**
 * Filtre sur l'extension
 * 
 * @author ogattaz
 * 
 */
public class CXFileFilterExtension extends CXFileFilter implements FileFilter {

	private CExtensions pListExt;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	// 16w_109 - enrichissement de la log
	@Override
	public String toString() {
		StringBuilder wSB = new StringBuilder();
		wSB.append( String.format("FilterExtension(%s)=[%s]", includer(),CXListUtils.collectionToString(pListExt, ";") ));
		if (hasSubFileFilter())
			wSB.append(SEPARATOR).append(getSubFileFilter().toString());
		return wSB.toString();
	}

	/**
	 * aListExt : Liste des extension s�par�es par ";"
	 * 
	 * @param aListExt
	 */
	public CXFileFilterExtension(String aListExt) {
		this(aListExt, null, INCLUDE);
	}

	/**
	 * @param aListExt
	 *          Liste des extension s�par�es par ";"
	 * @param aSubFileFilter
	 * @param aInclude
	 */
	public CXFileFilterExtension(String aListExt, FileFilter aSubFileFilter, boolean aInclude) {
		super(aSubFileFilter, aInclude);
		pListExt = new CExtensions( aListExt, SEPARATOR);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File pathname) {
		boolean wRes = !include();
		if (pathname.isDirectory())
			wRes = true;
		else {
			String wFileName = pathname.getName();
			// test de l'extension situ�e derri�re le dernier "sepExtension"
			if (pListExt.contains(CXStringUtils.strRightBack(wFileName, CXFile.sepExtension)))
				wRes = include();
			// 16j_102 - modification du comportement du filtre "CXFileFilterExtension" pour qu'il traite les extensions
			// multiples
			// si myArchive.tar.gz => l'extension est "tar.gz"
			else if (nameWithMultipleExtension(wFileName) && pListExt.hasMultipleExtension()
					&& pListExt.match(wFileName))
				wRes = include();
		}
		if (wRes && hasSubFileFilter())
			wRes = getSubFileFilter().accept(pathname);
		return wRes;
	}

	/**
	 * 
	 * 16j_102 - modification du comportement du filtre "CXFileFilterExtension" pour qu'il traite les extensions multiples
	 * @param aFileName
	 * @return
	 */
	private boolean nameWithMultipleExtension(String aFileName) {
		return CXStringUtils.countChar(aFileName, CXFile.sepExtensionChar) > 1;
	}
}
