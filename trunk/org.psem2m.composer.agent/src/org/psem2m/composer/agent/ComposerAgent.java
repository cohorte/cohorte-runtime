/**
 * File:   ComposerAgent.java
 * Author: Thomas Calmant
 * Date:   26 oct. 2011
 */
package org.psem2m.composer.agent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleException;
import org.psem2m.composer.ComponentBean;
import org.psem2m.composer.ComposerAgentConstants;
import org.psem2m.composer.ComposerAgentSignals;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.base.activators.CPojoBase;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.isolates.services.remote.signals.ISignalBroadcaster;
import org.psem2m.isolates.services.remote.signals.ISignalData;
import org.psem2m.isolates.services.remote.signals.ISignalListener;
import org.psem2m.isolates.services.remote.signals.ISignalReceiver;

/**
 * Implementation of a composer agent
 * 
 * @author Thomas Calmant
 */
@Component(name = "psem2m-composer-agent-factory", publicFactory = false)
@Instantiate(name = "psem2m-composer-agent")
public class ComposerAgent extends CPojoBase implements ISignalListener {

    /** iPOJO signal broadcaster dependency ID */
    private static final String IPOJO_ID_BROADCASTER = "signals-broadcaster";

    /** iPOJO factories dependency ID */
    private static final String IPOJO_ID_FACTORIES = "ipojo-factories";

    /** Local factories */
    private final Map<String, Factory> pFactories = new HashMap<String, Factory>();

    /** Maps fields names and IDs for each component type */
    private final Map<String, Map<String, String>> pFactoriesFieldsIds = new HashMap<String, Map<String, String>>();

    /** The logger */
    @Requires
    private IIsolateLoggerSvc pLogger;

    /** The factory class complete metadata field */
    private Field pMetadataFactoryField;

    /** Platform properties */
    @Requires
    private IPlatformDirsSvc pPlatformDirs;

    /** Signal broadcaster */
    @Requires(id = IPOJO_ID_BROADCASTER)
    private ISignalBroadcaster pSignalBroadcaster;

    /**
     * Default constructor
     */
    public ComposerAgent() {

        super();
    }

    /**
     * Called by iPOJO when a Factory service is bound
     * 
     * @param aFactory
     *            A new factory service
     */
    @Bind(id = IPOJO_ID_FACTORIES, aggregate = true, optional = true)
    protected void bindFactory(final Factory aFactory) {

        // Store the factory name (component type name)
        final String factoryName = aFactory.getName();

        // Prepare a field - ID map and attach it to the component type
        final Map<String, String> fieldIdMap = new HashMap<String, String>();
        pFactoriesFieldsIds.put(factoryName, fieldIdMap);

        // Set up the map content
        if (aFactory instanceof IPojoFactory) {
            // Working on an IPojoFactory
            final Element componentModel = getFactoryMetadata((IPojoFactory) aFactory);

            final Element[] requiresElems = componentModel
                    .getElements(ComposerAgentConstants.REQUIRES_ELEMENT_NAME);

            if (requiresElems != null) {
                for (final Element requires : requiresElems) {

                    final String name = requires
                            .getAttribute(ComposerAgentConstants.REQUIRES_FIELD);
                    if (name != null) {
                        // The name is the most important part
                        final String id = requires
                                .getAttribute(ComposerAgentConstants.REQUIRES_ID);
                        fieldIdMap.put(name, id);
                    }
                }
            }
        }

        pFactories.put(factoryName, aFactory);

        // Signal the arrival to monitors
        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.MONITORS,
                ComposerAgentSignals.SIGNAL_ISOLATE_ADD_FACTORY,
                new String[] { factoryName });

        pLogger.logInfo(this, "BIND FACTORY", "Factory bound & notified :",
                factoryName);
    }

    /**
     * Called by iPOJO when a signal receiver is bound
     * 
     * @param aSignalReceiver
     *            The bound service
     */
    @Bind(optional = false)
    protected void bindSignalReceiver(final ISignalReceiver aSignalReceiver) {

        // Register to all composer agent signals
        aSignalReceiver.registerListener(
                ComposerAgentSignals.FILTER_ALL_REQUESTS, this);

        pLogger.logInfo(this, "bindSignalReceiver",
                "Bound to a signal receiver");
    }

    /**
     * Sends a signal to the given isolate to indicate which components can be
     * instantiated here.
     * 
     * @param aIsolateId
     *            The isolate to answer to
     * @param aComponents
     *            Components to find in the isolate
     */
    protected void canHandleComponents(final String aIsolateId,
            final ComponentBean[] aComponents) {

        // Don't forget : the isolate ID can change due to the SlaveAgent
        final String currentIsolateId = pPlatformDirs.getIsolateId();

        // Result list
        final List<ComponentBean> handledComponents = new ArrayList<ComponentBean>();

        // Test all beans...
        for (final ComponentBean component : aComponents) {

            // ... host isolate ID
            final String componentHostId = component.getIsolate();
            if (componentHostId != null
                    && !componentHostId.equals(currentIsolateId)) {
                // Bad host
                continue;
            }

            // ... factory presence
            final String componentType = component.getType();
            if (componentType != null && pFactories.containsKey(componentType)) {
                // We can do it
                handledComponents.add(component);
            }
        }

        // Reply to the requester with a signal
        final ComponentBean[] resultArray = handledComponents
                .toArray(new ComponentBean[handledComponents.size()]);

        pSignalBroadcaster.sendData(aIsolateId,
                ComposerAgentSignals.SIGNAL_RESPONSE_HANDLES_COMPONENTS,
                resultArray);
    }

    /**
     * Retrieves the complete metadata of the given factory if authorized.
     * Returns null on error.
     * 
     * Because {@link IPojoFactory} is the abstract class extended by all iPOJO
     * 1.8.0 factories, all Factory services instances should be handled by this
     * method.
     * 
     * @param aFactory
     *            An iPOJO Factory implementation
     * 
     * @return The factory complete metadata, null on exception or if not found.
     */
    protected Element getFactoryMetadata(final IPojoFactory aFactory) {

        if (pMetadataFactoryField == null) {
            // Get the interesting field

            try {
                // Get the field containing the component complete metadata
                pMetadataFactoryField = IPojoFactory.class
                        .getDeclaredField("m_componentMetadata");

                pMetadataFactoryField.setAccessible(true);

            } catch (final SecurityException e) {
                pLogger.logSevere(this, "getFactoryMetadata",
                        "Unauthorized access to the component metadata field",
                        e);
                return null;

            } catch (final NoSuchFieldException e) {
                pLogger.logSevere(this, "getFactoryMetadata",
                        "The component metadata field has not been found",
                        "(iPOJO implementation changed ?)", e);
                return null;
            }
        }

        try {
            return (Element) pMetadataFactoryField.get(aFactory);

        } catch (final IllegalArgumentException e) {
            pLogger.logSevere(this, "getFactoryMetadata",
                    "Bad factory type to retrieve the metadata field value", e);

        } catch (final IllegalAccessException e) {
            pLogger.logSevere(this, "getFactoryMetadata",
                    "Unauthorized access to the component metadata field", e);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.signals.ISignalListener#
     * handleReceivedSignal(java.lang.String,
     * org.psem2m.isolates.services.remote.signals.ISignalData)
     */
    @Override
    public void handleReceivedSignal(final String aSignalName,
            final ISignalData aSignalData) {

        final String signalSender = aSignalData.getIsolateSender();
        final Object signalContent = aSignalData.getSignalContent();

        pLogger.logInfo(this, "RECEIVED SIGNAL", "sender=", signalSender,
                "signal=", aSignalName, "object=", signalContent);

        if (ComposerAgentSignals.SIGNAL_CAN_HANDLE_COMPONENTS
                .equals(aSignalName)) {
            // Test if the isolate can instantiate the given components

            if (signalContent instanceof ComponentBean[]) {

                canHandleComponents(signalSender,
                        (ComponentBean[]) signalContent);
            }

        } else if (ComposerAgentSignals.SIGNAL_INSTANTIATE_COMPONENTS
                .equals(aSignalName)) {
            // Instantiate requested components

            if (signalContent instanceof ComponentBean[]) {

                try {
                    instantiateComponents(signalSender,
                            (ComponentBean[]) signalContent);

                } catch (final Exception e) {
                    pLogger.logSevere(this, "KING KONG KONG", e);
                }
            }
        }
    }

    /**
     * Tries to instantiate the given components in the current isolate, then
     * sends a signal to the given isolate with the result.
     * 
     * @param aIsolateId
     *            The isolate to answer to
     * @param aComponents
     *            Components to instantiate in the isolate
     */
    protected void instantiateComponents(final String aIsolateId,
            final ComponentBean[] aComponents) {

        pLogger.logInfo(this, "KING KING KING", "instantiateComponents - "
                + aComponents);

        // Current isolate ID
        final String isolateId = pPlatformDirs.getIsolateId();

        // List of the isolates that succeeded
        final List<String> succeededComponents = new ArrayList<String>();

        // List of the isolates that failed
        final List<String> failedComponents = new ArrayList<String>();

        // Find the composite name from the first component
        String compositeName = null;

        // Try to instantiate each component
        for (final ComponentBean component : aComponents) {

            pLogger.logInfo(this, "KING KING KING", "Working on component : "
                    + component);

            if (compositeName == null) {
                compositeName = component.getCompositeName();
            }

            // Get the factory
            final Factory factory = pFactories.get(component.getType());
            if (factory == null) {
                // Unknown component type
                failedComponents.add(component.getName());
                continue;
            }

            // Prepare instance properties
            final Properties properties = component
                    .generateProperties(pFactoriesFieldsIds.get(component
                            .getType()));
            properties.put(ComposerAgentConstants.HOST_ISOLATE, isolateId);

            try {
                // Instantiate the component
                factory.createComponentInstance(properties);
                succeededComponents.add(component.getName());

            } catch (final Exception e) {

                // Fail !
                failedComponents.add(component.getName());
                pLogger.logSevere(this, "", "Error instantiating component '"
                        + component.getName() + "'", e);
            }
        }

        pLogger.logInfo(this, "KING KING KING", "preparing answer");

        // Set the composite name
        final HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(ComposerAgentSignals.RESULT_KEY_COMPOSITE, compositeName);

        // Set the succeeded components names
        final String[] succeededArray = succeededComponents
                .toArray(new String[succeededComponents.size()]);
        resultMap.put(ComposerAgentSignals.RESULT_KEY_INSTANTIATED,
                succeededArray);

        // Set the failed components names
        final String[] failedArray = failedComponents
                .toArray(new String[failedComponents.size()]);
        resultMap.put(ComposerAgentSignals.RESULT_KEY_FAILED, failedArray);

        // Send the signal
        pSignalBroadcaster.sendData(aIsolateId,
                ComposerAgentSignals.SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS,
                resultMap);

        pLogger.logInfo(this, "KING KING KING", "sent to " + isolateId);

        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.MONITORS,
                ComposerAgentSignals.SIGNAL_RESPONSE_INSTANTIATE_COMPONENTS,
                resultMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#invalidatePojo()
     */
    @Override
    @Invalidate
    public void invalidatePojo() throws BundleException {

        pLogger.logInfo(this, "invalidatePojo", "Composer agent Gone");
    }

    /**
     * Called by iPOJO when a factory service is unbound
     * 
     * @param aFactory
     *            A factory service going away
     */
    @Unbind(id = IPOJO_ID_FACTORIES, aggregate = true, optional = true)
    protected void unbindFactory(final Factory aFactory) {

        final String factoryName = aFactory.getName();

        // Remove the factory from the map
        pFactories.remove(factoryName);

        // Signal the removal to monitors
        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.MONITORS,
                ComposerAgentSignals.SIGNAL_ISOLATE_REMOVE_FACTORY,
                new String[] { factoryName });
    }

    /**
     * Called by iPOOJO when a signal broadcaster is gone, which means just
     * before this agent is invalidated.
     * 
     * @param aSignalBroadcaster
     *            A signal broadcaster
     */
    @Unbind(id = IPOJO_ID_BROADCASTER)
    protected void unbindSignalBroadcaster(
            final ISignalBroadcaster aSignalBroadcaster) {

        // Send a last signal to monitors to forget this agent
        pSignalBroadcaster.sendData(
                ISignalBroadcaster.EEmitterTargets.MONITORS,
                ComposerAgentSignals.SIGNAL_ISOLATE_FACTORIES_GONE, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.base.activators.CPojoBase#validatePojo()
     */
    @Override
    @Validate
    public void validatePojo() throws BundleException {

        pLogger.logInfo(this, "validatePojo", "Composer agent Ready");
    }
}
