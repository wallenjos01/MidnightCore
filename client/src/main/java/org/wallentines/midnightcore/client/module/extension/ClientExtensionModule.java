package org.wallentines.midnightcore.client.module.extension;

import org.wallentines.midnightcore.client.module.ClientModule;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;

@SuppressWarnings("unused")
public interface ClientExtensionModule extends ClientModule {

    <T extends ClientExtension> T getExtension(Class<T> clazz);

    Collection<Identifier> getLoadedExtensionIds();

}
