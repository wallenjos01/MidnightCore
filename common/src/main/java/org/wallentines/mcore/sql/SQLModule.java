package org.wallentines.mcore.sql;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.sql.DriverRepository;
import org.wallentines.mdcfg.sql.PresetRegistry;
import org.wallentines.mdcfg.sql.DatabasePreset;
import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class SQLModule {

    protected PresetRegistry registry;
    protected Executor executor;

    private static ConfigObject applyPlaceholders(ConfigObject obj, PlaceholderContext ctx) {
        if(obj.isString()) {
            return new ConfigPrimitive(UnresolvedComponent.parse(obj.asString()).getOrThrow().resolveFlat(ctx));
        } else if(obj.isSection()) {
            ConfigSection sec = new ConfigSection();
            for(String key : obj.asSection().getKeys()) {
                sec.set(key, applyPlaceholders(obj.asSection().get(key), ctx));
            }
            return sec;
        } else if(obj.isList()) {
            ConfigList list = new ConfigList();
            for(ConfigObject entry : obj.asList().values()) {
                list.add(applyPlaceholders(entry, ctx));
            }
            return list;
        } else {
            return obj;
        }
    }

    protected void init(ConfigSection config, Executor executor) {

        this.registry = PresetRegistry.SERIALIZER.deserialize(ConfigContext.INSTANCE, applyPlaceholders(config, new PlaceholderContext())).getOrThrow();
        this.executor = executor;

    }

    public PresetRegistry getRegistry() {
        return registry;
    }

    public DriverRepository getRepository() {
        return registry.getRepository();
    }

    public DatabasePreset getPreset(String id) {
        return registry.getPreset(id);
    }

    public Collection<String> getPresetIds() {
        return registry.getPresets().keySet();
    }

    public CompletableFuture<SQLConnection> connect(ConfigSection config) {
        return registry.connect(config, executor);
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "sql");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("repository", new ConfigSection()
                    .with("type", "maven")
                    .with("folder", "%global_config_dir%/MidnightCore/sql_drivers"));

}
