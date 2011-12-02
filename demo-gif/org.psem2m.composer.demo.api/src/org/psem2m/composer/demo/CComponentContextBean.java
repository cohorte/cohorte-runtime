/**
 * File:   ComponentContextBean.java
 * Author: Thomas Calmant
 * Date:   15 nov. 2011
 */
package org.psem2m.composer.demo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.psem2m.utilities.CXException;

/**
 * Implementation of a treatment chain context
 * 
 * @author Thomas Calmant
 */
public class CComponentContextBean implements IComponentContext {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The errors list */
    private final List<String> pErrors = new LinkedList<String>();

    /** The treatment metadata */
    private final Map<String, Object> pMetadata = new HashMap<String, Object>();

    /** The treatment request */
    private final Map<String, Object> pRequest = new HashMap<String, Object>();

    /** The treatment result data */
    private final List<Map<String, Object>> pResults = new LinkedList<Map<String, Object>>();

    /**
     * Default constructor
     */
    public CComponentContextBean() {

        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#addError(java.lang.
     * CharSequence, java.lang.CharSequence)
     */
    @Override
    public void addError(final CharSequence aComponentName,
            final CharSequence aErrorMessage) {

        addError(aComponentName, aErrorMessage, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#addError(java.lang.
     * CharSequence, java.lang.CharSequence, java.lang.Throwable)
     */
    @Override
    public void addError(final CharSequence aComponentName,
            final CharSequence aErrorMessage, final Throwable aThrowable) {

        final StringBuilder builder = new StringBuilder();

        if (aComponentName != null && aComponentName.length() != 0) {
            builder.append(aComponentName).append(" : ");

        } else {
            builder.append("<unknown-component> : ");
        }

        if (aErrorMessage != null && aErrorMessage.length() != 0) {
            // Add the error description
            builder.append(aErrorMessage);

            if (aThrowable != null) {
                // Throwable string on next line
                builder.append("\n");
            }
        }

        if (aThrowable != null) {
            // Add the stack trace
            builder.append(CXException.eInString(aThrowable));
        }

        // We have something to store...
        pErrors.add(builder.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.test.api.IComponentContext#addResult(java.util.Map)
     */
    @Override
    public void addResult(final Map<String, Object> aResult) {

        if (aResult != null) {
            pResults.add(aResult);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#getErrors()
     */
    @Override
    public List<String> getErrors() {

        return pErrors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#getMetadata()
     */
    @Override
    public Map<String, Object> getMetadata() {

        return pMetadata;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#getRequest()
     */
    @Override
    public Map<String, Object> getRequest() {

        return pRequest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#getData()
     */
    @Override
    public List<Map<String, Object>> getResults() {

        return pResults;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#hasError()
     */
    @Override
    public boolean hasError() {

        return !pErrors.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.composer.test.api.IComponentContext#hasResult()
     */
    @Override
    public boolean hasResult() {

        return !pResults.isEmpty();
    }

    /**
     * @param aErrors
     *            the errors to set
     */
    public void setErrors(final List<String> aErrors) {

        pErrors.clear();
        if (aErrors != null) {
            pErrors.addAll(aErrors);
        }
    }

    /**
     * @param aMetadata
     *            the metadata to set
     */
    public void setMetadata(final Map<String, Object> aMetadata) {

        pMetadata.clear();

        if (aMetadata != null) {
            pMetadata.putAll(aMetadata);
        }
    }

    /**
     * @param aRequest
     *            the request to set
     */
    public void setRequest(final Map<String, Object> aRequest) {

        pRequest.clear();

        if (aRequest != null) {
            pRequest.putAll(aRequest);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.test.api.IComponentContext#setResult(java.util.Map)
     */
    @Override
    public void setResult(final Map<String, Object> aResult) {

        if (aResult != null) {
            pResults.clear();
            pResults.add(aResult);
        }
    }

    /**
     * @param aData
     *            the data to set
     */
    public void setResults(final List<Map<String, Object>> aData) {

        pResults.clear();

        if (aData != null) {
            pResults.addAll(aData);
        }
    }
}
