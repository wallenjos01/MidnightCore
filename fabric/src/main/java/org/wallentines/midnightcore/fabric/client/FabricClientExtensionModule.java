package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.wallentines.midnightcore.client.MidnightCoreClient;
import org.wallentines.midnightcore.client.module.ClientModule;
import org.wallentines.midnightcore.client.module.extension.ClientExtension;
import org.wallentines.midnightcore.client.module.extension.ClientExtensionModule;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

@Environment(EnvType.CLIENT)
public class FabricClientExtensionModule implements ClientExtensionModule {

    private final ModuleManager<ClientExtensionModule, ClientExtension> manager = new ModuleManager<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreClient data) {

        manager.loadAll(section.getSection("extensions"), this, ClientExtension.REGISTRY);

        FabricClientMessagingModule mod = MidnightCoreClient.getModule(FabricClientMessagingModule.class);
        mod.registerLoginHandler(ExtensionHelper.SUPPORTED_EXTENSION_PACKET, buf -> ExtensionHelper.createResponse(buf, manager.getLoadedModuleIds(), id -> manager.getModuleById(id).getVersion()));
        mod.registerHandler(ExtensionHelper.SUPPORTED_EXTENSION_PACKET, buf -> ExtensionHelper.createResponse(buf, manager.getLoadedModuleIds(), id -> manager.getModuleById(id).getVersion()));

        return true;
    }

    @Override
    public <T extends ClientExtension> T getExtension(Class<T> clazz) {
        return manager.getModule(clazz);
    }

    @Override
    public Collection<Identifier> getLoadedExtensionIds() {
        return manager.getLoadedModuleIds();
    }


    public static final ModuleInfo<MidnightCoreClient, ClientModule> MODULE_INFO =
        new ModuleInfo<MidnightCoreClient, ClientModule>(
            FabricClientExtensionModule::new,
            ExtensionHelper.ID,
            new ConfigSection().with("extensions", new ConfigSection())
        ).dependsOn(AbstractMessagingModule.ID);
}
