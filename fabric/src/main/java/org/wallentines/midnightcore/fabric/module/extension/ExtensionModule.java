package org.wallentines.midnightcore.fabric.module.extension;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.util.Collection;

public interface ExtensionModule extends Module<MidnightCoreAPI> {

    <T extends Extension> T getExtension(Class<T> clazz);

    Collection<Identifier> getLoadedExtensionIds();

    boolean isClient();

    Registry<ModuleInfo<ExtensionModule, Extension>> SUPPORTED_EXTENSIONS = new Registry<>();
    Identifier SUPPORTED_EXTENSION_PACKET = new Identifier(Constants.DEFAULT_NAMESPACE, "supported_extensions");

    Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "extension");
}
