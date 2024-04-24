package org.wallentines.mcore.messenger;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.StringRegistry;

public interface MessengerType {

    Messenger create(MessengerModule module, ConfigSection params);


    StringRegistry<MessengerType> REGISTRY = new StringRegistry<>();

}
