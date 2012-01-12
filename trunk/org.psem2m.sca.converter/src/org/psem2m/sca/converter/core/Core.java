/**
 * File:   Core.java
 * Author: Thomas Calmant
 * Date:   4 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.io.File;

import org.psem2m.composer.config.impl.JsonComposerConfigHandler;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.sca.converter.model.Composite;

/**
 * @author Thomas Calmant
 * 
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
     * @param args
     */
    public static void main(final String[] args) throws Exception {

        final boolean tuscany = true;

        final File sca_folder;
        final File root_file;
        if (tuscany) {
            sca_folder = new File(TUSCANY_FOLDER);
            root_file = new File(sca_folder, TUSCANY_ROOT);

        } else {
            sca_folder = new File(PSEM2M_FOLDER);
            root_file = new File(sca_folder, PSEM2M_ROOT);
        }

        final SCAParser parser = new SCAParser();
        final SCA2Composer converter = new SCA2Composer();

        // Parse the document
        System.out.println("Parse : " + sca_folder);
        final Composite scaComposite = parser.parse(sca_folder, root_file);
        System.err.flush();
        System.out.flush();

        System.out.println("To PSEM2M...");
        System.out.flush();
        System.err.flush();

        final ComponentsSetBean composet = converter
                .convertToComposer(scaComposite);

        System.err.flush();

        System.out.println("------------  SCA  ---------------------------");
        System.out.println(scaComposite);

        System.out.println("------------  PSEM2M  ------------------------");
        System.out.println(composet.toCompleteString());

        File file;
        if (tuscany) {
            file = new File(OUTPUT_FOLDER, OUTPUT_TUSCANY);

        } else {
            file = new File(OUTPUT_FOLDER, OUTPUT_PSEM2M);
        }

        final JsonComposerConfigHandler config = new JsonComposerConfigHandler();
        config.write(composet, file.getAbsolutePath());
    }
}
