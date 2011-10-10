/**
 * File:   IsolateDescription.java
 * Author: Thomas Calmant
 * Date:   2 sept. 2011
 */
package org.psem2m.isolates.base.conf.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.psem2m.isolates.services.conf.IBundleDescr;
import org.psem2m.isolates.services.conf.IIsolateDescr;

/**
 * Description of an isolate
 * 
 * @author Thomas Calmant
 */
public class IsolateDescription implements IIsolateDescr {

    /** Serializable version */
    private static final long serialVersionUID = 1L;

    /** The isolate access URL */
    private String pAccessUrl;

    /** Isolate bundles (can't be null) */
    private final Set<IBundleDescr> pBundles = new LinkedHashSet<IBundleDescr>();

    /** Isolate ID */
    private String pIsolateId;

    /** Isolate kind, must never be null */
    private String pIsolateKind = "";

    /** Isolate Java VM arguments (can't be null, must be ordered) */
    private final List<String> pVmArguments = new ArrayList<String>();

    /**
     * Default constructor
     */
    public IsolateDescription() {

        // Do nothing
    }

    /**
     * Sets up the isolate description
     * 
     * @param aIsolateId
     */
    public IsolateDescription(final String aIsolateId) {

        pIsolateId = aIsolateId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.conf.IIsolateDescr#getAccessUrl()
     */
    @Override
    public String getAccessUrl() {

        return pAccessUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IIsolateDescr#getBundles()
     */
    @Override
    public Set<IBundleDescr> getBundles() {

        return Collections.unmodifiableSet(pBundles);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IIsolateDescr#getId()
     */
    @Override
    public String getId() {

        return pIsolateId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.conf.IIsolateDescr#getKind()
     */
    @Override
    public String getKind() {

        return pIsolateKind;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.config.json.IIsolateDescr#getVMArgs()
     */
    @Override
    public List<String> getVMArgs() {

        return Collections.unmodifiableList(pVmArguments);
    }

    /**
     * Sets the bundle access URL
     * 
     * @param aUrl
     *            The access URL
     */
    public void setAccessUrl(final String aUrl) {

        pAccessUrl = aUrl;
    }

    /**
     * Sets the isolate bundles
     * 
     * @param aBundles
     *            the isolate bundles
     */
    public void setBundles(final Set<IBundleDescr> aBundles) {

        pBundles.clear();
        pBundles.addAll(aBundles);
    }

    /**
     * Sets the isolate ID
     * 
     * @param aIsolateId
     *            the isolate ID
     */
    public void setId(final String aIsolateId) {

        pIsolateId = aIsolateId;
    }

    /**
     * Sets the kind of isolate
     * 
     * @param aKind
     *            The kind of the isolate
     * 
     * @see IIsolateDescr#getKind()
     */
    public void setKind(final String aKind) {

        if (aKind != null) {
            pIsolateKind = aKind;

        } else {
            pIsolateKind = "";
        }
    }

    /**
     * Sets the Java virtual machine arguments
     * 
     * @param aVmArgs
     *            the VM arguments
     */
    public void setVMArgs(final List<String> aVmArgs) {

        pVmArguments.clear();
        pVmArguments.addAll(aVmArgs);
    }
}
