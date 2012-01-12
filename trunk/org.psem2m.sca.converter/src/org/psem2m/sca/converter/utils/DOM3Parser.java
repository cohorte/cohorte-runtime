/**
 * File:   DOM3Parser.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.psem2m.sca.converter.core.resolver.EmbeddedResourceResolver;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSParser;

/**
 * Simple DOM3 XML document parser
 * 
 * @author Thomas Calmant
 */
public class DOM3Parser implements DOMErrorHandler {

    /** The logger */
    private Logger pLogger = Logger.getLogger("DOM3Parser");

    /** The DOM3 DOM3Parser */
    private LSParser pParser;

    /**
     * Called to prepare the configuration of the parser, at the end of
     * {@link #init(DOMErrorHandler)}
     * 
     * @param aErrorHandler
     *            A DOM error handler (can be null)
     */
    protected void configureParser(final DOMErrorHandler aErrorHandler) {

        // Configure the parser
        final DOMConfiguration config = pParser.getDomConfig();

        // Error handler configuration
        if (aErrorHandler != null) {
            config.setParameter("error-handler", aErrorHandler);

        } else {
            config.setParameter("error-handler", this);
        }

        // Validate files
        config.setParameter("validate", Boolean.TRUE);

        // Local resource resolver (provides xsd files)
        config.setParameter("resource-resolver", new EmbeddedResourceResolver());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.dom.DOMErrorHandler#handleError(org.w3c.dom.DOMError)
     */
    @Override
    public boolean handleError(final DOMError aError) {

        if (pLogger == null) {
            // Don't work for nothing...
            return true;
        }

        final DOMLocator location = aError.getLocation();
        final File file = new File(String.valueOf(location.getUri()));

        if (file.getName().toLowerCase().endsWith(".xsd")) {
            // Ignore errors inside XML schemas...
            return true;
        }

        // Error level
        final Level level;
        final StringBuilder builder = new StringBuilder("[DOM3-");
        switch (aError.getSeverity()) {
        case DOMError.SEVERITY_ERROR:
            builder.append("error");
            level = Level.WARNING;
            break;

        case DOMError.SEVERITY_FATAL_ERROR:
            builder.append("FATAL");
            level = Level.SEVERE;
            break;

        case DOMError.SEVERITY_WARNING:
            builder.append("warning");
            level = Level.WARNING;
            break;

        default:
            builder.append("other");
            level = Level.INFO;
            break;
        }

        // File and line of error
        builder.append("]: in ").append(file.getName());
        builder.append("@").append(location.getLineNumber());

        // Message
        builder.append(" : ").append(aError.getMessage());

        // Print it
        if (pLogger != null) {
            pLogger.log(level, builder.toString());
        }

        // Always continue the process...
        return true;
    }

    /**
     * Prepares the parser
     * 
     * @param aErrorHandler
     *            A DOM error handler (can be null)
     * 
     * @see DOMImplementationRegistry#newInstance() for a description of
     *      possibly thrown exceptions
     * 
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws ClassCastException
     */
    public void init(final DOMErrorHandler aErrorHandler)
            throws ClassCastException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        // DOM level 3
        final DOMImplementationRegistry registry = DOMImplementationRegistry
                .newInstance();

        final DOMImplementationLS impl = (DOMImplementationLS) registry
                .getDOMImplementation("LS");

        // DOM3Parser compatible with DOM level 3 Load/Save
        pParser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS,
                null);

        // Configure the parser
        configureParser(aErrorHandler);
    }

    /**
     * Parses a composite file.
     * 
     * The {@link #init(DOMErrorHandler)} method must be called before this one.
     * 
     * @param aFile
     *            A composite file
     */
    public Document parseFile(final File aFile) throws FileNotFoundException {

        if (!aFile.isFile()) {
            throw new FileNotFoundException(aFile
                    + " is not a valid file path.");
        }

        return pParser.parseURI(aFile.toURI().toString());
    }

    /**
     * Sets the logger
     * 
     * @param aLogger
     *            A logger (can be null)
     */
    public void setLogger(final Logger aLogger) {

        pLogger = aLogger;
    }
}
