package org.wallentines.midnightcore.api.module.data;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.module.Module;

import java.nio.file.Path;

public interface DataModule extends Module<MidnightCoreAPI> {

    DataProvider getGlobalProvider();

    DataProvider getOrCreateProvider(Path folderPath);

}
