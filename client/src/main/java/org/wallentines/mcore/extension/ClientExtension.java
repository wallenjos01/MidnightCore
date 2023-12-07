package org.wallentines.mcore.extension;

import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.Module;

/**
 * An interface for implementing client side extension behavior
 */
public interface ClientExtension extends Module<ClientExtensionModule> {

    /**
     * Gets the version of the loaded extension
     * @return The version of the extension
     */
    Version getVersion();

}
