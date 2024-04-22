package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.pluginmsg.Packet;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * A packet which declares the loaded client extensions to the server. Should only be sent as a response when the server
 * queries for extensions.
 */
public class ServerboundExtensionPacket implements Packet {

    private final Map<Identifier, Version> extensions;

    /**
     * Constructs a new packet with the given client modules
     * @param clientModules The modules available on the client
     */
    public ServerboundExtensionPacket(Map<Identifier, Version> clientModules) {

        this.extensions = Map.copyOf(clientModules);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void write(ByteBuf buffer) {

        PacketBufferUtil.writeVarInt(buffer, extensions.size());
        for(Map.Entry<Identifier, Version> ent : extensions.entrySet()) {
            PacketBufferUtil.writeUtf(buffer, ent.getKey().toString());
            PacketBufferUtil.writeUtf(buffer, ent.getValue().toString());
        }
    }

    /**
     * Gets a map of extensions supported by the client, and their versions.
     * @return The extensions supported by the client
     */
    public Map<Identifier, Version> getExtensions() {
        return extensions;
    }

    /**
     * Creates an extension packet by reading from a data buffer.
     * @param buffer The bytes to read.
     * @return A new extension packet.
     */
    public static ServerboundExtensionPacket read(ByteBuf buffer) {

        HashMap<Identifier, Version> modules = new HashMap<>();
        int count = PacketBufferUtil.readVarInt(buffer);

        for(int i = 0 ; i < count ; i++) {
            modules.put(
                    Identifier.parseOrDefault(PacketBufferUtil.readUtf(buffer), MidnightCoreAPI.MOD_ID),
                    Version.fromString(PacketBufferUtil.readUtf(buffer))
            );
        }

        return new ServerboundExtensionPacket(modules);
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "ext");
}
