package org.wallentines.midnightcore.spigot;

import org.wallentines.midnightcore.spigot.adapter.Adapters;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onLoad() {

        MidnightCore.onLoad(this);

    }

    @Override
    public void onEnable() {

        // Adapter
        Adapters.findAdapter();
        MidnightCore.onEnable(getDataFolder(), getServer(), this);

    }

}