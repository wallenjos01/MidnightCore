package org.wallentines.midnightcore.spigot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.MidnightCoreImpl;
import org.wallentines.midnightcore.common.Registries;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepointModule;
import org.wallentines.midnightcore.common.module.skin.AbstractSkinModule;
import org.wallentines.midnightcore.spigot.adapter.Adapters;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.midnightlib.config.ConfigRegistry;

import java.nio.file.Path;

public class MidnightCore extends JavaPlugin {

    private static final Logger LOGGER = LogManager.getLogger("MidnightCore");

    @Override
    public void onLoad() {

        MidnightCorePlugin.PLUGIN = this;
        Constants.registerDefaults(YamlConfigProvider.INSTANCE);

        try {

            // Register Standard GSON provider if present (>= 1_8_R2)
            Class.forName("com.google.gson.Gson");
            ConfigRegistry.INSTANCE.registerProvider(org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider.INSTANCE);

        } catch (ClassNotFoundException ex) {
            // Ignore
        }

    }

    @Override
    public void onEnable() {

        Path dataFolder = getDataFolder().toPath();

        String bkVer = Bukkit.getVersion();
        String ver = bkVer.substring(bkVer.indexOf("MC: ") + 4, bkVer.length() - 1);

        Version version = Version.SERIALIZER.deserialize(ver);

        MidnightCoreImpl api = new MidnightCoreImpl(
                dataFolder,
                version,
                ItemConverters.getItemConverter(version),
                new SpigotPlayerManager(),
                SpigotInventoryGUI::new,
                SpigotScoreboard::new,
                (str, b) -> getServer().dispatchCommand(getServer().getConsoleSender(), str)
        );

        // Adapter
        Adapters.findAdapter();

        Registries.MODULE_REGISTRY.register(AbstractSavepointModule.ID, SpigotSavepointModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(AbstractSkinModule.ID, SpigotSkinModule.MODULE_INFO);

        getServer().getPluginManager().callEvent(new MidnightCoreLoadModulesEvent(api, Registries.MODULE_REGISTRY));
        api.loadModules();

        LOGGER.info("MidnightCore Enabled");
        getServer().getPluginManager().callEvent(new MidnightCoreInitializeEvent(api));

    }

}