package org.wallentines.midnightcore.common.module.session;

import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.module.session.SessionModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractSessionModule implements SessionModule {

    private final HashMap<UUID, Session> sessions = new HashMap<>();

    private final HandlerList<Session.SessionShutdownEvent> shutdownCallbacks = new HandlerList<>();
    private final HandlerList<Session.SessionPlayerEvent> joinCallbacks = new HandlerList<>();
    private final HandlerList<Session.SessionPlayerEvent> leaveCallbacks = new HandlerList<>();

    @Override
    public void registerSession(Session session) {

        if(sessions.containsKey(session.getId())) {
            throw new IllegalStateException("Attempt to register session with duplicate ID!");
        }

        sessions.put(session.getId(), session);
        session.shutdownEvent().register(this, ev -> {
            shutdownEvent().invoke(ev);
            sessions.remove(ev.getSession().getId());
        });
        session.joinEvent().register(this, ev -> joinEvent().invoke(ev));
        session.leaveEvent().register(this, ev -> leaveEvent().invoke(ev));
    }

    @Override
    public Session getSession(UUID id) {
        return sessions.get(id);
    }

    @Override
    public Session getSession(MPlayer player) {

        for(Session s : sessions.values()) {
            if(s.contains(player)) return s;
        }

        return null;
    }

    @Override
    public boolean isInSession(MPlayer player) {

        for(Session s : sessions.values()) {
            if(s.contains(player)) return true;
        }

        return false;
    }

    @Override
    public void shutdownSession(Session session) {

        UUID id = session.getId();

        session.shutdown();
        sessions.remove(id);
    }

    @Override
    public void shutdownAll() {

        List<Session> sess = new ArrayList<>(sessions.values());
        for(Session s : sess) {
            s.shutdown();
        }

        sessions.clear();
    }

    @Override
    public void shutdownAll(Predicate<Session> test) {

        List<Session> sess = new ArrayList<>(sessions.values());
        for(Session s : sess) {
            if(test.test(s)) {
                s.shutdown();
            }
        }
    }

    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        return true;
    }

    @Override
    public Collection<Session> getSessions() {
        return sessions.values();
    }

    protected void tickAll() {

        for(Session sess : sessions.values()) {
            sess.tick();
        }

    }

    @Override
    public HandlerList<Session.SessionShutdownEvent> shutdownEvent() {
        return shutdownCallbacks;
    }

    @Override
    public HandlerList<Session.SessionPlayerEvent> joinEvent() {
        return joinCallbacks;
    }

    @Override
    public HandlerList<Session.SessionPlayerEvent> leaveEvent() {
        return leaveCallbacks;
    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "session");
}
