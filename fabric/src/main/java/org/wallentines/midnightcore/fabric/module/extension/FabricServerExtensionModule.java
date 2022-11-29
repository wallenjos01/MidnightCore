package org.wallentines.midnightcore.fabric.module.extension;

import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.extension.ServerExtensionModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.module.extension.Extension;
import org.wallentines.midnightcore.api.module.extension.ExtensionModule;
import org.wallentines.midnightcore.common.module.extension.AbstractExtensionModule;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerBeginQueryEvent;
import org.wallentines.midnightcore.fabric.module.messaging.FabricMessagingModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public class FabricServerExtensionModule extends AbstractExtensionModule implements ServerExtensionModule {

    private final ModuleManager<ExtensionModule, Extension> manager = new ModuleManager<>();
    private final HashMap<UUID, HashMap<Identifier, Version>> enabledExtensions = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        boolean delaySend = section.getBoolean("delay_send");

        // Load Extensions
        manager.loadAll(section.getSection("extensions"), this, REGISTRY);

        FriendlyByteBuf supportedData = new FriendlyByteBuf(createPacket(manager.getLoadedModuleIds()));

        // Events
        if(delaySend) {

            FabricMessagingModule mod = MidnightCoreAPI.getModule(FabricMessagingModule.class);
            if(mod == null) {
                return false;
            }

            mod.registerHandler(SUPPORTED_EXTENSION_PACKET, (mpl, res) -> {
                enabledExtensions.put(mpl.getUUID(), handleResponse(mpl.getUsername(), new FriendlyByteBuf(res)));
            });

            Event.register(PlayerJoinEvent.class, this, 90, ev -> {
                FabricPlayer fp = FabricPlayer.wrap(ev.getPlayer());
                mod.sendRawMessage(fp, SUPPORTED_EXTENSION_PACKET, supportedData.array());
            });

        } else {

            Event.register(ServerBeginQueryEvent.class, this, ev -> {
                ev.getNegotiator().sendMessage(SUPPORTED_EXTENSION_PACKET, supportedData, res -> {
                    enabledExtensions.put(ev.getProfile().getId(), handleResponse(ev.getProfile().getName(), res));
                });
            });
        }

        return true;
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> clazz) {

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

    @Override
    public boolean isClient() {
        return false;
    }

    public boolean playerHasExtension(MPlayer player, Identifier id) {
        return enabledExtensions.containsKey(player.getUUID()) && enabledExtensions.get(player.getUUID()).containsKey(id);
    }

    public Version getExtensionVersion(MPlayer player, Identifier id) {
        return playerHasExtension(player, id) ? enabledExtensions.get(player.getUUID()).get(id) : null;
    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>(FabricServerExtensionModule::new, ID, new ConfigSection()
            .with("extensions", new ConfigSection())
            .with("delay_send", false)
    ).dependsOn(FabricMessagingModule.ID);

}
