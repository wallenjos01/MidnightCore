package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.common.module.extension.AbstractExtensionModule;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;

@Environment(EnvType.CLIENT)
public class MidnightCoreClient implements ClientModInitializer {

    public static final ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> CLIENT_MODULES = new ModuleManager<>();

    @Override
    public void onInitializeClient() {

        MidnightCoreAPI.getLogger().info("Starting MidnightCore Client");
        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) {
            throw new IllegalStateException("MidnightCore Client was initialized before MidnightCoreAPI!");
        }

        // Default Client Modules
        Registries.CLIENT_MODULE_REGISTRY.register(AbstractMessagingModule.ID, FabricClientMessagingModule.MODULE_INFO);
        Registries.CLIENT_MODULE_REGISTRY.register(AbstractExtensionModule.ID, FabricClientExtensionModule.MODULE_INFO);

        FileConfig config = FileConfig.findOrCreate("client", api.getDataFolder());
        ConfigSection sec = config.getRoot().getOrCreateSection("modules");
        CLIENT_MODULES.loadAll(sec, api, Registries.CLIENT_MODULE_REGISTRY);

        MidnightCoreAPI.getLogger().info("Loaded " + CLIENT_MODULES.getCount() + " client modules");
        config.save();
    }
}
