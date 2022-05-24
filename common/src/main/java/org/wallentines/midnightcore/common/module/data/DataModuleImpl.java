package org.wallentines.midnightcore.common.module.data;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.data.DataModule;
import org.wallentines.midnightcore.api.module.data.DataProvider;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.util.HashMap;

public class DataModuleImpl implements DataModule {

    private final HashMap<Path, DataProviderImpl> providers = new HashMap<>();
    private DataProviderImpl global;

    private MidnightCoreAPI api;

    @Override
    public DataProvider getGlobalProvider() {
        return global;
    }

    @Override
    public DataProvider getOrCreateProvider(Path folderPath) {
        return providers.computeIfAbsent(folderPath, k -> new DataProviderImpl(folderPath));
    }

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        this.api = data;
        reload(section);

        return true;
    }

    @Override
    public void reload(ConfigSection config) {

        String globalFolder = config.getString(CONFIG_GLOBAL_FOLDER);

        Path folderPath = api.getDataFolder().toPath().resolve(globalFolder);
        global = new DataProviderImpl(folderPath);

    }

    private static final String CONFIG_GLOBAL_FOLDER = "global_folder_name";
    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "data");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(DataModuleImpl::new, ID, new ConfigSection().with(CONFIG_GLOBAL_FOLDER, "data"));
}
