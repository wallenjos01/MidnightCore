package org.wallentines.midnightcore.api.player;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class DataProvider {

    private final File dataFolder;
    private final HashMap<String, FileConfig> loaded = new HashMap<>();

    public DataProvider(File dataFolder) {

        if (!dataFolder.isDirectory())
            throw new IllegalArgumentException("Attempt to create a data provider with non-folder " + dataFolder.getPath() + "!");

        this.dataFolder = dataFolder;
    }

    private FileConfig findOrCreate(String id) {
        return loaded.computeIfAbsent(id, k -> FileConfig.findOrCreate(id, dataFolder));
    }

    public ConfigSection getData(String id) {
        return findOrCreate(id).getRoot();
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
