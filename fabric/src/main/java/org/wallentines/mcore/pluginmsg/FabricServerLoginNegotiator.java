package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.LoginPacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.wallentines.mcore.mixin.AccessorLoginPacketHandler;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.WrappedComponent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;


public class FabricServerLoginNegotiator extends ServerLoginNegotiator {

    private final ServerLoginPacketListenerImpl listener;
    private final LoginPacketSender connection;

    /**
     * Creates a new login negotiator for the player with the given packet listener, and Connection
     * @param listener The packet handler for the player
     * @param connection The connection to the logging in player
     */
    public FabricServerLoginNegotiator(ServerLoginPacketListenerImpl listener, LoginPacketSender connection) {
        super(((AccessorLoginPacketHandler) listener).getGameProfile().getId(), ((AccessorLoginPacketHandler) listener).getGameProfile().getName());
        this.listener = listener;
        this.connection = connection;
    }

    @Override
    public void kick(Component message) {
        listener.disconnect(new WrappedComponent(message));
    }

    @Override
    public void sendPacket(Identifier id, ByteBuf data) {

        connection.sendPacket(ConversionUtil.toResourceLocation(id), PacketByteBufs.copy(data));
    }
}
