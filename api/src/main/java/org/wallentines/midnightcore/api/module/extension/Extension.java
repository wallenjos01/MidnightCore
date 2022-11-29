package org.wallentines.midnightcore.api.module.extension;

import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.Module;

public interface Extension extends Module<ExtensionModule> {

    Version getVersion();

}
