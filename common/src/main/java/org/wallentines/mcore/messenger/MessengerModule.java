package org.wallentines.mcore.messenger;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.smi.Messenger;
import org.wallentines.smi.MessengerType;

import java.util.Collections;
import java.util.Map;

public class MessengerModule {

    private Map<String, Messenger> messengers;

    public static final Registry<Identifier, MessengerType<?>> REGISTRY = Registry.create("smi");

    protected void loadAll(ConfigSection section, Registry<Identifier, MessengerType<?>> registry) {
        Serializer<Messenger> serializer = Messenger.createSerializer(registry);
        messengers = serializer.mapOf().deserialize(ConfigContext.INSTANCE, section).getOrThrow();
    }

    protected void shutdown() {
        messengers.values().forEach(Messenger::close);
        messengers = Collections.emptyMap();
    }

    @Nullable
    public Messenger getMessenger(String name) {
        return messengers.get(name);
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messenger");
}
