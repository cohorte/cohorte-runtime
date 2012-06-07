package org.psem2m.utilities;

public interface IXObjectBase extends IXDescriber, IXIdentifiable<String> {

	public static String EMPTY = CXStringUtils.EMPTY;

	/**
	 * 
	 */
	public void destroy();

	/**
	 * @return
	 */
	IXObjectBase getParent();

	/**
	 * @return
	 */
	public boolean hasParent();

}
