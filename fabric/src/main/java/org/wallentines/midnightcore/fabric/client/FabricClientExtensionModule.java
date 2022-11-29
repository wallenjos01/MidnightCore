package org.wallentines.midnightcore.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.module.extension.AbstractExtensionModule;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.api.module.extension.Extension;
import org.wallentines.midnightcore.api.module.extension.ExtensionModule;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

@Environment(EnvType.CLIENT)
public class FabricClientExtensionModule extends AbstractExtensionModule {

    private final ModuleManager<ExtensionModule, Extension> manager = new ModuleManager<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        manager.loadAll(section.getSection("extensions"), this, ExtensionModule.REGISTRY);

        FabricClientMessagingModule mod = MidnightCoreClient.CLIENT_MODULES.getModule(FabricClientMessagingModule.class);
        mod.registerLoginHandler(AbstractExtensionModule.SUPPORTED_EXTENSION_PACKET, buf -> createResponse(buf, manager.getLoadedModuleIds(), id -> manager.getModuleById(id).getVersion()));
        mod.registerHandler(AbstractExtensionModule.SUPPORTED_EXTENSION_PACKET, buf -> createResponse(buf, manager.getLoadedModuleIds(), id -> manager.getModuleById(id).getVersion()));

        return true;
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> clazz) {
        return manager.getModule(clazz);
    }

    @Override
    public Collection<Identifier> getLoadedExtensionIds() {
        return manager.getLoadedModuleIds();
    }

    @Override
    public boolean isClient() {
        return true;
    }


    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO =
        new ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>(
            FabricClientExtensionModule::new,
            AbstractExtensionModule.ID,
            new ConfigSection().with("extensions", new ConfigSection())
        ).dependsOn(AbstractMessagingModule.ID);
}
