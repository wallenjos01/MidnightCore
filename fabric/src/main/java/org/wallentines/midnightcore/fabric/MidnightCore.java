package org.wallentines.midnightcore.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.MidnightCoreImpl;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightcore.fabric.command.ExecuteAugment;
import org.wallentines.midnightcore.fabric.command.MainCommand;
import org.wallentines.midnightcore.fabric.command.TestCommand;
import org.wallentines.midnightcore.fabric.event.MidnightCoreModulesLoadedEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStartEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightcore.fabric.item.FabricInventoryGUI;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.level.EmptyGenerator;
import org.wallentines.midnightcore.fabric.module.extension.FabricServerExtensionModule;
import org.wallentines.midnightcore.fabric.module.messaging.FabricMessagingModule;
import org.wallentines.midnightcore.fabric.module.savepoint.FabricSavepointModule;
import org.wallentines.midnightcore.fabric.module.session.FabricSessionModule;
import org.wallentines.midnightcore.fabric.module.skin.FabricSkinModule;
import org.wallentines.midnightcore.fabric.module.vanish.FabricVanishModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayerManager;
import org.wallentines.midnightcore.fabric.text.FabricScoreboard;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class MidnightCore implements ModInitializer {

    // Keep track of the first created MidnightCore instance. Right now, this is used to access the lang provider
    // and currently running server. In the future, this should not be necessary
    private static MidnightCore INSTANCE;

    // Keep track of the currently running server. In the future, a more consistent way to access the server should
    // be implemented and this should be removed. This will likely come in the form of a "MServer" API, which exposes
    // common server functions to all platforms.
    private MinecraftServer server;

    // This should be moved to the common level, as it should be accessible by other platforms
    private LangProvider provider;

    public MidnightCore() {
        if(INSTANCE == null) INSTANCE = this;
    }

    @Override
    public void onInitialize() {

        // Determine the data folder
        Path dataFolder = Paths.get("config/MidnightCore");

        // Register all the things
        Constants.registerDefaults(JsonConfigProvider.INSTANCE);
        Constants.CONFIG_DEFAULTS.set("vanilla_permissions", true);
        Constants.CONFIG_DEFAULTS.set("register_main_command", true);
        Constants.CONFIG_DEFAULTS.set("register_test_command", false);
        Constants.CONFIG_DEFAULTS.set("augment_execute_command", true);

        // Determine Minecraft version
        Optional<ModContainer> cont = FabricLoader.getInstance().getModContainer("minecraft");
        String versionStr = cont.isPresent() ? cont.get().getMetadata().getVersion().getFriendlyString() : "1.14";
        Version version = Version.SERIALIZER.deserialize(versionStr);

        // Empty World Generator. Useful for some mods using the Dynamic Level system for quick creation of empty levels,
        // or importing of levels that should not generate new chunks
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, new ResourceLocation(Constants.DEFAULT_NAMESPACE, "empty"), EmptyGenerator.CODEC);

        // Create the API
        MidnightCoreImpl api = new MidnightCoreImpl(
                dataFolder,
                version,
                FabricItem::new,
                new FabricPlayerManager(),
                FabricInventoryGUI::new,
                FabricScoreboard::new,
                (str, b) -> {
                    CommandSourceStack sta = b ? server.createCommandSourceStack().withSuppressedOutput() : server.createCommandSourceStack();
                    server.getCommands().performPrefixedCommand(sta, str);
                },
                run -> server.submit(run)
        );
        MidnightCoreAPI.getLogger().info("Starting MidnightCore with Game Version " + version.toString());

        // Register default fabric modules
        Registries.MODULE_REGISTRY.register(FabricSkinModule.ID, FabricSkinModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricSavepointModule.ID, FabricSavepointModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricVanishModule.ID, FabricVanishModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricMessagingModule.ID, FabricMessagingModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricSessionModule.ID, FabricSessionModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricServerExtensionModule.ID, FabricServerExtensionModule.MODULE_INFO);

        // Register Requirements which cannot be implemented at the common level
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(Constants.DEFAULT_NAMESPACE, "item"), FabricItem.ITEM_REQUIREMENT);

        // Find all mods that request to be loaded now
        List<ModInitializer> inits = FabricLoader.getInstance().getEntrypoints(Constants.DEFAULT_NAMESPACE, ModInitializer.class);
        inits.forEach(ModInitializer::onInitialize);

        // Find all mods that request to be loaded with the current Minecraft version only
        List<ModInitializer> verInits = FabricLoader.getInstance().getEntrypoints(Constants.DEFAULT_NAMESPACE + ":" + versionStr, ModInitializer.class);
        verInits.forEach(ModInitializer::onInitialize);

        // Create a lang provider for internal use
        Path lang = dataFolder.resolve("lang");
        FileUtil.tryCreateDirectory(lang);

        provider = new LangProvider(
                lang,
                JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/midnightcore/lang/en_us.json")),
                api.getServerLocale()
        );

        Event.register(ServerStartEvent.class, this, 10, event -> {

            server = event.getServer();

            // Load modules each time a server starts, so integrated servers load properly
            api.loadModules();
            MidnightCoreModulesLoadedEvent.invoke(new MidnightCoreModulesLoadedEvent(api, api.getModuleManager(), server));
        });

        Event.register(ServerStopEvent.class, this, 90, event -> {

            // Unload server modules when the server shuts down
            api.unloadModules();
            server = null;
        });

        Event.register(CommandLoadEvent.class, this, event -> {

            // Register commands if enabled
            if(api.getConfig().getBoolean("register_main_command")) MainCommand.register(event.getDispatcher());
            if(api.getConfig().getBoolean("register_test_command")) TestCommand.register(event.getDispatcher());

            // Adds a new "requirement" argument to the execute command, so requirements registered by this or other
            // mods can be easily used in command blocks
            if(api.getConfig().getBoolean("augment_execute_command")) ExecuteAugment.register(event.getDispatcher());
        });

    }

    public MinecraftServer getServer() {

        return server;
    }

    public LangProvider getLangProvider() {
        return provider;
    }

    public static MidnightCore getInstance() {
        return INSTANCE;
    }
}