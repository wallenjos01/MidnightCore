package org.wallentines.mcore.sql;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

public class ConnectionSpec {

    public final String driver;
    public final String url;
    public final String database;
    public final String username;
    public final String password;
    public final String tablePrefix;
    public final ConfigSection parameters;

    public ConnectionSpec(String driver, String url, String database, String username, String password, String tablePrefix, ConfigSection parameters) {
        this.driver = driver;
        this.url = url;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
        this.parameters = parameters;
    }

    public static final Serializer<ConnectionSpec> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("driver", cs -> cs.driver),
            Serializer.STRING.entry("url", cs -> cs.url),
            Serializer.STRING.entry("database", cs -> cs.database),
            Serializer.STRING.<ConnectionSpec>entry("username", cs -> cs.username).optional(),
            Serializer.STRING.<ConnectionSpec>entry("password", cs -> cs.password).optional(),
            Serializer.STRING.<ConnectionSpec>entry("table_prefix", cs -> cs.tablePrefix).optional(),
            ConfigSection.SERIALIZER.<ConnectionSpec>entry("params", cs -> cs.parameters).orElse(new ConfigSection()),
            ConnectionSpec::new
    );

}
