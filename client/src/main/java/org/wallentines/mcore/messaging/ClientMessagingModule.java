package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

public abstract class ClientMessagingModule implements ClientModule {

    private final Registry<ClientPacketHandler> handlers = new Registry<>(MidnightCoreAPI.MOD_ID);
    private final Registry<ClientLoginPacketHandler> loginHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);


    public abstract void sendMessage(ClientPacket packet);

    public void registerPacketHandler(Identifier id, ClientPacketHandler handler) {
        handlers.register(id, handler);
    }

    public void registerLoginPacketHandler(Identifier id, ClientLoginPacketHandler handler) {
        loginHandlers.register(id, handler);
    }

    protected boolean handlePacket(Identifier id, ByteBuf buf) {

        ClientPacketHandler handler = handlers.get(id);
        if(handler == null) {
            return false;
        }

        try {
            handler.handle(buf);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.warn("An error occurred while handling a custom packet!");
            ex.printStackTrace();
        }
        return true;
    }


    protected ByteBuf handleLoginPacket(Identifier id, ByteBuf buf) {

        ClientLoginPacketHandler handler = loginHandlers.get(id);
        if(handler == null) {
            return null;
        }

        ByteBuf out = null;
        try {
            out = handler.respond(buf);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.warn("An error occurred while handling a login packet!");
            ex.printStackTrace();
        }

        return out;
    }

}
