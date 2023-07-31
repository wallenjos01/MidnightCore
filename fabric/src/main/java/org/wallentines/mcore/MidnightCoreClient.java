package org.wallentines.mcore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.wallentines.mcore.extension.ClientExtensionModule;
import org.wallentines.mcore.messaging.ClientMessagingModule;
import org.wallentines.mcore.messaging.FabricClientMessagingModule;

@Environment(EnvType.CLIENT)
public class MidnightCoreClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModule.REGISTRY.register(ClientMessagingModule.ID, FabricClientMessagingModule.MODULE_INFO);
        ClientModule.REGISTRY.register(ClientExtensionModule.ID, ClientExtensionModule.MODULE_INFO);
    }

}
