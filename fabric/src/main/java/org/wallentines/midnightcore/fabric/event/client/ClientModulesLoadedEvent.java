package org.wallentines.midnightcore.fabric.event.client;

import net.minecraft.client.Minecraft;
import org.wallentines.midnightcore.client.MidnightCoreClient;
import org.wallentines.midnightcore.client.module.ClientModule;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleManager;

@SuppressWarnings("unused")
public class ClientModulesLoadedEvent extends Event {

    private final MidnightCoreClient midnightCore;
    private final ModuleManager<MidnightCoreClient, ClientModule> manager;
    private final Minecraft client;

    public ClientModulesLoadedEvent(MidnightCoreClient midnightCore, ModuleManager<MidnightCoreClient, ClientModule> manager, Minecraft client) {
        this.manager = manager;
        this.midnightCore = midnightCore;
        this.client = client;
    }

    public MidnightCoreClient getMidnightCore() {
        return midnightCore;
    }

    public ModuleManager<MidnightCoreClient, ClientModule> getManager() {
        return manager;
    }

    public Minecraft getClient() {
        return client;
    }
}
