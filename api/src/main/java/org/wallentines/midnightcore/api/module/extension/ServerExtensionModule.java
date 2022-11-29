package org.wallentines.midnightcore.api.module.extension;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.registry.Identifier;

public interface ServerExtensionModule extends ExtensionModule {

    boolean playerHasExtension(MPlayer player, Identifier id);

    Version getExtensionVersion(MPlayer player, Identifier id);

}
