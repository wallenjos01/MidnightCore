package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.client.ClientRegistries;
import org.wallentines.midnightcore.client.MidnightCoreClient;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.fabric.event.client.ClientModulesLoadedEvent;
import org.wallentines.midnightlib.event.Event;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        MidnightCoreClient client = new MidnightCoreClient();

        // Default Client Modules
        ClientRegistries.CLIENT_MODULE_REGISTRY.register(AbstractMessagingModule.ID, FabricClientMessagingModule.MODULE_INFO);
        ClientRegistries.CLIENT_MODULE_REGISTRY.register(ExtensionHelper.ID, FabricClientExtensionModule.MODULE_INFO);

        // Allow other client mods to load before modules are loaded
        FabricLoader.getInstance().getEntrypoints(MidnightCoreAPI.DEFAULT_NAMESPACE + "_client", ClientModInitializer.class).forEach(ClientModInitializer::onInitializeClient);

        client.loadModules();

        // Let other mods know that modules are ready
        Event.invoke(new ClientModulesLoadedEvent(client, client.getModuleManager(), Minecraft.getInstance()));

    }


}
