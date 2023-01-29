package org.wallentines.midnightcore.api.module.session;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.event.HandlerList;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public interface SessionModule extends ServerModule {

    void registerSession(Session session);

    Session getSession(UUID id);

    Session getSession(MPlayer player);

    boolean isInSession(MPlayer player);

    void shutdownSession(Session session);

    void shutdownAll();

    void shutdownAll(Predicate<Session> test);

    Collection<Session> getSessions();

    HandlerList<Session.SessionShutdownEvent> shutdownEvent();
    HandlerList<Session.SessionPlayerEvent> joinEvent();
    HandlerList<Session.SessionPlayerEvent> leaveEvent();

}
