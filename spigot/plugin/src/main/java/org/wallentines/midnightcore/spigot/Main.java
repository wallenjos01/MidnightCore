package org.wallentines.midnightcore.spigot;

import org.bukkit.command.PluginCommand;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.spigot.adapter.Adapters;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.midnightcore.spigot.command.MainCommand;

public class Main extends JavaPlugin {
    @Override
    public void onLoad() {

        MidnightCore.onLoad(this);

    }

    @Override
    public void onEnable() {

        PluginCommand cmd = getCommand("mcore");
        if(cmd == null) {
            MidnightCoreAPI.getLogger().warn("Missing main command from plugin.yml!");
        } else {
            MainCommand main = new MainCommand();
            cmd.setExecutor(main);
            cmd.setTabCompleter(main);
        }

        // Adapter
        Adapters.findAdapter();

        MidnightCore.onEnable(getDataFolder(), getServer(), this);

    }

    @Override
    public void onDisable() {
        MidnightCore.onDisable();
    }
}