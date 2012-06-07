package org.psem2m.utilities;

/**
 * @author Adonix Grenoble
 * @version 140_000
 */
public interface IHistoryHandler
{
	public final static int LEVEL_VERSION = 0;
	public final static int LEVEL_MODIF = 1;
	public final static int LEVEL_DETAIL = 2;
	public void startElement(int aLevel,String aContent);
}
