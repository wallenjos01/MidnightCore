package org.wallentines.mcore.sql;

import net.minecraft.Util;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricServerSQLModule extends SQLModule implements ServerModule {
    @Override
    public boolean initialize(ConfigSection config, Server data) {

        init(config, Util.backgroundExecutor());
        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricServerSQLModule::new, ID, DEFAULT_CONFIG.copy()
            .with("presets", new ConfigSection()
                    .with("default", new DatabasePreset(
                            UnresolvedComponent.completed(Component.text("h2")),
                            UnresolvedComponent.parse("%mcore_config_dir%/db").getOrThrow(),
                            null,
                            null,
                            null,
                            null,
                            new ConfigSection()
            ), DatabasePreset.SERIALIZER)));
}
