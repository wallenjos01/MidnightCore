package org.wallentines.mcore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.SharedConstants;
import org.wallentines.mcore.extension.FabricServerExtensionModule;
import org.wallentines.mcore.extension.ServerExtensionModule;
import org.wallentines.mcore.item.FabricInventoryGUI;
import org.wallentines.mcore.item.InventoryGUI;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.lang.PlaceholderManager;
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
import org.wallentines.mdcfg.codec.JSONCodec;

public class MidnightCore implements ModInitializer {


    @Override
    public void onInitialize() {

        // Default Modules
        ServerModule.REGISTRY.register(SkinModule.ID, FabricSkinModule.MODULE_INFO);
        ServerModule.REGISTRY.register(SavepointModule.ID, FabricSavepoint.MODULE_INFO);
        ServerModule.REGISTRY.register(SessionModule.ID, FabricSessionModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerMessagingModule.ID, FabricServerMessagingModule.MODULE_INFO);
        ServerModule.REGISTRY.register(ServerExtensionModule.ID, FabricServerExtensionModule.MODULE_INFO);

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TestCommand.register(dispatcher));
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
        Player.registerPlaceholders(PlaceholderManager.INSTANCE);

        // Lifecycle
        ServerLifecycleEvents.SERVER_STARTING.register(Server.RUNNING_SERVER::set);
        ServerLifecycleEvents.SERVER_STARTED.register(Server.START_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPING.register(Server.STOP_EVENT::invoke);
        ServerLifecycleEvents.SERVER_STOPPED.register(srv -> Server.RUNNING_SERVER.reset());


    }
}
