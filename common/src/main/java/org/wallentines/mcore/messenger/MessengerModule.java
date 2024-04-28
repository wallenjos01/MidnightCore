package org.wallentines.mcore.messenger;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * A module which allows the server to send messages to other servers running MidnightCore
 */
public abstract class MessengerModule {

    private final Map<String, Messenger> messengers;
    private PluginMessageBroker broker;
    private boolean initialized;

    protected MessengerModule() {
        this.messengers = new HashMap<>();
    }

    /**
     * Initializes the module by creating a messenger from the given configuration
     * @param data The messenger configuration
     * @return Whether initialization was successful
     */
    protected boolean init(ConfigSection data, PluginMessageBroker.Factory factory) {

        ConfigSection sec = data.getSection("messengers");

        for(String key : sec.getKeys()) {
            ConfigSection messenger = sec.getSection(key);

            if(!messenger.hasString("type")) {
                MidnightCoreAPI.LOGGER.error("Missing required key type for messenger " + key + "!");
            }

            String type = messenger.getString("type");
            MessengerType mt = MessengerType.REGISTRY.get(type);
            if(mt == null) {
                MidnightCoreAPI.LOGGER.error("Unable to find messenger type " + type + "!");
                return false;
            }

            // Create a plugin message broker only if necessary
            if(mt.usesPluginMessageBroker() && broker == null) {
                broker = factory.create(this, data.getOptional("broker").map(ConfigObject::asSection).orElse(new ConfigSection()));
            }

            Messenger msg;
            try {
                msg = mt.create(this, messenger);
            } catch (Throwable th) {
                MidnightCoreAPI.LOGGER.error("An error occurred while creating messenger " + key + "!", th);
                return false;
            }

            if(msg == null) {
                MidnightCoreAPI.LOGGER.error("Unable to create messenger for " + type + "!");
                return false;
            }

            messengers.put(key, msg);
        }

        initialized = true;
        return true;
    }

    protected void shutdown() {
        
        for(Messenger msg : messengers.values()) {
            msg.shutdown();
        }

        if(broker != null) {
            broker.shutdown();
        }

        messengers.clear();
        initialized = false;
    }

    @Nullable
    public PluginMessageBroker getPluginMessageBroker() {
        return broker;
    }

    public Messenger getMessenger() {
        return getMessenger(null);
    }

    public Messenger getMessenger(String name) {

        if(!initialized) throw new IllegalStateException("Attempt to get messenger before module was initialized!");
        if(name == null) name = "default";

        return messengers.get(name);
    }

    static {
        MessengerType.REGISTRY.register("plugin_message", PluginMessenger.TYPE);
        MessengerType.REGISTRY.register("composite", CompositeMessenger.TYPE);
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messenger");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("enabled", false)
            .with("messengers", new ConfigSection()
                    .with("default", new ConfigSection()
                            .with("type", "plugin_message")
                            .with("encrypt", false)
                    ));
}
