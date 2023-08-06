package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.messaging.Packet;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.List;

/**
 * A packet sent to the client which declares supported extensions to clients
 */
public class ClientboundExtensionPacket implements Packet {

    private final List<Identifier> extensions;

    /**
     * Creates an extension packet by reading a loaded module manager
     * @param manager The module manager to read
     */
    public ClientboundExtensionPacket(ModuleManager<ServerExtensionModule, ServerExtension> manager) {

        this.extensions = List.copyOf(manager.getLoadedModuleIds());
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

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extensions");
}
