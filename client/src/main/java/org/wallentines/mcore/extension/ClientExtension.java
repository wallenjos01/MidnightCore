package org.wallentines.mcore.extension;

import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.Module;

public interface ClientExtension extends Module<ClientExtensionModule> {

    Version getVersion();

}
