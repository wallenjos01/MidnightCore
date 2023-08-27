package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.Adapters;
import org.wallentines.mcore.adapter.GenericAdapter;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.BinaryCodec;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.io.IOException;
import java.util.Objects;

public class MidnightCore extends JavaPlugin {


    @Override
    public void onEnable() {

        String apiVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".",",").split(",")[3];

        Adapter adapter;
        try {
            adapter = Adapters.findAdapter(apiVersion, this);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("Unable to find version adapter! Some features will not work!", ex);
            adapter = new GenericAdapter(this);
        }

        try {
            if(!adapter.initialize()) {
                adapter = new GenericAdapter(this);
                adapter.initialize();
            }
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("Unable to enable version adapter! Plugin will be disabled!", ex);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(YamlCodec.fileCodec());
        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());
        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(BinaryCodec.fileCodec());

        Adapter.INSTANCE.set(adapter);
        GameVersion.CURRENT_VERSION.set(adapter.getGameVersion());

        Server.RUNNING_SERVER.set(new SpigotServer());

        ItemStack.FACTORY.set(SpigotItem::new);
        InventoryGUI.FACTORY.set(SpigotInventoryGUI::new);
        CustomScoreboard.FACTORY.set(SpigotScoreboard::new);

        Objects.requireNonNull(getCommand("mcoretest")).setExecutor(new TestCommand());

        ConfigSection defaults = new ConfigSection();
        try {
            defaults = JSONCodec.loadConfig(MidnightCore.class.getResourceAsStream("/en_us.json")).asSection();
        } catch (IOException ex) {
            MidnightCoreAPI.LOGGER.error("Unable to load default lang entries from jar resource! " + ex.getMessage());
        }

        MidnightCoreServer.registerPlaceholders(PlaceholderManager.INSTANCE);
        MidnightCoreServer.INSTANCE.set(new MidnightCoreServer(Server.RUNNING_SERVER.get(), LangRegistry.fromConfig(defaults, PlaceholderManager.INSTANCE)));
    }

    @Override
    public void onDisable() {

        Server server = Server.RUNNING_SERVER.getOrNull();
        if(server != null) {
            server.shutdownEvent().invoke(server);
        }
    }


}
