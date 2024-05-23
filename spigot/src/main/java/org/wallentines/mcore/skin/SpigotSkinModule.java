package org.wallentines.mcore.skin;

import org.bukkit.Bukkit;
import org.wallentines.mcore.*;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.SkinUpdater;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mcore.util.MojangUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

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

        data.getPlayers().forEach(this::handleLogin);
        server.joinEvent().register(this, this::handleLogin);

        return true;
    }

    @Override
    public void forceUpdate(Player player) {
        setSkin(player, getSkin(player));
    }

    @Override
    public void setSkin(Player player, Skin skin) {
        Adapter adapter = Adapter.INSTANCE.get();
        adapter.runOnServer(() -> {
            try {
                updater.changePlayerSkin(ConversionUtil.validate(player).getInternal(), skin);
            } catch (Throwable th) {
                MidnightCoreAPI.LOGGER.error("An error occurred while updating a player's skin!", th);
            }
        });
    }

    @Override
    public void resetSkin(Player player) {
        if(getSkin(player) == loginSkins.get(player)) return;
        setSkin(player, loginSkins.get(player));
    }

    private void handleLogin(Player player) {

        loginSkins.put(player, getSkin(player));
        if(offlineModeSkins && !Bukkit.getServer().getOnlineMode()) {
            MojangUtil.getSkinByNameAsync(player.getUsername()).thenAccept(skin -> {
                loginSkins.put(player, skin);
                if(getSkin(player) != null) setSkin(player, skin);
            });
        }
    }
}
