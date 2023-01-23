package org.wallentines.midnightcore.api.module.extension;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public interface ServerExtensionModule extends ServerModule {

    <T extends ServerExtension> T getExtension(Class<T> clazz);

    Collection<Identifier> getLoadedExtensionIds();

    boolean playerHasExtension(MPlayer player, Identifier id);

    Version getExtensionVersion(MPlayer player, Identifier id);


    ConfigSection DEFAULT_CONFIG = new ConfigSection().with("blacklisted_extensions", new ArrayList<>()).with("required_extensions", new ArrayList<>());

}
