package org.wallentines.midnightcore.spigot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.MidnightCoreImpl;
import org.wallentines.midnightcore.common.Registries;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepointModule;
import org.wallentines.midnightcore.common.module.skin.AbstractSkinModule;
import org.wallentines.midnightcore.spigot.config.YamlConfigProvider;
import org.wallentines.midnightcore.spigot.event.MidnightCoreInitializeEvent;
import org.wallentines.midnightcore.spigot.event.MidnightCoreLoadModulesEvent;
import org.wallentines.midnightcore.spigot.item.ItemConverters;
import org.wallentines.midnightcore.spigot.item.SpigotInventoryGUI;
import org.wallentines.midnightcore.spigot.module.savepoint.SpigotSavepointModule;
import org.wallentines.midnightcore.spigot.module.skin.SpigotSkinModule;
import org.wallentines.midnightcore.spigot.player.SpigotPlayerManager;
import org.wallentines.midnightcore.spigot.text.SpigotScoreboard;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigRegistry;

import java.io.File;
import java.nio.file.Path;

public class MidnightCorePlugin {

    private static final Logger LOGGER = LogManager.getLogger("MidnightCore");

    private static Plugin INSTANCE;

    public static void onLoad(Plugin instance) {
        INSTANCE = instance;

        Constants.registerDefaults(YamlConfigProvider.INSTANCE);

        try {

            // Register Standard GSON provider if present (>= 1_8_R2)
            Class.forName("com.google.gson.Gson");
            ConfigRegistry.INSTANCE.registerProvider(org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider.INSTANCE);

        } catch (ClassNotFoundException ex) {
            // Ignore
        }
    }

    public static void onEnable(File dataFolder, Server server) {

        Path dataDir = dataFolder.toPath();

        String bkVer = Bukkit.getVersion();
        String ver = bkVer.substring(bkVer.indexOf("MC: ") + 4, bkVer.length() - 1);

        Version version = Version.SERIALIZER.deserialize(ver);

        MidnightCoreImpl api = new MidnightCoreImpl(
                dataDir,
                version,
                ItemConverters.getItemConverter(version),
                new SpigotPlayerManager(),
                SpigotInventoryGUI::new,
                SpigotScoreboard::new,
                (str, b) -> server.dispatchCommand(server.getConsoleSender(), str)
        );

        Registries.MODULE_REGISTRY.register(AbstractSavepointModule.ID, SpigotSavepointModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(AbstractSkinModule.ID, SpigotSkinModule.MODULE_INFO);

        server.getPluginManager().callEvent(new MidnightCoreLoadModulesEvent(api, Registries.MODULE_REGISTRY));
        api.loadModules();

        LOGGER.info("MidnightCore Enabled");
        server.getPluginManager().callEvent(new MidnightCoreInitializeEvent(api));

    }

    public static Plugin getInstance() {

        return INSTANCE;
    }


}
