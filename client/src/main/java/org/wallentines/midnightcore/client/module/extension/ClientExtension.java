package org.wallentines.midnightcore.client.module.extension;

import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

public interface ClientExtension extends Module<ClientExtensionModule> {

    Version getVersion();

    Registry<ModuleInfo<ClientExtensionModule, ClientExtension>> REGISTRY = new Registry<>();

}
