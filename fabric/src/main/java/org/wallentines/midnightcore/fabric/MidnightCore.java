package org.wallentines.midnightcore.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.MidnightCoreImpl;
import org.wallentines.midnightcore.api.Registries;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.fabric.command.ExecuteAugment;
import org.wallentines.midnightcore.fabric.command.MainCommand;
import org.wallentines.midnightcore.fabric.command.TestCommand;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStartEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightcore.fabric.item.FabricInventoryGUI;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.server.EmptyGenerator;
import org.wallentines.midnightcore.fabric.module.extension.FabricServerExtensionModule;
import org.wallentines.midnightcore.fabric.module.messaging.FabricMessagingModule;
import org.wallentines.midnightcore.fabric.module.savepoint.FabricSavepointModule;
import org.wallentines.midnightcore.fabric.module.session.FabricSessionModule;
import org.wallentines.midnightcore.fabric.module.skin.FabricSkinModule;
import org.wallentines.midnightcore.fabric.module.vanish.FabricVanishModule;
import org.wallentines.midnightcore.fabric.server.FabricServer;
import org.wallentines.midnightcore.fabric.text.FabricScoreboard;
import org.wallentines.midnightlib.Version;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class MidnightCore implements ModInitializer {

    @Override
    public void onInitialize() {

        Constants.registerDefaults();

        // Determine the data folder
        Path dataFolder = Paths.get("config/MidnightCore");

        // Register all the things
        //Constants.registerDefaults(JsonConfigProvider.INSTANCE);
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
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, new ResourceLocation(MidnightCoreAPI.DEFAULT_NAMESPACE, "empty"), EmptyGenerator.CODEC);

        ConfigSection langDefaults = JSONCodec.minified().decode(ConfigContext.INSTANCE, getClass().getResourceAsStream("/midnightcore/lang/en_us.json")).asSection();

        // Create the API
        MidnightCoreImpl api = new MidnightCoreImpl(
                dataFolder,
                version,
                langDefaults,
                FabricItem::new,
                FabricInventoryGUI::new,
                FabricScoreboard::new
        );

        MidnightCoreAPI.getLogger().info("Starting MidnightCore with Game Version " + version.toString());

        // Register default fabric modules
        Registries.MODULE_REGISTRY.register(FabricSkinModule.ID, FabricSkinModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricSavepointModule.ID, FabricSavepointModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricVanishModule.ID, FabricVanishModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricMessagingModule.ID, FabricMessagingModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(FabricSessionModule.ID, FabricSessionModule.MODULE_INFO);
        Registries.MODULE_REGISTRY.register(ExtensionHelper.ID, FabricServerExtensionModule.MODULE_INFO);

        // Register Requirements which cannot be implemented at the common level
        Registries.REQUIREMENT_REGISTRY.register(new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "item"), FabricItem.ITEM_REQUIREMENT);

        // Find all mods that request to be loaded now
        List<ModInitializer> inits = FabricLoader.getInstance().getEntrypoints(MidnightCoreAPI.DEFAULT_NAMESPACE, ModInitializer.class);
        inits.forEach(ModInitializer::onInitialize);

        // Find all mods that request to be loaded with the current Minecraft version only
        List<ModInitializer> verInits = FabricLoader.getInstance().getEntrypoints(MidnightCoreAPI.DEFAULT_NAMESPACE + ":" + versionStr, ModInitializer.class);
        verInits.forEach(ModInitializer::onInitialize);


        Event.register(ServerStartEvent.class, this, 10, event -> api.setActiveServer(new FabricServer(api, event.getServer())));

        Event.register(ServerStopEvent.class, this, 90, event -> api.setActiveServer(null));

        Event.register(CommandLoadEvent.class, this, event -> {

            // Register commands if enabled
            if(api.getConfig().getBoolean("register_main_command")) MainCommand.register(event.getDispatcher());
            if(api.getConfig().getBoolean("register_test_command")) TestCommand.register(event.getDispatcher());

            // Adds a new "requirement" argument to the execute command, so requirements registered by this or other
            // mods can be easily used in command blocks
            if(api.getConfig().getBoolean("augment_execute_command")) ExecuteAugment.register(event.getDispatcher());
        });

    }
}