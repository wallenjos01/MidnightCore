package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.fabric.module.extension.ExtensionModule;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;

@Environment(EnvType.CLIENT)
public class ClientInit {

    private static final ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> CLIENT_MODULES = new ModuleManager<>();

    public static void init(MidnightCoreAPI api) {

        Registries.CLIENT_MODULE_REGISTRY.register(ExtensionModule.ID, ClientExtensionModule.MODULE_INFO);

        FileConfig config = FileConfig.findOrCreate("client", api.getDataFolder());

        ConfigSection sec = config.getRoot().getOrCreateSection("modules");
        CLIENT_MODULES.loadAll(sec, api, Registries.CLIENT_MODULE_REGISTRY);

        MidnightCoreAPI.getLogger().info("Loaded " + CLIENT_MODULES.getCount() + " client modules");

        config.save();

    }
}
