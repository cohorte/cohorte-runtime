package org.psem2m.isolates.osgi;

import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public abstract class CPojoBase extends CXObjectBase implements IPojoBase {

	public static String LIB_POJO_ID = "PojoId";

	/**
	 * Explicit default constructor
	 */
	public CPojoBase() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(final Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_POJO_ID, getPojoId());
		return aBuffer;
	}

	/**
	 * @return the id of the bundle
	 */
	public abstract String getPojoId();

	/**
	 * @throws Exception
	 */
	public abstract void invalidatePojo();

	/**
	 * @throws Exception
	 */
	public abstract void validatePojo();

}
