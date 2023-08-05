package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.messaging.Packet;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * A packet sent to the client which declares supported extensions to clients
 */
public class ClientboundExtensionPacket implements Packet {

    private final Map<Identifier, Version> extensions;

    /**
     * Creates an extension packet by reading a loaded module manager
     * @param manager The module manager to read
     */
    public ClientboundExtensionPacket(ModuleManager<ServerExtensionModule, ServerExtension> manager) {

        HashMap<Identifier, Version> map = new HashMap<>();
        for(Identifier id : manager.getLoadedModuleIds()) {
            map.put(id, manager.getModuleById(id).getVersion());
        }

        this.extensions = Map.copyOf(map);
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

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extensions");
}
