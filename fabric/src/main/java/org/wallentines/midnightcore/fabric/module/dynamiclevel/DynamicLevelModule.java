package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class DynamicLevelModule implements Module<MidnightCoreAPI> {

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        return true;
    }
    public DynamicLevelStorage createLevelStorage(Path path, Path backupPath) {
        return DynamicLevelStorage.create(this, path, backupPath);
    }
    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "dynamic_level");
    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(DynamicLevelModule::new, ID, new ConfigSection());

}