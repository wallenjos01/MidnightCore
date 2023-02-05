package org.wallentines.midnightcore.api.module.extension;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

public interface ServerExtension extends Module<ServerExtensionModule> {

    Version getVersion();

    Registry<ModuleInfo<ServerExtensionModule, ServerExtension>> REGISTRY = new Registry<>(MidnightCoreAPI.DEFAULT_NAMESPACE);

}
