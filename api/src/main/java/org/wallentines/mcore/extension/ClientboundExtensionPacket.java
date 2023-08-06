package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.messaging.Packet;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ClientboundExtensionPacket implements Packet {

    private final List<Identifier> extensions;


    public ClientboundExtensionPacket(List<Identifier> extensions) {
        this.extensions = extensions;
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

    public List<Identifier> getExtensions() {
        return extensions;
    }

    public static ClientboundExtensionPacket read(ByteBuf buffer) {

        int count = PacketBufferUtil.readVarInt(buffer);

        List<Identifier> extensions = new ArrayList<>();
        for (int i = 0; i < count; i++) {

            Identifier id = Identifier.parseOrDefault(PacketBufferUtil.readUtf(buffer), MidnightCoreAPI.MOD_ID);
            extensions.add(id);

        }

        return new ClientboundExtensionPacket(extensions);
    }

    private static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extensions");
}
