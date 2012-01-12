/**
 * File:   ScaConverterService.java
 * Author: Thomas Calmant
 * Date:   12 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.io.File;
import java.io.IOException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleException;
import org.psem2m.composer.config.IComposerConfigHandler;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.sca.converter.model.Composite;

/**
 * The SCA loader service
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-sca-converter-factory", publicFactory = false)
@Provides(specifications = IComposerConfigHandler.class)
@Instantiate(name = "psem2m-composer-sca-converter-factory")
public class ScaConverterService extends CPojoBase implements
        IComposerConfigHandler {

    /** The converter */
    private SCAConverter pConverter;

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The parser */
    private SCAParser pParser;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.config.IComposerConfigHandler#canHandle(java.lang
     * .String)
     */
    @Override
    public boolean canHandle(final String aFileName) {

        // Accept all files ending with ".composite"
        return aFileName.toLowerCase().endsWith(".composite");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "SCA Converter gone");
        pParser = null;
        pConverter = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.config.IComposerConfigHandler#load(java.lang.String)
     */
    @Override
    public ComponentsSetBean load(final String aFileName) {

        final File file = new File(aFileName).getAbsoluteFile();
        final File folder = file.getParentFile();

        // Parse...
        final Composite sca;
        try {
            sca = pParser.parse(folder, file);

        } catch (final Exception e) {
            pLogger.logWarn(this, "load",
                    "Error parsing the SCA composition in ", folder, " : ", e);
            return null;
        }

        return pConverter.convertToComposer(sca);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pConverter = new SCAConverter();
        try {
            pParser = new SCAParser();

        } catch (final Exception e) {
            pLogger.logSevere(this, "validatePojo",
                    "Can't set up the SCA parser :", e);
            return;
        }

        pLogger.logInfo(this, "validatePojo", "SCA Converter Ready");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.composer.config.IComposerConfigHandler#write(org.psem2m.composer
     * .model.ComponentsSetBean, java.lang.String)
     */
    @Override
    public void write(final ComponentsSetBean aComponentsSet,
            final String aFileName) throws IOException {

        // Does nothing
        pLogger.logSevere(this, "write", "Not implemented.");

        throw new IOException("Not implemented");
    }
}
