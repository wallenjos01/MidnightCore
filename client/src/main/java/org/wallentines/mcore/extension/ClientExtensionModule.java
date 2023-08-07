package org.wallentines.mcore.extension;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.messaging.ClientMessagingModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.List;

/**
 * A client module for loading extensions and communicating with servers who support the extensions
 */
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

        cmm.registerPacketHandler(ServerboundExtensionPacket.ID, (client, byteBuf) -> {
            try {
                ClientboundExtensionPacket pck = ClientboundExtensionPacket.read(byteBuf);
                cmm.sendMessage(createPacket(manager, pck.getExtensions()));
            } catch (Exception ex) {
                MidnightCoreAPI.LOGGER.warn("An exception occurred while processing an extension packet!", ex);
            }

        });

        cmm.registerLoginPacketHandler(ServerboundExtensionPacket.ID, byteBuf -> {

            try {
                ClientboundExtensionPacket pck = ClientboundExtensionPacket.read(byteBuf);
                ByteBuf out = Unpooled.buffer();
                createPacket(manager, pck.getExtensions()).write(out);
                return out;

            } catch (Exception ex) {
                MidnightCoreAPI.LOGGER.warn("An exception occurred while processing an extension packet!", ex);
                return null;
            }

        });

        return true;
    }

    private ServerboundExtensionPacket createPacket(ModuleManager<ClientExtensionModule, ClientExtension> manager, List<Identifier> serverModules) {

        HashMap<Identifier, Version> out = new HashMap<>();
        for(Identifier id : serverModules) {
            if(manager.isModuleLoaded(id)) {
                out.put(id, manager.getModuleById(id).getVersion());
            }
        }
        return new ServerboundExtensionPacket(out);
    }


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "extension");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("extensions", new ConfigSection());

    public static final ModuleInfo<Client, ClientModule> MODULE_INFO = new ModuleInfo<Client, ClientModule>(ClientExtensionModule::new, ID, DEFAULT_CONFIG).dependsOn(ClientMessagingModule.ID);

}
