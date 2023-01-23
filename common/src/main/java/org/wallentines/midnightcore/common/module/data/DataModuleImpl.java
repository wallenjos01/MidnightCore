package org.wallentines.midnightcore.common.module.data;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.data.DataModule;
import org.wallentines.midnightcore.api.module.data.DataProvider;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.nio.file.Path;
import java.util.HashMap;

public class DataModuleImpl implements DataModule {

    private final HashMap<Path, DataProviderImpl> providers = new HashMap<>();
    private DataProviderImpl global;

    private boolean enabled = false;

    @Override
    public DataProvider getGlobalProvider() {
        if(!enabled) throw Constants.MODULE_DISABLED;
        return global;
    }

    @Override
    public DataProvider getOrCreateProvider(Path folderPath) {
        if(!enabled) throw Constants.MODULE_DISABLED;
        return providers.computeIfAbsent(folderPath, k -> new DataProviderImpl(folderPath));
    }

    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        String globalFolder = section.getString(CONFIG_GLOBAL_FOLDER);
        Path folderPath = getAPI().getDataFolder().toPath().resolve(globalFolder);
        global = new DataProviderImpl(folderPath);

        enabled = true;
        return true;
    }

    @Override
    public void disable() {
        if(!enabled) throw Constants.MODULE_DISABLED;
        global.saveAll();
        enabled = false;
    }

    private static final String CONFIG_GLOBAL_FOLDER = "global_folder_name";
    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "data");
    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<>(DataModuleImpl::new, ID, new ConfigSection().with(CONFIG_GLOBAL_FOLDER, "data"));
}
