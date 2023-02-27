package org.wallentines.midnightcore.fabric.module.extension;

import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.extension.AbstractServerExtensionModule;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerBeginQueryEvent;
import org.wallentines.midnightcore.fabric.module.messaging.FabricMessagingModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricServerExtensionModule extends AbstractServerExtensionModule {

    private boolean delaySend;

    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        delaySend = section.getBoolean("delay_send");
        return super.initialize(section, data);
    }

    @Override
    protected void registerEvents() {
        // Events
        if(delaySend) {

            Event.register(PlayerJoinEvent.class, this, 90, ev -> {
                FabricPlayer fp = FabricPlayer.wrap(ev.getPlayer());
                getMessagingModule().sendRawMessage(fp, ExtensionHelper.SUPPORTED_EXTENSION_PACKET, getSupportedExtensionsPacket().array());
            });

        } else {

            Event.register(ServerBeginQueryEvent.class, this, ev ->
                    ev.getNegotiator().sendMessage(ExtensionHelper.SUPPORTED_EXTENSION_PACKET, new FriendlyByteBuf(getSupportedExtensionsPacket()), res ->
                            enabledExtensions.put(ev.getProfile().getId(), ExtensionHelper.handleResponse(ev.getProfile().getName(), res))));
        }
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<MServer, ServerModule>(FabricServerExtensionModule::new, ExtensionHelper.ID, new ConfigSection()
            .with("extensions", new ConfigSection())
            .with("delay_send", false)
    ).dependsOn(FabricMessagingModule.ID);

}
