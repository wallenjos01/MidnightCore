package org.wallentines.mcore.sql;

import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.HashMap;
import java.util.Map;

public class DatabasePreset {

    public final UnresolvedComponent driver;
    public final UnresolvedComponent url;
    public final UnresolvedComponent database;
    public final UnresolvedComponent username;
    public final UnresolvedComponent password;
    public final UnresolvedComponent tablePrefix;
    public final ConfigSection parameters;


    public DatabasePreset(UnresolvedComponent driver, UnresolvedComponent url, UnresolvedComponent database, UnresolvedComponent username, UnresolvedComponent password, UnresolvedComponent tablePrefix, ConfigSection parameters) {
        this.driver = driver;
        this.url = url;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
        this.parameters = parameters;
    }

    public SerializeResult<ConnectionSpec> finalize(ConfigSection section) {

        Map<String, UnresolvedComponent> comps = new HashMap<>();
        comps.put("driver", this.driver);
        comps.put("url", this.url);
        comps.put("database", this.database);
        comps.put("username", this.username);
        comps.put("password", this.password);
        comps.put("table_prefix", this.tablePrefix);

        Map<String, String> values = new HashMap<>();
        PlaceholderContext ctx = new PlaceholderContext();

        for(String s : comps.keySet()) {
            if(section.hasString(s)) {
                SerializeResult<UnresolvedComponent> res = UnresolvedComponent.parse(section.getString(s), false);
                if(!res.isComplete()) {
                    return SerializeResult.failure(res.getError());
                }
                comps.put(s, res.getOrThrow());
            }
            UnresolvedComponent uc = comps.get(s);
            if(uc != null) {
                values.put(s, uc.resolveFlat(ctx));
            }
        }

        if (values.get("driver") == null || values.get("url") == null) {
            return SerializeResult.failure("Unable to finalize database connection spec! Must have at least driver and url!");
        }

        ConfigSection params = section.getOrCreateSection("params");
        params.fill(parameters);

        return SerializeResult.success(new ConnectionSpec(values.get("driver"), values.get("url"), values.get("database"), values.get("username"), values.get("password"), values.get("table_prefix"), params));
    }


    public static final Serializer<DatabasePreset> SERIALIZER = ObjectSerializer.create(
            UnresolvedComponent.SERIALIZER.<DatabasePreset>entry("driver", dp -> dp.driver).optional(),
            UnresolvedComponent.SERIALIZER.<DatabasePreset>entry("url", dp -> dp.url).optional(),
            UnresolvedComponent.SERIALIZER.<DatabasePreset>entry("database", dp -> dp.database).optional(),
            UnresolvedComponent.SERIALIZER.<DatabasePreset>entry("username", dp -> dp.username).optional(),
            UnresolvedComponent.SERIALIZER.<DatabasePreset>entry("password", dp -> dp.password).optional(),
            UnresolvedComponent.SERIALIZER.<DatabasePreset>entry("table_prefix", dp -> dp.tablePrefix).optional(),
            ConfigSection.SERIALIZER.<DatabasePreset>entry("params", dp -> dp.parameters).orElse(new ConfigSection()),
            DatabasePreset::new
    );

}
