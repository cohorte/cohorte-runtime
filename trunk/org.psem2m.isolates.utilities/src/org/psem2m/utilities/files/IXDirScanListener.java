package org.psem2m.utilities.files;




/**
 * 16j_101
 * 
 * @author ogattaz
 *
 */
public interface IXDirScanListener {
	
	public void listenOneFile(int aScanLevel,int aIdx,CXFileBase aFile);
	
	public void listenEndScan();

}
