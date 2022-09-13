package org.wallentines.midnightcore.spigot;

import org.wallentines.midnightcore.spigot.adapter.Adapters;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.midnightcore.spigot.command.TestCommand;

public class MidnightCore extends JavaPlugin {
    @Override
    public void onLoad() {

        MidnightCorePlugin.onLoad(this);

    }

    @Override
    public void onEnable() {

        // Adapter
        Adapters.findAdapter();

        MidnightCorePlugin.onEnable(getDataFolder(), getServer());

        getCommand("mcoretest").setExecutor(new TestCommand());

    }

}