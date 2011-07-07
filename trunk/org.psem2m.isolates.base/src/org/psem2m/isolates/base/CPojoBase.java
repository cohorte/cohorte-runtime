package org.psem2m.isolates.base;

import org.apache.felix.ipojo.Pojo;
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
	@Override
	public String getPojoId() {
		try {
			return ((Pojo) this).getComponentInstance().getInstanceName();
		} catch (Exception e) {
			return "???";
		}
	}

	/**
	 * @throws Exception
	 */
	@Override
	public abstract void invalidatePojo();

	/**
	 * @throws Exception
	 */
	@Override
	public abstract void validatePojo();

}
