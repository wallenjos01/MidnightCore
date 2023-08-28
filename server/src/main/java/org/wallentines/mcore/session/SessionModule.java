package org.wallentines.mcore.session;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.data.DataManager;
import org.wallentines.mcore.savepoint.Savepoint;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.BinaryCodec;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.types.Singleton;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;


public abstract class SessionModule implements ServerModule {

    protected final Singleton<Server> server = new Singleton<>();
    private final HashMap<UUID, Session> runningSessions = new HashMap<>();
    private DataManager dataManager;

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        this.server.set(data);

        File recoveryFolder = data.getConfigDirectory().resolve("MidnightCore").resolve("recovery").toFile();
        if(!recoveryFolder.isDirectory() && !recoveryFolder.mkdirs()) {
            MidnightCoreAPI.LOGGER.error("Unable to create recovery folder at " + recoveryFolder.getAbsolutePath() + "!");
            return false;
        }

        FileCodecRegistry reg = new FileCodecRegistry();
        reg.registerFileCodec(BinaryCodec.fileCodec());
        this.dataManager = new DataManager(recoveryFolder, reg);

        return true;
    }

    /**
     * Creates and registers a new session with the given factory. Unregistered sessions should just call the Factory's
     * create method directly
     * @param namespace The namespace for the new session
     * @param factory The factory by which to create a new session
     * @return A new registered session
     * @param <T> The type of session to create
     */
    public <T extends Session> T createSession(String namespace, Session.Factory<T> factory) {
        T out = factory.create(this, server.get(), namespace);
        runningSessions.put(out.getId(), out);
        out.isRegistered = true;
        out.shutdownEvent.register(this, sess -> runningSessions.remove(sess.getId()));
        return out;
    }

    /**
     * Gets a list of registered Sessions
     * @return A list of registered sessions
     */
    public Collection<Session> getSessions() {
        return runningSessions.values();
    }

    /**
     * Gets a stream of registered Sessions that match the given predicate
     * @param predicate The predicate to check Sessions by
     * @return A stream of registered Sessions
     */
    public Stream<Session> getSessions(Predicate<Session> predicate) {
        return runningSessions.values().stream().filter(predicate);
    }

    /**
     * Gets the registered Session the given player is in
     * @param player The player to search for
     * @return The registered Session that player is in, or null if they aren't in a Session
     */
    public Session getPlayerSession(Player player) {
        for(Session sess : runningSessions.values()) {
            if(sess.contains(player)) return sess;
        }
        return null;
    }

    /**
     * Shuts down all registered Sessions
     */
    public void shutdownAll() {

        List.copyOf(runningSessions.values()).forEach(Session::shutdown);
    }

    /**
     * Shuts down all registered Sessions which match the given predicate
     * @param predicate The predicate to check Sessions by
     */
    public void shutdownAll(Predicate<Session> predicate) {

        runningSessions.values().stream().filter(predicate).toList().forEach(Session::shutdown);
    }

    /**
     * Attempts to recover player save points when they join the server. Should be called each time a player joins the
     * server
     * @param player The player to attempt recovery for
     */
    protected void loadRecovery(Player player) {

        ConfigSection recovery = dataManager.getDataOrNull(player.getUUID().toString());
        if(recovery == null) {
            return;
        }

        SavepointModule module = server.get().getModuleManager().getModule(SavepointModule.class);
        if(module == null) {
            throw new IllegalStateException("Savepoint module must be loaded!");
        }

        SerializeResult<Savepoint> savepoint = module.getSerializer().deserialize(ConfigContext.INSTANCE, recovery);

        if (savepoint.isComplete()) {
            savepoint.getOrThrow().load(player);
            if (!dataManager.clear(player.getUUID().toString())) {
                MidnightCoreAPI.LOGGER.warn("Failed to delete session recovery file! Please delete it manually, or else the session module may attempt to restore it again!");
            }
        } else {
            MidnightCoreAPI.LOGGER.warn("Failed to recover session data for " + player.getUsername() + "!");
        }

    }

    protected void clearRecovery(Player player) {
        dataManager.clear(player.getUUID().toString());
    }

    protected void saveRecovery(Player player, SavepointModule spm, Savepoint sp) {

        SerializeResult<ConfigObject> res = spm.getSerializer().serialize(ConfigContext.INSTANCE, sp);
        if(!res.isComplete()) {
            MidnightCoreAPI.LOGGER.warn("Unable to save recovery data for " + player.getUsername() + "! " + res.getError());
            return;
        }

        dataManager.save(player.getUUID().toString(), res.getOrThrow().asSection());
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "session");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("enabled", false);

}
