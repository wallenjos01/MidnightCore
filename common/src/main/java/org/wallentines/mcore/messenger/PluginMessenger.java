package org.wallentines.mcore.messenger;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.event.EventHandler;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.smi.Message;
import org.wallentines.smi.Messenger;
import org.wallentines.smi.MessengerType;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PluginMessenger implements Messenger {

    private final MessengerType<PluginMessenger> type;

    final String namespace;
    private final PluginMessageBroker broker;
    private final boolean encrypt;

    private final Map<Identifier, HandlerList<Message>> handlers;

    public PluginMessenger(MessengerType<PluginMessenger> type, PluginMessageBroker broker, String namespace, boolean encrypt) {
        this.type = type;
        this.broker = broker;
        this.namespace = namespace;
        this.encrypt = encrypt;
        this.handlers = new ConcurrentHashMap<>();

        this.broker.register(this);
    }

    @Override
    public MessengerType<?> getType() {
        return type;
    }

    @Override
    public void publish(Message msg) {
        broker.send(namespace, msg, encrypt);
    }

    @Override
    public void publish(Message msg, long ttl) {
        broker.send(namespace, msg, encrypt, ttl);
    }

    @Override
    public void subscribe(Identifier channel, Object listener, EventHandler<Message> handler) {
        handlers.computeIfAbsent(channel, k -> new HandlerList<>()).register(listener, handler);
    }

    @Override
    public void unsubscribe(Identifier channel, Object listener) {
        HandlerList<Message> handler = handlers.get(channel);
        if (handler != null) {
            handler.unregisterAll(listener);
        }
    }

    @Override
    public void unsubscribeAll(Identifier channel) {
        HandlerList<Message> handler = handlers.get(channel);
        if (handler != null) {
            handler.unregisterAll();
        }
    }

    @Override
    public void close() {
        handlers.clear();
    }

    void handle(MessengerPacket packet) {

        if(packet.isSystemMessage()) {
            MidnightCoreAPI.LOGGER.warn("Attempt to handle system message!");
            return;
        }

        HandlerList<Message> list = handlers.get(packet.channel);

        if(list == null) {
            return;
        }
        if(!Objects.equals(packet.namespace, namespace)) {
            MidnightCoreAPI.LOGGER.warn("Received a message with mismatched namespace! ({})", packet.namespace);
            return;
        }

        list.invoke(packet.toMessage());
    }

    public static MessengerType<PluginMessenger> createType(PluginMessageBroker broker) {

        return new MessengerType<PluginMessenger>() {

            private final Serializer<PluginMessenger> serializer = ObjectSerializer.create(
                    Serializer.STRING.entry("namespace", pm -> pm.namespace),
                    Serializer.BOOLEAN.entry("encrypt", pm -> pm.encrypt),
                    (ns, enc) -> new PluginMessenger(this, broker, ns, enc)
            );

            @Override
            public Class<PluginMessenger> getMessengerClass() {
                return PluginMessenger.class;
            }

            @Override
            public Serializer<PluginMessenger> getSerializer() {
                return serializer;
            }
        };
    }

}
