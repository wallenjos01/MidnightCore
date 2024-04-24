package org.wallentines.mcore.messenger;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public abstract class MessengerModule {

    private Messenger messenger;

    protected boolean init(ConfigSection data) {

        String type = data.getString("type");
        MessengerType mt = MessengerType.REGISTRY.get(type);
        if(mt == null) {
            MidnightCoreAPI.LOGGER.error("Unable to find messenger type " + type + "!");
            return false;
        }

        messenger = mt.create(this, data);
        return messenger != null;
    }

    protected void shutdown() {
        if(messenger == null) throw new IllegalStateException("Attempt to shutdown before module was initialized!");
        messenger.shutdown();
        messenger = null;
    }

    public Messenger getMessenger() {
        if(messenger == null) throw new IllegalStateException("Attempt to get messenger before module was initialized!");
        return messenger;
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messenger");
}
