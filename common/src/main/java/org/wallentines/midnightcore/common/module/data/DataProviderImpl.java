package org.wallentines.midnightcore.common.module.data;

import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.module.data.DataProvider;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.mdcfg.ConfigSection;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

@Deprecated
public class DataProviderImpl implements DataProvider {

    private final File dataFolder;
    private final HashMap<String, FileConfig> loaded = new HashMap<>();

    public DataProviderImpl(Path dataFolderPath) {
        this.dataFolder = FileUtil.tryCreateDirectory(dataFolderPath);

        if(this.dataFolder == null) throw new IllegalStateException("Unable to create data folder!");
    }

    private FileConfig findOrCreate(String id) {
        return loaded.computeIfAbsent(id, k -> FileConfig.findOrCreate(id, dataFolder));
    }

    @Override
    public ConfigSection getData(String id) {
        return findOrCreate(id).getRoot();
    }

    @Override
    public void setData(String id, ConfigSection sec) {
        findOrCreate(id).setRoot(sec);
    }

    @Override
    public void saveData(String id) {
        if(loaded.containsKey(id)) {
            loaded.get(id).save();
            loaded.remove(id);
        }
    }

    @Override
    public void clearData(String id) {
        loaded.remove(id);
    }

    @Override
    public ConfigSection reloadData(String id) {
        clearData(id);
        return getData(id);
    }

    @Override
    public void saveAll() {
        for(String s : new ArrayList<>(loaded.keySet())) {
            saveData(s);
        }
    }
}
