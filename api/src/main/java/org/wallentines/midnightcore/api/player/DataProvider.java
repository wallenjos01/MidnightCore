package org.wallentines.midnightcore.api.player;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.midnightcore.api.FileConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class DataProvider {

    private final File dataFolder;
    private final HashMap<String, FileWrapper<ConfigObject>> loaded = new HashMap<>();

    public DataProvider(File dataFolder) {

        if (!dataFolder.isDirectory())
            throw new IllegalArgumentException("Attempt to create a data provider with non-folder " + dataFolder.getPath() + "!");

        this.dataFolder = dataFolder;
    }

    private FileWrapper<ConfigObject> findOrCreate(String id) {
        return loaded.computeIfAbsent(id, k -> FileConfig.findOrCreate(id, dataFolder, new ConfigSection()));
    }

    public ConfigSection getData(String id) {
        return findOrCreate(id).getRoot().asSection();
    }

    public void setData(String id, ConfigSection sec) {
        findOrCreate(id).setRoot(sec);
    }

    public void saveData(String id) {
        if(loaded.containsKey(id)) {
            loaded.get(id).save();
            loaded.remove(id);
        }
    }

    public void clearData(String id) {
        loaded.remove(id);
    }

    public ConfigSection reloadData(String id) {
        clearData(id);
        return getData(id);
    }

    public void saveAll() {
        for(String s : new ArrayList<>(loaded.keySet())) {
            saveData(s);
        }
    }

    public ConfigSection getData(MPlayer player) { return getData(player.getUUID().toString()); }

    public void setData(MPlayer player, ConfigSection section) { setData(player.getUUID().toString(), section); }

    public void saveData(MPlayer player) { saveData(player.getUUID().toString()); }

    public void clearData(MPlayer player) { clearData(player.getUUID().toString()); }

    public ConfigSection reloadData(MPlayer player) { return reloadData(player.getUUID().toString()); }

}
