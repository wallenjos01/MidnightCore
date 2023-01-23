package org.wallentines.midnightcore.velocity.server;

import com.velocitypowered.api.proxy.ProxyServer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.common.server.AbstractServer;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.player.VelocityPlayerManager;

public class VelocityServer extends AbstractServer {


    private final ProxyServer server;
    private final MidnightCore plugin;
    private final VelocityPlayerManager playerManager;

    public VelocityServer(MidnightCoreAPI api, ProxyServer server, MidnightCore plugin) {

        super(api);

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

    public ProxyServer getInternal() {
        return server;
    }
}
