package org.wallentines.mcore.messenger;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Registry;

/**
 * An interface for creating Messengers
 */
public interface MessengerType {

    /**
     * Creates a messenger from the given module with the given parameters
     * @param module The module which owns the messenger
     * @param params Parameters to use in creation
     * @return A new messenger
     */
    Messenger create(MessengerModule module, ConfigSection params);


    /**
     * Determines whether this messenger type requires the plugin message broker.
     * @return Whether the messenger needs the plugin message broker.
     */
    default boolean usesPluginMessageBroker() {
        return false;
    }

    /**
     * A registry of messenger types available to the messenger module
     */
    Registry<String, MessengerType> REGISTRY = Registry.createStringRegistry();

}
