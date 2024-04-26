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
    protected final String namespace;

    protected PluginMessenger(PluginMessageBroker broker, boolean encrypt, String namespace) {
        this.handlers = new HashMap<>();
        this.encrypt = encrypt;
        this.namespace = namespace;
        this.broker = broker;
    }

    void handle(PluginMessageBroker.Packet pck) {
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
        if(handlers.containsKey(channel)) {
            MidnightCoreAPI.LOGGER.warn("Re-registered messenger channel " + channel);
        }
        handlers.compute(channel, (k,v) -> {
            if(v == null) v = new HandlerList<>();
            v.register(listener, handler);
            return v;
        });
    }

    public static class Type implements MessengerType {

        private final PluginMessageBroker.Factory factory;
        private final Map<MessengerModule, PluginMessageBroker> brokers = new HashMap<>();

        public Type(PluginMessageBroker.Factory factory) {
            this.factory = factory;
        }

        @Override
        public Messenger create(MessengerModule module, ConfigSection params) {

            return new PluginMessenger(
                    brokers.computeIfAbsent(module, k -> factory.create()),
                    params.getOrDefault("encrypt", false),
                    params.getOrDefault("namespace", (String) null)
            );
        }
    }

}
