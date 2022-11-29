package org.wallentines.midnightcore.api.module.extension;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;

public interface ExtensionModule extends Module<MidnightCoreAPI> {

    <T extends Extension> T getExtension(Class<T> clazz);

    Collection<Identifier> getLoadedExtensionIds();

    boolean isClient();

    Registry<ModuleInfo<ExtensionModule, Extension>> REGISTRY = new Registry<>();

    ConfigSection DEFAULT_CONFIG = new ConfigSection().with("blacklisted_extensions", new ArrayList<>()).with("required_extensions", new ArrayList<>());

}
