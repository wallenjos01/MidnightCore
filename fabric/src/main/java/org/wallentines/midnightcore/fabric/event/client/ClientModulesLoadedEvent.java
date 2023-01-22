package org.wallentines.midnightcore.fabric.event.client;

import net.minecraft.client.Minecraft;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.client.MidnightCoreClient;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;

public class ClientModulesLoadedEvent extends Event {

    private final MidnightCoreClient midnightCore;
    private final ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> manager;
    private final Minecraft client;

    public ClientModulesLoadedEvent(MidnightCoreClient midnightCore, ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> manager, Minecraft client) {
        this.manager = manager;
        this.midnightCore = midnightCore;
        this.client = client;
    }

    public MidnightCoreClient getMidnightCore() {
        return midnightCore;
    }

    public ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> getManager() {
        return manager;
    }

    public Minecraft getClient() {
        return client;
    }
}
