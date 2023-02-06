package org.wallentines.midnightcore.fabric.module.extension;

import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.extension.ServerExtensionModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.module.extension.ServerExtension;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerBeginQueryEvent;
import org.wallentines.midnightcore.fabric.module.messaging.FabricMessagingModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.Version;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public class FabricServerExtensionModule implements ServerExtensionModule {

    private final ModuleManager<ServerExtensionModule, ServerExtension> manager = new ModuleManager<>();
    private final HashMap<UUID, HashMap<Identifier, Version>> enabledExtensions = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        boolean delaySend = section.getBoolean("delay_send");

        // Load Extensions
        manager.loadAll(section.getSection("extensions"), this, ServerExtension.REGISTRY);

        FriendlyByteBuf supportedData = new FriendlyByteBuf(ExtensionHelper.createPacket(manager.getLoadedModuleIds()));

        // Events
        if(delaySend) {

            FabricMessagingModule mod = data.getModule(FabricMessagingModule.class);
            if(mod == null) {
                return false;
            }

            mod.registerHandler(ExtensionHelper.SUPPORTED_EXTENSION_PACKET, (mpl, res) ->
                enabledExtensions.put(mpl.getUUID(), ExtensionHelper.handleResponse(mpl.getUsername(), new FriendlyByteBuf(res))));

            Event.register(PlayerJoinEvent.class, this, 90, ev -> {
                FabricPlayer fp = FabricPlayer.wrap(ev.getPlayer());
                mod.sendRawMessage(fp, ExtensionHelper.SUPPORTED_EXTENSION_PACKET, supportedData.array());
            });

        } else {

            Event.register(ServerBeginQueryEvent.class, this, ev ->
                ev.getNegotiator().sendMessage(ExtensionHelper.SUPPORTED_EXTENSION_PACKET, supportedData, res ->
                    enabledExtensions.put(ev.getProfile().getId(), ExtensionHelper.handleResponse(ev.getProfile().getName(), res))));
        }

        return true;
    }

    @Override
    public <T extends ServerExtension> T getExtension(Class<T> clazz) {

        return manager.getModule(clazz);
    }

    @Override
    public Collection<Identifier> getLoadedExtensionIds() {

        return manager.getLoadedModuleIds();
    }

    @Override
    public void disable() {
        manager.unloadAll();
    }

    public boolean playerHasExtension(MPlayer player, Identifier id) {
        Map<Identifier, Version> extensions = enabledExtensions.get(player.getUUID());
        return extensions != null && extensions.containsKey(id);
    }

    public Version getExtensionVersion(MPlayer player, Identifier id) {
        return playerHasExtension(player, id) ? enabledExtensions.get(player.getUUID()).get(id) : null;
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<MServer, ServerModule>(FabricServerExtensionModule::new, ExtensionHelper.ID, new ConfigSection()
            .with("extensions", new ConfigSection())
            .with("delay_send", false)
    ).dependsOn(FabricMessagingModule.ID);

}
