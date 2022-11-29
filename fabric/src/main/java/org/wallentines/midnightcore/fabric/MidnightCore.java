package org.wallentines.midnightcore.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
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
import org.wallentines.midnightcore.fabric.level.DynamicLevelModule;
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

    private static MidnightCore INSTANCE;
    private MinecraftServer server;

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

        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(Constants.DEFAULT_NAMESPACE, "empty"), EmptyGenerator.CODEC);

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
        Registries.MODULE_REGISTRY.register(DynamicLevelModule.ID, DynamicLevelModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricVanishModule.ID, FabricVanishModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricMessagingModule.ID, FabricMessagingModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricSessionModule.ID, FabricSessionModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricServerExtensionModule.ID, FabricServerExtensionModule.MODULE_INFO);

        // Register some Requirements too
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(Constants.DEFAULT_NAMESPACE, "item"), FabricItem.ITEM_REQUIREMENT);

        // Find all mods that request to be loaded now
        List<ModInitializer> inits = FabricLoader.getInstance().getEntrypoints(Constants.DEFAULT_NAMESPACE, ModInitializer.class);
        inits.forEach(ModInitializer::onInitialize);

        // Find all mods that request to be loaded with the current Minecraft version only
        List<ModInitializer> verInits = FabricLoader.getInstance().getEntrypoints(Constants.DEFAULT_NAMESPACE + ":" + versionStr, ModInitializer.class);
        verInits.forEach(ModInitializer::onInitialize);

        // Create a lang provider for our use
        Path lang = dataFolder.resolve("lang");
        FileUtil.tryCreateDirectory(lang);

        provider = new LangProvider(
                lang,
                JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/midnightcore/lang/en_us.json")),
                api.getServerLocale()
        );

        // Events
        Event.register(ServerStartEvent.class, this, 10, event -> {

            server = event.getServer();

            api.loadModules();
            MidnightCoreModulesLoadedEvent.invoke(new MidnightCoreModulesLoadedEvent(api, api.getModuleManager()));
        });

        Event.register(CommandLoadEvent.class, this, event -> {
            if(api.getConfig().getBoolean("register_main_command")) MainCommand.register(event.getDispatcher());
            if(api.getConfig().getBoolean("register_test_command")) TestCommand.register(event.getDispatcher());
            if(api.getConfig().getBoolean("augment_execute_command")) ExecuteAugment.register(event.getDispatcher());
        });

        Event.register(ServerStopEvent.class, this, 90, event -> {

            api.unloadModules();
            server = null;
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