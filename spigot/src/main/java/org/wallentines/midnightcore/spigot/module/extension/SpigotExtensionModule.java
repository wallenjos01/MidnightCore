package org.wallentines.midnightcore.spigot.module.extension;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.extension.AbstractServerExtensionModule;
import org.wallentines.midnightcore.common.module.extension.ExtensionHelper;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.spigot.MidnightCore;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.midnightlib.module.ModuleInfo;

public class SpigotExtensionModule extends AbstractServerExtensionModule implements Listener {
    @Override
    protected void registerEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(this, MidnightCore.getInstance());
    }

    @EventHandler
    private void onLogin(PlayerLoginEvent event) {
        getMessagingModule().sendRawMessage(SpigotPlayer.wrap(event.getPlayer()), ExtensionHelper.SUPPORTED_EXTENSION_PACKET, getSupportedExtensionsPacket().array());
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<MServer, ServerModule>(SpigotExtensionModule::new, ExtensionHelper.ID, new ConfigSection()
            .with("extensions", new ConfigSection())
    ).dependsOn(AbstractMessagingModule.ID);

}
