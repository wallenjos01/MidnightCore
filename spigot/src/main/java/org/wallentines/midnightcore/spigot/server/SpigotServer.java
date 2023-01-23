package org.wallentines.midnightcore.spigot.server;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.item.ItemConverter;
import org.wallentines.midnightcore.common.server.AbstractServer;
import org.wallentines.midnightcore.spigot.item.SpigotInventoryGUI;
import org.wallentines.midnightcore.spigot.player.SpigotPlayerManager;
import org.wallentines.midnightcore.spigot.text.SpigotScoreboard;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class SpigotServer extends AbstractServer {

    private final Server server;
    private final Plugin plugin;
    private final ItemConverter itemConverter;
    private final SpigotPlayerManager playerManager;

    public SpigotServer(Server server, Plugin plugin, ItemConverter itemConverter) {

        this.server = server;
        this.plugin = plugin;
        this.itemConverter = itemConverter;
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

    @Override
    public InventoryGUI createInventoryGUI(MComponent title) {
        return new SpigotInventoryGUI(title);
    }

    @Override
    public CustomScoreboard createScoreboard(String id, MComponent title) {
        return new SpigotScoreboard(id, title);
    }

    @Override
    public MItemStack createItemStack(Identifier typeId, int count, ConfigSection tag) {
        return itemConverter.create(typeId, count, tag);
    }

    public Server getServer() {
        return server;
    }
}
