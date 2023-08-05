package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.*;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

public abstract class ProxyMessagingModule implements ProxyModule {

    protected final Registry<PacketHandler<ProxyPlayer>> playerHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);
    protected final Registry<PacketHandler<ProxyPlayer>> serverHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);
    protected final Registry<PacketHandler<ProxyPlayer>> loginHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);

    public final HandlerList<ProxyLoginNegotiator> onLogin = new HandlerList<>();

    public void sendPlayerMessage(ProxyPlayer player, Packet packet) {
        ByteBuf out = Unpooled.buffer();
        packet.write(out);
        sendPlayerMessage(player, packet.getId(), out);
    }

    public void sendServerMessage(ProxyServer server, Packet packet) {
        ByteBuf out = Unpooled.buffer();
        packet.write(out);
        sendServerMessage(server, packet.getId(), out);
    }


    protected abstract void sendPlayerMessage(ProxyPlayer player, Identifier id, ByteBuf out);
    protected abstract void sendServerMessage(ProxyServer server, Identifier id, ByteBuf out);

    public void registerPlayerHandler(Identifier id, PacketHandler<ProxyPlayer> player) {
        this.playerHandlers.register(id, player);
    }

    public void registerServerHandler(Identifier id, PacketHandler<ProxyPlayer> player) {
        this.serverHandlers.register(id, player);
    }

    public void registerLoginHandler(Identifier id, PacketHandler<ProxyPlayer> handler) {
        this.loginHandlers.register(id, handler);
    }

    protected boolean handle(ProxyPlayer player, Identifier id, ByteBuf buffer) {
        return handleGeneric(player, playerHandlers, id, buffer);
    }

    protected void handleLogin(ProxyPlayer player, Identifier id, ByteBuf buffer) {
        handleGeneric(player, loginHandlers, id, buffer);
    }

    protected boolean handleServer(ProxyPlayer server, Identifier id, ByteBuf buffer) {
        return handleGeneric(server, serverHandlers, id, buffer);
    }

    private <T> boolean handleGeneric(T data, Registry<PacketHandler<T>> registry, Identifier id, ByteBuf buffer) {

        if(!registry.contains(id)) {
            return false;
        }

        try {
            registry.get(id).handle(data, buffer);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while handling a message from a Server!", ex);
        }

        return true;
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messaging");

}
