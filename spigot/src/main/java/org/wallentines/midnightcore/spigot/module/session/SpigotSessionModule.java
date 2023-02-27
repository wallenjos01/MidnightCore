package org.wallentines.midnightcore.spigot.module.session;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.session.AbstractSessionModule;
import org.wallentines.midnightcore.spigot.MidnightCore;
import org.wallentines.midnightcore.spigot.module.savepoint.SpigotSavepointModule;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;
import org.wallentines.midnightlib.module.ModuleInfo;

public class SpigotSessionModule extends AbstractSessionModule implements Listener {

    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        Bukkit.getPluginManager().registerEvents(this, MidnightCore.getInstance());
        return super.initialize(section, data);
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {

        MPlayer mpl = SpigotPlayer.wrap(event.getPlayer());
        Session sess = getSession(mpl);
        if(sess != null) sess.removePlayer(mpl);
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<MServer, ServerModule>(SpigotSessionModule::new, ID, new ConfigSection()).dependsOn(SpigotSavepointModule.ID);

}
