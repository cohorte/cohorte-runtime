/**
 * File:   BundleDescription.java
 * Author: "Thomas Calmant"
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.base.conf.beans;

import org.psem2m.isolates.services.conf.IBundleDescr;

/**
 * Description of a bundle
 * 
 * @author Thomas Calmant
 */
public class BundleDescription implements IBundleDescr {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** Bundle file name, if specified */
    private String pFile;

    /** True if the bundle is optional */
    private boolean pOptional;

    /** Bundle symbolic name, mandatory */
    private String pSymbolicName;

    /** Bundle version, if specified */
    private String pVersion;

    /**
     * Default constructor
     */
    public BundleDescription() {
	// Do nothing
    }

    /**
     * Sets up the bundle description
     * 
     * @param aSymbolicName
     *            The bundle symbolic name (mandatory)
     */
    public BundleDescription(final String aSymbolicName) {
	this(aSymbolicName, null, null, false);
    }

    /**
     * Sets up the bundle description
     * 
     * @param aSymbolicName
     *            The bundle symbolic name (mandatory)
     * @param aVersion
     *            Bundle version
     * @param aFile
     *            Bundle file
     * @param aOptional
     *            Bundle is optional
     */
    public BundleDescription(final String aSymbolicName, final String aVersion,
	    final String aFile, final boolean aOptional) {

	pSymbolicName = aSymbolicName;
	pVersion = aVersion;
	pFile = aFile;
	pOptional = aOptional;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IBundleDescr#getFile()
     */
    @Override
    public String getFile() {
	return pFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IBundleDescr#getOptional()
     */
    @Override
    public boolean getOptional() {
	return pOptional;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IBundleDescr#getSymbolicName()
     */
    @Override
    public String getSymbolicName() {
	return pSymbolicName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IBundleDescr#getVersion()
     */
    @Override
    public String getVersion() {
	return pVersion;
    }

    /**
     * Sets the bundle file (bean method)
     * 
     * @param aFile
     *            Bundle file
     */
    public void setFile(final String aFile) {
	pFile = aFile;
    }

    /**
     * Set the bundle are optional or not (bean method)
     * 
     * @param aOptional
     *            Tur if the bundle is optional
     */
    public void setOptional(final boolean aOptional) {
	pOptional = aOptional;
    }

    /**
     * Sets the bundle symbolic name (bean method)
     * 
     * @param aSymbolicName
     *            The bundle symbolic name
     */
    public void setSymbolicName(final String aSymbolicName) {
	pSymbolicName = aSymbolicName;
    }

    /**
     * Sets the bundle version (bean method)
     * 
     * @param aVersion
     *            the bundle version
     */
    public void setVersion(final String aVersion) {
	pVersion = aVersion;
    }
}
