package org.wallentines.mcore.extension;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.wallentines.mcore.*;
import org.wallentines.mcore.pluginmsg.ServerPluginMessageModule;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class SpigotExtensionModule extends ServerExtensionModule {

    @Override
    protected void registerJoinListener(Consumer<Player> player) {
        Bukkit.getPluginManager().registerEvents(new JoinListener(player, this), MidnightCore.getPlugin(MidnightCore.class));
    }

    private static class JoinListener implements Listener {

        Consumer<Player> callback;
        WeakReference<SpigotExtensionModule> module;

        public JoinListener(Consumer<Player> callback, SpigotExtensionModule module) {
            this.callback = callback;
            this.module = new WeakReference<>(module);
        }

        @EventHandler
        private void onJoin(PlayerLoginEvent event) {
            SpigotExtensionModule mod = module.get();
            if(mod == null || Server.RUNNING_SERVER.get().getModuleManager().getModule(SpigotExtensionModule.class) != mod) {
                module.clear();
                return;
            }

            callback.accept(new SpigotPlayer(Server.RUNNING_SERVER.get(), event.getPlayer()));
        }

    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(SpigotExtensionModule::new, ID, DEFAULT_CONFIG).dependsOn(ServerPluginMessageModule.ID);


}
