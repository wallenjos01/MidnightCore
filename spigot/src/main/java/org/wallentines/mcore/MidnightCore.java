package org.wallentines.mcore;

import org.bukkit.plugin.java.JavaPlugin;

public class MidnightCore extends JavaPlugin {

    @Override
    public void onLoad() {
        MidnightCoreAPI.LOGGER.info("MidnightCore loading");
    }

    @Override
    public void onEnable() {
        MidnightCoreAPI.LOGGER.info("MidnightCore enabled");
    }

}
