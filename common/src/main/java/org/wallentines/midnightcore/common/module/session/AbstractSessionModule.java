package org.wallentines.midnightcore.common.module.session;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.module.session.SessionModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public abstract class AbstractSessionModule implements SessionModule {

    private final HashMap<UUID, Session> sessions = new HashMap<>();

    @Override
    public void registerSession(Session session) {

        if(sessions.containsKey(session.getId())) {
            throw new IllegalStateException("Attempt to register session with duplicate ID!");
        }

        sessions.put(session.getId(), session);
        session.addShutdownCallback(() -> sessions.remove(session.getId()));
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
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        AbstractSession.registerPlaceholders(PlaceholderManager.INSTANCE);
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

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "session");
}
