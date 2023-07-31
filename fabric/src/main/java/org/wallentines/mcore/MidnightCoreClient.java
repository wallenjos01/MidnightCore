package org.wallentines.mcore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.wallentines.mcore.extension.ServerExtensionModule;
import org.wallentines.mcore.messaging.FabricClientMessagingModule;

@Environment(EnvType.CLIENT)
public class MidnightCoreClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModule.REGISTRY.register(ServerExtensionModule.ID, FabricClientMessagingModule.MODULE_INFO);
    }

}
