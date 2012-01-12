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
import org.psem2m.sca.converter.model.Component;
import org.psem2m.sca.converter.model.Composite;
import org.psem2m.sca.converter.model.INameable;
import org.psem2m.sca.converter.model.Implementation;
import org.psem2m.sca.converter.model.Property;
import org.psem2m.sca.converter.model.Reference;
import org.psem2m.sca.converter.model.Service;

/**
 * Converts an SCA hierarchized composition model into a PSEM2M Composer model.
 * 
 * @author Thomas Calmant
 */
public class SCA2Composer {

    /**
     * Converts a SCA component into a PSEM2M Composer component. Only works
     * with PSEM2M implemented SCA component (with the
     * <em>implementation.psem2m</em> implementation tag.
     * 
     * @param aComponent
     *            A SCA component
     * @return The PSEM2M representation of the component
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
            final INameable target = ref.getTargets().iterator().next();

            if (target instanceof Component) {
                // Reference points to a component, get the component name
                wires.put(fieldName, target.getCompleteAlias());

            } else if (target instanceof Service) {
                // Reference points to a service, get its container name
                final INameable container = ((Service) target).getContainer();
                if (container != null) {
                    wires.put(fieldName, container.getCompleteAlias());
                }
            }
        }
        psem2mComponent.setWires(wires);

        // Properties
        final Map<String, String> properties = new HashMap<String, String>();
        for (final Property scaProperty : aComponent.getProperties()) {

            final String value = scaProperty.getValue();
            if (value != null && !value.isEmpty()) {
                // Only store meaningful values
                properties.put(scaProperty.getQualifiedName()
                        .getLocalNameLastPart(), value);
            }
        }
        psem2mComponent.setProperties(properties);

        return psem2mComponent;
    }

    /**
     * Converts a SCA composite into a PSEM2M Composer components set
     * 
     * @param aScaComposite
     *            A SCA composite
     * @return The PSEM2M representation of the composite
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
     * Converts the given SCA composition root (a composite) into a PSEM2M
     * composition root.
     * 
     * @param aScaComposite
     *            The root SCA composite
     * @return The corresponding PSEM2M Composer representation
     */
    public ComponentsSetBean convertToComposer(final Composite aScaComposite) {

        return convertComposite(null, aScaComposite);
    }
}
