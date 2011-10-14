package org.psem2m.isolates.services.remote.signals;

/**
 * Represents a signal data convert
 */
public interface ISignalDataConverter {

    /**
     * Converts the given signal content, formatted in the given content type,
     * into a ISignalData object.
     * 
     * Returns null if the signal content is invalid or if the content type is
     * not supported.
     * 
     * @param aSignalContent
     *            The signal content
     * @param aContentType
     *            The signal content type
     * @return The signal data object, null on error
     */
    ISignalData toSignalData(String aSignalContent, String aContentType);

    /**
     * Converts the given signal data into a string in the given format. If the
     * signal data can't be converted in the requested format or is invalid,
     * this method returns null.
     * 
     * @param aSignalData
     *            A signal data object
     * @param aContentType
     *            The output string content type
     * 
     * @return The signal data in the selected form, null on error
     */
    String toSignalFormat(ISignalData aSignalData, String aContentType);
}
