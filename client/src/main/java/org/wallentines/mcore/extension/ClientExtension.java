package org.wallentines.mcore.extension;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

/**
 * An interface for implementing client side extension behavior
 */
public interface ClientExtension extends Module<ClientExtensionModule> {

    /**
     * Gets the version of the loaded extension
     * @return The version of the extension
     */
    Version getVersion();

    Registry<Identifier, ModuleInfo<ClientExtensionModule, ClientExtension>> REGISTRY = Registry.create(MidnightCoreAPI.MOD_ID);

}
