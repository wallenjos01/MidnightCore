package org.wallentines.midnightcore.fabric.server;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.server.AbstractServer;
import org.wallentines.midnightcore.fabric.item.FabricInventoryGUI;
import org.wallentines.midnightcore.fabric.item.FabricItem;
import org.wallentines.midnightcore.fabric.player.FabricPlayerManager;
import org.wallentines.midnightcore.fabric.text.FabricScoreboard;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricServer extends AbstractServer {

    private final MinecraftServer internal;
    private final FabricPlayerManager playerManager;

    public FabricServer(MinecraftServer server) {
        this.internal = server;
        this.playerManager = new FabricPlayerManager();
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

    @Override
    public InventoryGUI createInventoryGUI(MComponent title) {

        return new FabricInventoryGUI(title);
    }

    @Override
    public CustomScoreboard createScoreboard(String id, MComponent title) {

        return new FabricScoreboard(id, title);
    }

    @Override
    public MItemStack createItemStack(Identifier typeId, int count, ConfigSection tag) {

        return new FabricItem(typeId, count, tag);
    }

    public MinecraftServer getInternal() {
        return internal;
    }
}
