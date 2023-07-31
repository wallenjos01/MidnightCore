package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.messaging.ClientPacket;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ServerboundExtensionPacket implements ClientPacket {

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extensions");

    private final Map<Identifier, Version> extensions;

    public ServerboundExtensionPacket(ModuleManager<ClientExtensionModule, ClientExtension> manager, Map<Identifier, Version> serverModules) {

        HashMap<Identifier, Version> out = new HashMap<>();
        for(Identifier id : serverModules.keySet()) {
            if(manager.isModuleLoaded(id)) {
                out.put(id, manager.getModuleById(id).getVersion());
            }
        }

        this.extensions = Map.copyOf(out);
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
}
