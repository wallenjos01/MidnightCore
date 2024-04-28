package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.EventHandler;
import org.wallentines.midnightlib.event.HandlerList;

import java.util.*;

/**
 * A messenger which uses plugin messages to communicate across traditional Minecraft proxies
 */
public class PluginMessenger implements Messenger {

    private final PluginMessageBroker broker;
    private final Map<String, HandlerList<Message>> handlers;
    protected final boolean encrypt;
    protected final boolean allowQueued;
    protected final String namespace;

    protected PluginMessenger(PluginMessageBroker broker, boolean encrypt, boolean allowQueued, String namespace) {
        this.handlers = new HashMap<>();
        this.encrypt = encrypt;
        this.namespace = namespace;
        this.allowQueued = allowQueued;
        this.broker = broker;

        broker.register(this);
    }

    void handle(PluginMessageBroker.Packet pck) {

        if(pck.flags.contains(PluginMessageBroker.Flag.SYSTEM)) {
            MidnightCoreAPI.LOGGER.warn("Attempt to handle system message!");
            return;
        }

        HandlerList<Message> list = handlers.get(pck.channel);

        if(list == null) {
            return;
        }
        if(!Objects.equals(pck.namespace, namespace)) {
            MidnightCoreAPI.LOGGER.warn("Received a message with mismatched namespace! (" + pck.namespace + ")");
            return;
        }
        list.invoke(pck.toMessage(this));
    }

    @Override
    public void publish(String channel, ByteBuf message) {
        broker.send(channel, encrypt, namespace, message, false);
    }

    @Override
    public void queue(String channel, ByteBuf message) {
        broker.send(channel, encrypt, namespace, message, true);
    }

    @Override
    public void unsubscribe(Object listener, String channel) {
        handlers.computeIfPresent(channel, (k,v) -> {
            v.unregisterAll(listener);
            return v;
        });
    }

    @Override
    public void subscribe(Object listener, String channel, EventHandler<Message> handler) {
        handlers.compute(channel, (k,v) -> {
            if(v == null) v = new HandlerList<>();
            v.register(listener, handler);
            return v;
        });
    }

    public EnumSet<PluginMessageBroker.Flag> getFlags() {
        EnumSet<PluginMessageBroker.Flag> out = EnumSet.noneOf(PluginMessageBroker.Flag.class);
        if(encrypt) out.add(PluginMessageBroker.Flag.ENCRYPTED);
        if(allowQueued) out.add(PluginMessageBroker.Flag.QUEUE);
        if(namespace != null) out.add(PluginMessageBroker.Flag.NAMESPACED);
        return out;
    }

    public static final MessengerType TYPE = new MessengerType() {

        @Override
        public Messenger create(MessengerModule module, ConfigSection params) {

            PluginMessageBroker broker = module.getPluginMessageBroker();
            if(broker == null) throw new IllegalArgumentException("A plugin message broker is required!");

            return new PluginMessenger(
                    broker,
                    params.getOrDefault("encrypt", false),
                    params.getOrDefault("allow_queued", true),
                    params.getOrDefault("namespace", (String) null)
            );
        }

        @Override
        public boolean usesPluginMessageBroker() {
            return true;
        }
    };

}
