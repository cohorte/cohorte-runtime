/**
 * File:   Core.java
 * Author: Thomas Calmant
 * Date:   4 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.io.File;

import org.psem2m.composer.config.IComposerConfigHandler;
import org.psem2m.composer.config.impl.JsonComposerConfigHandler;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.sca.converter.model.Composite;

/**
 * Converter entry point (for stand-alone execution mode)
 * 
 * @author Thomas Calmant
 */
public class Core {

    public static final String OUTPUT_FOLDER = "/home/tcalmant/Bureau/";
    public static final String OUTPUT_PSEM2M = "psem2m.js";
    public static final String OUTPUT_TUSCANY = "tuscany.js";

    public static final String PSEM2M_FOLDER = "/home/tcalmant/programmation/workspaces/jvm-forker/org.psem2m.composer.demo.sca/src.old/";
    public static final String PSEM2M_ROOT = "application.composite";

    public static final String TUSCANY_FOLDER = "/home/tcalmant/Téléchargements/tuscany-sca-2.0-Beta3-src/samples/applications/store/src/main/resources/";
    public static final String TUSCANY_ROOT = "store.composite";

    /**
     * Converter entry point
     * 
     * @param aArgs
     *            Arguments
     */
    public static void main(final String[] aArgs) throws Exception {

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
        final SCA2Composer converter = new SCA2Composer();

        // Parse the document
        System.out.println("Parse : " + rootFolder);
        final Composite scaComposite = parser.parse(rootFolder,
                rootCompositeFile);

        final ComponentsSetBean composet = converter
                .convertToComposer(scaComposite);

        final IComposerConfigHandler config = new JsonComposerConfigHandler();
        config.write(composet, null);
    }
}
