package org.wallentines.mcore.skin;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.wallentines.mcore.*;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.SkinUpdater;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.MojangUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class SpigotSkinModule extends SkinModule {

    private final HashMap<Player, Skin> loginSkins = new HashMap<>();
    private boolean offlineModeSkins;
    private SkinUpdater updater;

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(SpigotSkinModule::new, ID, DEFAULT_CONFIG);

    @Override
    public boolean initialize(ConfigSection section, Server data) {
        if(!super.initialize(section, data)) return false;

        updater = Adapter.INSTANCE.get().getSkinUpdater();
        if(updater == null) {
            return false;
        }

        offlineModeSkins = section.getBoolean("get_skins_in_offline_mode");

        Bukkit.getPluginManager().registerEvents(new SkinListener(this), MidnightCore.getPlugin(MidnightCore.class));
        return true;
    }

    @Override
    public void setSkin(Player player, Skin skin) {
        updater.changePlayerSkin(ConversionUtil.validate(player).getInternal(), skin);
    }

    @Override
    public void resetSkin(Player player) {
        setSkin(player, loginSkins.get(player));
    }

    private static class SkinListener implements Listener {

        final WeakReference<SpigotSkinModule> parent;

        public SkinListener(SpigotSkinModule parent) {
            this.parent = new WeakReference<>(parent);
        }

        @EventHandler
        private void onJoin(PlayerLoginEvent event) {

            SpigotSkinModule mod = parent.get();
            if(mod == null) return;

            if(Server.RUNNING_SERVER.get().getModuleManager().getModule(SpigotSkinModule.class) != mod) {
                parent.clear();
                return;
            }

            SpigotPlayer player = new SpigotPlayer(Server.RUNNING_SERVER.get(), event.getPlayer());
            mod.loginSkins.put(player, mod.getSkin(player));
            if(mod.offlineModeSkins) {
                MojangUtil.getSkinByNameAsync(player.getUsername()).thenAccept(skin -> {
                    mod.loginSkins.put(player, skin);
                    mod.setSkin(player, skin);
                });
            }
        }

    }
}
