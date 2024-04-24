package org.wallentines.mcore.messenger;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.StringRegistry;

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
     * A registry of messenger types available to the messenger module
     */
    StringRegistry<MessengerType> REGISTRY = new StringRegistry<>();

}
