package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpigotServer implements Server, Listener {

    private final ModuleManager<Server, ServerModule> moduleManager = new ModuleManager<>();
    private final HandlerList<Server> tickEvent = new HandlerList<>();
    private final HandlerList<Server> shutdownEvent = new HandlerList<>();

    public SpigotServer() {
        Adapter.INSTANCE.get().addTickListener(() -> tickEvent.invoke(this));
    }

    @Override
    public Player getPlayer(UUID uuid) {

        org.bukkit.entity.Player internal = Bukkit.getPlayer(uuid);
        if(internal == null) return null;

        return new SpigotPlayer(this, internal);
    }

    @Override
    public Player findPlayer(String name) {

        org.bukkit.entity.Player internal = Bukkit.getPlayer(name);
        if(internal == null) return null;

        return new SpigotPlayer(this, internal);
    }

    @Override
    public Collection<Player> getPlayers() {
        return Bukkit.getOnlinePlayers().stream().map(pl -> (Player) new SpigotPlayer(this, pl)).collect(Collectors.toList());
    }

    @Override
    public void runCommand(String command, boolean quiet) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public boolean isDedicatedServer() {
        return true;
    }

    @Override
    public ModuleManager<Server, ServerModule> getModuleManager() {
        return moduleManager;
    }

    @Override
    public Path getConfigDirectory() {
        return MidnightCoreAPI.GLOBAL_CONFIG_DIRECTORY.get();
    }

    @Override
    public HandlerList<Server> tickEvent() {
        return tickEvent;
    }

    @Override
    public HandlerList<Server> shutdownEvent() {
        return shutdownEvent;
    }

    @Override
    public void submit(Runnable runnable) {
        Adapter.INSTANCE.get().runOnServer(runnable);
    }
}
