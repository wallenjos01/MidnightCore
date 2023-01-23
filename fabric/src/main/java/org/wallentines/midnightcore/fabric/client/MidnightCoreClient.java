package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.fabric.event.client.ClientModulesLoadedEvent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;

@Environment(EnvType.CLIENT)
public class MidnightCoreClient implements ClientModInitializer {


    private static MidnightCoreClient INSTANCE = null;

    private final ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> modules = new ModuleManager<>();

    @Override
    public void onInitializeClient() {

        MidnightCoreAPI.getLogger().info("Starting MidnightCore Client");

        if(INSTANCE == null) {
            INSTANCE = this;
        }

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) {
            throw new IllegalStateException("MidnightCore Client was initialized before MidnightCoreAPI!");
        }

        // Default Client Modules
        Registries.CLIENT_MODULE_REGISTRY.register(AbstractMessagingModule.ID, FabricClientMessagingModule.MODULE_INFO);
        Registries.CLIENT_MODULE_REGISTRY.register(ExtensionHelper.ID, FabricClientExtensionModule.MODULE_INFO);

        FileConfig config = FileConfig.findOrCreate("client", api.getDataFolder());
        ConfigSection sec = config.getRoot().getOrCreateSection("modules");
        modules.loadAll(sec, api, Registries.CLIENT_MODULE_REGISTRY);

        // Let other mods know that modules are ready
        Event.invoke(new ClientModulesLoadedEvent(this, modules, Minecraft.getInstance()));

        MidnightCoreAPI.getLogger().info("Loaded " + modules.getCount() + " client modules");
        config.save();
    }

    public static MidnightCoreClient getInstance() {
        return INSTANCE;
    }

    public static <T extends Module<MidnightCoreAPI>> T getModule(Class<T> clazz) {
        return INSTANCE.modules.getModule(clazz);
    }

}
