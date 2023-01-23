package org.wallentines.midnightcore.api.module.extension;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

@SuppressWarnings("unused")
public interface ClientExtensionModule extends Module<MidnightCoreAPI> {

    <T extends ClientExtension> T getExtension(Class<T> clazz);

    Collection<Identifier> getLoadedExtensionIds();

}
