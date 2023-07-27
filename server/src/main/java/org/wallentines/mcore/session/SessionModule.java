package org.wallentines.mcore.session;

import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.types.Singleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;


public abstract class SessionModule implements ServerModule {

    protected Singleton<Server> server;

    private final HashMap<UUID, Session> runningSessions = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        this.server.set(data);

        return true;
    }

    public <T extends Session> T createSession(String namespace, Session.Factory<T> factory) {
        T out = factory.create(this, server.get(), namespace);
        runningSessions.put(out.getId(), out);
        out.shutdownEvent.register(this, sess -> runningSessions.remove(sess.getId()));
        return out;
    }

    public Collection<Session> getSessions() {
        return runningSessions.values();
    }

    public Stream<Session> getSessions(Predicate<Session> predicate) {
        return runningSessions.values().stream().filter(predicate);
    }

    public void shutdownAll() {

        List.copyOf(runningSessions.values()).forEach(Session::shutdown);
    }

    public void shutdownAll(Predicate<Session> predicate) {

        runningSessions.values().stream().filter(predicate).toList().forEach(Session::shutdown);
    }

}
