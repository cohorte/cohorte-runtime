package org.psem2m.utilities.files;

import java.io.File;
import java.util.Collection;

import org.psem2m.utilities.CXAbstractListComparator;
import org.psem2m.utilities.CXSortList;



/**
 * @author parents
 *
 */
public class CXSortListFiles extends CXSortList<File>
{

	private static final long serialVersionUID = 3978703982749364787L;

	/**
	 */
	public CXSortListFiles()
	{
		super();
	}

	/**
	 * @param aSortAsc
	 */
	public CXSortListFiles(boolean aSortAsc)
	{
		super(aSortAsc);
	}

	/**
	 * @param aComp
	 */
	public CXSortListFiles(CXSortListFileAbstractComparator<File> aComp)
	{
		super(aComp);
	}

	/**
	 * @param c
	 * @throws Exception
	 */
	public CXSortListFiles(Collection<File> c) throws Exception
	{
		super(c);
	}

	/**
	 * @param c
	 * @param aComp
	 * @throws Exception
	 */
	public CXSortListFiles(Collection<File> c, CXSortListFileAbstractComparator<File> aComp) throws Exception
	{
		super(c, aComp);
	}

	/**
	 * Constructeur par d�faut
	 */
	@Override
	protected CXAbstractListComparator<File> getDefaultComparator()
	{
		return new CAdminFilePathComparator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.utilities.CXSortList#getDefaultComparator(boolean)
	 */
	@Override
	protected CXAbstractListComparator<File> getDefaultComparator(boolean aSortAsc)
	{
		return new CAdminFilePathComparator(aSortAsc);
	}

	/**
	 * 
	 */
	public void sortByDateAsc()
	{
		sort(new CAdminFileDateComparator());
	}

	/**
	 * 
	 */
	public void sortByDateDesc()
	{
		sort(new CAdminFileDateComparator(false));
	}

	/**
	 * 
	 */
	public void sortByPathAsc()
	{
		sort(new CAdminFilePathComparator());
	}

	/**
	 * 
	 */
	public void sortByPathDesc()
	{
		sort(new CAdminFilePathComparator(false));
	}

	/**
	 * 
	 */
	public void sortBySizeAsc()
	{
		sort(new CAdminFileSizeComparator());
	}

	/**
	 * 
	 */
	public void sortBySizeDesc()
	{
		sort(new CAdminFileSizeComparator(false));
	}
}