package org.wallentines.mcore.savepoint;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.wallentines.mcore.*;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.lang.ref.WeakReference;
import java.util.EnumSet;

public class SpigotSavepointModule extends SavepointModule {

    @Override
    public void resetPlayer(Player player, EnumSet<SaveFlag> flags) {

        SpigotPlayer spl = ConversionUtil.validate(player);
        org.bukkit.entity.Player internal = spl.getInternal();

        if(flags.contains(SaveFlag.NBT)) {
            org.bukkit.Location l = internal.getLocation();
            Adapter.INSTANCE.get().loadTag(internal, new ConfigSection());
            for(PotionEffect eff : internal.getActivePotionEffects()) {
                internal.removePotionEffect(eff.getType());
            }
            internal.teleport(l);
            internal.setFireTicks(0);
            internal.setArrowsInBody(0);
        }

        if(flags.contains(SaveFlag.GAME_MODE)) {
            internal.setGameMode(Bukkit.getDefaultGameMode());
        }

    }

    @Override
    public Savepoint.Factory getFactory() {
        return SpigotSavepoint::save;
    }

    @Override
    public Serializer<Savepoint> getSerializer() {
        return SpigotSavepoint.SERIALIZER.map(sp -> {
            if(!(sp instanceof SpigotSavepoint)) {
                return null;
            }
            return (SpigotSavepoint) sp;
        }, ssp -> ssp);
    }

    @Override
    protected Savepoint createSavepoint(Player player, EnumSet<SaveFlag> flags) {
        return SpigotSavepoint.save(player, flags);
    }

    @Override
    public boolean initialize(ConfigSection config, Server data) {

        Bukkit.getPluginManager().registerEvents(new SavepointListener(this), MidnightCore.getPlugin(MidnightCore.class));

        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(SpigotSavepointModule::new, SavepointModule.ID, new ConfigSection());


    private static class SavepointListener implements Listener {

        WeakReference<SpigotSavepointModule> module;

        public SavepointListener(SpigotSavepointModule module) {
            this.module = new WeakReference<>(module);
        }

        @EventHandler
        private void onLeave(PlayerQuitEvent event) {
            SpigotSavepointModule mod = module.get();
            if(mod == null || Server.RUNNING_SERVER.get().getModuleManager().getModule(SpigotSavepointModule.class) != mod) {
                module.clear();
                return;
            }

            mod.clearSavepoints(new SpigotPlayer(Server.RUNNING_SERVER.get(), event.getPlayer()));
        }

    }

}
