package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.messaging.Packet;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A packet sent to clients to query the extensions they have enabled
 */
public class ClientboundExtensionPacket implements Packet {

    private final List<Identifier> extensions;


    /**
     * Constructs a new extension packet with the given supported extensions
     * @param extensions The extensions supported by the server, will be sent to the client.
     */
    public ClientboundExtensionPacket(Collection<Identifier> extensions) {
        this.extensions = List.copyOf(extensions);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void write(ByteBuf buffer) {
        PacketBufferUtil.writeVarInt(buffer, extensions.size());
        for(Identifier ent : extensions) {
            PacketBufferUtil.writeUtf(buffer, ent.toString());
        }
    }

    /**
     * Gets a list of the extensions supported by the server. The client should respond with the versions of these
     * extensions it supports.
     * @return A list of extension identifiers
     */
    public List<Identifier> getExtensions() {
        return extensions;
    }

    /**
     * Creates a new extension packet by reading a data buffer
     * @param buffer The bytes to read
     * @return A new extension packet
     */
    public static ClientboundExtensionPacket read(ByteBuf buffer) {

        int count = PacketBufferUtil.readVarInt(buffer);

        List<Identifier> extensions = new ArrayList<>();
        for (int i = 0; i < count; i++) {

            Identifier id = Identifier.parseOrDefault(PacketBufferUtil.readUtf(buffer), MidnightCoreAPI.MOD_ID);
            extensions.add(id);

        }

        return new ClientboundExtensionPacket(extensions);
    }

    private static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "ext");
}
