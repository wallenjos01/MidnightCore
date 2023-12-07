package org.wallentines.mcore.skin;

import org.wallentines.mcore.*;
import org.wallentines.mcore.savepoint.SavepointCreatedEvent;
import org.wallentines.mcore.savepoint.SavepointLoadedEvent;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

public abstract class SkinModule implements ServerModule {

    protected Server server;

    public abstract void setSkin(Player player, Skin skin);

    public abstract void resetSkin(Player player);

    public Skin getSkin(Player player) {
        return player.getSkin();
    }

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        this.server = data;

        if(section.getBoolean("save_skins_in_savepoints")) {
            Event.register(SavepointCreatedEvent.class, this, ev -> {
                Skin skin = getSkin(ev.getPlayer());
                if(skin != null) ev.getSavepoint().getExtraData().set("skin", skin, Skin.SERIALIZER);
            });
            Event.register(SavepointLoadedEvent.class, this, ev -> {
                ev.getSavepoint().getExtraData().getOptional("skin", Skin.SERIALIZER).ifPresent(sk -> setSkin(ev.getPlayer(), sk));
            });
        }

        return true;
    }

    @Override
    public void disable() {
        for(Player player : server.getPlayers()) {
            resetSkin(player);
        }
    }

    public abstract void forceUpdate(Player player);

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "skin");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("get_skins_in_offline_mode", true)
            .with("save_skins_in_savepoints", true);

}
