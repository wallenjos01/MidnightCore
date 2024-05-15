package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.Adapters;
import org.wallentines.mcore.adapter.GenericAdapter;
import org.wallentines.mcore.extension.ServerExtensionModule;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.messenger.ServerMessengerModule;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.mcore.pluginmsg.SpigotPluginMessageModule;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.savepoint.SpigotSavepointModule;
import org.wallentines.mcore.session.SessionModule;
import org.wallentines.mcore.session.SpigotSessionModule;
import org.wallentines.mcore.skin.SkinModule;
import org.wallentines.mcore.skin.SpigotSkinModule;
import org.wallentines.mcore.sql.SQLModule;
import org.wallentines.mcore.sql.SpigotSQLModule;
import org.wallentines.mcore.util.CommandUtil;
import org.wallentines.mcore.util.VersionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.BinaryCodec;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.io.IOException;
import java.nio.file.Paths;

public class MidnightCore extends JavaPlugin {



    private Adapter initAdapter() {

        // Find Adapter
        GameVersion version = VersionUtil.findVersion();
        if(version == null) return new GenericAdapter(this);

        Adapter adapter = Adapters.findAdapter(version, this);
        if (adapter == null) {
            MidnightCoreAPI.LOGGER.warn("MidnightCore does not fully support this version! Certain functions may not work correctly.");
            adapter = new GenericAdapter(this);
        }

        // Initialize Adapter
        try {
            if(adapter.initialize()) {
                return adapter;
            }
        } catch (Exception ex) {
            // Ignore
        }

        adapter = new GenericAdapter(this);
        if(!adapter.initialize()) {
            throw new IllegalStateException("Failed to enable generic adapter!");
        }
        return adapter;

    }


    @Override
    public void onEnable() {

        Adapter adapter;
        try {
            adapter = initAdapter();
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("Unable to enable version adapter! Plugin will be disabled!", ex);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Adapter and version
        Adapter.INSTANCE.set(adapter);
        GameVersion.CURRENT_VERSION.set(adapter.getGameVersion());

        // Create server
        SpigotServer server = new SpigotServer(this);

        Server.RUNNING_SERVER.resetEvent.register(this, ev -> {
            MidnightCoreAPI.LOGGER.warn("Running server was reset!");
            Thread.dumpStack();
        });

        // Load Modules
        server.loadModules(ServerModule.REGISTRY);

        Server.RUNNING_SERVER.set(server);

        // MidnightCore
        ConfigSection defaults = new ConfigSection();
        try {
            defaults = JSONCodec.loadConfig(MidnightCore.class.getResourceAsStream("/en_us.json")).asSection();
        } catch (IOException ex) {
            MidnightCoreAPI.LOGGER.error("Unable to load default lang entries from jar resource! " + ex.getMessage());
        }

        MidnightCoreServer.INSTANCE.set(new MidnightCoreServer(Server.RUNNING_SERVER.get(), LangRegistry.fromConfig(defaults, PlaceholderManager.INSTANCE)));

        // Commands
        CommandUtil.registerCommand(this, new MainCommandExecutor());

        if(MidnightCoreServer.INSTANCE.get().registerTestCommand()) {
            CommandUtil.registerCommand(this, new TestCommandExecutor());
        }
    }

    @Override
    public void onDisable() {
        Server server = Server.RUNNING_SERVER.getOrNull();
        if(server != null) {
            server.getModuleManager().unloadAll();
            server.shutdownEvent().invoke(server);
        } else {
            MidnightCoreAPI.LOGGER.warn("Server was null!");
        }
    }

    static {

        // Codecs
        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(YamlCodec.fileCodec());
        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());
        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(BinaryCodec.fileCodec());

        MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.set(Paths.get("plugins"));

        // Factories
        ItemStack.FACTORY.set(new SpigotItem.Factory());
        SingleInventoryGUI.FACTORY.set(SpigotInventoryGUI::new);
        CustomScoreboard.FACTORY.set(SpigotScoreboard::new);

        // Placeholders
        MidnightCoreServer.registerPlaceholders(PlaceholderManager.INSTANCE);

        // Default Modules
        ServerModule.REGISTRY.register(SkinModule.ID, SpigotSkinModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerPluginMessageModule.ID, SpigotPluginMessageModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerExtensionModule.ID, ServerExtensionModule.MODULE_INFO);
        ServerModule.REGISTRY.register(SavepointModule.ID, SpigotSavepointModule.MODULE_INFO);
        ServerModule.REGISTRY.register(SessionModule.ID, SpigotSessionModule.MODULE_INFO);
        ServerModule.REGISTRY.register(SQLModule.ID, SpigotSQLModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerMessengerModule.ID, ServerMessengerModule.ALWAYS_REGISTER);

        PlaceholderManager.INSTANCE.registerSupplier("midnightcore_version", PlaceholderSupplier.inline(ctx -> getPlugin(MidnightCore.class).getDescription().getVersion()));

    }


}
