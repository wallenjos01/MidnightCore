package org.wallentines.midnightcore.spigot.server;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.common.server.AbstractServer;
import org.wallentines.midnightcore.spigot.player.SpigotPlayerManager;

public class SpigotServer extends AbstractServer {

    private final Server server;
    private final Plugin plugin;
    private final SpigotPlayerManager playerManager;

    public SpigotServer(MidnightCoreAPI api, Server server, Plugin plugin) {

        super(api);
        
        this.server = server;
        this.plugin = plugin;
        this.playerManager = new SpigotPlayerManager(this);
    }

    @Override
    public void submit(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public boolean isLocalServer() {
        return false;
    }

    @Override
    public boolean isProxy() {
        return false;
    }

    @Override
    public void executeCommand(String command, boolean quiet) {
        server.dispatchCommand(server.getConsoleSender(), command);
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public Server getServer() {
        return server;
    }
}
