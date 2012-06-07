package org.psem2m.utilities.files;

import java.io.File;

public interface IXFileProcessor{
	
	/**
	 * @param aFile
	 * @return vrai si 
	 * @throws Exception
	 */
	public boolean processFile(File aFile)throws Exception;
}
