package org.wallentines.midnightcore.fabric.server;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.common.server.AbstractServer;
import org.wallentines.midnightcore.fabric.player.FabricPlayerManager;

public class FabricServer extends AbstractServer {

    private final MinecraftServer internal;
    private final FabricPlayerManager playerManager;

    public FabricServer(MidnightCoreAPI api, MinecraftServer server) {
        super(api);
        this.internal = server;
        this.playerManager = new FabricPlayerManager(this);

        server.addTickable(() -> tickEvent.invoke(new ServerEvent(this)));
    }

    @Override
    public void submit(Runnable runnable) {

        internal.submit(runnable);
    }

    @Override
    public boolean isLocalServer() {

        return !internal.isDedicatedServer();
    }

    @Override
    public boolean isProxy() {
        return false;
    }

    @Override
    public void executeCommand(String command, boolean quiet) {

        CommandSourceStack sta = internal.createCommandSourceStack();
        if(quiet) {
            sta = sta.withSuppressedOutput();
        }

        internal.getCommands().performPrefixedCommand(sta, command);
    }

    @Override
    public PlayerManager getPlayerManager() {

        return playerManager;
    }

    public MinecraftServer getInternal() {
        return internal;
    }
}
