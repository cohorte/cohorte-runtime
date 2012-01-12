/**
 * File:   SCAConverter.java
 * Author: Thomas Calmant
 * Date:   11 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.psem2m.composer.model.ComponentBean;
import org.psem2m.composer.model.ComponentsSetBean;
import org.psem2m.sca.converter.model.Component;
import org.psem2m.sca.converter.model.Composite;
import org.psem2m.sca.converter.model.INameable;
import org.psem2m.sca.converter.model.Implementation;
import org.psem2m.sca.converter.model.Property;
import org.psem2m.sca.converter.model.Reference;
import org.psem2m.sca.converter.model.Service;
import org.psem2m.sca.converter.utils.QName;

/**
 * Converts an SCA hierarchized composition model into a PSEM2M Composer model.
 * 
 * @author Thomas Calmant
 */
public class SCAConverter {

    /** The logger */
    private Logger pLogger;

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
            final StringBuilder builder = new StringBuilder();
            builder.append("Not a PSEM2M implementation '");
            builder.append(implem);
            builder.append("' for component ");
            builder.append(aComponent.getQualifiedName());

            // Not a PSEM2M implementation
            log(Level.INFO, builder);
            return null;
        }

        final ComponentBean psem2mComponent = new ComponentBean();
        psem2mComponent.setName(aComponent.getQualifiedName().getLocalName());
        psem2mComponent.setType(implem.getXmlAttribute("type"));
        psem2mComponent.setIsolate(implem.getXmlAttribute("isolate"));

        // Wires and filters
        final StringBuilder filter = new StringBuilder();
        for (final Reference ref : aComponent.getReferences()) {

            // Empty targets list
            if (ref.getTargets().isEmpty()) {
                continue;
            }

            final QName refName = ref.getQualifiedName();
            if (refName == null) {
                log(Level.WARNING, "Reference without name : " + ref);
                continue;
            }

            // The reference field name
            final String fieldName = refName.getLocalNameLastPart();

            // The targets iterator
            final Iterator<INameable> iterator = ref.getTargets().iterator();

            switch (ref.getMultiplicity()) {
            case ONE_ONE:
            case ZERO_ONE:
                // Reference to one element
                if (ref.getTargets().size() > 1) {
                    log(Level.WARNING,
                            "Only one target should be indicated for : "
                                    + refName);
                }

                // Use a wire (single target)
                psem2mComponent.setWire(fieldName,
                        getTargetCompleteName(iterator.next()));
                break;

            case ONE_N:
            case ZERO_N:
                // Reference to multiple elements

                // Reset the builder
                filter.setLength(0);

                filter.append("(|");
                while (iterator.hasNext()) {
                    // FIXME iPOJO style...
                    filter.append("(instance.name=");
                    filter.append(getTargetCompleteName(iterator.next()));
                    filter.append(')');
                }
                filter.append(')');

                psem2mComponent.setFieldFilter(fieldName, filter.toString());
                break;
            }
        }

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

    /**
     * Retrieves the complete aliased name of the targeted component. Returns
     * null if the component can't be computed.
     * 
     * @param aTarget
     *            A component or a service targeted by a reference
     * @return The complete aliased name of the component, or null.
     */
    protected String getTargetCompleteName(final INameable aTarget) {

        if (aTarget instanceof Component) {
            // Reference points to a component, get the component name
            return aTarget.getCompleteAlias();

        } else if (aTarget instanceof Service) {
            // Reference points to a service, get its container name
            final INameable container = ((Service) aTarget).getContainer();
            if (container != null) {
                return container.getCompleteAlias();
            }
        }

        return null;
    }

    private void log(final Level aLevel, final CharSequence aMessage) {

        if (pLogger != null) {
            pLogger.log(aLevel, aMessage.toString());
        }
    }
}
