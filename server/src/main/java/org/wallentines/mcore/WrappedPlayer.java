package org.wallentines.mcore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;

/**
 * A data type which store a reference to a player, and automatically updates the reference if the player respawns
 * or reconnects.
 */
public class WrappedPlayer {

    private WeakReference<Player> internal;
    private final UUID uid;
    private final Server server;

    public WrappedPlayer(UUID uid, Server server) {
        this.internal = new WeakReference<>(null);
        this.uid = uid;
        this.server = server;
    }

    public WrappedPlayer(@NotNull Player internal) {
        this.internal = new WeakReference<>(internal);
        this.uid = internal.getUUID();
        this.server = internal.getServer();
    }

    /**
     * Gets a reference to the player, renewing it if necessary
     * @return A reference to the stored player, or null if the player has left the server
     */
    @Nullable
    public Player get() {
        Player pl = internal.get();
        if(pl == null || pl.isRemoved()) {
            pl = server.getPlayer(uid);
            internal = new WeakReference<>(pl);
        }
        return pl;
    }

    /**
     * Gets the target Player's UUID
     * @return The target Player's UUID
     */
    public UUID getUUID() {
        return uid;
    }

    /**
     * Gets the server upon which this WrappedPlayer was created
     * @return The server
     */
    public Server getServer() {
        return server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof WrappedPlayer) {
            WrappedPlayer that = (WrappedPlayer) o;
            return server == that.server && Objects.equals(uid, that.uid);
        } else if(o instanceof Player) {
            Player that = (Player) o;
            return server == that.getServer() && Objects.equals(uid, that.getUUID());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, server);
    }
}
