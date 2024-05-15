package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.EventHandler;
import org.wallentines.midnightlib.event.HandlerList;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A messenger which uses plugin messages to communicate across traditional Minecraft proxies
 */
public class PluginMessenger extends Messenger {

    private final PluginMessageBroker broker;
    private final Map<String, HandlerList<Message>> handlers;
    protected final String namespace;

    protected PluginMessenger(MessengerModule module, PluginMessageBroker broker, String namespace) {
        super(module);
        this.handlers = new HashMap<>();
        this.namespace = namespace;
        this.broker = broker;

        broker.register(this);
    }

    void handle(PluginMessageBroker.Packet pck) {

        if(pck.isSystemMessage()) {
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
        publish(channel, 0, message);
    }

    @Override
    public void publish(String channel, int ttl, ByteBuf message) {
        broker.send(channel, namespace, ttl, message);
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

    public static final MessengerType TYPE = new MessengerType() {

        @Override
        public Messenger create(MessengerModule module, ConfigSection params) {

            PluginMessageBroker broker = module.getPluginMessageBroker();
            if(broker == null) throw new IllegalArgumentException("A plugin message broker is required!");

            return new PluginMessenger(
                    module,
                    broker,
                    params.getOrDefault("namespace", (String) null)
            );
        }

        @Override
        public boolean usesPluginMessageBroker() {
            return true;
        }
    };

}
