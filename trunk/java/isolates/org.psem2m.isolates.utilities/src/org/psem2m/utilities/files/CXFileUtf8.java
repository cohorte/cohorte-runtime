package org.psem2m.utilities.files;

/**
 * Lecture/Ecriture de fichiers XML
 * Encodage UTF-8 par d�faut
 */
public class CXFileUtf8 extends CXFileText
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3617289017402931254L;
	/*
	//TESTS - DEBUT 
	private static int pNew=0;
	private static int pFin=0;
	private void cptNew(){
		pNew++;
		System.out.println("New CXFileUtf8 - New["+pNew+"] - Fin["+pFin+"] - Diff["+(pNew-pFin)+"]");
	}
	private void cptFin(){
		pFin++;
		System.out.println("Fin CXFileUtf8 - New["+pNew+"] - Fin["+pFin+"] - Diff["+(pNew-pFin)+"]");
	}
	// TESTS - FIN
	 */ 
	/**
	 * @param aFile
	 */
	public CXFileUtf8(CXFile aFile)
	{
		super(aFile);
		myInit();
	}
	
	@Override
	public void finalize(){
		//cptFin();
		super.finalize();
	}
	/**
	 * @param aFullPath
	 */
	public CXFileUtf8(String aFullPath)
	{
		super(aFullPath);
		myInit();
	}
	

	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileUtf8(String aParentDir, String aFileName)
	{
		super(aParentDir, aFileName);
		myInit();
	}
	

	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileUtf8(CXFileDir aParentDir, String aFileName)
	{
		super(aParentDir, aFileName);
		myInit();
	}
	

	/**
	 * 
	 */
	protected void myInit()
	{
		//cptNew();
		/*
		 * On r��crit le BOM utf-8 standard m�me si le fichiher existe
		 */
		setKeepExistingBOM(false);
		/*
		 * On force l'encdage par d�faut � UTF-8
		 */
		setDefaultEncoding(ENCODING_UTF_8);
	}
}
