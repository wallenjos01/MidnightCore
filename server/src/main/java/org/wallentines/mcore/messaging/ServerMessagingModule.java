package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.util.function.Function;

public abstract class ServerMessagingModule implements ServerModule {

    private final Registry<Type<?>> handlers = new Registry<>(MidnightCoreAPI.MOD_ID);

    public final HandlerList<ServerLoginNegotiator> onLogin = new HandlerList<>();

    public void sendPacket(Player player, ServerPacket packet) {

        Type<?> type = getPacketType(packet);
        if(type == null) {

            MidnightCoreAPI.LOGGER.warn("Could not find packet type for packet with class " + packet.getClass());
            return;
        }

        ByteBuf out = Unpooled.buffer();
        packet.write(out);

        sendPacket(player, handlers.getId(type), out);
    }

    public <T extends ServerPacket> void registerPacketType(Identifier packetId, Class<T> packetType, Function<ByteBuf, T> reader) {

        handlers.register(packetId, new Type<>(packetType, reader));
    }

    protected abstract void sendPacket(Player player, Identifier packetId, ByteBuf data);

    protected void handlePacket(Player sender, Identifier packetId, ByteBuf data) {

        if(!handlers.contains(packetId)) return;
        handlers.get(packetId).read(data).handle(sender);
    }

    private Type<?> getPacketType(ServerPacket packet) {
        for(Type<?> type : handlers) {
            if(type.getPacketClass().isAssignableFrom(packet.getClass())) {
                return type;
            }
        }
        return null;
    }

    private static class Type<T extends ServerPacket> {

        private final Class<T> packetType;
        private final Function<ByteBuf, T> reader;

        public Type(Class<T> packetType, Function<ByteBuf, T> reader) {
            this.packetType = packetType;
            this.reader = reader;
        }

        public Class<T> getPacketClass() {
            return packetType;
        }

        public T read(ByteBuf buffer) {
            return reader.apply(buffer);
        }
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messaging");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("udp_server", false)
            .with("udp_server_port", 25565);
}
