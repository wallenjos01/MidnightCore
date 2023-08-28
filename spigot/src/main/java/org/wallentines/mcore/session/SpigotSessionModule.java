package org.wallentines.mcore.session;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wallentines.mcore.MidnightCore;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.SpigotPlayer;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.lang.ref.WeakReference;

public class SpigotSessionModule extends SessionModule {

    @Override
    public boolean initialize(ConfigSection section, Server data) {
        if(!super.initialize(section, data)) return false;

        Bukkit.getPluginManager().registerEvents(new SessionListener(this), MidnightCore.getPlugin(MidnightCore.class));

        return true;
    }

    private static class SessionListener implements Listener {

        WeakReference<SpigotSessionModule> module;

        public SessionListener(SpigotSessionModule module) {
            this.module = new WeakReference<>(module);
        }

        private SpigotSessionModule getModule() {
            SpigotSessionModule mod = module.get();
            if(mod == null || Server.RUNNING_SERVER.get().getModuleManager().getModule(SpigotSessionModule.class) != mod) {
                module.clear();
                return null;
            }
            return mod;
        }

        @EventHandler
        private void onLeave(PlayerQuitEvent event) {

            SpigotSessionModule mod = getModule();
            if(mod == null) return;

            SpigotPlayer spl = new SpigotPlayer(Server.RUNNING_SERVER.get(), event.getPlayer());
            Session sess = mod.getPlayerSession(spl);
            if(sess != null) sess.removePlayer(spl);
        }

        @EventHandler
        private void onJoin(PlayerJoinEvent event) {

            SpigotSessionModule mod = getModule();
            if(mod == null) return;

            SpigotPlayer spl = new SpigotPlayer(Server.RUNNING_SERVER.get(), event.getPlayer());
            mod.loadRecovery(spl);
        }

    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(SpigotSessionModule::new, ID, new ConfigSection()).dependsOn(SavepointModule.ID);

}
