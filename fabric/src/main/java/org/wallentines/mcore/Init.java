package org.wallentines.mcore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import org.wallentines.mcore.extension.FabricServerExtensionModule;
import org.wallentines.mcore.extension.ServerExtensionModule;
import org.wallentines.mcore.item.FabricInventoryGUI;
import org.wallentines.mcore.item.InventoryGUI;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.LangRegistry;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.messaging.FabricServerMessagingModule;
import org.wallentines.mcore.messaging.ServerMessagingModule;
import org.wallentines.mcore.savepoint.FabricSavepoint;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.session.FabricSessionModule;
import org.wallentines.mcore.session.SessionModule;
import org.wallentines.mcore.skin.FabricSkinModule;
import org.wallentines.mcore.skin.SkinModule;
import org.wallentines.mcore.text.CustomScoreboard;
import org.wallentines.mcore.text.FabricScoreboard;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.midnightlib.types.ResettableSingleton;

import java.io.IOException;

public class Init implements ModInitializer {

    @Override
    public void onInitialize() {

        // Default Modules
        ServerModule.REGISTRY.register(SkinModule.ID, FabricSkinModule.MODULE_INFO);
        ServerModule.REGISTRY.register(SavepointModule.ID, FabricSavepoint.MODULE_INFO);
        ServerModule.REGISTRY.register(SessionModule.ID, FabricSessionModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerMessagingModule.ID, FabricServerMessagingModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerExtensionModule.ID, FabricServerExtensionModule.MODULE_INFO);

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MainCommand.register(dispatcher);
            TestCommand.register(dispatcher);
        });
    }

    static {

        // File Codecs
        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());

        // Version
        GameVersion.CURRENT_VERSION.set(new GameVersion(SharedConstants.getCurrentVersion().getId(), SharedConstants.getProtocolVersion()));

        // Factories
        ItemStack.FACTORY.set(new ItemFactory());
        InventoryGUI.FACTORY.set(FabricInventoryGUI::new);
        CustomScoreboard.FACTORY.set(FabricScoreboard::new);

        // Placeholders
        MidnightCoreServer.registerPlaceholders(PlaceholderManager.INSTANCE);

        PlaceholderManager.INSTANCE.registerSupplier("midnightcore_version", PlaceholderSupplier.inline(ctx ->
                FabricLoader.getInstance().getModContainer("midnightcore")
                        .map(con -> con.getMetadata().getVersion().getFriendlyString())
                        .orElse("Unknown")));

        // Lifecycle
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            Server.RUNNING_SERVER.set(server);

            ConfigSection defaults = new ConfigSection();
            try {
                defaults = JSONCodec.loadConfig(Init.class.getResourceAsStream("/midnightcore/en_us.json")).asSection();
            } catch (IOException ex) {
                MidnightCoreAPI.LOGGER.error("Unable to load default lang entries from jar resource! " + ex.getMessage());
            }
            ((ResettableSingleton<MidnightCoreServer>) MidnightCoreServer.INSTANCE).reset();
            MidnightCoreServer.INSTANCE.set(new MidnightCoreServer(server, LangRegistry.fromConfig(defaults, PlaceholderManager.INSTANCE)));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(Server.START_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPING.register(Server.STOP_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPED.register(srv -> Server.RUNNING_SERVER.reset());


    }
}
