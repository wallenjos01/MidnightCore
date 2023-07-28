package org.wallentines.mcore.session;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.savepoint.Savepoint;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.util.FileExecutor;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.types.Singleton;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

    public Session getPlayerSession(Player player) {
        for(Session sess : runningSessions.values()) {
            if(sess.contains(player)) return sess;
        }
        return null;
    }

    public void shutdownAll() {

        List.copyOf(runningSessions.values()).forEach(Session::shutdown);
    }

    public void shutdownAll(Predicate<Session> predicate) {

        runningSessions.values().stream().filter(predicate).toList().forEach(Session::shutdown);
    }


    protected void attemptRecovery(Player player) {

        File lookup = server.get().getStorageDirectory().resolve(MidnightCoreAPI.MOD_ID + "-session-recovery-" + player.getUUID().toString() + ".json").toFile();
        new FileExecutor(lookup, (file) -> {
            if (!lookup.isFile()) return;

            SavepointModule module = server.get().getModuleManager().getModule(SavepointModule.class);
            SerializeResult<Savepoint> savepoint = module.getSerializer().deserialize(
                    ConfigContext.INSTANCE,
                    JSONCodec.fileCodec().loadFromFile(ConfigContext.INSTANCE, lookup, StandardCharsets.UTF_8));

            if(savepoint.isComplete()) {
                savepoint.getOrThrow().load(player);
                if(!lookup.delete()) {
                    MidnightCoreAPI.LOGGER.warn("Failed to delete session recovery file " + file.getAbsolutePath() + "! Please delete it manually, or else the session module may attempt to restore it again!");
                }
            } else {
                MidnightCoreAPI.LOGGER.warn("Failed to recover session data for " + player.getUsername() + "!");
            }
        }).start();

    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "session");

}
