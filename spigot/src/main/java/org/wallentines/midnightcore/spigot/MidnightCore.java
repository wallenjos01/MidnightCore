package org.wallentines.midnightcore.spigot;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.MidnightCoreImpl;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.common.module.savepoint.AbstractSavepointModule;
import org.wallentines.midnightcore.common.module.skin.AbstractSkinModule;
import org.wallentines.midnightcore.spigot.config.YamlCodec;
import org.wallentines.midnightcore.spigot.event.MidnightCoreInitializeEvent;
import org.wallentines.midnightcore.spigot.event.MidnightCoreLoadModulesEvent;
import org.wallentines.midnightcore.spigot.item.ItemHelper;
import org.wallentines.midnightcore.spigot.item.SpigotInventoryGUI;
import org.wallentines.midnightcore.spigot.module.savepoint.SpigotSavepointModule;
import org.wallentines.midnightcore.spigot.module.skin.SpigotSkinModule;
import org.wallentines.midnightcore.spigot.text.SpigotScoreboard;
import org.wallentines.midnightcore.spigot.server.SpigotServer;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.mdcfg.ConfigSection;

import java.io.File;
import java.nio.file.Path;

public class MidnightCore {

    private static Plugin INSTANCE;

    public static void onLoad(Plugin instance) {
        INSTANCE = instance;

        FileConfig.REGISTRY.registerFileCodec(YamlCodec.fileCodec());
        Constants.registerDefaults();
    }

    public static void onEnable(File dataFolder, Server server, Plugin plugin) {


        Path dataDir = dataFolder.toPath();

        String bkVer = Bukkit.getVersion();
        String ver = bkVer.substring(bkVer.indexOf("MC: ") + 4, bkVer.length() - 1);

        Version version = Version.SERIALIZER.deserialize(ver);

        ConfigSection langDefaults = YamlCodec.INSTANCE.decode(ConfigContext.INSTANCE, MidnightCore.class.getResourceAsStream("/lang/en_us.yml")).asSection();

        MidnightCoreImpl api = new MidnightCoreImpl(
                dataDir,
                version,
                langDefaults,
                ItemHelper.getItemConverter(version),
                SpigotInventoryGUI::new,
                SpigotScoreboard::new
        );

        MidnightCoreAPI.getLogger().info("Starting MidnightCore with Game Version " + version);

        // Register Spigot Modules
        Registries.MODULE_REGISTRY.register(AbstractSavepointModule.ID, SpigotSavepointModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(AbstractSkinModule.ID, SpigotSkinModule.MODULE_INFO);
        server.getPluginManager().callEvent(new MidnightCoreLoadModulesEvent(api, Registries.MODULE_REGISTRY));

        api.setActiveServer(new SpigotServer(api, server, plugin));

        server.getPluginManager().callEvent(new MidnightCoreInitializeEvent(api));
    }

    public static Plugin getInstance() {

        return INSTANCE;
    }


}
