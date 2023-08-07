package org.wallentines.mcore.messaging;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;


public class FabricServerLoginNegotiator extends ServerLoginNegotiator {

    private final PacketSender connection;

    /**
     * Creates a new login negotiator for the player with the given packet listener, and Connection
     * @param profile The profile of the logging in player
     * @param connection The connection to the logging in player
     */
    public FabricServerLoginNegotiator(GameProfile profile, PacketSender connection) {
        super(profile.getId(), profile.getName());
        this.connection = connection;
    }

    @Override
    public void sendPacket(Identifier id, ByteBuf data) {

        connection.sendPacket(ConversionUtil.toResourceLocation(id), PacketByteBufs.copy(data));
    }
}
