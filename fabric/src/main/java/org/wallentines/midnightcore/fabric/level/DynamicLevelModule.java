package org.wallentines.midnightcore.fabric.level;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;

public class DynamicLevelModule implements Module<MidnightCoreAPI> {

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        return true;
    }
    public DynamicLevelStorage createLevelStorage(Path path, Path backupPath) {
        return DynamicLevelStorage.create(path, backupPath);
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "dynamic_level");
    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(DynamicLevelModule::new, ID, new ConfigSection());

}