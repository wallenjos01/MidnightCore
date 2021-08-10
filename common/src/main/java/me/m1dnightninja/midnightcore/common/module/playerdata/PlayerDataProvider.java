package me.m1dnightninja.midnightcore.common.module.playerdata;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerDataProvider implements IPlayerDataProvider {

    private final HashMap<UUID, FileConfig> loadedData = new HashMap<>();
    private final File folder;

    PlayerDataProvider(File folder) {
        this.folder = folder;
    }

    @Override
    public ConfigSection getPlayerData(UUID u) {
        return getOrLoadPlayerFile(u).getRoot();
    }

    @Override
    public void setPlayerData(UUID u, ConfigSection sec) {

        getOrLoadPlayerFile(u).setRoot(sec);
    }

    @Override
    public void savePlayerData(UUID u) {

        if(!loadedData.containsKey(u)) return;
        getOrLoadPlayerFile(u).save();

        loadedData.remove(u);
    }

    @Override
    public void clearPlayerData(UUID u) {

        getOrLoadPlayerFile(u).setRoot(new ConfigSection());
    }

    @Override
    public void reloadPlayerData(UUID u) {

        loadedData.put(u, loadOrCreateDataForPlayer(u));
    }

    @Override
    public void saveAllPlayerData() {

        for(UUID u : new ArrayList<>(loadedData.keySet())) {
            savePlayerData(u);
        }
    }

    protected FileConfig loadOrCreateDataForPlayer(UUID u) {
        FileConfig conf = FileConfig.findFile(folder.listFiles(), u.toString());
        if(conf == null) {
            conf = new FileConfig(new File(folder, u + MidnightCoreAPI.getInstance().getDefaultConfigProvider().getFileExtension()));
        }

        return conf;
    }

    protected FileConfig getOrLoadPlayerFile(UUID u) {

        return loadedData.computeIfAbsent(u, this::loadOrCreateDataForPlayer);

    }


}
