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

/**
 * A packet which declares the loaded client extensions to the server. Should only be sent as a response when the server
 * queries for extensions.
 */
public class ServerboundExtensionPacket implements ClientPacket {

    private final Map<Identifier, Version> extensions;

    /**
     * Constructs a new packet by reading the given module manager and filtering based on the given server modules
     * @param manager The extension module manager to read
     * @param serverModules The modules available on the server
     */
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

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extensions");
}
