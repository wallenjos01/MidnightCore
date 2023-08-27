package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.Adapters;
import org.wallentines.mcore.adapter.GenericAdapter;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.item.SpigotItem;

import java.util.Objects;

public class MidnightCore extends JavaPlugin {


    @Override
    public void onEnable() {

        String apiVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".",",").split(",")[3];

        Adapter adapter;
        try {
            adapter = Adapters.findAdapter(apiVersion, this);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("Unable to find version adapter! Some features will not work!", ex);
            adapter = new GenericAdapter(this);
        }

        try {
            if(!adapter.initialize()) {
                adapter = new GenericAdapter(this);
                adapter.initialize();
            }
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("Unable to enable version adapter! Plugin will be disabled!", ex);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        MidnightCoreAPI.LOGGER.warn("onLoad() called");

        Server.RUNNING_SERVER.set(new SpigotServer());
        Adapter.INSTANCE.set(adapter);
        GameVersion.CURRENT_VERSION.set(adapter.getGameVersion());

        ItemStack.FACTORY.set(SpigotItem::new);

        Objects.requireNonNull(getCommand("mcoretest")).setExecutor(new TestCommand());
        MidnightCoreServer.INSTANCE.set(new MidnightCoreServer(Server.RUNNING_SERVER.get(), null));
    }

    @Override
    public void onDisable() {

        Server server = Server.RUNNING_SERVER.getOrNull();
        if(server != null) {
            server.shutdownEvent().invoke(server);
        }
    }


}
