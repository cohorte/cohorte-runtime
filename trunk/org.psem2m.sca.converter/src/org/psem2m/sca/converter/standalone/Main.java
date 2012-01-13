/**
 * File:   Main.java
 * Author: Thomas Calmant
 * Date:   4 janv. 2012
 */
package org.psem2m.sca.converter.standalone;

import java.io.File;

import org.psem2m.composer.config.IComposerConfigHandler;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.sca.converter.core.SCAConverter;
import org.psem2m.sca.converter.core.SCAParser;
import org.psem2m.sca.converter.model.Composite;

/**
 * Converter entry point (for stand-alone execution mode)
 * 
 * @author Thomas Calmant
 */
public class Main {

    /**
     * Converter entry point
     * 
     * @param aArgs
     *            Arguments
     */
    public static void main(final String[] aArgs) throws Exception {

        // Load the class (may throw an error)
        final Class<?> clazz = Class
                .forName("org.psem2m.composer.config.impl.JsonComposerConfigHandler");

        // Prepare the writer (may throw an error)
        final IComposerConfigHandler config = (IComposerConfigHandler) clazz
                .getConstructor((Class<?>[]) null).newInstance((Object[]) null);

        if (aArgs.length < 2) {
            System.out
                    .println("Usage : converter root_file.composite [folder]");
            return;
        }

        // Given folder or root file ones
        final File rootFolder;
        if (aArgs.length >= 2) {
            rootFolder = new File(aArgs[1]);
            if (!rootFolder.isDirectory()) {
                System.err.println("Invalid directory : " + rootFolder);
                return;
            }

        } else {
            // Use the file parent as root folder
            rootFolder = new File(".").getAbsoluteFile();
        }

        // Root file
        final File rootCompositeFile = new File(rootFolder, aArgs[0]);

        final SCAParser parser = new SCAParser();
        final SCAConverter converter = new SCAConverter();

        // Parse the document
        System.out.println("Parse : " + rootFolder);
        final Composite scaComposite = parser.parse(rootFolder,
                rootCompositeFile);

        final ComponentsSetBean composet = converter
                .convertToComposer(scaComposite);

        config.write(composet, null);
    }
}
