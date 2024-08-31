package org.wallentines.mcore.sql;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.DriverRepository;
import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class SQLModule {

    protected DriverRepository repo;
    protected Executor executor;
    protected final Registry<String, DatabasePreset> presets = Registry.createStringRegistry();

    protected void init(ConfigSection config, Executor executor) {

        presets.clear();
        this.executor = executor;

        Map<String, DriverRepository.DriverSpec> drivers = new HashMap<>(DriverRepository.DEFAULT_DRIVERS);
        ConfigSection driverSec = config.getSection("additional_drivers");
        for(String key : driverSec.getKeys()) {
            DriverRepository.DriverSpec spec = driverSec.get(key, DriverRepository.DRIVER_SERIALIZER);
            drivers.put(key, spec);
        }

        String type = config.getOrDefault("repo_type", "folder");
        File folder = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("MidnightCore").resolve(config.getOrDefault("repo_dir", "sql_drivers")).toFile();
        switch (type) {
            case "maven": {
                if (!folder.isDirectory() && !folder.mkdirs()) {
                    throw new IllegalStateException("Unable to create SQL driver directory!");
                }
                this.repo = new DriverRepository.Maven(folder, drivers);
                break;
            }
            case "folder": {
                if (!folder.isDirectory() && !folder.mkdirs()) {
                    throw new IllegalStateException("Unable to create SQL driver directory!");
                }
                this.repo = new DriverRepository.Folder(folder, drivers);
                break;
            }
            case "classpath":
                this.repo = new DriverRepository.Classpath(drivers);
                break;
            default:
                throw new IllegalArgumentException("Unknown repository type " + type + "!");
        }

        ConfigSection presetSec = config.getSection("presets");
        for(String key : presetSec.getKeys()) {
            presets.register(key, presetSec.get(key, DatabasePreset.SERIALIZER));
        }

    }

    public DriverRepository getRepository() {
        return repo;
    }

    public DatabasePreset getPreset(String id) {
        return presets.get(id);
    }

    public Collection<String> getPresetIds() {
        return presets.getIds();
    }

    public CompletableFuture<SQLConnection> connect(ConnectionSpec spec) {

        String combinedUrl = spec.url;
        if(spec.database != null) {
            combinedUrl += "/" + spec.database;
        }

        final String finalUrl = combinedUrl;
        return CompletableFuture.supplyAsync(() -> repo.getDriver(spec.driver).create(finalUrl, spec.username, spec.password, spec.tablePrefix, spec.parameters));
    }

    public CompletableFuture<SQLConnection> connect(DatabasePreset preset, ConfigSection config) {
        ConnectionSpec spec = preset.finalize(config).getOrThrow();
        return connect(spec);
    }

    public CompletableFuture<SQLConnection> connect(ConfigSection config) {
        String preset = config.getOrDefault("preset", "default");
        return connect(getPreset(preset), config);
    }


    public CompletableFuture<SQLConnection> connect(ConnectionSpec spec, Executor executor) {

        String combinedUrl = spec.url;
        if(spec.database != null) {
            combinedUrl += "/" + spec.database;
        }

        final String finalUrl = combinedUrl;
        return CompletableFuture.supplyAsync(() -> repo.getDriver(spec.driver).create(finalUrl, spec.username, spec.password, spec.tablePrefix, spec.parameters), executor);
    }

    public CompletableFuture<SQLConnection> connect(DatabasePreset preset, ConfigSection config, Executor executor) {
        ConnectionSpec spec = preset.finalize(config).getOrThrow();
        return connect(spec, executor);
    }

    public CompletableFuture<SQLConnection> connect(ConfigSection config, Executor executor) {
        String preset = config.getOrDefault("preset", "default");
        return connect(getPreset(preset), config, executor);
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "sql");

    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("enabled", false)
            .with("repo_type", "maven")
            .with("repo_dir", "sql_drivers")
            .with("additional_drivers", new ConfigSection());

}
