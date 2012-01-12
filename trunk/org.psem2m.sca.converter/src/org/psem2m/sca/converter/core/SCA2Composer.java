/**
 * File:   SCA2Composer.java
 * Author: Thomas Calmant
 * Date:   11 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.sca.converter.model.AbstractSCAElement;
import org.psem2m.sca.converter.model.Component;
import org.psem2m.sca.converter.model.Composite;
import org.psem2m.sca.converter.model.Implementation;
import org.psem2m.sca.converter.model.Property;
import org.psem2m.sca.converter.model.Reference;

/**
 * @author Thomas Calmant
 * 
 */
public class SCA2Composer {

    /**
     * @param aComponent
     * @return
     */
    protected ComponentBean convertComponent(final Component aComponent) {

        final Implementation implem = aComponent.getImplementation();

        if (implem == null
                || !SCAConstants.IMPLEMENTATION_PSEM2M.equals(implem.getKind())) {
            // Not a PSEM2M implementation
            System.err.println("Not a PSEM2M implementation - " + implem
                    + " - for " + aComponent.getQualifiedName());
            return null;
        }

        final ComponentBean psem2mComponent = new ComponentBean();
        psem2mComponent.setName(aComponent.getQualifiedName().getLocalName());
        psem2mComponent.setType(implem.getXmlAttribute("type"));
        psem2mComponent.setIsolate(implem.getXmlAttribute("isolate"));

        // Wires
        final Map<String, String> wires = new HashMap<String, String>();
        for (final Reference ref : aComponent.getReferences()) {

            if (ref.getQualifiedName() == null) {
                System.err.println("Reference without name : " + ref);
                continue;
            }

            final String fieldName = ref.getQualifiedName()
                    .getLocalNameLastPart();

            if (ref.getTargets().size() > 1) {
                System.out
                        .println("WARNING: only single target references are handled");

            } else if (ref.getTargets().isEmpty()) {
                // No target
                continue;
            }

            // FIXME make an LDAP filter
            // FIXME handle all targets
            final AbstractSCAElement<?> target = ref.getTargets().iterator()
                    .next();
            wires.put(fieldName, target.getCompleteName());
        }
        psem2mComponent.setWires(wires);

        // Properties
        final Map<String, String> properties = new HashMap<String, String>();
        for (final Property scaProperty : aComponent.getProperties()) {

            properties.put(scaProperty.getQualifiedName().getLocalName(),
                    scaProperty.getValue());
        }
        psem2mComponent.setProperties(properties);

        return psem2mComponent;
    }

    /**
     * @param aScaComposite
     * @return
     */
    protected ComponentsSetBean convertComposite(
            final ComponentsSetBean aParent, final Composite aScaComposite) {

        // Prepare the components set
        final ComponentsSetBean composet = new ComponentsSetBean();
        composet.setName(aScaComposite.getAlias());
        composet.setParent(aParent);

        // Components
        for (final Component component : aScaComposite.getComponents()) {

            final ComponentBean psem2mComponent = convertComponent(component);
            if (psem2mComponent != null) {
                composet.addComponent(psem2mComponent);
            }
        }

        // Sub-composites
        final List<ComponentsSetBean> subComposets = new ArrayList<ComponentsSetBean>();
        for (final Composite composite : aScaComposite.getComposites()) {
            subComposets.add(convertComposite(composet, composite));
        }
        composet.setComponentSets(subComposets);

        return composet;
    }

    /**
     * 
     * @param aScaComposite
     * @return
     */
    public ComponentsSetBean convertToComposer(final Composite aScaComposite) {

        return convertComposite(null, aScaComposite);
    }
}
