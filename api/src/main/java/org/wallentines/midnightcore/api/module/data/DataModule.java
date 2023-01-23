package org.wallentines.midnightcore.api.module.data;

import org.wallentines.midnightcore.api.module.ServerModule;

import java.nio.file.Path;

@SuppressWarnings("unused")
@Deprecated
public interface DataModule extends ServerModule {

    DataProvider getGlobalProvider();

    DataProvider getOrCreateProvider(Path folderPath);

}
