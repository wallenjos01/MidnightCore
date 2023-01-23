package org.wallentines.midnightcore.velocity.server;

import com.velocitypowered.api.proxy.ProxyServer;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.server.AbstractServer;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.item.DummyItem;
import org.wallentines.midnightcore.velocity.player.VelocityPlayerManager;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class VelocityServer extends AbstractServer {


    private final ProxyServer server;
    private final MidnightCore plugin;
    private final VelocityPlayerManager playerManager;

    public VelocityServer(ProxyServer server, MidnightCore plugin) {
        this.server = server;
        this.plugin = plugin;

        this.playerManager = new VelocityPlayerManager(this);
        this.playerManager.register();
    }

    @Override
    public void submit(Runnable runnable) {
        server.getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public boolean isLocalServer() {
        return false;
    }

    @Override
    public boolean isProxy() {
        return true;
    }

    @Override
    public void executeCommand(String command, boolean quiet) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public InventoryGUI createInventoryGUI(MComponent title) {
        return null;
    }

    @Override
    public CustomScoreboard createScoreboard(String id, MComponent title) {
        return null;
    }

    @Override
    public MItemStack createItemStack(Identifier typeId, int count, ConfigSection tag) {
        return new DummyItem(typeId, count, tag);
    }

    public ProxyServer getInternal() {
        return server;
    }
}
