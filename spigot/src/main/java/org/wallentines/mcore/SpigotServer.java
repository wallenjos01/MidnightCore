package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public class SpigotServer implements Server, Listener {

    private final ModuleManager<Server, ServerModule> moduleManager = new ModuleManager<>(ServerModule.REGISTRY, this);
    private final HandlerList<Server> tickEvent = new HandlerList<>();
    private final HandlerList<Server> shutdownEvent = new HandlerList<>();
    private final HandlerList<Player> joinEvent = new HandlerList<>();
    private final HandlerList<Player> leaveEvent = new HandlerList<>();

    public SpigotServer(MidnightCore plugin) {
        Adapter.INSTANCE.get().addTickListener(() -> tickEvent.invoke(this));
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
    public Stream<Player> getPlayers() {
        return Bukkit.getOnlinePlayers().stream().map(pl -> (Player) new SpigotPlayer(this, pl));
    }

    @Override
    public int getPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
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
    public HandlerList<Player> joinEvent() {
        return joinEvent;
    }

    @Override
    public HandlerList<Player> leaveEvent() {
        return leaveEvent;
    }

    @Override
    public void submit(Runnable runnable) {
        Adapter.INSTANCE.get().runOnServer(runnable);
    }

    @Override
    public GameVersion getVersion() {
        return Adapter.INSTANCE.get().getGameVersion();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        joinEvent.invoke(new SpigotPlayer(this, event.getPlayer()));
    }
    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        leaveEvent.invoke(new SpigotPlayer(this, event.getPlayer()));
    }

}
