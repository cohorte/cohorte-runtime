package org.psem2m.utilities;

/**
 * @author isandlaTech - ogattaz
 * 
 */
class CXIdentifier implements IXIdentifier {

	private final static String ANONYMOUS = "anonymous";
	private final long pCpt;
	private final String pFullId;
	private final String pId;

	/**
	 * @param aId
	 */
	CXIdentifier(String aId) {
		super();
		pId = (aId != null) ? aId : ANONYMOUS;
		pCpt = CXObjectCounter.getCpt();
		pFullId = buildFullId();
	}

	/**
	 * @return
	 */
	private String buildFullId() {
		StringBuilder wSB = new StringBuilder(32);
		wSB.append(getId());
		wSB.append('+');
		wSB.append(getCpt());
		return wSB.toString();
	}

	@Override
	public long getCpt() {
		return pCpt;
	}

	@Override
	public String getId() {
		return pId;
	}

	public boolean hasId() {
		return pId != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pFullId;
	}
}

/**
 * @author ogattaz
 * 
 */
public abstract class CXObjectBase implements IXObjectBase {

	private final static IXObjectBase ROOTOBJECT = new CXObjectRoot();

	private final IXIdentifier pIdentifier;

	private final IXObjectBase pParent;

	/**
	 * @param aId
	 */
	public CXObjectBase() {
		this(null, null);
	}

	/**
	 * @param aParent
	 * @param aId
	 */
	public CXObjectBase(IXObjectBase aParent) {
		this(aParent, null);
	}

	/**
	 * @param aParent
	 * @param aId
	 */
	public CXObjectBase(IXObjectBase aParent, String aId) {
		super();
		pParent = (aParent != null) ? aParent : ROOTOBJECT;
		pIdentifier = buildIdentifier(aId);
	}

	/**
	 * @param aId
	 */
	public CXObjectBase(String aId) {
		this(null, aId);
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_ID, getIdentifier());
		return aBuffer;
	}

	/**
	 * @param aId
	 * @param aClass
	 * @return
	 */
	private IXIdentifier buildIdentifier(String aId) {
		if (aId == null || aId.length() == 0)
			aId = getClass().getSimpleName();
		return new CXIdentifier(aId);
	}

	public int calcDescriptionLength() {
		return 128;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXIdentifiable#compareTo(org.psem2m.utilities.
	 * IXIdentifiable)
	 */
	@Override
	public int compareTo(IXIdentifiable<?> defElem) {
		return getIdentifier().compareTo(defElem.getIdentifier().toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXObjectBase#destroy()
	 */
	@Override
	public abstract void destroy();

	/**
	 * @param aChild
	 * @return
	 */
	protected IXObjectBase destroyChild(IXObjectBase aChild) {
		if (aChild != null)
			aChild.destroy();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXIdentifiable#getId()
	 */
	@Override
	public String getIdentifier() {
		return pIdentifier.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXObjectBase#getParent()
	 */
	@Override
	public IXObjectBase getParent() {
		return pParent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXObjectBase#hasParent()
	 */
	@Override
	public boolean hasParent() {
		return getParent() != null && getParent() != ROOTOBJECT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.IXDescriber#toDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();
	}
}

/**
 * Objcet instance counter
 * 
 * @author ogattaz
 * 
 */
class CXObjectCounter {

	private static final CXObjectCounter sCounter = new CXObjectCounter();

	/**
	 * @return
	 */
	static long getCpt() {
		return sCounter.getNextCpt();
	}

	private long pCpt = 0;

	/**
	 * @return
	 */
	synchronized long getNextCpt() {
		pCpt++;
		return pCpt;
	}

}

/**
 * @author isandlaTech - ogattaz
 * 
 */
class CXObjectRoot implements IXObjectBase {

	private final static String ME = "ROOTOBJECT_0";

	/**
	 * 
	 */
	public CXObjectRoot() {
		super();
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_ID, getIdentifier());
		return aBuffer;
	}

	public int calcDescriptionLength() {
		return 128;
	}

	@Override
	public int compareTo(IXIdentifiable<?> aIdentifiable) {
		return (this == aIdentifiable) ? 0 : -1;
	}

	@Override
	public void destroy() {
	}

	@Override
	public String getIdentifier() {
		return ME;
	}

	@Override
	public IXObjectBase getParent() {
		return null;
	}

	@Override
	public boolean hasParent() {
		return false;
	}

	@Override
	public String toDescription() {
		return addDescriptionInBuffer(
				new StringBuilder(calcDescriptionLength())).toString();
	}

}
