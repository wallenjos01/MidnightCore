package org.wallentines.mcore.messenger;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.smi.Messenger;
import org.wallentines.smi.MessengerType;
import org.wallentines.smi.MessengerManager;
import org.wallentines.smi.MessengerManagerImpl;

public class MessengerModule {

    public static final Registry<Identifier, MessengerType<?>> REGISTRY = Registry.create("smi");
    protected MessengerManagerImpl manager;

    protected void init(ConfigSection section, Registry<Identifier, MessengerType<?>> registry) {
        manager = new MessengerManagerImpl(registry);
        manager.loadAll(section);

        if(MessengerManager.Holder.gInstance == null) {
            MessengerManagerImpl.register(manager);
        }
    }

    protected void shutdown() {
        manager.clear();
        if(MessengerManager.Holder.gInstance == manager) {
            MessengerManager.Holder.gInstance = null;
        }
    }

    @Nullable
    public Messenger getMessenger(String name) {
        return manager.messenger(name);
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messenger");
}
