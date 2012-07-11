package org.psem2m.isolates.ui;

/**
 * @author ogattaz
 *
 */
public abstract class CSnapshotAbstract {
	
	private final String pName;
	
    /**
     * @param aState
     */
    public CSnapshotAbstract(final String aName) {

        super();
        pName = aName;
    }
    
    /**
     * @return
     */
    public String getName(){
    	return pName;
    }

    /**
     * @return
     */
    public abstract CSnapshotAbstract getChild(final int aIdx);

    /**
     * @return
     */
    public abstract int getChildCount();
    
    
    /**
     * @param aChild
     * @return
     */
    public abstract int getIndexOfChild(final CSnapshotAbstract aChild);
    
    /**
     * @return
     */
    public abstract String getTextInfo();

}
