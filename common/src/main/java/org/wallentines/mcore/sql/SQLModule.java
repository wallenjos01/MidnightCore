package org.wallentines.mcore.sql;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.DriverRepository;
import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class SQLModule {

    protected DriverRepository repo;
    protected final StringRegistry<DatabasePreset> presets = new StringRegistry<>();

    protected void init(ConfigSection config) {

        presets.clear();

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

    public SQLConnection connect(ConnectionSpec spec) {
        return repo.getDriver(spec.driver).create(spec.url + "/" + spec.database, spec.username, spec.password, spec.parameters);
    }

    public SQLConnection connect(DatabasePreset preset, ConfigSection config) {
        ConnectionSpec spec = preset.finalize(config).getOrThrow();
        return connect(spec);
    }

    public SQLConnection connect(ConfigSection config) {
        String preset = config.getOrDefault("preset", "default");
        return connect(getPreset(preset), config);
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "sql");

    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("enabled", false)
            .with("repo_type", "maven")
            .with("repo_dir", "sql_drivers")
            .with("additional_drivers", new ConfigSection());

}
