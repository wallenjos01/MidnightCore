package org.wallentines.midnightcore.api.module.session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightcore.api.module.savepoint.SavepointModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;
import java.util.function.Function;

public abstract class AbstractSession implements Session {

    protected static final Logger LOGGER = LogManager.getLogger("Session");
    public static final Random RANDOM = new Random();

    private final UUID uid;
    private final SavepointModule spm;
    private final EnumSet<Savepoint.SaveFlag> flags;
    private final String namespace;
    private final Identifier spId;

    private boolean shutdown = false;

    private final Set<MPlayer> players = new HashSet<>();

    private final HandlerList<SessionShutdownEvent> shutdownCallbacks = new HandlerList<>();
    private final HandlerList<SessionPlayerEvent> joinCallbacks = new HandlerList<>();
    private final HandlerList<SessionPlayerEvent> leaveCallbacks = new HandlerList<>();

    public AbstractSession(String namespace) {
        this(namespace, EnumSet.allOf(Savepoint.SaveFlag.class));
    }

    public AbstractSession(String namespace, EnumSet<Savepoint.SaveFlag> savepointFlags) {

        this.namespace = namespace;

        this.uid = UUID.randomUUID();
        this.spm = MidnightCoreAPI.getModule(SavepointModule.class);
        this.flags = savepointFlags;

        this.spId = new Identifier(namespace, uid.toString());
    }

    @Override
    public final UUID getId() {
        return uid;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public final boolean addPlayer(MPlayer player) {

        if(players.contains(player)) return false;
        if(!shouldAddPlayer(player)) return false;

        spm.savePlayer(player, spId, flags);

        players.add(player);

        try {
            onAddPlayer(player);
        } catch (Exception ex) {
            LOGGER.warn("An error occurred while adding a player to a session!");
            ex.printStackTrace();
        }

        joinEvent().invoke(new SessionPlayerEvent(this, player));

        return true;
    }

    @Override
    public void removePlayer(MPlayer player) {

        leaveEvent().invoke(new SessionPlayerEvent(this, player));

        spm.loadPlayer(player, spId);
        spm.removeSavePoint(player, spId);

        players.remove(player);

        try {
            onRemovePlayer(player);
        } catch (Exception ex) {
            LOGGER.warn("An error occurred while removing a player from a session!");
            ex.printStackTrace();
        }

        if (players.isEmpty()) {
            shutdown();
        }

    }

    @Override
    public Collection<MPlayer> getPlayers() {
        return new HashSet<>(players);
    }

    @Override
    public Collection<MPlayer> getPlayers(Function<MPlayer, Boolean> filter) {
        HashSet<MPlayer> out = new HashSet<>();
        for(MPlayer pl : players) {
            if(filter.apply(pl)) out.add(pl);
        }
        return out;
    }

    @Override
    public MPlayer getRandomPlayer() {

        int count = getPlayerCount();
        if(count == 0) throw new IllegalStateException("Attempt to obtain a random player from an empty session!");

        int index = 0;
        int rIndex = RANDOM.nextInt(count);

        for (MPlayer pl : getPlayers()) {
            if (rIndex == index) return pl;
            index++;
        }

        return getPlayers().iterator().next();
    }

    @Override
    public int getPlayerCount() {
        return getPlayers().size();
    }

    @Override
    public boolean contains(MPlayer player) {
        return getPlayers().contains(player);
    }

    @Override
    public void shutdown() {

        if (shutdown) {
            return;
        }
        shutdown = true;

        List<MPlayer> toRemove = new ArrayList<>(players);
        for (MPlayer pl : toRemove) {
            removePlayer(pl);
        }

        shutdownCallbacks.invoke(new SessionShutdownEvent(this));

        try {
            onShutdown();
        } catch (Exception ex) {
            LOGGER.warn("An error occurred while shutting down a session!");
            ex.printStackTrace();
        }
        Event.unregisterAll(this);
    }

    @Override
    public void broadcastMessage(MComponent message) {
        for(MPlayer pl : getPlayers()) {
            pl.sendMessage(message);
        }
    }

    @Override
    public void broadcastMessage(String key, LangProvider provider, Object... data) {

        HashMap<String, MComponent> cache = new HashMap<>();
        for(MPlayer pl : getPlayers()) {
            pl.sendMessage(cache.computeIfAbsent(pl.getLocale(), k -> provider.getMessage(key, pl, data)));
        }
    }

    @Override
    public HandlerList<SessionShutdownEvent> shutdownEvent() {
        return shutdownCallbacks;
    }

    @Override
    public HandlerList<SessionPlayerEvent> joinEvent() {
        return joinCallbacks;
    }

    @Override
    public HandlerList<SessionPlayerEvent> leaveEvent() {
        return leaveCallbacks;
    }
    protected SavepointModule getSavepointModule() {
        return spm;
    }

    protected abstract boolean shouldAddPlayer(MPlayer player);
    protected abstract void onAddPlayer(MPlayer player);
    protected abstract void onRemovePlayer(MPlayer player);
    protected abstract void onShutdown();

    public static void registerPlaceholders(PlaceholderManager registry) {

        registry.getInlinePlaceholders().register("session_player_count", PlaceholderSupplier.create(AbstractSession.class, s -> s.getPlayerCount() + "", () -> "0"));
    }

}
