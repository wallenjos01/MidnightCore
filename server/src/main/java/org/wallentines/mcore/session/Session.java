package org.wallentines.mcore.session;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.WrappedPlayer;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;

import java.util.*;
import java.util.stream.Stream;

public abstract class Session {

    protected final UUID uuid;
    protected final Server server;
    protected final String namespace;
    protected final HashSet<WrappedPlayer> players = new HashSet<>();
    public final HandlerList<Session> shutdownEvent = new SingletonHandlerList<>();
    private boolean running = true;

    protected Session(Server server, String namespace) {
        this.uuid = UUID.randomUUID();
        this.server = server;
        this.namespace = namespace;
    }

    public UUID getId() {
        return uuid;
    }

    public String getNamespace() {
        return namespace;
    }

    public Server getServer() {
        return server;
    }

    public Stream<UUID> getPlayerIds() {
        return players.stream().map(WrappedPlayer::getUUID);
    }

    public Stream<Player> getPlayers() {
        return players.stream().map(WrappedPlayer::get);
    }

    public boolean contains(Player player) {
        return players.contains(player.wrap());
    }

    public void broadcastMessage(Component message) {
        getPlayers().forEach(pl -> pl.sendMessage(message));
    }

    public void broadcastMessage(String message) {
        broadcastMessage(Component.text(message));
    }

    public boolean addPlayer(Player player) {

        if(!running || !shouldAddPlayer(player)) return false;

        EnumSet<SavepointModule.SaveFlag> flags = getSavepointFlags();
        if(flags != null && flags.size() > 0) {
            server.getModuleManager().getModule(SavepointModule.class).savePlayer(player, uuid.toString(), flags);
        }

        players.add(player.wrap());

        try {
            onAddPlayer(player);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.warn("An exception occurred while adding a player to a session!");
            ex.printStackTrace();
            removePlayer(player);
            return false;
        }
        return true;
    }

    public void removePlayer(Player player) {

        EnumSet<SavepointModule.SaveFlag> flags = getSavepointFlags();
        if(flags != null && flags.size() > 0) {
            server.getModuleManager().getModule(SavepointModule.class).loadPlayer(player, uuid.toString());
        }

        try {
            onRemovePlayer(player);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.warn("An exception occurred while removing a player from a session!");
            ex.printStackTrace();
        }

        players.remove(player.wrap());

        if(players.isEmpty()) {
            shutdown();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {

        if(!running) return;

        try {
            onShutdown();
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.warn("An exception occurred while shutting down a session!");
            ex.printStackTrace();
        }

        running = false;
        getPlayers().forEach(this::removePlayer);

        shutdownEvent.invoke(this);
    }

    protected abstract boolean shouldAddPlayer(Player player);
    protected abstract EnumSet<SavepointModule.SaveFlag> getSavepointFlags();
    protected abstract void onAddPlayer(Player player);
    protected abstract void onRemovePlayer(Player player);
    protected abstract void onShutdown();

    public interface Factory<T extends Session> {
        T create(SessionModule module, Server server, String namespace);
    }
}
