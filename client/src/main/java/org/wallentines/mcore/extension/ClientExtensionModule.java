package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.messaging.ClientMessagingModule;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ClientExtensionModule implements ClientModule {

    private final ModuleManager<ClientExtensionModule, ClientExtension> manager = new ModuleManager<>();
    private ClientMessagingModule cmm;
    @Override
    public boolean initialize(ConfigSection section, Client data) {

        cmm = data.getModuleManager().getModule(ClientMessagingModule.class);
        if(cmm == null) {
            MidnightCoreAPI.LOGGER.warn("Unable to initialize client extension module! The client messaging module is required!");
            return false;
        }

        cmm.registerPacketHandler(ServerboundExtensionPacket.ID, byteBuf -> cmm.sendMessage(new ServerboundExtensionPacket(manager, readServerPacket(byteBuf))));

        cmm.registerLoginPacketHandler(ServerboundExtensionPacket.ID, byteBuf -> {

            ByteBuf out = Unpooled.buffer();
            new ServerboundExtensionPacket(manager, readServerPacket(byteBuf)).write(out);

            return out;
        });

        return true;
    }

    private Map<Identifier, Version> readServerPacket(ByteBuf buf) {

        int extensions = PacketBufferUtil.readVarInt(buf);

        HashMap<Identifier, Version> versions = new HashMap<>();
        for (int i = 0; i < extensions; i++) {

            Identifier id = Identifier.parseOrDefault(PacketBufferUtil.readUtf(buf), MidnightCoreAPI.MOD_ID);
            if(!manager.isModuleLoaded(id)) {
                continue;
            }

            Version version = Version.fromString(PacketBufferUtil.readUtf(buf));
            versions.put(id, version);
        }

        return versions;
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extension");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("extensions", new ConfigSection());

    public static final ModuleInfo<Client, ClientModule> MODULE_INFO = new ModuleInfo<Client, ClientModule>(ClientExtensionModule::new, ID, DEFAULT_CONFIG).dependsOn(ClientMessagingModule.ID);

}
