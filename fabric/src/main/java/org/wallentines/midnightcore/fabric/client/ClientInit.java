package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.fabric.module.extension.ExtensionModule;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {

    private final ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> CLIENT_MODULES = new ModuleManager<>();

    @Override
    public void onInitializeClient() {

        MidnightCoreAPI.getLogger().info("Starting MidnightCore Client");

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) MidnightCoreAPI.getLogger().warn("api is null");

        Registries.CLIENT_MODULE_REGISTRY.register(ExtensionModule.ID, ClientExtensionModule.MODULE_INFO);

        FileConfig config = FileConfig.findOrCreate("client", api.getDataFolder());

        ConfigSection sec = config.getRoot().getOrCreateSection("modules");
        CLIENT_MODULES.loadAll(sec, api, Registries.CLIENT_MODULE_REGISTRY);

        MidnightCoreAPI.getLogger().info("Loaded " + CLIENT_MODULES.getCount() + " client modules");

        config.save();

    }
}
