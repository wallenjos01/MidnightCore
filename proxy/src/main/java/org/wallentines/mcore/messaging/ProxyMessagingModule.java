package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.*;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

public abstract class ProxyMessagingModule implements ProxyModule {

    protected final Registry<PacketHandler<ProxyPlayer>> playerHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);
    protected final Registry<PacketHandler<ProxyServer>> serverHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);


    @Override
    public boolean initialize(ConfigSection section, Proxy data) {
        return true;
    }

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
    protected abstract void sendServerMessage(ProxyServer player, Identifier id, ByteBuf out);

    public void registerPlayerHandler(Identifier id, PacketHandler<ProxyPlayer> player) {
        this.playerHandlers.register(id, player);
    }

    public void registerServerHandler(Identifier id, PacketHandler<ProxyServer> player) {
        this.serverHandlers.register(id, player);
    }

    protected void handle(ProxyPlayer player, Identifier id, ByteBuf buffer) {

        if(!playerHandlers.contains(id)) {
            return;
        }

        playerHandlers.get(id).handle(player, buffer);
    }

    protected void handle(ProxyServer server, Identifier id, ByteBuf buffer) {

        if(!serverHandlers.contains(id)) {
            return;
        }

        serverHandlers.get(id).handle(server, buffer);
    }

}
