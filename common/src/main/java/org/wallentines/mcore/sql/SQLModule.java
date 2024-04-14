package org.wallentines.mcore.sql;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.DriverRepository;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class SQLModule {

    protected DriverRepository repo;

    protected void init(ConfigSection config) {

        Map<String, DriverRepository.DriverSpec> drivers = new HashMap<>(DriverRepository.DEFAULT_DRIVERS);
        ConfigSection driverSec = config.getSection("additional_drivers");
        for(String key : driverSec.getKeys()) {
            DriverRepository.DriverSpec spec = driverSec.get(key, DriverRepository.DRIVER_SERIALIZER);
            drivers.put(key, spec);
        }

        String type = config.getOrDefault("type", "folder");
        if(type.equals("folder")) {
            File folder = MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get().resolve("MidnightCore").resolve(config.getOrDefault("folder", "sql_drivers")).toFile();
            if(!folder.isDirectory() && !folder.mkdirs()) {
                throw new IllegalStateException("Unable to create SQL driver directory!");
            }
            this.repo = new DriverRepository.Folder(folder, drivers);
        } else if(type.equals("classpath")) {
            this.repo = new DriverRepository.Classpath(drivers);
        } else {
            throw new IllegalArgumentException("Unknown repository type " + type + "!");
        }
    }

    public DriverRepository getRepository() {
        return repo;
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "sql");


}
