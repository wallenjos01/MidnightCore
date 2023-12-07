package org.wallentines.mcore.extension;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

/**
 * An interface for defining server-side extension behavior
 */
public interface ServerExtension extends Module<ServerExtensionModule> {

    /**
     * Returns the version of the extension
     * @return The extension version
     */
    Version getVersion();

    /**
     * A registry which contains the module information for loading server extensions
     */
    Registry<ModuleInfo<ServerExtensionModule, ServerExtension>> REGISTRY = new Registry<>(MidnightCoreAPI.MOD_ID);

}
