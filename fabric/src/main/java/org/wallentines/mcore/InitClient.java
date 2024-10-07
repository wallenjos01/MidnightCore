package org.wallentines.mcore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.wallentines.mcore.extension.ClientExtensionModule;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.pluginmsg.ClientPluginMessageModule;
import org.wallentines.mcore.pluginmsg.FabricClientPluginMessageModule;
import org.wallentines.mcore.sql.FabricClientSQLModule;

@Environment(EnvType.CLIENT)
public class InitClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModule.REGISTRY.register(ClientPluginMessageModule.ID, FabricClientPluginMessageModule.MODULE_INFO);
        ClientModule.REGISTRY.register(ClientExtensionModule.ID, ClientExtensionModule.MODULE_INFO);
        ClientModule.REGISTRY.register(FabricClientSQLModule.ID, FabricClientSQLModule.MODULE_INFO);
    }

    static {

        Client.registerPlaceholders(PlaceholderManager.INSTANCE);
    }

}
